package com.gdavidpb.tuindice.evaluations.data.evaluation

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.SettingsDataSource
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform

class EvaluationDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val settingsDataSource: SettingsDataSource
) : EvaluationRepository {
	override suspend fun getEvaluation(uid: String, eid: String): Flow<Evaluation> {
		return localDataSource.getEvaluation(uid, eid)
			.distinctUntilChanged()
			.transform { evaluation ->
				if (evaluation != null)
					emit(evaluation)
				else
					emit(
						remoteDataSource.getEvaluation(eid).also { response ->
							localDataSource.saveEvaluations(uid, listOf(response))
						}
					)
			}
	}

	override suspend fun getEvaluations(uid: String): Flow<List<Evaluation>> {
		return localDataSource.getEvaluations(uid)
			.distinctUntilChanged()
			.transform { localEvaluations ->
				val isOnCooldown = settingsDataSource.isGetEvaluationsOnCooldown()

				if (isOnCooldown)
					emit(localEvaluations)
				else {
					if (localEvaluations.isNotEmpty()) emit(localEvaluations)

					val remoteEvaluations = remoteDataSource.getEvaluations()

					localDataSource.saveEvaluations(uid, remoteEvaluations)

					settingsDataSource.setGetEvaluationsOnCooldown()

					emit(remoteEvaluations)
				}
			}
	}

	override suspend fun addEvaluation(uid: String, add: EvaluationAdd) {
		localDataSource.addEvaluation(uid, add)

		val evaluation = remoteDataSource.addEvaluation(add)

		localDataSource.saveEvaluations(uid, listOf(evaluation))
	}

	override suspend fun updateEvaluation(uid: String, update: EvaluationUpdate): Evaluation {
		return remoteDataSource.updateEvaluation(update).also { evaluation ->
			localDataSource.saveEvaluations(uid, listOf(evaluation))
		}
	}

	override suspend fun removeEvaluation(uid: String, remove: EvaluationRemove) {
		localDataSource.removeEvaluation(uid, remove)
		noAwait { remoteDataSource.removeEvaluation(remove) }
	}

	override suspend fun getAvailableSubjects(uid: String): Flow<List<Subject>> {
		return localDataSource.getAvailableSubjects(uid)
	}
}