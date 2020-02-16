package com.gdavidpb.tuindice.utils.extensions

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

data class SnackBarBuilder(
        val view: View,
        var length: Int = Snackbar.LENGTH_SHORT,
        var message: CharSequence = "",
        var actionText: CharSequence = "",
        var action: () -> Unit = {},
        var dismissed: (event: Int) -> Unit = {}
) {
    fun length(value: Int) {
        length = value
    }

    fun message(value: CharSequence) {
        message = value
    }

    fun action(text: CharSequence, onClick: () -> Unit) {
        actionText = text
        action = onClick
    }

    fun onDismissed(listener: (event: Int) -> Unit) {
        dismissed = listener
    }

    fun build() = Snackbar
            .make(view, message, length)
            .setAction(actionText) {
                action()
            }.apply {
                object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                        removeCallback(this)

                        dismissed(event)
                    }
                }.also { callback ->
                    addCallback(callback)
                }
            }
}

fun Fragment.snackBar(builder: SnackBarBuilder.() -> Unit) = SnackBarBuilder(requireView()).apply(builder)