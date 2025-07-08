package com.gdavidpb.tuindice.evaluations.data.source.store

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.mapper.toRemoteEvaluation
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult

class EvaluationUpdater(
	private val evaluationsApiDataSource: EvaluationsApiDataSource
) : Updater<EvaluationKey, List<Evaluation>, EvaluationWriteResponse> by Updater.by(
	post = { key, input ->
		require(key is EvaluationKey.Write || key is EvaluationKey.Remove)

		runCatching {
			when (key) {
				is EvaluationKey.Write.Add ->
					evaluationsApiDataSource
						.addEvaluation(evaluation = input.first().toRemoteEvaluation())

				is EvaluationKey.Write.Update ->
					evaluationsApiDataSource
						.updateEvaluation(evaluation = input.first().toRemoteEvaluation())

				is EvaluationKey.Remove.ById ->
					evaluationsApiDataSource
						.removeEvaluation(eid = key.eid)

				else ->
					throw IllegalStateException()
			}
		}.fold(
			onSuccess = {
				UpdaterResult.Success.Typed(
					value = EvaluationWriteResponse(key)
				)
			},
			onFailure = { throwable ->
				UpdaterResult.Error.Exception(throwable)
			}
		)
	}
)