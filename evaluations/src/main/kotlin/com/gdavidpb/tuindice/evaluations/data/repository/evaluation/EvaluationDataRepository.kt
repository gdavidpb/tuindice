package com.gdavidpb.tuindice.evaluations.data.repository.evaluation

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store.EvaluationKey
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest

class EvaluationDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val settingsDataSource: SettingsDataSource
) : EvaluationRepository {
	private val store: Store<EvaluationKey, List<Evaluation>> by lazy {
		StoreBuilder.from(
			fetcher = Fetcher.of { key: EvaluationKey ->
				when (key) {
					is EvaluationKey.Read.All ->
						remoteDataSource.getEvaluations()
				}
			},
			sourceOfTruth = SourceOfTruth.of(
				reader = { key: EvaluationKey ->
					when (key) {
						is EvaluationKey.Read.All ->
							localDataSource.getEvaluations(
								uid = key.uid
							)
								.map { evaluations -> evaluations.map { evaluation -> evaluation.toEvaluation() } }
					}
				},
				writer = { key: EvaluationKey, output: List<RemoteEvaluation> ->
					when (key) {
						is EvaluationKey.Read.All -> {
							settingsDataSource.setGetEvaluationsOnCooldown()

							localDataSource.saveEvaluations(
								uid = key.uid,
								evaluations = output.map { evaluation -> evaluation.toLocalEvaluation() }
							)
						}
					}
				}
			)
		)
			.build()
	}

	override suspend fun getEvaluations(uid: String): Flow<List<Evaluation>> {
		val isOnCooldown = settingsDataSource.isGetEvaluationsOnCooldown()

		val stream = if (isOnCooldown)
			store.stream(
				request = StoreReadRequest.cached(
					key = EvaluationKey.Read.All(uid),
					refresh = false
				)
			)
		else
			store.stream(
				request = StoreReadRequest.fresh(
					key = EvaluationKey.Read.All(uid),
					fallBackToSourceOfTruth = true
				)
			)

		return stream
			.distinctUntilChanged()
			.mapNotNull { response -> response.requireData() }
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