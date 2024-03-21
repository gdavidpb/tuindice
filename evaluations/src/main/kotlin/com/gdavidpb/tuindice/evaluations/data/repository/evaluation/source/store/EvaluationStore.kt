package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder

class EvaluationStore(
	private val fetcher: EvaluationFetcher,
	private val sourceOfTruth: EvaluationSourceOfTruth
) : Store<EvaluationKey, List<Evaluation>> by StoreBuilder.from(
	fetcher = fetcher,
	sourceOfTruth = sourceOfTruth
).build()