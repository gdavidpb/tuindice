package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.MutableStoreBuilder

@OptIn(ExperimentalStoreApi::class)
class EvaluationStore(
	private val fetcher: EvaluationFetcher,
	private val sourceOfTruth: EvaluationSourceOfTruth,
	private val converter: EvaluationConverter,
	private val updater: EvaluationUpdater
) : MutableStore<EvaluationKey, List<Evaluation>> by MutableStoreBuilder.from(
	fetcher = fetcher,
	sourceOfTruth = sourceOfTruth,
	converter = converter
).build(
	updater = updater
)