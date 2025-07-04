package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.data.source.store.EvaluationConverter
import com.gdavidpb.tuindice.evaluations.data.source.store.EvaluationFetcher
import com.gdavidpb.tuindice.evaluations.data.source.store.EvaluationKey
import com.gdavidpb.tuindice.evaluations.data.source.store.EvaluationReadResponse
import com.gdavidpb.tuindice.evaluations.data.source.store.EvaluationSourceOfTruth
import com.gdavidpb.tuindice.evaluations.data.source.store.EvaluationUpdater
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.impl.extensions.get

@OptIn(ExperimentalStoreApi::class)
class EvaluationDataRepository(
	private val fetcher: EvaluationFetcher,
	private val sourceOfTruth: EvaluationSourceOfTruth,
	private val converter: EvaluationConverter,
	private val updater: EvaluationUpdater,
	private val localDataSource: LocalDataSource,
	private val settingsDataSource: SettingsDataSource
) : MutableStore<EvaluationKey, List<Evaluation>> by MutableStoreBuilder.from(
	fetcher = fetcher,
	sourceOfTruth = sourceOfTruth,
	converter = converter
).build(
	updater = updater
), EvaluationRepository {
	override suspend fun getEvaluationsFlow(uid: String): Flow<List<Evaluation>> {
		val isOnCooldown = settingsDataSource.isGetEvaluationsOnCooldown()

		val evaluations = if (isOnCooldown)
			stream<EvaluationReadResponse>(
				request = StoreReadRequest.cached(
					key = EvaluationKey.Read.All(uid),
					refresh = false
				)
			)
		else
			stream<EvaluationReadResponse>(
				request = StoreReadRequest.fresh(
					key = EvaluationKey.Read.All(uid),
					fallBackToSourceOfTruth = true
				)
			)

		return evaluations
			.distinctUntilChanged()
			.mapNotNull { response -> response.dataOrNull() }
	}

	override suspend fun getEvaluation(uid: String, eid: String): Evaluation? {
		return get<EvaluationKey, List<Evaluation>, EvaluationReadResponse>(
			key = EvaluationKey.Read.ById(uid, eid)
		).firstOrNull()
	}

	override suspend fun addEvaluation(uid: String, add: EvaluationAdd) {
		val evaluation = add.toEvaluation()

		write(
			StoreWriteRequest.of(
				key = EvaluationKey.Write.Add(uid, evaluation),
				value = listOf(evaluation)
			)
		)
	}

	override suspend fun updateEvaluation(uid: String, update: EvaluationUpdate) {
		val evaluation = get<EvaluationKey, List<Evaluation>, EvaluationReadResponse>(
			key = EvaluationKey.Read.ById(uid = uid, eid = update.id)
		).first()

		val updatedEvaluation = evaluation.copy(
			grade = update.grade,
			maxGrade = update.maxGrade ?: evaluation.maxGrade,
			date = update.date ?: evaluation.date,
			type = update.type ?: evaluation.type
		)

		write(
			StoreWriteRequest.of(
				key = EvaluationKey.Write.Update(uid, updatedEvaluation),
				value = listOf(updatedEvaluation)
			)
		)
	}

	override suspend fun removeEvaluation(uid: String, remove: EvaluationRemove) {
		clear(
			EvaluationKey.Remove.ById(
				uid = uid,
				eid = remove.id
			)
		)
	}

	override suspend fun getAvailableSubjects(uid: String): List<Subject> {
		return localDataSource.getAvailableSubjects(uid)
			.map { localSubject -> localSubject.toSubject() }
	}
}