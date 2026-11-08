package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentFailure
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentState
import com.gdavidpb.tuindice.assistant.domain.model.PreparedAssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.usecase.PrepareAssistantAttachmentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.UploadAssistantAttachmentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.param.PrepareAssistantAttachmentParams
import com.gdavidpb.tuindice.assistant.domain.usecase.param.UploadAssistantAttachmentParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatDraft
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentLimits
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.last
import kotlin.uuid.Uuid

/**
 * The photos of the message being written. Each photo the student adds is read on the device and then sent to the
 * service, each step in a job of its own. A job runs one use case and reports its outcome as an event; the machine's
 * loop takes the event and decides what it starts, so a copy of a photo the message already holds is taken out before
 * anything is sent for it. The files are kept until their read is over, so a read that failed can start again, and an
 * upload that failed sends what was read.
 *
 * Only the machine's loop touches [jobs] and [files].
 */
class AssistantChatAttachments(
	private val prepareAssistantAttachmentUseCase: PrepareAssistantAttachmentUseCase,
	private val uploadAssistantAttachmentUseCase: UploadAssistantAttachmentUseCase
) {
	private val jobs = mutableMapOf<String, Job>()

	// The file of each photo whose read is not over. It leaves when the read is over (see readFinished), so a read that
	// failed keeps it.
	private val files = mutableMapOf<String, PlatformFile>()

	// Adds [picked] photos to the message as far as it has room. The photos past the room are not taken, and the chat
	// says so; each photo taken starts its own read.
	internal fun add(
		host: MachineHost<AssistantChat.Effect>,
		draft: AssistantChatDraft,
		session: AssistantChatSession,
		picked: List<PlatformFile>
	) {
		val room = (AttachmentLimits.MAX_PER_MESSAGE - draft.attachments.value.size).coerceAtLeast(0)
		val taken = picked.take(room)

		session.attachmentLimitHit = picked.size > taken.size

		taken.forEach { file ->
			val localId = Uuid.random().toString()

			files[localId] = file
			draft.setAttachments(
				draft.attachments.value + AssistantAttachment(
					localId = localId,
					name = file.name,
					thumbnail = null,
					state = AssistantAttachmentState.Preparing,
					content = null
				)
			)

			startRead(host = host, localId = localId, file = file)
		}
	}

	// Tries the attachment [localId] again: one whose upload failed sends what was read, one that could not be read starts
	// over. Its tray item shows it as being read or sent again.
	fun retry(host: MachineHost<AssistantChat.Effect>, draft: AssistantChatDraft, localId: String) {
		val attachment = draft.attachments.value.firstOrNull { it.localId == localId } ?: return
		val content = attachment.content

		if (content != null) {
			draft.updateAttachment(localId) { current -> current.copy(state = AssistantAttachmentState.Uploading) }

			startUpload(
				host = host,
				localId = localId,
				prepared = PreparedAssistantAttachment(
					name = attachment.name,
					content = content,
					thumbnail = attachment.thumbnail
				)
			)
		} else {
			// Without what was read and without the file, there is nothing left to send: the attachment stays failed.
			val file = files[localId] ?: return

			draft.updateAttachment(localId) { current -> current.copy(state = AssistantAttachmentState.Preparing) }

			startRead(host = host, localId = localId, file = file)
		}
	}

	// Takes the attachment [localId] out: its read or upload stops, and its file is forgotten.
	fun remove(localId: String) {
		jobs.remove(localId)?.cancel()
		files.remove(localId)
	}

	// The read of [localId] is over and its bytes are with the attachment: the file is not held for a retry any more.
	fun readFinished(localId: String) {
		files.remove(localId)
	}

	// Starts the upload of what [localId] read. The machine starts it when the read's event is taken.
	internal fun startUpload(
		host: MachineHost<AssistantChat.Effect>,
		localId: String,
		prepared: PreparedAssistantAttachment
	) {
		jobs[localId]?.cancel()

		jobs[localId] = host.launchMachineJob {
			upload(host = host, localId = localId, prepared = prepared)
		}
	}

	// The attachments a send takes out of the tray: their reads and uploads are over, so what the chat kept for them goes.
	// A refused send that gives them back keeps their bytes in the attachment itself.
	fun release(localIds: Collection<String>) {
		localIds.forEach { localId -> remove(localId) }
	}

	// The ids the chat still keeps anything for. A test checks that a sent attachment leaves none behind.
	internal fun trackedIds(): Set<String> = jobs.keys + files.keys

	// The chat leaves its content: the reads and uploads stop, and each one in flight fails, so its retry is there on the
	// next opening. The files of the reads that did not finish are kept for that retry.
	fun stop(draft: AssistantChatDraft) {
		jobs.values.forEach { job -> job.cancel() }
		jobs.clear()

		draft.attachments.value
			.filter { attachment ->
				attachment.state == AssistantAttachmentState.Preparing || attachment.state == AssistantAttachmentState.Uploading
			}
			.forEach { attachment ->
				draft.updateAttachment(attachment.localId) { current ->
					current.copy(state = AssistantAttachmentState.Failed(AssistantAttachmentFailure.Other))
				}
			}
	}

	// The read of [localId]: its bytes, or the reason the file cannot be read, come back as the event.
	private fun startRead(host: MachineHost<AssistantChat.Effect>, localId: String, file: PlatformFile) {
		jobs[localId]?.cancel()

		jobs[localId] = host.launchMachineJob {
			val state = prepareAssistantAttachmentUseCase.execute(PrepareAssistantAttachmentParams(file = file)).last()

			when (state) {
				is UseCaseState.Data -> host.processInternalEvent(
					AssistantChatInternalEvent.AttachmentPrepared(localId, state.value)
				)

				is UseCaseState.Error -> host.processInternalEvent(
					AssistantChatInternalEvent.AttachmentFailed(localId, state.error.toFailure())
				)

				is UseCaseState.Loading -> Unit
			}
		}
	}

	// Sends what was read. The service keeps the photo, or refuses it for the reason the event says.
	private suspend fun upload(
		host: MachineHost<AssistantChat.Effect>,
		localId: String,
		prepared: PreparedAssistantAttachment
	) {
		val state = uploadAssistantAttachmentUseCase.execute(
			UploadAssistantAttachmentParams(attachment = prepared)
		).last()

		when (state) {
			is UseCaseState.Data -> host.processInternalEvent(
				AssistantChatInternalEvent.AttachmentUploaded(localId, state.value)
			)

			is UseCaseState.Error -> host.processInternalEvent(state.error.toUploadEvent(localId))

			is UseCaseState.Loading -> Unit
		}
	}
}
