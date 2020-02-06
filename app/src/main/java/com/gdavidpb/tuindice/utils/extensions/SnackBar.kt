package com.gdavidpb.tuindice.utils.extensions

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

data class SnackBarBuilder(
        val view: View,
        var length: Int = Snackbar.LENGTH_SHORT,
        var message: CharSequence = "",
        var actionText: CharSequence = "",
        var action: () -> Unit = {},
        var dismissed: () -> Unit = {},
        var internalView: View? = null,
        var internalAttach: View.OnAttachStateChangeListener? = null
)

fun Fragment.snackBar(builder: SnackBarBuilder.() -> Unit) = SnackBarBuilder(requireView()).apply(builder)

fun SnackBarBuilder.length(value: Int) {
    length = value
}

fun SnackBarBuilder.message(value: CharSequence) {
    message = value
}

fun SnackBarBuilder.action(text: CharSequence, onClick: () -> Unit) {
    actionText = text
    action = onClick
}

fun SnackBarBuilder.onDismissed(listener: () -> Unit) {
    dismissed = listener
}

fun SnackBarBuilder.build() = Snackbar
        .make(view, message, Snackbar.LENGTH_LONG)
        .setAction(actionText) {
            internalView?.removeOnAttachStateChangeListener(internalAttach)

            action()
        }
        .also { snackBar ->
            internalView = snackBar.view

            internalAttach = object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(view: View) {
                    dismissed()
                }

                override fun onViewAttachedToWindow(view: View) {
                }
            }

            internalView?.addOnAttachStateChangeListener(internalAttach)
        }