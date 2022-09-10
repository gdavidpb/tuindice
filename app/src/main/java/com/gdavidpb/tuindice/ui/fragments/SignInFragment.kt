package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Event
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.ui.adapters.LoadingAdapter
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInFragment : NavigationFragment() {

    private val viewModel by viewModel<SignInViewModel>()

    private val loadingMessages by config<List<String>>(ConfigKeys.LOADING_MESSAGES)

    private val inputMethodManager by inject<InputMethodManager>()

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

        tInputPassword.setAction {
            onSignInClick()
        }

        val accentColor = requireContext().getCompatColor(R.color.color_accent)

        tViewPolicies.apply {
            setSpans {
                listOf(ForegroundColorSpan(accentColor), TypefaceSpan("sans-serif-medium"), UnderlineSpan())
            }

            setLink(getString(R.string.link_terms_and_conditions)) {
                navigate(SignInFragmentDirections.navToUrl(
                        title = getString(R.string.label_terms_and_conditions),
                        url = BuildConfig.URL_APP_TERMS_AND_CONDITIONS
                ))
            }

            setLink(getString(R.string.link_privacy_policy)) {
                navigate(SignInFragmentDirections.navToUrl(
                        title = getString(R.string.label_privacy_policy),
                        url = BuildConfig.URL_APP_PRIVACY_POLICY
                ))
            }
        }.build()

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
            hideSoftKeyboard(inputMethodManager)

            iViewLogo.performClick()

            viewModel.signIn(
                    usbId = tInputUsbId.getUsbId(),
                    password = tInputPassword.getPassword()
            )
        }
    }

    private fun showLoading(value: Boolean) {
        if (pBarLogging.isVisible == value) return

        val layout = if (value) {
            pBarLogging.isVisible = true
            vFlipperLoading.isVisible = true

            if (vFlipperLoading.adapter == null) {
                val items = loadingMessages.shuffled()

                vFlipperLoading.adapter = LoadingAdapter(items)
            }

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

    private fun navToSplash() {
        navigate(SignInFragmentDirections.navToSplash())
    }

    private fun signInObserver(result: Event<Boolean, SignInError>?) {
        when (result) {
            is Event.OnLoading -> {
                showLoading(true)
            }
            is Event.OnSuccess -> {
                val hasCache = result.value

                if (hasCache)
                    navToSplash()
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
                navToSplash()
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