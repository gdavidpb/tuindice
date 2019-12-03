package com.gdavidpb.tuindice.utils.extensions

import android.util.Base64
import com.gdavidpb.tuindice.BuildConfig
import okhttp3.RequestBody
import okio.Buffer
import java.io.ByteArrayInputStream
import java.security.cert.X509Certificate
import java.util.zip.DeflaterInputStream

fun RequestBody?.bodyToString(): String {
    if (this == null) return ""

    val buffer = Buffer().also(::writeTo)

    val string = buffer.readUtf8()

    buffer.close()

    return string
}

fun String.deflate(): String = toByteArray()
        .let(::ByteArrayInputStream)
        .let(::DeflaterInputStream)
        .run { readBytes().also { close() } }
        .let { bytes -> Base64.encodeToString(bytes, Base64.DEFAULT) }

fun String.noSensitiveData(): String = replace("(username|password)=[^&]+&".toRegex(), "")
        .run { if (isHtml()) deflate() else this }

fun String.isHtml() = contains("<[^>]*>".toRegex())

fun X509Certificate.getProperty(key: String) = "(?<=$key=)[^,]+|$".toRegex().find(subjectDN.name)?.value

infix fun Int.negRem(value: Int) = (this % value) + if (this >= 0) 0 else value

inline fun Any?.isNull(exec: () -> Unit) = this ?: exec()

inline fun <T> T?.notNull(exec: (T) -> Unit): T? = this?.also { exec(this) }

inline fun release(block: () -> Unit) {
    if (!BuildConfig.DEBUG) block()
}
