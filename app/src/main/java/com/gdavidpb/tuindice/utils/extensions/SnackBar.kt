package com.gdavidpb.tuindice.utils.extensions

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

data class SnackBarBuilder(
        val view: View,
        var length: Int,
        var message: CharSequence = "",
        var actionText: CharSequence = "",
        var action: () -> Unit = {},
        var dismissed: (event: Int) -> Unit = {}
) {
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

fun Activity.snackBar(length: Int = Snackbar.LENGTH_LONG, builder: SnackBarBuilder.() -> Unit) =
        SnackBarBuilder(contentView ?: window.decorView, length).apply(builder)

fun Fragment.snackBar(length: Int = Snackbar.LENGTH_LONG, builder: SnackBarBuilder.() -> Unit) =
        SnackBarBuilder(requireView(), length).apply(builder)
