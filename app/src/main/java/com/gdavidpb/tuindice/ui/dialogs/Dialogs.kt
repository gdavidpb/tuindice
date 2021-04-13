package com.gdavidpb.tuindice.ui.dialogs

import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.*

fun AppCompatActivity.linkFailureDialog() {
    alert {
        titleResource = R.string.alert_title_link_failure
        messageResource = R.string.alert_message_link_failure

        isCancelable = false

        positiveButton(R.string.exit) {
            finish()
        }
    }
}

fun AppCompatActivity.fatalFailureDialog() {
    alert {
        titleResource = R.string.alert_title_fatal_failure
        messageResource = R.string.alert_message_fatal_failure

        isCancelable = false

        positiveButton(R.string.restart) {
            clearApplicationUserData()

            recreate()
        }

        negativeButton(R.string.exit) {
            finish()
        }
    }
}

fun AppCompatActivity.credentialsChangedDialog() {
    clearApplicationUserData()

    alert {
        titleResource = R.string.alert_title_credentials_failure
        messageResource = R.string.alert_message_credentials_failure

        isCancelable = false

        positiveButton(R.string.accept) {
            recreate()
        }
    }
}


fun AppCompatActivity.disabledFailureDialog() {
    clearApplicationUserData()

    alert {
        titleResource = R.string.alert_title_disabled_failure
        messageResource = R.string.alert_message_disabled_failure

        isCancelable = false

        positiveButton(R.string.exit) {
            finish()
        }
    }
}

fun AppCompatActivity.disabledAccountDialog() {
    clearApplicationUserData()

    alert {
        titleResource = R.string.alert_title_disabled_failure
        messageResource = R.string.alert_message_disabled_failure

        isCancelable = false

        positiveButton(R.string.accept) {
            finish()
        }
    }
}