package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult

class EvaluationUpdater(
	private val remoteDataSource: RemoteDataSource
) : Updater<EvaluationKey, List<Evaluation>, EvaluationWriteResponse> by Updater.by(
	post = { key, input ->
		require(key is EvaluationKey.Write || key is EvaluationKey.Remove)

		runCatching {
			when (key) {
				is EvaluationKey.Write.Add ->
					remoteDataSource.addEvaluation(add = input.first().toEvaluationAdd())

				is EvaluationKey.Write.Update ->
					remoteDataSource.updateEvaluation(update = input.first().toEvaluationUpdate())

				is EvaluationKey.Remove.ById ->
					remoteDataSource.removeEvaluation(remove = EvaluationRemove(id = key.eid))

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