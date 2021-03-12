package com.gdavidpb.tuindice.utils.extensions

import android.content.DialogInterface
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
        onClicked: (dialog: DialogInterface) -> Unit = {}
) {
    setPositiveButton(buttonTextResource) { dialog, _ -> onClicked(dialog) }
}

fun (AlertDialog.Builder).negativeButton(
        @StringRes buttonTextResource: Int,
        onClicked: (dialog: DialogInterface) -> Unit = {}
) {
    setNegativeButton(buttonTextResource) { dialog, _ -> onClicked(dialog) }
}

fun (AlertDialog.Builder).createView(@LayoutRes layout: Int, init: View.() -> Unit): View =
        View.inflate(context, layout, null)
                .apply(init)
                .also { view -> setView(view) }

inline fun FragmentActivity.alert(builder: AlertDialog.Builder.() -> Unit): AlertDialog =
        AlertDialog.Builder(this).apply(builder).show()

inline fun Fragment.alert(builder: AlertDialog.Builder.() -> Unit): AlertDialog =
        AlertDialog.Builder(requireActivity()).apply(builder).show()