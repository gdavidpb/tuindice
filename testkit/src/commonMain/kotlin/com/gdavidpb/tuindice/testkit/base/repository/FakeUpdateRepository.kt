package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository

class FakeUpdateRepository(
	private val updateAction: UpdateAction? = null,
	private val launchResult: UpdateLaunchResult = UpdateLaunchResult.Launched
) : UpdateRepository {
	var checkCalls = mutableListOf<Int>()
	val launchedActions = mutableListOf<UpdateAction>()

	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? {
		checkCalls += stalenessDays
		return updateAction
	}

	override suspend fun launchUpdate(action: UpdateAction): UpdateLaunchResult {
		launchedActions += action
		return launchResult
	}
}
