package com.gdavidpb.tuindice.platform

import com.gdavidpb.tuindice.base.domain.model.UpdateAction

interface IosUpdateCapability {
	suspend fun checkForUpdate(stalenessDays: Int): UpdateAction?
	suspend fun launchUpdate(action: UpdateAction)
}
