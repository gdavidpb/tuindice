package com.gdavidpb.tuindice.record.data.source.api.mapper

import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteDeleteQuarterAck
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSetSubjectGradeAck
import com.gdavidpb.tuindice.record.data.source.api.response.DeleteQuarterResponse
import com.gdavidpb.tuindice.record.data.source.api.response.SetSubjectGradeResponse

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
