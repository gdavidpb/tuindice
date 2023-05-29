package com.gdavidpb.tuindice.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gdavidpb.tuindice.base.domain.model.ServicesStatus
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.base.utils.RequestCodes
import com.gdavidpb.tuindice.base.utils.extension.bottomSheetDialog
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.route.TuIndiceApp
import com.gdavidpb.tuindice.ui.theme.TuIndiceTheme
import com.google.android.gms.common.GoogleApiAvailability
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

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
	}

	private fun eventCollector(event: Main.Event) {
		when (event) {
			is Main.Event.ShowNoServicesDialog ->
				showNoServicesDialog(status = event.status)

			else -> {}
		}
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
}