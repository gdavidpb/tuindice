package com.gdavidpb.tuindice.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import com.gdavidpb.tuindice.base.utils.RequestCodes
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.base.utils.extension.launchRepeatOnLifecycle
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.screen.Screen
import com.gdavidpb.tuindice.ui.screen.TuIndiceApp
import com.gdavidpb.tuindice.ui.theme.TuIndiceTheme
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

	private val reviewManager by inject<ReviewManager>()

	private val updateManager by inject<AppUpdateManager>()

	private val viewModel by viewModel<MainViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			TuIndiceTheme {
				TuIndiceApp(
					screens = listOf(
						Screen.Summary,
						Screen.Record,
						Screen.About
					)
				)
			}
		}

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(viewEvent, ::eventCollector)
			}
		}

		viewModel.requestReviewAction(reviewManager)
	}

	private fun eventCollector(event: Main.Event) {
		when (event) {
			is Main.Event.ShowReviewDialog ->
				showReviewDialog(reviewInfo = event.reviewInfo)

			is Main.Event.StartUpdateFlow ->
				updateManager.startUpdateFlowForResult(
					event.updateInfo,
					AppUpdateType.IMMEDIATE,
					this@MainActivity,
					RequestCodes.APP_UPDATE
				)
		}
	}

	override fun onResume() {
		super.onResume()

		viewModel.checkUpdateAction(updateManager)
	}

	private fun showReviewDialog(reviewInfo: ReviewInfo) {
		launchRepeatOnLifecycle(state = Lifecycle.State.RESUMED) {
			reviewManager.launchReview(
				activity = this@MainActivity,
				reviewInfo = reviewInfo
			)
		}
	}
}