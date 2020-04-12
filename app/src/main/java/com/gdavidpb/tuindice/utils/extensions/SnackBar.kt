package com.gdavidpb.tuindice.utils.extensions

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.*
import com.google.android.material.snackbar.Snackbar

data class SnackBarBuilder(
        val view: View,
        var length: Int,
        var message: CharSequence = "",
        @StringRes
        var messageResource: Int = 0,
        @StringRes
        var actionTextResource: Int = 0,
        var action: () -> Unit = {},
        @AnimationMode
        var animationMode: Int = ANIMATION_MODE_FADE,
        @ColorRes
        var textColorResource: Int = 0,
        @ColorRes
        var backgroundColorResource: Int = 0,
        var dismissed: (event: Int) -> Unit = {}
) {
    fun action(@StringRes textResource: Int, onClick: () -> Unit) {
        actionTextResource = textResource
        action = onClick
    }

    fun onDismissed(listener: (event: Int) -> Unit) {
        dismissed = listener
    }

    fun build(): Snackbar {
        val builtMessage = when {
            message.isNotEmpty() -> message
            messageResource != 0 -> view.context.getString(messageResource)
            else -> throw IllegalArgumentException("There is no value for 'message' or 'messageResource'.")
        }

        return Snackbar.make(view, builtMessage, length)
                .setAnimationMode(animationMode)
                .apply {
                    if (textColorResource != 0)
                        setTextColor(ContextCompat.getColor(context, textColorResource))

                    if (backgroundColorResource != 0)
                        setBackgroundTint(ContextCompat.getColor(context, backgroundColorResource))

                    if (actionTextResource != 0)
                        setAction(context.getString(actionTextResource)) { action() }

                    object : BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                            removeCallback(this)

                            dismissed(event)
                        }
                    }.also { callback ->
                        addCallback(callback)
                    }
                }
    }
}

inline fun FragmentActivity.snackBar(length: Int = Snackbar.LENGTH_LONG, builder: SnackBarBuilder.() -> Unit) =
        SnackBarBuilder(contentView ?: window.decorView, length).apply(builder).build().show()

inline fun Fragment.snackBar(length: Int = Snackbar.LENGTH_LONG, builder: SnackBarBuilder.() -> Unit) =
        SnackBarBuilder(requireView(), length).apply(builder).build().show()
