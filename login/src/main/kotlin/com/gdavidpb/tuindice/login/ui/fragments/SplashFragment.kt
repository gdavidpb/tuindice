package com.gdavidpb.tuindice.login.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.base.ui.fragments.NavigationFragment
import com.gdavidpb.tuindice.base.domain.model.ServicesStatus
import com.gdavidpb.tuindice.base.domain.model.StartUpAction
import com.gdavidpb.tuindice.base.domain.usecase.base.Result
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.base.ui.dialogs.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.base.utils.RequestCodes
import com.gdavidpb.tuindice.base.utils.extensions.bottomSheetDialog
import com.gdavidpb.tuindice.base.utils.extensions.connectionSnackBar
import com.gdavidpb.tuindice.base.utils.extensions.observe
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.domain.usecase.error.StartUpError
import com.gdavidpb.tuindice.login.presentation.viewmodel.SplashViewModel
import com.google.android.gms.common.GoogleApiAvailability
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : NavigationFragment() {

	private val viewModel by viewModel<SplashViewModel>()

	private val mainViewModel by sharedViewModel<MainViewModel>()

	private val googleApiAvailability by inject<GoogleApiAvailability>()

	override fun onCreateView() = R.layout.fragment_splash

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val intent = requireActivity().intent

		viewModel.fetchStartUpAction(dataString = intent.dataString ?: "")
	}

	override fun onInitObservers() {
		with(viewModel) {
			observe(startUpAction, ::startUpObserver)
		}
	}

	private fun showNoServicesDialog() {
		bottomSheetDialog<ConfirmationBottomSheetDialog> {
			titleResource = R.string.dialog_title_no_gms_failure
			messageResource = R.string.dialog_message_no_gms_failure

			positiveButton(R.string.exit) { requireActivity().finish() }
		}.apply {
			isCancelable = false
		}
	}

	private fun startUpObserver(result: Result<StartUpAction, StartUpError>?) {
		when (result) {
			is Result.OnSuccess -> {
				handleStartUpAction(action = result.value)
			}
			is Result.OnError -> {
				startUpErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun startUpErrorHandler(error: StartUpError?) {
		when (error) {
			is StartUpError.NoConnection -> connectionSnackBar(error.isNetworkAvailable)
			is StartUpError.NoServices -> handleNoServices(error.servicesStatus)
			else -> {}
		}
	}

	private fun handleNoServices(servicesStatus: ServicesStatus) {
		val activity = requireActivity()
		val status = servicesStatus.status

		if (googleApiAvailability.isUserResolvableError(status))
			googleApiAvailability.getErrorDialog(
				activity,
				servicesStatus.status,
				RequestCodes.PLAY_SERVICES_RESOLUTION_REQUEST
			)?.apply {
				setOnCancelListener { activity.finish() }
				setOnDismissListener { activity.finish() }
			}?.show() ?: showNoServicesDialog()
		else
			showNoServicesDialog()
	}

	private fun handleStartUpAction(action: StartUpAction) {
		when (action) {
			is StartUpAction.Main -> {
				val navOptions = NavOptions.Builder()
					.setPopUpTo(action.screen, true)
					.build()

				runCatching {
					findNavController().navigate(action.screen, null, navOptions)
				}.onFailure {
					navigate(SplashFragmentDirections.navToSummary())
				}

				mainViewModel.sync()
			}
			is StartUpAction.SignIn -> {
				navigate(SplashFragmentDirections.navToSignIn())
			}
		}
	}
}