package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Quarter

interface QuarterRepository {
	suspend fun getQuarters(uid: String): List<Quarter>
	suspend fun removeQuarter(uid: String, qid: String)
}