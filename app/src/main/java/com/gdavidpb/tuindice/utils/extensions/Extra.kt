package com.gdavidpb.tuindice.utils.extensions

import android.util.Base64
import okhttp3.RequestBody
import okio.Buffer
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterInputStream
import java.util.zip.InflaterInputStream

private val sensitiveData = arrayOf(
        "(?<=password=)[^&\\s\\n]+",    /* Password param  */
        "(?<=JSESSIONID=)[^;]+",        /* JSession cookie */
        "(?<=jsessionid=)[^\"?]+",      /* JSession href   */
        "(?<=ticket=)[^&\\s\\n]+"       /* Ticket param    */
).map { it.toRegex() }

fun RequestBody?.toString() = this?.let { requestBody ->
    Buffer()
            .also(requestBody::writeTo)
            .use { buffer -> buffer.readUtf8() }
} ?: ""

fun String.deflateHtml(): String = let(Jsoup::parse)
        .removeCommentsAndStyles()
        .outerHtml()
        .replace("(<[^>]*>)".toRegex()) { "\n${it.value}\n" }
        .replace("[\\s\\n\\t]+$".toRegex(RegexOption.MULTILINE), "")
        .replace("\n", "")
        .toByteArray()
        .deflate()

fun ByteArray.deflate(): String = DeflaterInputStream(let(::ByteArrayInputStream), Deflater().apply { setLevel(Deflater.BEST_COMPRESSION) })
        .use { stream -> stream.readBytes() }
        .let { bytes -> Base64.encodeToString(bytes, Base64.DEFAULT) }

fun String.inflate(): ByteArray = Base64.decode(this, Base64.DEFAULT)
        .let(::ByteArrayInputStream)
        .let(::InflaterInputStream)
        .use { stream -> stream.readBytes() }

fun String.noSensitiveData(): String = sensitiveData.fold(this) { acc, regex ->
    acc.replace(regex, "******")
}.run { if (isHtml()) deflateHtml() else this }

fun String.isHtml() = contains("<[^>]*>".toRegex())

inline fun Any?.isNull(exec: () -> Unit) = this ?: exec()