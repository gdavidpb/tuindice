package com.gdavidpb.tuindice.utils.extensions

import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.R

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
            // TODO clearApplicationUserData()

            recreate()
        }

        negativeButton(R.string.exit) {
            finish()
        }
    }
}