package com.gdavidpb.tuindice.summary.domain.usecase

/**
 * The three things Summary observes, each on its own because each can speak when the others do not:
 * the user (an account with nothing stored never emits one), what the sync says of the account and
 * what the hub says beyond the profile.
 */
class SummaryObservations(
	val user: ObserveUserUseCase,
	val sync: ObserveSyncUseCase,
	val hub: ObserveSummaryHubUseCase
)
