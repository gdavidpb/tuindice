package com.gdavidpb.tuindice.login.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.base.domain.model.ServicesStatus
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragment.NavigationFragment
import com.gdavidpb.tuindice.base.utils.RequestCodes
import com.gdavidpb.tuindice.base.utils.extension.bottomSheetDialog
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.base.utils.extension.launchRepeatOnLifecycle
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.presentation.contract.Splash
import com.gdavidpb.tuindice.login.presentation.viewmodel.SplashViewModel
import com.google.android.gms.common.GoogleApiAvailability
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : NavigationFragment() {

	private val viewModel by viewModel<SplashViewModel>()

	private val googleApiAvailability by inject<GoogleApiAvailability>()

	override fun onCreateView() = R.layout.fragment_splash

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(viewState, ::stateCollector)
				collect(viewEvent, ::effectCollector)
			}
		}

		val intent = requireActivity().intent

		viewModel.startUpAction(data = intent.dataString ?: "")
	}

	private fun stateCollector(state: Splash.State) {
		when (state) {
			Splash.State.Starting -> {}
			Splash.State.Started -> {}
			Splash.State.Failed -> {}
		}
	}

	private fun effectCollector(event: Splash.Event) {
		when (event) {
			is Splash.Event.NavigateTo -> navigateToEvent(navId = event.navId)
			is Splash.Event.NavigateToSignIn -> navigateToSignInEvent()
			is Splash.Event.ShowNoServicesDialog -> showNoServicesEvent(status = event.status)
		}
	}

	private fun navigateToEvent(@IdRes navId: Int) {
		val navOptions = NavOptions.Builder()
			.setPopUpTo(navId, true)
			.build()

		runCatching {
			findNavController().navigate(navId, null, navOptions)
		}.onFailure {
			navigate(SplashFragmentDirections.navToSummary())
		}
	}

	private fun navigateToSignInEvent() {
		navigate(SplashFragmentDirections.navToSignIn())
	}

	private fun showNoServicesEvent(status: ServicesStatus) {
		val activity = requireActivity()
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
				activity,
				status.status,
				RequestCodes.PLAY_SERVICES_RESOLUTION
			)?.apply {
				setOnCancelListener { activity.finish() }
				setOnDismissListener { activity.finish() }
			}?.show() ?: dialog()
		else
			dialog()
	}
}