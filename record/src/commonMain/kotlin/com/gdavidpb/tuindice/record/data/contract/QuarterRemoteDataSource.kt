package com.gdavidpb.tuindice.record.data.contract


import com.gdavidpb.tuindice.record.data.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.model.quarter.RemoteAddQuarterAck
import com.gdavidpb.tuindice.record.data.model.quarter.RemoteDeleteQuarterAck
import com.gdavidpb.tuindice.record.data.model.quarter.RemoteQuarter
import com.gdavidpb.tuindice.record.data.model.quarter.RemoteSetSubjectGradeAck

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
