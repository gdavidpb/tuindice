package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.animation.OvershootInterpolator
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.utils.Validation
import com.gdavidpb.tuindice.data.utils.`do`
import com.gdavidpb.tuindice.data.utils.`when`
import com.gdavidpb.tuindice.data.utils.firstInvalid
import com.gdavidpb.tuindice.domain.model.SignInResponse
import com.gdavidpb.tuindice.domain.usecase.coroutines.Event
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.presentation.viewmodel.LoginViewModel
import com.gdavidpb.tuindice.ui.adapters.LoadingAdapter
import com.gdavidpb.tuindice.ui.dialogs.disabledAccountDialog
import com.gdavidpb.tuindice.utils.KEY_LOADING_MESSAGES
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : NavigationFragment() {

    private val viewModel by viewModel<LoginViewModel>()

    private val loadingMessages by config<List<String>>(KEY_LOADING_MESSAGES)

    private val validations by lazy {
        arrayOf<Validation<*>>(
                `when`(tInputUsbId) { isBlank() } `do` { setError(R.string.error_empty) },
                `when`(tInputUsbId) { !isValid() } `do` { setError(R.string.error_usb_id) },
                `when`(tInputPassword) { isBlank() } `do` { setError(R.string.error_empty) }
        )
    }

    override fun onCreateView() = R.layout.fragment_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onViewCreated()

        with(viewModel) {
            observe(signIn, ::signInObserver)
            observe(sync, ::syncObserver)
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
                navigate(LoginFragmentDirections.navToUrl(
                        title = getString(R.string.label_terms_and_conditions),
                        url = BuildConfig.URL_APP_TERMS_AND_CONDITIONS
                ))
            }

            setLink(getString(R.string.link_privacy_policy)) {
                navigate(LoginFragmentDirections.navToUrl(
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
                is TextInputLayout -> {
                    requestFocus()

                    selectAll()

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
            vFlipperLoading.visible()

            if (vFlipperLoading.adapter == null) {
                val items = loadingMessages.shuffled()

                vFlipperLoading.adapter = LoadingAdapter(items)
            }

            vFlipperLoading.startFlipping()

            R.layout.fragment_login_loading
        } else {
            vFlipperLoading.gone()
            vFlipperLoading.stopFlipping()

            R.layout.fragment_login
        }

        cLayoutLogin.beginTransition(targetLayout = layout) {
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

    private fun signInObserver(result: Event<SignInResponse, SignInError>?) {
        when (result) {
            is Event.OnLoading -> {
                showLoading(true)
            }
            is Event.OnSuccess -> {
                viewModel.trySyncAccount()
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

    private fun syncObserver(result: Result<Boolean, SyncError>?) {
        when (result) {
            is Result.OnSuccess -> {
                navigate(LoginFragmentDirections.navToSplash())
            }
            is Result.OnError -> {
                showLoading(false)
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