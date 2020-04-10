package com.gdavidpb.tuindice.utils.extensions

import android.util.Base64
import com.gdavidpb.tuindice.R
import okhttp3.RequestBody
import okio.Buffer
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.util.zip.DeflaterInputStream

fun RequestBody?.bodyToString(): String {
    if (this == null) return ""

    val buffer = Buffer().also(::writeTo)

    val string = buffer.readUtf8()

    buffer.close()

    return string
}

fun String.deflateHtml(): String = let(Jsoup::parse)
        .html()
        .toByteArray()
        .let(::ByteArrayInputStream)
        .let(::DeflaterInputStream)
        .run { readBytes().also { close() } }
        .let { bytes -> Base64.encodeToString(bytes, Base64.DEFAULT) }

fun String.noSensitiveData(): String = replace("(username|password)=[^&]+&".toRegex(), "")
        .run { if (isHtml()) deflateHtml() else this }

fun String.isHtml() = contains("<[^>]*>".toRegex())

fun Long.bytes(): ByteArray = ByteBuffer.allocate(Long.SIZE_BYTES).run {
    putLong(this@bytes).array().also { clear() }
}

fun Int.isStartDestination() = when (this) {
    R.id.nav_summary,
    R.id.nav_record,
    R.id.nav_about -> true
    else -> false
}

inline fun Any?.isNull(exec: () -> Unit) = this ?: exec()

inline fun <T> T?.notNull(exec: (T) -> Unit): T? = this?.also { exec(this) }
