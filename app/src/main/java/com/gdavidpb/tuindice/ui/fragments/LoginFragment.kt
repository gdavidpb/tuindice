package com.gdavidpb.tuindice.ui.fragments

import android.app.ActivityManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.utils.`do`
import com.gdavidpb.tuindice.data.utils.`when`
import com.gdavidpb.tuindice.data.utils.firstInvalid
import com.gdavidpb.tuindice.domain.model.SignInResponse
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.presentation.viewmodel.LoginViewModel
import com.gdavidpb.tuindice.ui.adapters.LoadingAdapter
import com.gdavidpb.tuindice.utils.KEY_LOADING_MESSAGES
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : NavigationFragment() {

    private val viewModel by viewModel<LoginViewModel>()

    private val activityManager by inject<ActivityManager>()
    private val connectivityManager by inject<ConnectivityManager>()

    private val loadingMessages by config<List<String>>(KEY_LOADING_MESSAGES)

    private val validations by lazy {
        arrayOf(
                `when`(tInputUsbId) { text().isBlank() } `do` { errorResource = R.string.error_empty },
                `when`(tInputUsbId) { !text().isUsbId() } `do` { errorResource = R.string.error_usb_id },
                `when`(tInputPassword) { text().isBlank() } `do` { errorResource = R.string.error_empty },
                `when`(connectivityManager) { !isNetworkAvailable() } `do` { signInErrorHandler(SignInError.NoConnection) }
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

        eTextUsbId.onTextChanged { _, _, _, _ -> if (tInputUsbId.error != null) tInputUsbId.error = null }
        eTextPassword.onTextChanged { _, _, _, _ -> if (tInputPassword.error != null) tInputPassword.error = null }

        eTextUsbId.onTextChanged { s, _, before, _ ->
            if (s.length >= 2 && before == 0 && !s.contains("-")) {
                eTextUsbId.keyListener = DigitsKeyListener.getInstance("0123456789-")
                eTextUsbId.text?.insert(2, "-")
            } else
                eTextUsbId.keyListener = DigitsKeyListener.getInstance("0123456789")
        }

        eTextPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSignInClick()
                false
            } else
                true
        }

        arrayOf(tInputUsbId, tInputPassword).forEach { textInputLayout ->
            textInputLayout.editText.notNull { editText ->
                editText.onTextChanged { _, _, _, _ ->
                    textInputLayout.error = null
                }
            }
        }

        val accentColor = ContextCompat.getColor(requireContext(), R.color.color_accent)

        tViewPolicies.apply {
            setSpans {
                listOf(ForegroundColorSpan(accentColor), TypefaceSpan("sans-serif-medium"), UnderlineSpan())
            }

            setLink(getString(R.string.link_terms_and_conditions)) {
                LoginFragmentDirections.navToUrl(
                        title = getString(R.string.label_terms_and_conditions),
                        url = BuildConfig.URL_APP_TERMS_AND_CONDITIONS
                ).let(::navigate)
            }

            setLink(getString(R.string.link_privacy_policy)) {
                LoginFragmentDirections.navToUrl(
                        title = getString(R.string.label_privacy_policy),
                        url = BuildConfig.URL_APP_PRIVACY_POLICY
                ).let(::navigate)
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
            hideSoftKeyboard()

            iViewLogo.performClick()

            viewModel.signIn(
                    usbId = tInputUsbId.text(),
                    password = tInputPassword.text()
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

    private fun disabledFailureDialog() {
        activityManager.clearApplicationUserData()

        alert {
            titleResource = R.string.alert_title_disabled_failure
            messageResource = R.string.alert_message_disabled_failure

            isCancelable = false

            positiveButton(R.string.accept)
        }
    }

    private fun invalidCredentialsSnackBar() {
        snackBar {
            messageResource = R.string.snack_invalid_credentials
        }
    }

    private fun noConnectionSnackBar() {
        snackBar {
            messageResource = if (connectivityManager.isNetworkAvailable())
                R.string.snack_service_unreachable
            else
                R.string.snack_network_unavailable

            action(R.string.retry) { onSignInClick() }
        }
    }

    private fun defaultErrorSnackBar() {
        snackBar {
            messageResource = R.string.snack_bar_error_occurred

            action(R.string.retry) { onSignInClick() }
        }
    }

    private fun signInObserver(result: Result<SignInResponse, SignInError>?) {
        when (result) {
            is Result.OnLoading -> {
                showLoading(true)
            }
            is Result.OnSuccess -> {
                viewModel.trySyncAccount()
            }
            is Result.OnError -> {
                showLoading(false)

                signInErrorHandler(error = result.error)
            }
        }
    }

    private fun syncObserver(result: Result<Boolean, SyncError>?) {
        when (result) {
            is Result.OnSuccess -> {
                LoginFragmentDirections.navToSummary().let(::navigate)
            }
            is Result.OnError -> {
                showLoading(false)
            }
        }
    }

    private fun signInErrorHandler(error: SignInError?) {
        when (error) {
            SignInError.InvalidCredentials -> invalidCredentialsSnackBar()
            SignInError.AccountDisabled -> disabledFailureDialog()
            SignInError.NoConnection -> noConnectionSnackBar()
            else -> defaultErrorSnackBar()
        }
    }
}