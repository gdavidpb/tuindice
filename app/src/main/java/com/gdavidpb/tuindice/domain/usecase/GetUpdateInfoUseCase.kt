package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.extensions.await
import com.gdavidpb.tuindice.utils.extensions.isUpdateAvailable
import com.gdavidpb.tuindice.utils.extensions.isUpdateStalled
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.ktx.isImmediateUpdateAllowed

class GetUpdateInfoUseCase(
        private val configRepository: ConfigRepository
) : EventUseCase<AppUpdateManager, AppUpdateInfo, Nothing>() {
    override suspend fun executeOnBackground(params: AppUpdateManager): AppUpdateInfo? {
        val updateInfo = params.appUpdateInfo.await() ?: return null

        if (updateInfo.isUpdateStalled) return updateInfo

        val stalenessDays = configRepository.getLong(ConfigKeys.TIME_UPDATE_STALENESS_DAYS)

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