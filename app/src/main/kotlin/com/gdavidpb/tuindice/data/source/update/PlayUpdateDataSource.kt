package com.gdavidpb.tuindice.data.source.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.data.model.playcore.PlayCoreSurface
import com.gdavidpb.tuindice.data.repository.playcore.PlayCoreAvailabilityDataRepository
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityDataSource
import com.gdavidpb.tuindice.data.source.playcore.reportPlayCoreFailure
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import kotlinx.coroutines.tasks.await

class PlayUpdateDataSource(
	private val context: Context,
	private val appUpdateManager: AppUpdateManager,
	private val currentActivityDataSource: CurrentActivityDataSource,
	private val playCoreAvailabilityRepository: PlayCoreAvailabilityDataRepository,
	private val reportingRepository: ReportingRepository
) : UpdateRepository {
	private var pendingUpdateInfo: AppUpdateInfo? = null

	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? {
		if (!playCoreAvailabilityRepository.isAvailable(PlayCoreSurface.Update)) return null

		val updateInfo = runCatching {
			appUpdateManager.appUpdateInfo.await()
		}.onFailure { throwable ->
			reportingRepository.reportPlayCoreFailure(
				message = "play_update_check_failed",
				throwable = throwable
			)
		}.getOrNull() ?: return null

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
		if (!playCoreAvailabilityRepository.isAvailable(PlayCoreSurface.Update)) {
			openStoreFallback()
			return
		}

		val activity = currentActivityDataSource.get() ?: run {
			openStoreFallback()
			return
		}
		val updateInfo = pendingUpdateInfo ?: runCatching {
			appUpdateManager.appUpdateInfo.await()
		}.onFailure { throwable ->
			reportingRepository.reportPlayCoreFailure(
				message = "play_update_launch_info_failed",
				throwable = throwable
			)
		}.getOrNull() ?: run {
			openStoreFallback()
			return
		}

		when (action) {
			UpdateAction.Immediate ->
				runCatching {
					appUpdateManager.startUpdateFlowForResult(
						updateInfo,
						AppUpdateType.IMMEDIATE,
						activity,
						APP_UPDATE_REQUEST_CODE
					)
				}.onFailure { throwable ->
					reportingRepository.reportPlayCoreFailure(
						message = "play_update_launch_failed",
						throwable = throwable
					)
					openStoreFallback()
				}
		}
	}

	private fun openStoreFallback() {
		val packageName = context.packageName
		val marketUri = Uri.parse("market://details?id=$packageName")
		val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")

		val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		val webIntent = Intent(Intent.ACTION_VIEW, webUri)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

		runCatching {
			context.startActivity(marketIntent)
		}.onFailure {
			runCatching { context.startActivity(webIntent) }
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
