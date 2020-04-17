package com.gdavidpb.tuindice.utils.extensions

import android.util.Base64

fun ByteArray.base64(): String = Base64.encodeToString(this, Base64.DEFAULT)