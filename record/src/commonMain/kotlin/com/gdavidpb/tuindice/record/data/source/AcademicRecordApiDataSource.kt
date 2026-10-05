package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.model.AcademicRecordConflictException
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.data.source.api.mapper.buildAcademicUpsertAttemptOverrideRequest
import com.gdavidpb.tuindice.record.data.source.api.mapper.buildDeleteOverlayMutationRequest
import com.gdavidpb.tuindice.record.data.source.api.mapper.toAddSyntheticTermRequest
import com.gdavidpb.tuindice.record.data.source.api.mapper.toSyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.data.source.api.mapper.toUpdateSyntheticTermRequest
import com.gdavidpb.tuindice.record.data.source.api.mapper.toVersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.source.api.response.AcademicRecordResponse
import com.gdavidpb.tuindice.record.data.source.api.response.LoadSyntheticTermPreviewRequest
import com.gdavidpb.tuindice.record.data.source.api.response.OverlayConflictResponse
import com.gdavidpb.tuindice.record.data.source.api.response.SyntheticTermLoadPreviewResponse
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

class AcademicRecordApiDataSource(
	private val ktorClient: HttpClient
) : AcademicRecordRemoteDataRepository {
	override suspend fun getAcademicRecord(): VersionedAcademicRecord {
		return ktorClient.get("record/v5")
			.body<AcademicRecordResponse>()
			.toVersionedAcademicRecord()
	}

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		return overlayWrite {
			ktorClient.put("record/v5/overlay/attempts/$attemptId") {
				setBody(
					buildAcademicUpsertAttemptOverrideRequest(
						score = score,
						outcome = outcome,
						mutationId = mutationId,
						expectedRevision = expectedRevision
					)
				)
			}
				.body<AcademicRecordResponse>()
				.toVersionedAcademicRecord()
		}
	}

	override suspend fun deleteAttemptOverride(
		attemptId: String,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		return overlayWrite {
			ktorClient.delete("record/v5/overlay/attempts/$attemptId") {
				setBody(
					buildDeleteOverlayMutationRequest(
						mutationId = mutationId,
						expectedRevision = expectedRevision
					)
				)
			}
				.body<AcademicRecordResponse>()
				.toVersionedAcademicRecord()
		}
	}

	override suspend fun addSyntheticTerm(
		command: AcademicRecordMutation.AddSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		return overlayWrite {
			ktorClient.post("record/v5/overlay/terms") {
				setBody(
					command.toAddSyntheticTermRequest(
						mutationId = mutationId,
						expectedRevision = expectedRevision
					)
				)
			}
				.body<AcademicRecordResponse>()
				.toVersionedAcademicRecord()
		}
	}

	override suspend fun deleteSyntheticTerm(
		termRef: String,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		return overlayWrite {
			ktorClient.delete("record/v5/overlay/terms/$termRef") {
				setBody(
					buildDeleteOverlayMutationRequest(
						mutationId = mutationId,
						expectedRevision = expectedRevision
					)
				)
			}
				.body<AcademicRecordResponse>()
				.toVersionedAcademicRecord()
		}
	}

	override suspend fun updateSyntheticTerm(
		command: AcademicRecordMutation.UpdateSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		return overlayWrite {
			ktorClient.patch("record/v5/overlay/terms/${command.targetTermKey}") {
				setBody(
					command.toUpdateSyntheticTermRequest(
						mutationId = mutationId,
						expectedRevision = expectedRevision
					)
				)
			}
				.body<AcademicRecordResponse>()
				.toVersionedAcademicRecord()
		}
	}

	// A 409 of the overlay says why in its body: a stale precondition also names the revision to
	// retry with. The body is read here, once, because a stream cannot be read twice.
	private suspend fun overlayWrite(call: suspend () -> VersionedAcademicRecord): VersionedAcademicRecord {
		return try {
			call()
		} catch (exception: ResponseException) {
			if (exception.response.status != HttpStatusCode.Conflict) throw exception

			val body = runCatching { exception.response.body<OverlayConflictResponse>() }.getOrNull()

			throw AcademicRecordConflictException(
				reason = body?.reason,
				currentRevision = body?.currentRevision,
				original = exception
			)
		}
	}

	override suspend fun loadSyntheticTermPreview(subjectCodes: List<String>): SyntheticTermLoadPreview {
		return ktorClient.post("record/v5/overlay/terms/load-preview") {
			setBody(LoadSyntheticTermPreviewRequest(subjectCodes = subjectCodes))
		}
			.body<SyntheticTermLoadPreviewResponse>()
			.toSyntheticTermLoadPreview()
	}
}
