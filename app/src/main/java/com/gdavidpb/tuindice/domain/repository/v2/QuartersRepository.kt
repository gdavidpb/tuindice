package com.gdavidpb.tuindice.domain.repository.v2

import com.gdavidpb.tuindice.domain.model.Quarter

interface QuartersRepository {
	suspend fun addQuarter(quarter: Quarter): Quarter
	suspend fun getQuarters(): List<Quarter>
	suspend fun getQuarter(qid: String): Quarter
	suspend fun updateQuarter(qid: String, quarter: Quarter): Quarter
	suspend fun deleteQuarter(qid: String)
}