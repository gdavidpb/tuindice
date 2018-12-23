package com.gdavidpb.tuindice.ui.activities

import android.animation.ValueAnimator
import android.net.Uri
import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.DrawableRes
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.utils.*
import com.gdavidpb.tuindice.presentation.viewmodel.LoginActivityViewModel
import io.reactivex.observers.DisposableSingleObserver
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : BaseActivity() {

    private val viewModel: LoginActivityViewModel by viewModel()

    private val resolveResourceObserver = ResolveResourceObserver()

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
            viewModel.resolveResource(URL_PRIVACY_POLICY, resolveResourceObserver)
        }

        bLogin.onClickOnce {
            validations.firstInvalid {
                requestFocus()

                selectAll()

                lookAtMe()
            }.isNull {
                iViewLogo.performClick()

                //todo login
            }
        }
    }

    inner class ResolveResourceObserver : DisposableSingleObserver<Uri>() {
        override fun onSuccess(t: Uri) {
            browse(url = "$t")
        }

        override fun onError(e: Throwable) {
            //TODO ERROR
        }
    }
}