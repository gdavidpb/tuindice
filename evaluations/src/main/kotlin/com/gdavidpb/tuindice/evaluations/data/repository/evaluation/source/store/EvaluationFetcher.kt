package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store

import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.model.RemoteEvaluation
import org.mobilenativefoundation.store.store5.Fetcher

class EvaluationFetcher(
	private val remoteDataSource: RemoteDataSource
) : Fetcher<EvaluationKey, List<RemoteEvaluation>> by Fetcher.of(
	fetch = { key: EvaluationKey ->
		require(key is EvaluationKey.Read)

		when (key) {
			is EvaluationKey.Read.All ->
				remoteDataSource.getEvaluations()

			is EvaluationKey.Read.ById ->
				listOfNotNull(remoteDataSource.getEvaluation(eid = key.eid))
		}
	}
)