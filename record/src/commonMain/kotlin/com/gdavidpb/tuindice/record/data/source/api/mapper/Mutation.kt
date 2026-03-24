package com.gdavidpb.tuindice.record.data.source.api.mapper

import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteAddQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteDeleteQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSetSubjectGradeAck
import com.gdavidpb.tuindice.record.data.source.api.response.AddQuarterResponse
import com.gdavidpb.tuindice.record.data.source.api.response.DeleteQuarterResponse
import com.gdavidpb.tuindice.record.data.source.api.response.SetSubjectGradeResponse

fun AddQuarterResponse.toRemoteAddQuarterAck() = RemoteAddQuarterAck(
	mutationId = mutationId,
	quarter = quarterPatch.toRemoteQuarter(),
	affectedQuarters = affectedQuarters.map { quarter -> quarter.toRemoteQuarter() }
)

fun SetSubjectGradeResponse.toRemoteSetSubjectGradeAck() = RemoteSetSubjectGradeAck(
	mutationId = mutationId,
	subject = subjectPatch.toRemoteSubject(),
	affectedQuarters = affectedQuarters.map { quarter -> quarter.toRemoteQuarter() }
)

fun DeleteQuarterResponse.toRemoteDeleteQuarterAck() = RemoteDeleteQuarterAck(
	mutationId = mutationId,
	removedQuarterId = removedQuarterId,
	affectedQuarters = affectedQuarters.map { quarter -> quarter.toRemoteQuarter() }
)
