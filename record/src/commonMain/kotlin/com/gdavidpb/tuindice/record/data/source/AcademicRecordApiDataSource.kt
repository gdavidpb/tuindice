package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.data.source.api.mapper.buildAcademicUpsertAttemptOverrideRequest
import com.gdavidpb.tuindice.record.data.source.api.mapper.toVersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.source.api.mapper.toAddSyntheticTermRequest
import com.gdavidpb.tuindice.record.data.source.api.response.AcademicRecordResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class AcademicRecordApiDataSource(
	private val ktorClient: HttpClient
) : AcademicRecordRemoteDataRepository {
	override suspend fun getAcademicRecord(): VersionedAcademicRecord {
		return ktorClient.get("record/v3")
			.body<AcademicRecordResponse>()
			.toVersionedAcademicRecord()
	}

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?
	): VersionedAcademicRecord {
		return ktorClient.put("record/v3/overlay/attempts/$attemptId") {
			setBody(buildAcademicUpsertAttemptOverrideRequest(score = score, outcome = outcome))
		}
			.body<AcademicRecordResponse>()
			.toVersionedAcademicRecord()
	}

	override suspend fun deleteAttemptOverride(attemptId: String): VersionedAcademicRecord {
		return ktorClient.delete("record/v3/overlay/attempts/$attemptId")
			.body<AcademicRecordResponse>()
			.toVersionedAcademicRecord()
	}

	override suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm): VersionedAcademicRecord {
		return ktorClient.post("record/v3/overlay/terms") {
			setBody(command.toAddSyntheticTermRequest())
		}
			.body<AcademicRecordResponse>()
			.toVersionedAcademicRecord()
	}

	override suspend fun deleteSyntheticTerm(termId: String): VersionedAcademicRecord {
		return ktorClient.delete("record/v3/overlay/terms/$termId")
			.body<AcademicRecordResponse>()
			.toVersionedAcademicRecord()
	}
}
