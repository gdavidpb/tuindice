package com.gdavidpb.tuindice.evaluations.data.repository.evaluation

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store.EvaluationKey
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store.EvaluationStore
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.impl.extensions.get

class EvaluationDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val settingsDataSource: SettingsDataSource,
	private val store: EvaluationStore
) : EvaluationRepository {

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
			.mapNotNull { response -> response.dataOrNull() }
	}

	override suspend fun getEvaluation(uid: String, eid: String): Evaluation? {
		return store.get(EvaluationKey.Read.ById(uid, eid))
			.firstOrNull()
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