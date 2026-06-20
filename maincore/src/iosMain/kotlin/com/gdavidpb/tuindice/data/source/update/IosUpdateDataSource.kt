package com.gdavidpb.tuindice.data.source.update

import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.platform.IosUpdateCapability

class IosUpdateDataSource(
	private val updateCapability: IosUpdateCapability
) : UpdateRepository {
	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? {
		return updateCapability.checkForUpdate(stalenessDays)
	}

	override suspend fun launchUpdate(action: UpdateAction): UpdateLaunchResult {
		updateCapability.launchUpdate(action)
		return UpdateLaunchResult.Launched
	}
}
