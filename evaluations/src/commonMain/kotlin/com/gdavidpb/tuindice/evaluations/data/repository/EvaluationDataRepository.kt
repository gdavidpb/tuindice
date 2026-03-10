package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.data.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.mapper.toSubject
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.mapper.toRemoteEvaluation
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EvaluationDataRepository(
	private val databaseDataSource: DatabaseDataSource,
	private val evaluationsApiDataSource: EvaluationsApiDataSource,
	private val settingsDataSource: SettingsDataSource
) : EvaluationRepository {
	override suspend fun getEvaluationsFlow(): Flow<List<Evaluation>> {
		val isOnCooldown = settingsDataSource.isGetEvaluationsOnCooldown()

		if (!isOnCooldown) {
			val remoteEvaluations = evaluationsApiDataSource.getEvaluations()
			val localEvaluations = remoteEvaluations.map { it.toLocalEvaluation() }

			databaseDataSource.saveEvaluations(localEvaluations)
			settingsDataSource.setGetEvaluationsOnCooldown()
		}

		return databaseDataSource.getEvaluationsFlow()
			.map { localEvaluations -> localEvaluations.map { it.toEvaluation() } }
	}

	override suspend fun getEvaluation(eid: String): Evaluation? {
		return databaseDataSource.getEvaluation(eid)?.toEvaluation()
	}

	override suspend fun addEvaluation(add: EvaluationAdd) {
		val evaluation = add.toEvaluation()
		val localEvaluation = evaluation.toLocalEvaluation()
		val remoteEvaluation = evaluation.toRemoteEvaluation()

		databaseDataSource.addEvaluation(localEvaluation)
		evaluationsApiDataSource.addEvaluation(remoteEvaluation)
	}

	override suspend fun updateEvaluation(update: EvaluationUpdate) {
		val evaluation = getEvaluation(update.id) ?: return
		val resolvedScheduleMode = update.scheduleMode ?: if (update.date != null) {
			EvaluationScheduleMode.DATED
		} else {
			evaluation.scheduleMode
		}
		val resolvedDate = when (resolvedScheduleMode) {
			EvaluationScheduleMode.CONTINUOUS -> null
			EvaluationScheduleMode.DATED -> update.date ?: evaluation.date
		}

		val updatedEvaluation = evaluation.copy(
			scheduleMode = resolvedScheduleMode,
			grade = update.grade,
			maxGrade = update.maxGrade ?: evaluation.maxGrade,
			date = resolvedDate,
			type = update.type ?: evaluation.type,
			state = computeEvaluationState(
				scheduleMode = resolvedScheduleMode,
				grade = update.grade,
				date = resolvedDate
			)
		)

		val localEvaluation = updatedEvaluation.toLocalEvaluation()
		val remoteEvaluation = updatedEvaluation.toRemoteEvaluation()

		databaseDataSource.updateEvaluation(localEvaluation)
		evaluationsApiDataSource.updateEvaluation(remoteEvaluation)
	}

	override suspend fun removeEvaluation(remove: EvaluationRemove) {
		databaseDataSource.removeEvaluation(remove.id)
		evaluationsApiDataSource.removeEvaluation(remove.id)
	}

	override suspend fun getAvailableSubjects(): List<Subject> {
		return databaseDataSource.getAvailableSubjects()
			.map { localSubject -> localSubject.toSubject() }
	}
}
