package com.gdavidpb.tuindice.evaluations.data.repository.evaluation

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class EvaluationDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val settingsDataSource: SettingsDataSource
) : EvaluationRepository {
	override suspend fun getEvaluations(uid: String): Flow<List<Evaluation>> {
		return localDataSource.getEvaluations(uid)
			.distinctUntilChanged()
			.map { localEvaluations ->
				localEvaluations.map { localEvaluation ->
					localEvaluation.toEvaluation()
				}
			}
			.onStart {
				val isOnCooldown = settingsDataSource.isGetEvaluationsOnCooldown()

				if (!isOnCooldown) {
					val remoteEvaluations = remoteDataSource
						.getEvaluations()

					val localEvaluations = remoteEvaluations
						.map { remoteEvaluation -> remoteEvaluation.toLocalEvaluation() }

					localDataSource.saveEvaluations(uid, localEvaluations)

					settingsDataSource.setGetEvaluationsOnCooldown()
				}
			}
	}

	override suspend fun getEvaluation(uid: String, eid: String): Evaluation? {
		return localDataSource.getEvaluation(uid, eid)?.toEvaluation()
			?: remoteDataSource.getEvaluation(eid)
				?.toEvaluation()
				?.also { evaluation ->
					val localEvaluation = evaluation.toLocalEvaluation()

					localDataSource.saveEvaluations(uid, listOf(localEvaluation))
				}
	}

	override suspend fun addEvaluation(uid: String, add: EvaluationAdd) {
		localDataSource.addEvaluation(uid, add)

		noAwait {
			remoteDataSource.addEvaluation(add).also { evaluation ->
				localDataSource.saveEvaluations(uid, listOf(evaluation.toLocalEvaluation()))
			}
		}
	}

	override suspend fun updateEvaluation(uid: String, update: EvaluationUpdate) {
		localDataSource.updateEvaluation(uid, update)

		noAwait {
			remoteDataSource.updateEvaluation(update).also { evaluation ->
				localDataSource.saveEvaluations(uid, listOf(evaluation.toLocalEvaluation()))
			}
		}
	}

	override suspend fun removeEvaluation(uid: String, remove: EvaluationRemove) {
		localDataSource.removeEvaluation(uid, remove)

		noAwait {
			remoteDataSource.removeEvaluation(remove)
		}
	}

	override suspend fun getAvailableSubjects(uid: String): List<Subject> {
		return localDataSource.getAvailableSubjects(uid)
	}
}