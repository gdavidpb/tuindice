package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.OfficialOutcome
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.data.source.api.mapper.buildAcademicUpsertAttemptOverrideRequest
import com.gdavidpb.tuindice.record.data.source.api.mapper.toAcademicRecord
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
	override suspend fun getAcademicRecord(): AcademicRecord {
		return ktorClient.get("record/v1")
			.body<AcademicRecordResponse>()
			.toAcademicRecord()
	}

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: OfficialOutcome?
	): AcademicRecord {
		return ktorClient.put("record/v1/overlay/attempts/$attemptId") {
			setBody(buildAcademicUpsertAttemptOverrideRequest(score = score, outcome = outcome))
		}
			.body<AcademicRecordResponse>()
			.toAcademicRecord()
	}

	override suspend fun deleteAttemptOverride(attemptId: String): AcademicRecord {
		return ktorClient.delete("record/v1/overlay/attempts/$attemptId")
			.body<AcademicRecordResponse>()
			.toAcademicRecord()
	}

	override suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm): AcademicRecord {
		return ktorClient.post("record/v1/overlay/terms") {
			setBody(command.toAddSyntheticTermRequest())
		}
			.body<AcademicRecordResponse>()
			.toAcademicRecord()
	}

	override suspend fun deleteSyntheticTerm(termId: String): AcademicRecord {
		return ktorClient.delete("record/v1/overlay/terms/$termId")
			.body<AcademicRecordResponse>()
			.toAcademicRecord()
	}
}
