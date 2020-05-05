package com.gdavidpb.tuindice.utils.extensions

import android.util.Base64
import androidx.annotation.NavigationRes
import androidx.navigation.ui.AppBarConfiguration
import okhttp3.RequestBody
import okio.Buffer
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
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

fun AppBarConfiguration.isTopLevelDestination(@NavigationRes navId: Int) = topLevelDestinations.contains(navId)

inline fun Any?.isNull(exec: () -> Unit) = this ?: exec()

inline fun <T> T?.notNull(exec: (T) -> Unit): T? = this?.also { exec(this) }
