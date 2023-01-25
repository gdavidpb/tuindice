package com.gdavidpb.tuindice.login.ui.fragment

import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.domain.usecase.base.Event
import com.gdavidpb.tuindice.base.ui.fragment.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extension.*
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.domain.error.SignInError
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.ui.adapter.LoadingAdapter
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInFragment : NavigationFragment() {

	private val viewModel by viewModel<SignInViewModel>()

	private val loadingMessages by config { getLoadingMessages() }

	override fun onCreateView() = R.layout.fragment_sign_in

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		initPoliciesLinks()
		initLoadingMessagesFlipper()

		tInputPassword.setAction { onSignInClick() }
		iViewLogo.onClickOnce(::onLogoClick)
		btnSignIn.onClickOnce(::onSignInClick)
	}

	override fun onInitObservers() {
		with(viewModel) {
			observe(signIn, ::signInObserver)
		}
	}

	private fun onLogoClick() {
		iViewLogo.animateShake()
	}

	private fun onSignInClick() {
		viewModel.signIn(
			usbId = tInputUsbId.getUsbId(),
			password = tInputPassword.getPassword()
		)
	}

	private fun initPoliciesLinks() {
		val accentColor = requireContext().getCompatColor(R.color.color_accent)

		tViewPolicies.apply {
			setSpans {
				listOf(
					ForegroundColorSpan(accentColor),
					TypefaceSpan("sans-serif-medium"),
					UnderlineSpan()
				)
			}

			setLink(getString(R.string.link_terms_and_conditions)) {
				navigate(
					SignInFragmentDirections.navToBrowser(
						title = getString(R.string.label_terms_and_conditions),
						url = BuildConfig.URL_APP_TERMS_AND_CONDITIONS
					)
				)
			}

			setLink(getString(R.string.link_privacy_policy)) {
				navigate(
					SignInFragmentDirections.navToBrowser(
						title = getString(R.string.label_privacy_policy),
						url = BuildConfig.URL_APP_PRIVACY_POLICY
					)
				)
			}
		}.build()
	}

	private fun initLoadingMessagesFlipper() {
		val items = loadingMessages.shuffled()

		vFlipperLoading.adapter = LoadingAdapter(items)
	}

	private fun showLoading(value: Boolean) {
		if (pBarLogging.isVisible == value) return

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

	private fun signInObserver(result: Event<Unit, SignInError>?) {
		when (result) {
			is Event.OnLoading -> {
				showLoading(true)

				hideSoftKeyboard()

				iViewLogo.performClick()
			}
			is Event.OnSuccess -> {
				navigate(SignInFragmentDirections.navToSplash())
			}
			is Event.OnError -> {
				showLoading(false)

				signInErrorHandler(error = result.error)
			}
			else -> {
				showLoading(false)

				errorSnackBar()
			}
		}
	}

	private fun signInErrorHandler(error: SignInError?) {
		when (error) {
			is SignInError.Timeout -> errorSnackBar(R.string.snack_timeout) { onSignInClick() }
			is SignInError.InvalidCredentials -> errorSnackBar(R.string.snack_invalid_credentials)
			is SignInError.EmptyUsbId -> tInputUsbId.setError(R.string.error_empty)
			is SignInError.InvalidUsbId -> tInputUsbId.setError(R.string.error_usb_id)
			is SignInError.EmptyPassword -> tInputPassword.setError(R.string.error_empty)
			is SignInError.Unavailable -> errorSnackBar(R.string.snack_service_unavailable) { onSignInClick() }
			is SignInError.NoConnection -> connectionSnackBar(error.isNetworkAvailable) { onSignInClick() }
			is SignInError.AccountDisabled -> errorSnackBar(R.string.snack_account_disabled)
			else -> errorSnackBar { onSignInClick() }
		}
	}
}