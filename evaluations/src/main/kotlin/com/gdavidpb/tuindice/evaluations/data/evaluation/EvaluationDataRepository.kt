package com.gdavidpb.tuindice.evaluations.data.evaluation

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.domain.model.NewEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.UpdateEvaluation
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository

class EvaluationDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val networkRepository: NetworkRepository
) : EvaluationRepository {
	override suspend fun getEvaluation(uid: String, eid: String): Evaluation {
		val isNetworkAvailable = networkRepository.isAvailable()

		if (isNetworkAvailable)
			remoteDataSource.getEvaluation(eid).also { evaluation ->
				localDataSource.saveEvaluations(uid, evaluation)
			}

		return localDataSource.getEvaluation(uid, eid)
	}

	override suspend fun addEvaluation(uid: String, evaluation: NewEvaluation): Evaluation {
		val newEvaluation = remoteDataSource.addEvaluation(evaluation)

		localDataSource.saveEvaluations(uid, newEvaluation)

		return newEvaluation
	}

	override suspend fun updateEvaluation(uid: String, eid: String, evaluation: UpdateEvaluation): Evaluation {
		val updateEvaluation = remoteDataSource.updateEvaluation(evaluation)

		localDataSource.saveEvaluations(uid, updateEvaluation)

		return updateEvaluation
	}

	override suspend fun removeEvaluation(uid: String, eid: String) {
		remoteDataSource.removeEvaluation(eid)
		localDataSource.removeEvaluation(uid, eid)
	}

	override suspend fun getEvaluations(uid: String, sid: String): List<Evaluation> {
		val isNetworkAvailable = networkRepository.isAvailable()

		if (isNetworkAvailable)
			remoteDataSource.getEvaluations(sid).also { evaluations ->
				localDataSource.saveEvaluations(uid, *evaluations.toTypedArray())
			}

		return localDataSource.getEvaluations(uid, sid)
	}
}