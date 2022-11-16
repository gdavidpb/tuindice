package com.gdavidpb.tuindice.login.ui.fragments

import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.domain.usecase.base.Event
import com.gdavidpb.tuindice.base.domain.usecase.error.SyncError
import com.gdavidpb.tuindice.base.ui.fragments.NavigationFragment
import com.gdavidpb.tuindice.base.utils.*
import com.gdavidpb.tuindice.base.domain.usecase.base.Result
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.ui.adapters.LoadingAdapter
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInFragment : NavigationFragment() {

	private val viewModel by viewModel<SignInViewModel>()

	private val loadingMessages by config<List<String>>(ConfigKeys.LOADING_MESSAGES)

	private val validations by lazy {
		arrayOf<Validation<*>>(
			`when`(tInputUsbId) { isEmpty() } `do` { setError(R.string.error_empty) },
			`when`(tInputUsbId) { !isValid() } `do` { setError(R.string.error_usb_id) },
			`when`(tInputPassword) { isEmpty() } `do` { setError(R.string.error_empty) }
		)
	}

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
			observe(sync, ::syncObserver)
		}
	}

	private fun onLogoClick() {
		iViewLogo.animateShake()
	}

	private fun onSignInClick() {
		validations.firstInvalid {
			when (this) {
				is View -> {
					requestFocus()

					animateLookAtMe()
				}
			}
		}.isNull {
			hideSoftKeyboard()

			iViewLogo.performClick()

			viewModel.signIn(
				usbId = tInputUsbId.getUsbId(),
				password = tInputPassword.getPassword()
			)
		}
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

	private fun signInObserver(result: Event<Boolean, SignInError>?) {
		when (result) {
			is Event.OnLoading -> {
				showLoading(true)
			}
			is Event.OnSuccess -> {
				val hasCache = result.value

				if (hasCache)
					navigate(SignInFragmentDirections.navToSplash())
				else
					viewModel.sync()
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

	private fun syncObserver(result: Result<Boolean, SyncError>?) {
		when (result) {
			is Result.OnLoading -> {
				showLoading(true)
			}
			is Result.OnSuccess -> {
				navigate(SignInFragmentDirections.navToSplash())
			}
			is Result.OnError -> {
				showLoading(false)

				viewModel.signOut()

				syncErrorHandler(error = result.error)
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
			is SignInError.Unavailable -> errorSnackBar(R.string.snack_service_unavailable) { onSignInClick() }
			is SignInError.NoConnection -> connectionSnackBar(error.isNetworkAvailable) { onSignInClick() }
			is SignInError.AccountDisabled -> errorSnackBar(R.string.snack_account_disabled)
			else -> errorSnackBar { onSignInClick() }
		}
	}

	private fun syncErrorHandler(error: SyncError?) {
		when (error) {
			is SyncError.Timeout -> errorSnackBar(R.string.snack_timeout) { onSignInClick() }
			is SyncError.NoConnection -> connectionSnackBar(error.isNetworkAvailable) { onSignInClick() }
			else -> errorSnackBar { onSignInClick() }
		}
	}
}