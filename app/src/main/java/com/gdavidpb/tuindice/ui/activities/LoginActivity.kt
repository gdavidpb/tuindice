package com.gdavidpb.tuindice.ui.activities

import android.net.ConnectivityManager
import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.utils.`do`
import com.gdavidpb.tuindice.data.utils.`when`
import com.gdavidpb.tuindice.data.utils.firstInvalid
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.LoginViewModel
import com.gdavidpb.tuindice.utils.EXTRA_FIRST_START_UP
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_login.*
import okio.IOException
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    private val viewModel by viewModel<LoginViewModel>()

    private val connectivityManager by inject<ConnectivityManager>()

    private val validations by lazy {
        arrayOf(
                `when`(tInputUsbId) { text().isBlank() } `do` { errorResource = R.string.error_empty },
                `when`(tInputUsbId) { !text().isUsbId() } `do` { errorResource = R.string.error_usb_id },
                `when`(tInputPassword) { text().isBlank() } `do` { errorResource = R.string.error_empty },
                `when`(connectivityManager) { !isNetworkAvailable() } `do` { handleAuthException(IOException()) }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        onViewCreated()

        with(viewModel) {
            observe(auth, ::authObserver)
        }
    }

    private fun onViewCreated() {
        /* Set up background animation */
        (backgroundOne to backgroundTwo).animateInfiniteLoop()

        tViewAppVersion.text = versionName()

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
                btnSignIn.performClick()
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

        iViewLogo.onClickOnce(::onLogoClick)
        btnPrivacyPolicy.onClickOnce(::onPrivacyPolicyClick)
        btnSignIn.onClickOnce(::onSignInClick)
    }

    private fun onLogoClick() {
        iViewLogo.animateShake()
    }

    private fun onPrivacyPolicyClick() {
        browserActivity(title = R.string.label_privacy_policy, url = BuildConfig.URL_APP_PRIVACY_POLICY)
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
            iViewLogo.performClick()

            viewModel.auth(
                    usbId = tInputUsbId.text(),
                    password = tInputPassword.text()
            )
        }
    }

    private fun enableUI(value: Boolean) {
        if (value) {
            groupLoginProgress.gone()
            groupLogin.visible()
        } else {
            groupLogin.gone()
            groupLoginProgress.visible()
        }

        ConstraintSet().apply {
            clone(cLayoutMain)

            if (value) {
                connectBottomBottom(R.id.iViewLogo, R.id.guidelineBottomLogo)
                connectTopTop(R.id.iViewLogo, R.id.guidelineTopLogo)
            } else {
                connectBottomBottom(R.id.iViewLogo, R.id.guidelineBottomCenter)
                connectTopTop(R.id.iViewLogo, R.id.guidelineTopCenter)
            }

            applyTo(cLayoutMain)
        }
    }

    private fun authObserver(result: Result<AuthResponse>?) {
        when (result) {
            is Result.OnLoading -> {
                enableUI(false)
            }
            is Result.OnSuccess -> {
                startActivity<MainActivity>(
                        EXTRA_FIRST_START_UP to true
                )
                finish()
            }
            is Result.OnError -> {
                enableUI(true)

                handleAuthException(throwable = result.throwable)
            }
        }
    }

    private fun handleAuthException(throwable: Throwable) {
        val retryAction: (() -> Unit)? = if (!throwable.isInvalidCredentials()) {
            { onSignInClick() }
        } else
            null

        showSnackBarException(throwable, retryAction)
    }
}