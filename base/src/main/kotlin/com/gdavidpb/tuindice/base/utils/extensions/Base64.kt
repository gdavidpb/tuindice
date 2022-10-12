package com.gdavidpb.tuindice.base.utils.extensions

import android.util.Base64

fun ByteArray.base64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

fun String.base64(): ByteArray = Base64.decode(this, Base64.NO_WRAP)