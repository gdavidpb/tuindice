package com.gdavidpb.tuindice.base.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.extension.await
import com.gdavidpb.tuindice.base.utils.extension.isUpdateAvailable
import com.gdavidpb.tuindice.base.utils.extension.isUpdateStalled
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.ktx.isImmediateUpdateAllowed

class GetUpdateInfoUseCase(
	override val configRepository: ConfigRepository,
	override val reportingRepository: ReportingRepository
) : EventUseCase<AppUpdateManager, AppUpdateInfo, Nothing>() {
	override suspend fun executeOnBackground(params: AppUpdateManager): AppUpdateInfo? {
		val updateInfo = params.appUpdateInfo.await() ?: return null

		if (updateInfo.isUpdateStalled) return updateInfo

		val stalenessDays = configRepository.getTimeUpdateStalenessDays()

		val clientVersionStalenessDays = (updateInfo.clientVersionStalenessDays() ?: -1)
		val isUpdateAvailable = updateInfo.isUpdateAvailable
		val isImmediateUpdateAllowed = updateInfo.isImmediateUpdateAllowed
		val isStalenessDaysPassed = clientVersionStalenessDays >= stalenessDays

		return if (isUpdateAvailable && isImmediateUpdateAllowed && isStalenessDaysPassed)
			updateInfo
		else
			null
	}
}