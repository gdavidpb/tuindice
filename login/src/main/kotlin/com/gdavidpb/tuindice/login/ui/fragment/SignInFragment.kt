package com.gdavidpb.tuindice.login.ui.fragment

import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.ui.fragment.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extension.animateShake
import com.gdavidpb.tuindice.base.utils.extension.beginTransition
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.base.utils.extension.connectionSnackBar
import com.gdavidpb.tuindice.base.utils.extension.errorSnackBar
import com.gdavidpb.tuindice.base.utils.extension.hideSoftKeyboard
import com.gdavidpb.tuindice.base.utils.extension.launchRepeatOnLifecycle
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import kotlinx.android.synthetic.main.fragment_sign_in.btnSignIn
import kotlinx.android.synthetic.main.fragment_sign_in.cLayoutSignIn
import kotlinx.android.synthetic.main.fragment_sign_in.iViewLogo
import kotlinx.android.synthetic.main.fragment_sign_in.pBarLogging
import kotlinx.android.synthetic.main.fragment_sign_in.tInputPassword
import kotlinx.android.synthetic.main.fragment_sign_in.tInputUsbId
import kotlinx.android.synthetic.main.fragment_sign_in.tViewPolicies
import kotlinx.android.synthetic.main.fragment_sign_in.vFlipperLoading
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInFragment : NavigationFragment() {

	private val viewModel by viewModel<SignInViewModel>()

	override fun onCreateView() = R.layout.fragment_sign_in

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		tInputPassword.setAction { onSignInClick() }
		btnSignIn.setOnClickListener { onSignInClick() }
		iViewLogo.setOnClickListener { onLogoClick() }
		tViewPolicies.setOnTermsAndConditionsClickListener { onTermsAndConditionsClick() }
		tViewPolicies.setOnPrivacyPolicyClickListener { onPrivacyPolicyClick() }

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(viewState, ::stateCollector)
				collect(viewEvent, ::eventCollector)
			}
		}
	}

	private fun stateCollector(state: SignIn.State) {
		when (state) {
			is SignIn.State.Idle -> showLoading(value = false)
			is SignIn.State.LoggingIn -> showLoading(value = true, messages = state.messages)
			is SignIn.State.LoggedIn -> {}
		}
	}

	private fun eventCollector(event: SignIn.Event) {
		when (event) {
			is SignIn.Event.NavigateToSplash -> navigateToSplash()
			is SignIn.Event.NavigateToPrivacyPolicy -> navigateToPrivacyPolicy()
			is SignIn.Event.NavigateToTermsAndConditions -> navigateToTermsAndConditions()
			is SignIn.Event.HideSoftKeyboard -> hideSoftKeyboard()
			is SignIn.Event.ShakeLogo -> iViewLogo.animateShake()
			is SignIn.Event.ShowPasswordEmptyError -> tInputPassword.setError(R.string.error_empty)
			is SignIn.Event.ShowUsbIdEmptyError -> tInputUsbId.setError(R.string.error_empty)
			is SignIn.Event.ShowUsbIdInvalidError -> tInputUsbId.setError(R.string.error_usb_id)
			is SignIn.Event.ShowTimeoutSnackBar -> errorSnackBar(R.string.snack_service_unavailable) { onSignInClick() }
			is SignIn.Event.ShowNoConnectionSnackBar -> connectionSnackBar(event.isNetworkAvailable) { onSignInClick() }
			is SignIn.Event.ShowUnavailableSnackBar -> errorSnackBar(R.string.snack_timeout) { onSignInClick() }
			is SignIn.Event.ShowAccountDisabledSnackBar -> errorSnackBar(R.string.snack_account_disabled)
			is SignIn.Event.ShowInvalidCredentialsSnackBar -> errorSnackBar(R.string.snack_invalid_credentials)
			is SignIn.Event.ShowDefaultErrorSnackBar -> errorSnackBar { onSignInClick() }
		}
	}

	private fun onSignInClick() {
		val params = SignInParams(
			usbId = tInputUsbId.getUsbId(),
			password = tInputPassword.getPassword()
		)

		viewModel.signInAction(params)
	}

	private fun onLogoClick() {
		viewModel.tapLogoAction()
	}

	private fun onTermsAndConditionsClick() {
		viewModel.openTermsAndConditionsAction()
	}

	private fun onPrivacyPolicyClick() {
		viewModel.openPrivacyPolicyAction()
	}

	private fun navigateToSplash() {
		navigate(SignInFragmentDirections.navToSplash())
	}

	private fun navigateToTermsAndConditions() {
		navigate(
			SignInFragmentDirections.navToBrowser(
				title = getString(R.string.label_terms_and_conditions),
				url = BuildConfig.URL_APP_TERMS_AND_CONDITIONS
			)
		)
	}

	private fun navigateToPrivacyPolicy() {
		navigate(
			SignInFragmentDirections.navToBrowser(
				title = getString(R.string.label_privacy_policy),
				url = BuildConfig.URL_APP_PRIVACY_POLICY
			)
		)
	}

	private fun showLoading(value: Boolean, messages: List<String> = emptyList()) {
		if (pBarLogging.isVisible == value) return

		vFlipperLoading.setMessages(messages)

		val layout = if (value) {
			pBarLogging.isVisible = true
			vFlipperLoading.isVisible = true
			vFlipperLoading.startFlipping()

			R.layout.fragment_sign_in_loading
		} else {
			pBarLogging.isVisible = false
			vFlipperLoading.isVisible = false
			vFlipperLoading.stopFlipping()

			R.layout.fragment_sign_in
		}

		cLayoutSignIn.beginTransition(targetLayout = layout) {
			interpolator = OvershootInterpolator()
			duration = 1000
		}
	}
}