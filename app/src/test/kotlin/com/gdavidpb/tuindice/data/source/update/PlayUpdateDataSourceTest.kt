package com.gdavidpb.tuindice.data.source.update

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.data.model.playcore.PlayCoreSurface
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityDataSource
import com.gdavidpb.tuindice.data.source.playcore.FakePlayCoreAvailabilityDataRepository
import com.gdavidpb.tuindice.data.source.playcore.RecordingReportingRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.InstallStateUpdatedListener
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class PlayUpdateDataSourceTest {
	@Test
	fun checkForUpdate_whenPlayCoreIsUnavailable_doesNotRequestUpdateInfo() = runTest {
		val appUpdateManager = RecordingAppUpdateManager()
		val playCoreAvailabilityRepository = FakePlayCoreAvailabilityDataRepository(available = false)
		val dataSource = PlayUpdateDataSource(
			context = TestContext(),
			appUpdateManager = appUpdateManager,
			currentActivityDataSource = CurrentActivityDataSource(),
			playCoreAvailabilityRepository = playCoreAvailabilityRepository,
			reportingRepository = RecordingReportingRepository()
		)

		val result = dataSource.checkForUpdate(stalenessDays = 1)

		assertNull(result)
		assertEquals(0, appUpdateManager.appUpdateInfoCalls)
		assertEquals(listOf(PlayCoreSurface.Update), playCoreAvailabilityRepository.calls)
	}

	@Test
	fun checkForUpdate_whenUpdateInfoFails_reportsAndReturnsNull() = runTest {
		val failure = IllegalStateException("play update failed")
		val appUpdateManager = RecordingAppUpdateManager(
			appUpdateInfoTask = Tasks.forException(failure)
		)
		val reportingRepository = RecordingReportingRepository()
		val dataSource = PlayUpdateDataSource(
			context = TestContext(),
			appUpdateManager = appUpdateManager,
			currentActivityDataSource = CurrentActivityDataSource(),
			playCoreAvailabilityRepository = FakePlayCoreAvailabilityDataRepository(available = true),
			reportingRepository = reportingRepository
		)

		val result = dataSource.checkForUpdate(stalenessDays = 1)

		assertNull(result)
		assertEquals(1, appUpdateManager.appUpdateInfoCalls)
		assertEquals(listOf("play_update_check_failed"), reportingRepository.loggedMessages)
		assertEquals(1, reportingRepository.loggedExceptions.size)
		assertSame(failure, reportingRepository.loggedExceptions.single())
	}

	@Test
	fun launchUpdate_whenPlayCoreIsUnavailable_doesNotRequestUpdateInfo() = runTest {
		val appUpdateManager = RecordingAppUpdateManager()
		val playCoreAvailabilityRepository = FakePlayCoreAvailabilityDataRepository(available = false)
		val dataSource = PlayUpdateDataSource(
			context = TestContext(),
			appUpdateManager = appUpdateManager,
			currentActivityDataSource = CurrentActivityDataSource(),
			playCoreAvailabilityRepository = playCoreAvailabilityRepository,
			reportingRepository = RecordingReportingRepository()
		)

		dataSource.launchUpdate(UpdateAction.Immediate)

		assertEquals(0, appUpdateManager.appUpdateInfoCalls)
		assertEquals(0, appUpdateManager.launchUpdateCalls)
		assertEquals(listOf(PlayCoreSurface.Update), playCoreAvailabilityRepository.calls)
	}
}

private class TestContext : ContextWrapper(null) {
	val startedIntents = mutableListOf<Intent>()

	override fun getPackageName(): String = "com.gdavidpb.tuindice"

	override fun startActivity(intent: Intent) {
		startedIntents += intent
	}
}

private class RecordingAppUpdateManager(
	private val appUpdateInfoTask: Task<AppUpdateInfo> =
		Tasks.forException(IllegalStateException("app update info should not be requested"))
) : AppUpdateManager {
	var appUpdateInfoCalls = 0
	var launchUpdateCalls = 0

	override fun getAppUpdateInfo(): Task<AppUpdateInfo> {
		appUpdateInfoCalls++
		return appUpdateInfoTask
	}

	override fun completeUpdate(): Task<Void> = Tasks.forResult(null)

	override fun startUpdateFlow(
		appUpdateInfo: AppUpdateInfo,
		activity: Activity,
		appUpdateOptions: AppUpdateOptions
	): Task<Int> = Tasks.forException(UnsupportedOperationException())

	override fun registerListener(listener: InstallStateUpdatedListener) = Unit

	override fun unregisterListener(listener: InstallStateUpdatedListener) = Unit

	override fun startUpdateFlowForResult(
		appUpdateInfo: AppUpdateInfo,
		activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
		appUpdateOptions: AppUpdateOptions
	): Boolean {
		launchUpdateCalls++
		return true
	}

	override fun startUpdateFlowForResult(
		appUpdateInfo: AppUpdateInfo,
		appUpdateType: Int,
		activity: Activity,
		requestCode: Int
	): Boolean {
		launchUpdateCalls++
		return true
	}

	@Throws(IntentSender.SendIntentException::class)
	override fun startUpdateFlowForResult(
		appUpdateInfo: AppUpdateInfo,
		appUpdateType: Int,
		starter: IntentSenderForResultStarter,
		requestCode: Int
	): Boolean {
		launchUpdateCalls++
		return true
	}

	@Throws(IntentSender.SendIntentException::class)
	override fun startUpdateFlowForResult(
		appUpdateInfo: AppUpdateInfo,
		activity: Activity,
		appUpdateOptions: AppUpdateOptions,
		requestCode: Int
	): Boolean {
		launchUpdateCalls++
		return true
	}

	@Throws(IntentSender.SendIntentException::class)
	override fun startUpdateFlowForResult(
		appUpdateInfo: AppUpdateInfo,
		starter: IntentSenderForResultStarter,
		appUpdateOptions: AppUpdateOptions,
		requestCode: Int
	): Boolean {
		launchUpdateCalls++
		return true
	}
}
