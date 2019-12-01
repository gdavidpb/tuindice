package com.gdavidpb.tuindice.utils.extensions

import android.content.SharedPreferences

fun SharedPreferences.edit(transaction: SharedPreferences.Editor.() -> Unit) {
    with(edit()) {
        transaction()
        apply()
    }
}