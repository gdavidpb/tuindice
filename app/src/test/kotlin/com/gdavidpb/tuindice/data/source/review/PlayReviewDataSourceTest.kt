package com.gdavidpb.tuindice.data.source.review

import android.app.Activity
import com.gdavidpb.tuindice.data.model.playcore.PlayCoreSurface
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityDataSource
import com.gdavidpb.tuindice.data.source.playcore.FakePlayCoreAvailabilityDataRepository
import com.gdavidpb.tuindice.data.source.playcore.RecordingReportingRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayReviewDataSourceTest {
	@Test
	fun launchReview_whenPlayCoreIsUnavailable_doesNotRequestReview() = runTest {
		val reviewManager = RecordingReviewManager()
		val playCoreAvailabilityRepository = FakePlayCoreAvailabilityDataRepository(available = false)
		val dataSource = PlayReviewDataSource(
			reviewManager = reviewManager,
			currentActivityDataSource = CurrentActivityDataSource(),
			playCoreAvailabilityRepository = playCoreAvailabilityRepository,
			reportingRepository = RecordingReportingRepository()
		)

		dataSource.launchReview()

		assertEquals(0, reviewManager.requestReviewCalls)
		assertEquals(listOf(PlayCoreSurface.Review), playCoreAvailabilityRepository.calls)
	}

	@Test
	fun launchReview_whenThereIsNoActivity_doesNotRequestReview() = runTest {
		val reviewManager = RecordingReviewManager()
		val playCoreAvailabilityRepository = FakePlayCoreAvailabilityDataRepository(available = true)
		val dataSource = PlayReviewDataSource(
			reviewManager = reviewManager,
			currentActivityDataSource = CurrentActivityDataSource(),
			playCoreAvailabilityRepository = playCoreAvailabilityRepository,
			reportingRepository = RecordingReportingRepository()
		)

		dataSource.launchReview()

		assertEquals(0, reviewManager.requestReviewCalls)
		assertEquals(listOf(PlayCoreSurface.Review), playCoreAvailabilityRepository.calls)
	}
}

private class RecordingReviewManager : ReviewManager {
	var requestReviewCalls = 0

	override fun requestReviewFlow(): Task<ReviewInfo> {
		requestReviewCalls++
		return Tasks.forException(IllegalStateException("request review should not be called"))
	}

	override fun launchReviewFlow(
		activity: Activity,
		reviewInfo: ReviewInfo
	): Task<Void> = Tasks.forResult(null)
}
