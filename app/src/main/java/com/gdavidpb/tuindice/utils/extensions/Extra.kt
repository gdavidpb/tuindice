package com.gdavidpb.tuindice.utils.extensions

import android.util.Base64
import androidx.annotation.IdRes
import androidx.navigation.ui.AppBarConfiguration
import okhttp3.RequestBody
import okio.Buffer
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
import java.util.zip.DeflaterInputStream
import java.util.zip.InflaterInputStream

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
        .deflate()

fun ByteArray.deflate(): String = let(::ByteArrayInputStream)
        .let(::DeflaterInputStream)
        .use { stream -> stream.readBytes() }
        .let { bytes -> Base64.encodeToString(bytes, Base64.DEFAULT) }

fun String.inflate(): ByteArray = Base64.decode(this, Base64.DEFAULT)
        .let(::ByteArrayInputStream)
        .let(::InflaterInputStream)
        .use { stream -> stream.readBytes() }

fun String.noSensitiveData(): String = replace("(username|password)=[^&]+&".toRegex(), "")
        .run { if (isHtml()) deflateHtml() else this }

fun String.isHtml() = contains("<[^>]*>".toRegex())

fun AppBarConfiguration.isTopLevelDestination(@IdRes navId: Int) = topLevelDestinations.contains(navId)

inline fun Any?.isNull(exec: () -> Unit) = this ?: exec()

inline fun <T> T?.notNull(exec: (T) -> Unit): T? = this?.also { exec(this) }
