package com.gdavidpb.tuindice.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import com.gdavidpb.tuindice.base.domain.model.ServicesStatus
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.base.utils.RequestCodes
import com.gdavidpb.tuindice.base.utils.extension.bottomSheetDialog
import com.gdavidpb.tuindice.base.utils.extension.launchRepeatOnLifecycle
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.route.TuIndiceApp
import com.gdavidpb.tuindice.ui.theme.TuIndiceTheme
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

	private val reviewManager by inject<ReviewManager>()
	private val updateManager by inject<AppUpdateManager>()
	private val googleApiAvailability by inject<GoogleApiAvailability>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			TuIndiceTheme {
				TuIndiceApp(
					startData = intent.dataString
				)
			}
		}

		//viewModel.requestReviewAction(reviewManager)
	}

	private fun eventCollector(event: Main.Event) {
		when (event) {
			is Main.Event.ShowNoServicesDialog ->
				showNoServicesDialog(status = event.status)

			is Main.Event.ShowReviewDialog ->
				showReviewDialog(reviewInfo = event.reviewInfo)

			is Main.Event.StartUpdateFlow ->
				startUpdateFlow(updateInfo = event.updateInfo)

			else -> {}
		}
	}

	override fun onResume() {
		super.onResume()
		//viewModel.checkUpdateAction(updateManager)
	}

	private fun showNoServicesDialog(status: ServicesStatus) {
		val dialog = {
			bottomSheetDialog<ConfirmationBottomSheetDialog> {
				titleResource = R.string.dialog_title_no_gms_failure
				messageResource = R.string.dialog_message_no_gms_failure

				positiveButton(R.string.exit) { requireActivity().finish() }
			}.apply {
				isCancelable = false
			}
		}

		if (googleApiAvailability.isUserResolvableError(status.status))
			googleApiAvailability.getErrorDialog(
				this,
				status.status,
				RequestCodes.PLAY_SERVICES_RESOLUTION
			)?.apply {
				setOnCancelListener { finish() }
				setOnDismissListener { finish() }
			}?.show() ?: dialog()
		else
			dialog()
	}

	private fun showReviewDialog(reviewInfo: ReviewInfo) {
		launchRepeatOnLifecycle(state = Lifecycle.State.RESUMED) {
			reviewManager.launchReview(
				activity = this@MainActivity,
				reviewInfo = reviewInfo
			)
		}
	}

	private fun startUpdateFlow(updateInfo: AppUpdateInfo) {
		updateManager.startUpdateFlowForResult(
			updateInfo,
			AppUpdateType.IMMEDIATE,
			this,
			RequestCodes.APP_UPDATE
		)
	}
}