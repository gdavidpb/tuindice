package com.gdavidpb.tuindice.summary.presentation.machine

import androidx.compose.ui.graphics.ImageBitmap
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveShareTermUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SetShareBackgroundPreferenceUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ShareTermImageUseCase
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.transition.shareTermContentTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.shareTermIdleTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.shareTermLoadingTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.shareTermUnavailableTransitions
import com.gdavidpb.tuindice.summary.ui.image.toPngBytes
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_unavailable_message
import tuindice.summary.generated.resources.share_term_unavailable_title

class ShareTermMachine(
	private val observeShareTermUseCase: ObserveShareTermUseCase,
	private val setShareBackgroundPreferenceUseCase: SetShareBackgroundPreferenceUseCase,
	private val shareTermImageUseCase: ShareTermImageUseCase
) : ScreenMachine<ShareTerm.State, ShareTerm.Effect> {
	override fun initialState(): ShareTerm.State = ShareTerm.State.Idle

	override fun define(host: MachineHost<ShareTerm.Effect>): MachineDefinition<ShareTerm.State> {
		return MachineDefinition.define {
			shareTermIdleTransitions(machine = this@ShareTermMachine, host = host)
			shareTermLoadingTransitions()
			shareTermContentTransitions(machine = this@ShareTermMachine, host = host)
			shareTermUnavailableTransitions()
		}
	}

	// The terms are read once when the sheet opens, and again each time the hub says them again (each minute).
	internal fun startObservation(host: MachineHost<ShareTerm.Effect>) {
		host.launchMachineJob {
			observeShareTermUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						ShareTermInternalEvent.TermsObserved(source = useCaseState.value)
					)

					is UseCaseState.Error -> host.processInternalEvent(ShareTermInternalEvent.TermsUnavailable)
				}
			}
		}
	}

	// The card is encoded as a PNG in the job of the machine, which runs on the view model's background dispatcher, and
	// only then handed to the platform's share sheet. A card that cannot be encoded is a failed share.
	internal fun shareImage(host: MachineHost<ShareTerm.Effect>, bitmap: ImageBitmap) {
		host.launchMachineJob {
			val content = runCatching { bitmap.toPngBytes() }.getOrNull()

			if (content == null) {
				host.processInternalEvent(ShareTermInternalEvent.ImageShareFailed)
				return@launchMachineJob
			}

			shareTermImageUseCase.execute(content).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(ShareTermInternalEvent.ImageShared)

					is UseCaseState.Error -> host.processInternalEvent(ShareTermInternalEvent.ImageShareFailed)
				}
			}
		}
	}

	internal fun rememberBackground(host: MachineHost<ShareTerm.Effect>, backgroundId: String) {
		host.launchMachineJob {
			setShareBackgroundPreferenceUseCase.execute(backgroundId).collect { }
		}
	}
}

// The state of a sheet whose terms cannot be read, with the words that say so.
internal fun shareTermUnavailableState(): ShareTerm.State.Unavailable {
	return ShareTerm.State.Unavailable(
		title = UiText.Resource(Res.string.share_term_unavailable_title),
		message = UiText.Resource(Res.string.share_term_unavailable_message)
	)
}
