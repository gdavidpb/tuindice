package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.await
import com.gdavidpb.tuindice.base.utils.extension.isUpdateAvailable
import com.gdavidpb.tuindice.base.utils.extension.isUpdateStalled
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class GetUpdateInfoUseCase(
	private val configRepository: ConfigRepository
) : FlowUseCase<AppUpdateManager, AppUpdateInfo, Nothing>() {
	override suspend fun executeOnBackground(params: AppUpdateManager): Flow<AppUpdateInfo> {
		val updateInfo = params.appUpdateInfo.await() ?: return emptyFlow()

		if (updateInfo.isUpdateStalled) return flowOf(updateInfo)

		val stalenessDays = configRepository.getTimeUpdateStalenessDays()

		val clientVersionStalenessDays = (updateInfo.clientVersionStalenessDays() ?: -1)
		val isUpdateAvailable = updateInfo.isUpdateAvailable
		val isImmediateUpdateAllowed = updateInfo.isImmediateUpdateAllowed
		val isStalenessDaysPassed = clientVersionStalenessDays >= stalenessDays

		return if (isUpdateAvailable && isImmediateUpdateAllowed && isStalenessDaysPassed)
			flowOf(updateInfo)
		else
			emptyFlow()
	}
}