package com.gdavidpb.tuindice.activities

import android.animation.ValueAnimator
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.view.View

import com.gdavidpb.tuindice.abstracts.Initializer

import kotlinx.android.synthetic.main.activity_login.*
import java.util.*
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.widget.TextView
import com.gdavidpb.tuindice.*
import com.gdavidpb.tuindice.models.DstService
import android.support.constraint.ConstraintSet
import android.support.v7.app.AppCompatDelegate
import android.support.v7.content.res.AppCompatResources
import com.gdavidpb.tuindice.models.DstResultReceiver
import com.gdavidpb.tuindice.models.SQLiteHelper

class LoginActivity : AppCompatActivity(), Initializer, DstResultReceiver.Receiver {

    private val animationMessages by lazy { resources.getStringArray(R.array.messageWelcome) }

    private var animationTimer = Timer()

    private var animationFrame: Int = 0

    private var snackBarError: Snackbar? = null
    private var serviceConnection: ServiceConnection? = null

    private lateinit var receiver: DstResultReceiver

    /* Overrides */
    @Suppress("unchecked_cast")
    override fun onInitialize(view: View?) {
        /* Set up temporary data */
        val temporaryAccount = getDatabase().getTemporaryAccount()

        if (temporaryAccount.usbId.isNotEmpty()) {
            eTextUsbId.setText(temporaryAccount.usbId)
            eTextUsbId.setSelection(eTextUsbId.length())
        }

        if (temporaryAccount.password.isNotEmpty()) {
            eTextPassword.setText(temporaryAccount.password)
            eTextPassword.setSelection(eTextPassword.length())
        }

        /* Set up drawables */
        val drawableLauncher = AppCompatResources.getDrawable(this, R.drawable.ic_launcher)

        iViewLogo.setImageDrawable(drawableLauncher)

        eTextUsbId.setCompoundDrawables(
                getTintVector(R.drawable.ic_account, R.color.colorSecondaryText),
                null, null, null
        )

        eTextPassword.setCompoundDrawables(
                getTintVector(R.drawable.ic_password, R.color.colorSecondaryText),
                null, null, null
        )

        /* Set up welcome message animation */
        tSwitcherWelcome.setFactory {
            TextView.inflate(applicationContext, R.layout.view_switcher, null)
        }

        enableAnimation(true)

        /* Set up background animation */
        ValueAnimator.ofFloat(0f, 1f).animate({
            duration = 30000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
        }, {
            val width = backgroundOne.width
            val translationX = (width * animatedFraction)

            backgroundOne.translationX = translationX
            backgroundTwo.translationX = (translationX - width)
        })

        /* Set up events handlers */
        cBoxTerms.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                alertDialog {
                    setCancelable(false)
                    setTitle(R.string.alertTitleTerms)
                    setMessage(R.string.alertMessageTerms)
                    setPositiveButton(R.string.accept, null)
                    setNegativeButton(R.string.decline, { _, _ -> cBoxTerms.isChecked = false })
                }
        }

        bLogin.setOnClickListener { onLoginClick() }

        eTextUsbId.setOnEditorActionListener { _, actionId, _ ->

            when (actionId) {
                EditorInfo.IME_ACTION_NEXT -> isInvalid(tInputUsbId)
                else -> true
            }
        }

        eTextPassword.setOnEditorActionListener { _, actionId, _ ->

            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> isInvalid(tInputPassword)
                else -> true
            }
        }

        eTextUsbId.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (tInputUsbId.error != null)
                    tInputUsbId.error = null

                if (s.isNotEmpty()) {
                    val usbId = eTextUsbId.text.toString()
                    val password = eTextPassword.text.toString()

                    SQLiteHelper
                            .getInstance(applicationContext)
                            .setTemporaryAccount(DstAccount(usbId, password))
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })

        eTextPassword.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (tInputPassword.error != null)
                    tInputPassword.error = null

                if (s.isNotEmpty()) {
                    val usbId = eTextUsbId.text.toString()
                    val password = eTextPassword.text.toString()

                    SQLiteHelper
                            .getInstance(applicationContext)
                            .setTemporaryAccount(DstAccount(usbId, password))
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })

        eTextUsbId.setOnFocusChangeListener { _, hasFocus ->

            eTextUsbId.setCompoundDrawables(
                    getTintVector(R.drawable.ic_account,
                            if (hasFocus)
                                R.color.colorAccent
                            else
                                R.color.colorSecondaryText), null, null, null
            )
        }

        eTextPassword.setOnFocusChangeListener { _, hasFocus ->

            eTextPassword.setCompoundDrawables(
                    getTintVector(R.drawable.ic_password,
                            if (hasFocus)
                                R.color.colorAccent
                            else
                                R.color.colorSecondaryText), null, null, null
            )
        }

        eTextUsbId.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length >= 2 && before == 0 && !s.contains("-")) {
                    eTextUsbId.keyListener = DigitsKeyListener.getInstance("0123456789-")
                    eTextUsbId.text.insert(2, "-")
                } else
                    eTextUsbId.keyListener = DigitsKeyListener.getInstance("0123456789")
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })

        lLayoutLogin.visibility = View.VISIBLE
        bLogin.visibility = View.VISIBLE

        /* Set up error message from notifications */
        if (intent.hasExtra(Constants.EXTRA_RESPONSE)) {
            val response = intent.getSerializableExtra(Constants.EXTRA_RESPONSE) as DstResponse<DstAccount>

            updateUserInterface(response)
        } else {
            /* Try to communicate with the login service */
            /* This lambda is called just in case successful connection */
            serviceConnection = DstService.bindService(this, {
                setReceiver(receiver)

                val operation = getOperation()

                /* If service is executing an operation */
                if (operation != null) {
                    tSwitcherWelcome.setText(operation.toString(applicationContext))

                    cancelAllNotification()

                    updateUserInterface()
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Set Spanish-Venezuela as default locale */
        Locale.setDefault(Locale("es", "VE"))

        /* Set up vector compatibility */
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        /* Set up activity */
        setContentView(R.layout.activity_login)

        /* Set up receiver */
        receiver = DstResultReceiver(Handler(), this)

        onInitialize()
    }

    override fun onDestroy() {
        DstService.unbindService(this, serviceConnection)

        super.onDestroy()
    }

    @Suppress("unchecked_cast")
    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        when (resultCode) {
            Constants.RES_UPDATE -> {
                val message = resultData.getString(Constants.EXTRA_UPDATE)

                tSwitcherWelcome.setText(message)
            }
            Constants.RES_RESPONSE -> {
                val response = resultData.getSerializable(Constants.EXTRA_RESPONSE) as DstResponse<DstAccount>

                updateUserInterface(response)
            }
        }
    }

    private fun enableAnimation(value: Boolean) {
        tSwitcherWelcome.clearAnimation()

        if (value) {
            animationFrame = 0

            tSwitcherWelcome.setText(animationMessages[animationFrame])

            animationTimer.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            runOnUiThread({
                                if (tSwitcherWelcome != null) {
                                    tSwitcherWelcome.setText(animationMessages[animationFrame])
                                    animationFrame = ++animationFrame % animationMessages.size
                                } else
                                    animationTimer.cancel()
                            })
                        }
                    }, 0, 2000)
        } else {
            animationTimer.cancel()

            animationTimer.purge()

            animationTimer = Timer()

            tSwitcherWelcome.setText(String())
        }
    }

    private fun isInvalid(view: View): Boolean {
        when (view) {
            cBoxTerms -> {
                return if (!cBoxTerms.isChecked) {
                    cBoxTerms.lookAtMe()
                    true
                } else
                    false
            }
            tInputUsbId -> {
                val usbId: String? = tInputUsbId.editText?.text.toString()

                when {
                    usbId.isNullOrEmpty() -> {
                        tInputUsbId.lookAtMe()

                        tInputUsbId.error = getString(R.string.errorEmpty)

                        return true
                    }
                    usbId?.matches("^\\d{2}-\\d{5}$".toRegex()) != true -> {
                        tInputUsbId.lookAtMe()

                        tInputUsbId.error = getString(R.string.errorUSBID)

                        return true
                    }
                    else -> {
                        tInputUsbId.error = null

                        return false
                    }
                }
            }
            tInputPassword -> {
                val password: String? = tInputPassword.editText?.text.toString()

                return if (password.isNullOrEmpty()) {
                    tInputPassword.lookAtMe()

                    tInputPassword.error = getString(R.string.errorEmpty)

                    true
                } else {
                    tInputPassword.error = null

                    false
                }
            }

            else -> return true
        }
    }

    private fun updateUserInterface(response: DstResponse<DstAccount>? = null) {
        when {
            response == null -> {
                bLogin.visibility = View.GONE
                cBoxTerms.visibility = View.GONE
                tInputUsbId.visibility = View.GONE
                tInputPassword.visibility = View.GONE

                pBarLogging.visibility = View.VISIBLE

                snackBarError?.dismiss()

                enableAnimation(false)

                val constraintSet = ConstraintSet()

                constraintSet.clone(cLayoutMain)

                constraintSet.connect(
                        R.id.lLayoutLogin, ConstraintSet.BOTTOM,
                        ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
                        0)

                constraintSet.applyTo(cLayoutMain)
            }
            response.exception != null -> {
                bLogin.visibility = View.GONE
                cBoxTerms.visibility = View.VISIBLE
                tInputUsbId.visibility = View.VISIBLE
                tInputPassword.visibility = View.VISIBLE

                pBarLogging.visibility = View.GONE

                enableAnimation(true)

                val constraintSet = ConstraintSet()

                constraintSet.clone(cLayoutMain)

                constraintSet.clear(R.id.lLayoutLogin, ConstraintSet.BOTTOM)

                constraintSet.applyTo(cLayoutMain)

                lLayoutLogin.lookAtMe()

                snackBarError = cLayoutMain.snackBar(
                        response.exception!!.toDescription(),
                        Snackbar.LENGTH_INDEFINITE)
                { setAction(R.string.retry) { onLoginClick() } }
            }
            response.result != null -> {
                val account = response.result

                getDatabase().addAccount(account, true)

                launchActivity<MainActivity>()

                finish()
            }
        }
    }

    private fun onLoginClick() {
        if (isInvalid(tInputUsbId) or
                isInvalid(tInputPassword) or
                isInvalid(cBoxTerms)) {
            if (snackBarError?.isShown == true)
                bLogin.visibility = View.VISIBLE

            return
        }

        /* Data collecting */
        val usbId = eTextUsbId.text.toString()
        val password = eTextPassword.text.toString()

        /* Start login service */
        DstService.startService(usbId, password, applicationContext, receiver)

        updateUserInterface()
    }
}