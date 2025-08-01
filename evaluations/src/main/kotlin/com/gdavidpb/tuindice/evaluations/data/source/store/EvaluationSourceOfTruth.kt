package com.gdavidpb.tuindice.evaluations.data.source.store

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataSource
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.SourceOfTruth

class EvaluationSourceOfTruth(
	private val databaseDataSource: DatabaseDataSource,
	private val settingsDataSource: SettingsDataSource
) : SourceOfTruth<EvaluationKey, List<LocalEvaluation>, List<Evaluation>> by SourceOfTruth.of(
	reader = { key: EvaluationKey ->
		require(key is EvaluationKey.Read)

		when (key) {
			is EvaluationKey.Read.All ->
				databaseDataSource
					.getEvaluationsFlow()
					.map { evaluations -> evaluations.map { evaluation -> evaluation.toLocalEvaluation() } }

			is EvaluationKey.Read.ById ->
				flow {
					val evaluation = databaseDataSource
						.getEvaluation(eid = key.eid)
						?.toLocalEvaluation()

					val evaluations = listOfNotNull(evaluation)

					emit(evaluations)
				}
		}
	},
	writer = { key: EvaluationKey, input: List<LocalEvaluation> ->
		when (key) {
			is EvaluationKey.Read.All -> {
				settingsDataSource.setGetEvaluationsOnCooldown()

				databaseDataSource.saveEvaluations(
					evaluations = input
				)
			}

			is EvaluationKey.Read.ById ->
				databaseDataSource.saveEvaluations(
					evaluations = input
				)

			is EvaluationKey.Write.Add ->
				databaseDataSource.saveEvaluations(
					evaluations = input
				)

			is EvaluationKey.Write.Update ->
				databaseDataSource.updateEvaluation(
					evaluation = input.first()
				)

			is EvaluationKey.Remove.ById -> {
				databaseDataSource.removeEvaluation(
					eid = key.eid
				)
			}
		}
	},
	delete = { key ->
		require(key is EvaluationKey.Remove)

		when (key) {
			is EvaluationKey.Remove.ById ->
				databaseDataSource.removeEvaluation(
					eid = key.eid
				)
		}
	}
)