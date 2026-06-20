package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult

interface UpdateRepository {
	suspend fun checkForUpdate(stalenessDays: Int): UpdateAction?
	suspend fun launchUpdate(action: UpdateAction): UpdateLaunchResult
}
