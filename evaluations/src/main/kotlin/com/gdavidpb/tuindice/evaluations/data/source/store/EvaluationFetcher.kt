package com.gdavidpb.tuindice.evaluations.data.source.store

import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import org.mobilenativefoundation.store.store5.Fetcher

class EvaluationFetcher(
	private val evaluationsApiDataSource: EvaluationsApiDataSource
) : Fetcher<EvaluationKey, List<RemoteEvaluation>> by Fetcher.of(
	fetch = { key: EvaluationKey ->
		require(key is EvaluationKey.Read)

		when (key) {
			is EvaluationKey.Read.All ->
				evaluationsApiDataSource.getEvaluations()

			is EvaluationKey.Read.ById ->
				listOfNotNull(evaluationsApiDataSource.getEvaluation(eid = key.eid))
		}
	}
)