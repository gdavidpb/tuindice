package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.SettingsDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper.toLocalEvaluation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.SourceOfTruth

class EvaluationSourceOfTruth(
	private val localDataSource: LocalDataSource,
	private val settingsDataSource: SettingsDataSource
) : SourceOfTruth<EvaluationKey, List<RemoteEvaluation>, List<Evaluation>> by SourceOfTruth.of(
	reader = { key: EvaluationKey ->
		when (key) {
			is EvaluationKey.Read.All ->
				localDataSource.getEvaluations(
					uid = key.uid
				).map { evaluations -> evaluations.map { evaluation -> evaluation.toEvaluation() } }

			is EvaluationKey.Read.ById ->
				flow {
					val evaluation = localDataSource.getEvaluation(key.uid, key.eid)?.toEvaluation()
					val evaluations = listOfNotNull(evaluation)

					emit(evaluations)
				}
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

			is EvaluationKey.Read.ById -> {
				localDataSource.saveEvaluations(
					uid = key.uid,
					evaluations = output.map { evaluation -> evaluation.toLocalEvaluation() }
				)
			}
		}
	}
)