package com.gdavidpb.tuindice.utils.extensions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.gdavidpb.tuindice.utils.NO_GETTER
import kotlin.DeprecationLevel.ERROR

var (AlertDialog.Builder).titleResource: Int
    @Deprecated(message = NO_GETTER, level = ERROR) get() = throw NotImplementedError()
    set(@StringRes value) {
        setTitle(value)
    }

var (AlertDialog.Builder).messageResource: Int
    @Deprecated(message = NO_GETTER, level = ERROR) get() = throw NotImplementedError()
    set(@StringRes value) {
        setMessage(value)
    }

var (AlertDialog.Builder).isCancelable: Boolean
    @Deprecated(message = NO_GETTER, level = ERROR) get() = throw NotImplementedError()
    set(value) {
        setCancelable(value)
    }

fun (AlertDialog.Builder).positiveButton(
        @StringRes buttonTextResource: Int,
        onClicked: () -> Unit = {}
) {
    setPositiveButton(buttonTextResource) { _, _ -> onClicked() }
}

fun (AlertDialog.Builder).negativeButton(
        @StringRes buttonTextResource: Int,
        onClicked: () -> Unit = {}
) {
    setNegativeButton(buttonTextResource) { _, _ -> onClicked() }
}

fun (AlertDialog.Builder).createView(@LayoutRes layout: Int, init: View.() -> Unit): View =
        LayoutInflater.from(context).inflate(layout, null, false)
                .apply(init)
                .also { view -> setView(view) }

fun Context.alert(builder: AlertDialog.Builder.() -> Unit) =
        AlertDialog.Builder(this).apply(builder)

fun Fragment.alert(builder: AlertDialog.Builder.() -> Unit) =
        requireActivity().alert(builder)