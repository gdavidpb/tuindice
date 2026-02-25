package com.gdavidpb.tuindice.data.source.update

import com.gdavidpb.tuindice.data.source.activity.CurrentActivityProvider
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.UpdateGateway
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import kotlinx.coroutines.tasks.await

class PlayUpdateGateway(
	private val appUpdateManager: AppUpdateManager,
	private val currentActivityProvider: CurrentActivityProvider
) : UpdateGateway {
	private var pendingUpdateInfo: AppUpdateInfo? = null

	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? {
		val updateInfo = appUpdateManager.appUpdateInfo.await() ?: return null

		if (updateInfo.isUpdateStalled) {
			pendingUpdateInfo = updateInfo

			return UpdateAction.Immediate
		}

		val clientVersionStalenessDays = updateInfo.clientVersionStalenessDays() ?: -1
		val isStalenessDaysPassed = clientVersionStalenessDays >= stalenessDays
		val isImmediateUpdateAllowed = updateInfo.isImmediateUpdateAllowed
		val isUpdateAvailable = updateInfo.isUpdateAvailable

		return if (isUpdateAvailable && isImmediateUpdateAllowed && isStalenessDaysPassed) {
			pendingUpdateInfo = updateInfo

			UpdateAction.Immediate
		} else {
			null
		}
	}

	override suspend fun launchUpdate(action: UpdateAction) {
		val activity = currentActivityProvider.get() ?: return
		val updateInfo = pendingUpdateInfo ?: appUpdateManager.appUpdateInfo.await() ?: return

		when (action) {
			UpdateAction.Immediate ->
				appUpdateManager.startUpdateFlowForResult(
					updateInfo,
					AppUpdateType.IMMEDIATE,
					activity,
					APP_UPDATE_REQUEST_CODE
				)
		}
	}

	companion object {
		private const val APP_UPDATE_REQUEST_CODE = 1001
	}
}

private val AppUpdateInfo.isUpdateAvailable: Boolean
	get() = updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE

private val AppUpdateInfo.isUpdateStalled: Boolean
	get() = updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
