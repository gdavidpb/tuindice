package com.gdavidpb.tuindice.evaluations.data.evaluation

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform

class EvaluationDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource
) : EvaluationRepository {
	override suspend fun getEvaluation(uid: String, eid: String): Flow<Evaluation> {
		return localDataSource.getEvaluation(uid, eid)
			.distinctUntilChanged()
			.transform { evaluation ->
				if (evaluation != null)
					emit(evaluation)
				else
					remoteDataSource.getEvaluation(eid).also { response ->
						localDataSource.saveEvaluations(uid, listOf(response))
					}
			}
	}

	override suspend fun getEvaluations(uid: String, sid: String): Flow<List<Evaluation>> {
		return localDataSource.getEvaluations(uid, sid)
			.distinctUntilChanged()
			.transform { evaluations ->
				if (evaluations.isNotEmpty())
					emit(evaluations)
				else
					remoteDataSource.getEvaluations(sid).also { response ->
						localDataSource.saveEvaluations(uid, response)
					}
			}
	}

	override suspend fun addEvaluation(uid: String, add: EvaluationAdd): Evaluation {
		val newEvaluation = remoteDataSource.addEvaluation(add)

		localDataSource.saveEvaluations(uid, listOf(newEvaluation))

		return newEvaluation
	}

	override suspend fun updateEvaluation(uid: String, update: EvaluationUpdate): Evaluation {
		val evaluation = remoteDataSource.updateEvaluation(update)

		localDataSource.saveEvaluations(uid, listOf(evaluation))

		return evaluation
	}

	override suspend fun removeEvaluation(uid: String, eid: String) {
		remoteDataSource.removeEvaluation(eid)
		localDataSource.removeEvaluation(uid, eid)
	}
}