package com.gdavidpb.tuindice.utils.extensions

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes

inline val Activity.contentView: View?
    get() = findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)

fun Activity.selector(
        @StringRes textResource: Int,
        items: Array<String>,
        onClick: (String) -> Unit
) = alert {
    titleResource = textResource

    setItems(items) { _, which -> onClick(items[which]) }
}