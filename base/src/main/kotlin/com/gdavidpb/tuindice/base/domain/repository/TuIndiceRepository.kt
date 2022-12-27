package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Quarter

interface TuIndiceRepository {
	suspend fun getQuarters(uid: String): List<Quarter>
}