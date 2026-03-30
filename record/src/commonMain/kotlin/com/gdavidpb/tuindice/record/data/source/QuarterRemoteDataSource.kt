package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteAddQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteDeleteQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSetSubjectGradeAck

interface QuarterRemoteDataSource {
	suspend fun getQuarters(): List<RemoteQuarter>
	suspend fun getQuarter(qid: String): RemoteQuarter
	suspend fun removeQuarter(
		qid: String,
		mutationId: String,
		expectedRevision: Long
	): RemoteDeleteQuarterAck
	suspend fun addQuarter(
		add: RecordMutation.AddQuarter,
		mutationId: String,
		expectedRevision: Long
	): RemoteAddQuarterAck
	suspend fun setSubjectGrade(
		qid: String,
		sid: String,
		grade: Int,
		mutationId: String,
		expectedRevision: Long
	): RemoteSetSubjectGradeAck
}
