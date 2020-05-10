package com.gdavidpb.tuindice.ui.activities

import android.app.ActivityManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.exception.FatalException
import com.gdavidpb.tuindice.domain.model.exception.NoAuthenticatedException
import com.gdavidpb.tuindice.domain.model.exception.NoDataException
import com.gdavidpb.tuindice.domain.model.exception.SynchronizationException
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.firebase.auth.FirebaseAuthActionCodeException
import org.koin.android.ext.android.inject

abstract class NavigationActivity : AppCompatActivity() {
    private val activityManager by inject<ActivityManager>()
    private val inputMethodManager by inject<InputMethodManager>()

    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()

        return true
    }

    open fun handleException(throwable: Throwable): Boolean {
        return when {
            throwable.cause is FirebaseAuthActionCodeException -> {
                linkFailureDialog()
                true
            }
            throwable is NoAuthenticatedException -> {
                fatalFailureRestart()
                true
            }
            throwable is NoDataException -> {
                dataFailureDialog()
                true
            }
            throwable is SynchronizationException -> {
                syncFailureDialog()
                true
            }
            throwable is FatalException -> {
                fatalFailureDialog()
                true
            }
            else -> {
                false
            }
        }
    }

    protected fun hideSoftKeyboard() {
        inputMethodManager.hideSoftKeyboard(this)
    }

    protected fun clearApplicationUserData() {
        activityManager.clearApplicationUserData()
    }

    private fun fatalFailureRestart() {
        activityManager.clearApplicationUserData()

        recreate()
    }

    private fun linkFailureDialog() {
        alert {
            titleResource = R.string.alert_title_link_failure
            messageResource = R.string.alert_message_link_failure

            isCancelable = false

            positiveButton(R.string.exit) {
                finish()
            }
        }
    }

    private fun fatalFailureDialog() {
        alert {
            titleResource = R.string.alert_title_fatal_failure
            messageResource = R.string.alert_message_fatal_failure

            isCancelable = false

            positiveButton(R.string.restart) {
                fatalFailureRestart()
            }

            negativeButton(R.string.exit) {
                finish()
            }
        }
    }

    private fun syncFailureDialog() {
        alert {
            titleResource = R.string.alert_title_sync_failure
            messageResource = R.string.alert_message_sync_failure

            isCancelable = false

            positiveButton(R.string.open_settings) {
                openDataTime()
            }

            negativeButton(R.string.exit) {
                finish()
            }
        }
    }

    private fun dataFailureDialog() {
        alert {
            titleResource = R.string.alert_title_data_failure
            messageResource = R.string.alert_message_data_failure

            isCancelable = false

            positiveButton(R.string.restart) {
                fatalFailureRestart()
            }

            negativeButton(R.string.exit) {
                finish()
            }
        }
    }
}