package com.gdavidpb.tuindice.utils.extensions

import androidx.core.net.toUri

val String.oobCode: String
    get() = queryParameter("oobCode") ?: ""

val String.mode: String
    get() = queryParameter("mode") ?: ""

private fun String.queryParameter(key: String): String? {
    return toUri().getQueryParameter(key)
}