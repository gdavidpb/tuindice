package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.utils.Validation
import com.gdavidpb.tuindice.data.utils.`do`
import com.gdavidpb.tuindice.data.utils.`when`
import com.gdavidpb.tuindice.data.utils.firstInvalid
import com.gdavidpb.tuindice.domain.usecase.coroutines.Event
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.ui.adapters.LoadingAdapter
import com.gdavidpb.tuindice.ui.dialogs.disabledAccountDialog
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInFragment : NavigationFragment() {

    private val viewModel by viewModel<SignInViewModel>()

    private val loadingMessages by config<List<String>>(ConfigKeys.LOADING_MESSAGES)

    private val validations by lazy {
        arrayOf<Validation<*>>(
                `when`(tInputUsbId) { isBlank() } `do` { setError(R.string.error_empty) },
                `when`(tInputUsbId) { !isValid() } `do` { setError(R.string.error_usb_id) },
                `when`(tInputPassword) { isBlank() } `do` { setError(R.string.error_empty) }
        )
    }

    override fun onCreateView() = R.layout.fragment_sign_in

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onViewCreated()

        with(viewModel) {
            observe(signIn, ::signInObserver)
        }
    }

    private fun onViewCreated() {
        (backgroundOne to backgroundTwo).animateInfiniteLoop()

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
            requireAppCompatActivity().hideSoftKeyboard()

            iViewLogo.performClick()

            viewModel.signIn(
                    usbId = tInputUsbId.getUsbId(),
                    password = tInputPassword.getPassword()
            )
        }
    }

    private fun showLoading(value: Boolean) {
        val layout = if (value) {
            vFlipperLoading.isVisible = true

            if (vFlipperLoading.adapter == null) {
                val items = loadingMessages.shuffled()

                vFlipperLoading.adapter = LoadingAdapter(items)
            }

            vFlipperLoading.startFlipping()

            R.layout.fragment_sign_in_loading
        } else {
            vFlipperLoading.isVisible = false
            vFlipperLoading.stopFlipping()

            R.layout.fragment_sign_in
        }

        cLayoutSignIn.beginTransition(targetLayout = layout) {
            interpolator = OvershootInterpolator()
            duration = 1000
        }
    }

    private fun invalidCredentialsSnackBar() {
        snackBar {
            messageResource = R.string.snack_invalid_credentials
        }
    }

    private fun timeoutSnackBar() {
        snackBar {
            messageResource = R.string.snack_service_timeout

            action(R.string.retry) { onSignInClick() }
        }
    }

    private fun signInObserver(result: Event<Unit, SignInError>?) {
        when (result) {
            is Event.OnLoading -> {
                showLoading(true)
            }
            is Event.OnSuccess -> {
                navigate(SignInFragmentDirections.navToSplash())
            }
            is Event.OnTimeout -> {
                showLoading(false)

                timeoutSnackBar()
            }
            is Event.OnError -> {
                showLoading(false)

                signInErrorHandler(error = result.error)
            }
        }
    }

    private fun signInErrorHandler(error: SignInError?) {
        when (error) {
            is SignInError.InvalidCredentials -> invalidCredentialsSnackBar()
            is SignInError.AccountDisabled -> requireAppCompatActivity().disabledAccountDialog()
            is SignInError.NoConnection -> noConnectionSnackBar(error.isNetworkAvailable) { onSignInClick() }
            else -> defaultErrorSnackBar { onSignInClick() }
        }
    }
}