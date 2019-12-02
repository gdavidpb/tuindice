package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.BuildConfig
import okhttp3.RequestBody
import okio.Buffer
import java.security.cert.X509Certificate

fun RequestBody?.bodyToString(): String {
    if (this == null) return ""

    val buffer = Buffer().also(::writeTo)

    val string = buffer.readUtf8()

    buffer.close()

    return string
}

fun String.removeSensitiveData() = replace("\\s{2,}".toRegex(), "")
        /* Remove USBId and password */
        .replace("(username|password)=[^&]+&", "")

fun X509Certificate.getProperty(key: String) = "(?<=$key=)[^,]+|$".toRegex().find(subjectDN.name)?.value

infix fun Int.negRem(value: Int) = (this % value) + if (this >= 0) 0 else value

inline fun Any?.isNull(exec: () -> Unit) = this ?: exec()

inline fun <T> T?.notNull(exec: (T) -> Unit): T? = this?.also { exec(this) }

inline fun release(block: () -> Unit) {
    if (!BuildConfig.DEBUG) block()
}
