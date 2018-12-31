package com.gdavidpb.tuindice.ui.activities

import android.Manifest
import android.animation.ValueAnimator
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintSet
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.model.exception.AuthException
import com.gdavidpb.tuindice.data.utils.*
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.presentation.viewmodel.LoginActivityViewModel
import io.reactivex.observers.DisposableSingleObserver
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

class LoginActivity : BaseActivity(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
) {
    private val viewModel: LoginActivityViewModel by viewModel()

    private val connectivityManager: ConnectivityManager by inject()

    private val validations by lazy {
        arrayOf(
                tInputUsbId set R.string.errorEmpty `when` { text().isBlank() },
                tInputUsbId set R.string.errorUSBID `when` { !text().matches("^\\d{2}-\\d{5}$".toRegex()) },
                tInputPassword set R.string.errorEmpty `when` { text().isEmpty() }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        onViewCreated()
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

        /* Set up drawables using support */
        eTextUsbId.drawables(left = getCompatDrawable(R.drawable.ic_account, R.color.colorSecondaryText))
        eTextPassword.drawables(left = getCompatDrawable(R.drawable.ic_password, R.color.colorSecondaryText))

        eTextUsbId.onTextChanged { _, _, _, _ -> if (tInputUsbId.error != null) tInputUsbId.error = null }
        eTextPassword.onTextChanged { _, _, _, _ -> if (tInputPassword.error != null) tInputPassword.error = null }

        val onFocusChanged = fun(editText: EditText, @DrawableRes drawableRes: Int) = View.OnFocusChangeListener { _, hasFocus ->
            val color = if (hasFocus) R.color.colorAccent else R.color.colorSecondaryText

            editText.drawables(left = getCompatDrawable(drawableRes, color))
        }

        eTextUsbId.onFocusChangeListener = onFocusChanged(eTextUsbId, R.drawable.ic_account)
        eTextPassword.onFocusChangeListener = onFocusChanged(eTextPassword, R.drawable.ic_password)

        eTextUsbId.onTextChanged { s, _, before, _ ->
            if (s.length >= 2 && before == 0 && !s.contains("-")) {
                eTextUsbId.keyListener = DigitsKeyListener.getInstance("0123456789-")
                eTextUsbId.text?.insert(2, "-")
            } else
                eTextUsbId.keyListener = DigitsKeyListener.getInstance("0123456789")
        }

        eTextPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                bLogin.performClick()
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

        tViewPrivacyPolicy.onClickOnce {
            browse(url = URL_PRIVACY_POLICY)
        }

        bLogin.onClickOnce {
            validations.firstInvalid {
                requestFocus()

                selectAll()

                lookAtMe()
            }.isNull {
                iViewLogo.performClick()

                val request = AuthRequest(
                        usbId = tInputUsbId.text(),
                        password = tInputPassword.text()
                )

                viewModel.auth(request, AuthObserver())
            }
        }
    }

    private fun enableUI(value: Boolean) {
        arrayOf(
                tInputUsbId,
                tInputPassword,
                vFlipperWelcome,
                tViewPrivacyPolicy,
                bLogin
        ).forEach { it.visibility = if (value) View.VISIBLE else View.GONE }

        tViewLogging.visibility = if (value) View.GONE else View.VISIBLE
        pBarLogging.visibility = if (value) View.GONE else View.VISIBLE

        ConstraintSet().also {
            it.clone(cLayoutMain)

            if (value)
                it.clear(R.id.lLayoutLogin, ConstraintSet.BOTTOM)
            else
                it.connect(
                        R.id.lLayoutLogin, ConstraintSet.BOTTOM,
                        ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
                        0)

            it.applyTo(cLayoutMain)
        }
    }

    inner class AuthObserver : DisposableSingleObserver<AuthResponse>() {
        init {
            snackBar.dismiss()

            enableUI(false)
        }

        override fun onSuccess(t: AuthResponse) {
            startActivity<MainActivity>()
            finish()
        }

        override fun onError(e: Throwable) {
            enableUI(true)

            when (e) {
                is AuthException ->
                    when (e.code) {
                        AuthResponseCode.INVALID_CREDENTIALS -> R.string.snackInvalidCredentials
                        else -> R.string.snackServiceUnreachable
                    }
                is IOException ->
                    if (connectivityManager.isNetworkAvailable())
                        R.string.snackServiceUnreachable
                    else
                        R.string.snackNetworkUnavailable

                else -> R.string.snackInternalFailure
            }.also { message ->
                snackBar
                        .setText(message)
                        /* Add retry action */
                        .apply {
                            if (message != R.string.snackInvalidCredentials) {
                                setAction(R.string.retry) {
                                    val request = AuthRequest(
                                            usbId = tInputUsbId.text(),
                                            password = tInputPassword.text()
                                    )

                                    viewModel.auth(request, AuthObserver())
                                }
                            }
                        }
                        .show()
            }
        }
    }
}