package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.RemoteDataSource
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult

class EvaluationUpdater(
	private val remoteDataSource: RemoteDataSource
) : Updater<EvaluationKey, List<Evaluation>, EvaluationWriteResponse> by Updater.by(
	post = { key, input ->
		require(key is EvaluationKey.Write)

		when (key) {
			is EvaluationKey.Write.Add -> {
				runCatching {
					remoteDataSource.addEvaluation(evaluation = input.first())
				}.fold(
					onSuccess = {
						UpdaterResult.Success.Typed(
							value = EvaluationWriteResponse(key)
						)
					}, onFailure = { throwable ->
						UpdaterResult.Error.Exception(throwable)
					})
			}
		}
	}
)