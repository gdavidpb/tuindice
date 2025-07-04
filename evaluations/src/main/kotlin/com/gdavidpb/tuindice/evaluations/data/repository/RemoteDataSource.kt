package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation

interface RemoteDataSource {
	suspend fun getEvaluations(): List<RemoteEvaluation>
	suspend fun getEvaluation(eid: String): RemoteEvaluation?
	suspend fun addEvaluation(evaluation: RemoteEvaluation): RemoteEvaluation
	suspend fun updateEvaluation(evaluation: RemoteEvaluation): RemoteEvaluation
	suspend fun removeEvaluation(eid: String)
}