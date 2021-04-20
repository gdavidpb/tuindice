package com.gdavidpb.tuindice.utils.extensions

import android.content.DialogInterface
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
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
        onClick: (dialog: DialogInterface) -> Unit = {}
) {
    setPositiveButton(buttonTextResource) { dialog, _ -> onClick(dialog) }
}

fun (AlertDialog.Builder).negativeButton(
        @StringRes buttonTextResource: Int,
        onClick: (dialog: DialogInterface) -> Unit = {}
) {
    setNegativeButton(buttonTextResource) { dialog, _ -> onClick(dialog) }
}

fun (AlertDialog.Builder).createView(@LayoutRes layout: Int, init: View.() -> Unit): View =
        View.inflate(context, layout, null)
                .apply(init)
                .also { view -> setView(view) }

inline fun LifecycleOwner.alert(builder: AlertDialog.Builder.() -> Unit): AlertDialog =
        AlertDialog.Builder(context()).apply(builder).show()