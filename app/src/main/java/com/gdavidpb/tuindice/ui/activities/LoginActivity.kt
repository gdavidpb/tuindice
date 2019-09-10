package com.gdavidpb.tuindice.ui.activities

import android.animation.ValueAnimator
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintSet
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.exception.AuthException
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.LoginViewModel
import com.gdavidpb.tuindice.utils.*
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.HttpException
import java.io.IOException

class LoginActivity : BaseActivity() {

    private val viewModel: LoginViewModel by viewModel()

    private val connectivityManager: ConnectivityManager by inject()

    private val validations by lazy {
        arrayOf(
                tInputUsbId set R.string.error_empty `when` { text().isBlank() },
                tInputUsbId set R.string.error_usb_id `when` { !text().matches("^\\d{2}-\\d{5}$".toRegex()) },
                tInputPassword set R.string.error_empty `when` { text().isEmpty() }
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
        /* Set up validations */
        validations.setUp()

        /* Set up background animation */
        ValueAnimator.ofFloat(0f, 1f).animate({
            duration = TIME_BACKGROUND_ANIMATION
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
        }, {
            val width = backgroundOne.width
            val translationX = (width * animatedFraction)

            backgroundOne.translationX = translationX
            backgroundTwo.translationX = (translationX - width)
        })

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
                btnLogin.performClick()
                false
            } else
                true
        }

        /* Logo animation */
        iViewLogo.onClick {
            ValueAnimator.ofFloat(0f, 5f).animate({
                duration = 500
                repeatMode = ValueAnimator.REVERSE
                interpolator = CycleInterpolator(3f)
            }, {
                iViewLogo.rotation = animatedValue as Float
            })
        }

        btnPrivacyPolicy.onClickOnce(::onPrivacyPolicyClick)
        btnLogin.onClickOnce(::onLoginClick)
    }

    private fun onPrivacyPolicyClick() {
        browserActivity(title = R.string.label_privacy_policy, url = BuildConfig.URL_APP_PRIVACY_POLICY)
    }

    private fun onLoginClick() {
        validations.firstInvalid {
            requestFocus()

            selectAll()

            lookAtMe()
        }.isNull {
            iViewLogo.performClick()

            viewModel.auth(
                    usbId = tInputUsbId.text(),
                    password = tInputPassword.text()
            )
        }
    }

    private fun enableUI(value: Boolean) {
        arrayOf(
                tInputUsbId,
                tInputPassword,
                vFlipperWelcome,
                btnPrivacyPolicy,
                btnLogin
        ).forEach { it.visibility = if (value) View.VISIBLE else View.GONE }

        tViewLogging.visibility = if (value) View.GONE else View.VISIBLE
        pBarLogging.visibility = if (value) View.GONE else View.VISIBLE

        ConstraintSet().also {
            it.clone(cLayoutMain)

            if (value) {
                it.clear(R.id.iViewLogo, ConstraintSet.BOTTOM)

                it.connect(
                        R.id.iViewLogo, ConstraintSet.TOP,
                        R.id.guidelineTop, ConstraintSet.TOP,
                        0)
            } else {
                it.clear(R.id.iViewLogo, ConstraintSet.TOP)

                it.connect(
                        R.id.iViewLogo, ConstraintSet.BOTTOM,
                        R.id.guidelineCenter, ConstraintSet.BOTTOM,
                        0)
            }

            it.applyTo(cLayoutMain)
        }
    }

    private fun authObserver(result: Result<AuthResponse>?) {
        when (result) {
            is Result.OnLoading -> {
                snackBar.dismiss()

                enableUI(false)
            }
            is Result.OnSuccess -> {
                startActivity<MainActivity>()
                finish()
            }
            is Result.OnError -> {
                enableUI(true)

                when (result.throwable) {
                    is AuthException ->
                        when (result.throwable.code) {
                            AuthResponseCode.INVALID_CREDENTIALS -> R.string.snack_invalid_credentials
                            else -> R.string.snack_service_unreachable
                        }
                    is IOException, is HttpException ->
                        if (connectivityManager.isNetworkAvailable())
                            R.string.snack_service_unreachable
                        else
                            R.string.snack_network_unavailable

                    else -> R.string.snack_internal_failure
                }.also { message ->
                    snackBar
                            .setText(message)
                            /* Add retry onClick */
                            .apply {
                                if (message != R.string.snack_invalid_credentials) {
                                    setAction(R.string.retry) {

                                        viewModel.auth(
                                                usbId = tInputUsbId.text(),
                                                password = tInputPassword.text()
                                        )
                                    }
                                }
                            }
                            .show()
                }
            }
        }
    }
}