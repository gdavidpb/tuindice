package com.gdavidpb.tuindice.base.utils.extension

import android.util.Base64

fun ByteArray.encodeToBase64String(): String = Base64.encodeToString(this, Base64.NO_WRAP)

fun String.decodeFromBase64String(): ByteArray = Base64.decode(this, Base64.NO_WRAP)