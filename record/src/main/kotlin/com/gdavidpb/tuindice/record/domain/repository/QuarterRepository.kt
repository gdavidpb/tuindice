package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.record.domain.request.UpdateQuarterRequest

interface QuarterRepository {
	suspend fun getQuarters(uid: String, forceRemote: Boolean): List<Quarter>
	suspend fun removeQuarter(uid: String, qid: String)
	suspend fun updateQuarter(uid: String, qid: String, request: UpdateQuarterRequest): Quarter
}