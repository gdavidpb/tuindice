package com.gdavidpb.tuindice.utils.mappers

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Base64
import androidx.core.net.toUri
import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.domain.usecase.request.ResetRequest
import com.gdavidpb.tuindice.utils.REF_BASE
import com.gdavidpb.tuindice.utils.extensions.get
import com.gdavidpb.tuindice.utils.extensions.parse
import com.gdavidpb.tuindice.utils.extensions.trimAll
import com.google.firebase.auth.FirebaseUser
import java.io.Serializable
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

fun AuthRequest.toDstCredentials() = DstCredentials(usbId = usbId, password = password)

fun FirebaseUser.toAuth() = Auth(
        uid = uid,
        email = email ?: ""
)

fun String.toVerifyCode(): String {
    return toUri().getQueryParameterOrThrow("oobCode")
}

fun String.toResetRequest(): ResetRequest {
    val url = toUri()

    val linkUrl = url.getQueryParameterOrThrow("link").toUri()
    val code = linkUrl.getQueryParameterOrThrow("oobCode")
    val continueUrl = linkUrl.getQueryParameterOrThrow("continueUrl").toUri()
    val resetPassword = continueUrl.getQueryParameterOrThrow("resetPassword")

    val (email, password) = resetPassword.toResetParam()

    return ResetRequest(code, email, password)
}

fun String.toResetParam(): Pair<String, String> {
    val data = String(Base64.decode(this, Base64.DEFAULT)).split("\n")

    return data.first() to data.last()
}

fun Pair<String, String>.fromResetParam(): String {
    val data = "$first\n$second".toByteArray()

    return Base64.encodeToString(data, Base64.DEFAULT)
}

fun Uri.getQueryParameterOrThrow(key: String): String {
    return getQueryParameter(key) ?: throw IllegalArgumentException("$key: '$this'")
}

infix fun Int.distanceTo(x: Int): Double {
    val (a, b, c) = arrayOf(
            (Color.red(x) - Color.red(this)).toDouble(),
            (Color.green(x) - Color.green(this)).toDouble(),
            (Color.blue(x) - Color.blue(this)).toDouble()
    )

    return sqrt(a.pow(2.0) + b.pow(2.0) + c.pow(2.0))
}

fun String.toStartEndDate(refYear: Int): List<Date> {
    val normalizedText = "\\w+\\s*-\\s*\\w+\\s*\\d{4}".toRegex().find(this)!!.value

    val year = normalizedText.substringAfterLast(" ").trimAll().toIntOrNull() ?: 0
    val months = normalizedText.substringBeforeLast(" ").trimAll()

    return months
            .split("\\s*-\\s*".toRegex())
            .mapNotNull { month -> "$month $year".parse("MMMM yyyy") }
            .also { output ->
                checkIntegrity(input = this, output = output, refYear = refYear)
            }
}

fun String.toRefYear() = "^[^-]+".toRegex().find(this)!!.value.toInt() + REF_BASE

private fun checkIntegrity(input: String, output: List<Date>, refYear: Int) {
    if (output.size != 2)
        throw IllegalArgumentException("toStartEndDate: '$input'")

    val startYear = output[0].get(Calendar.YEAR)
    val endYear = output[1].get(Calendar.YEAR)

    val isInvalidDistance = abs(startYear - endYear) > 1

    val isInvalid = if (refYear != -1) {
        val validRange = (refYear - 1)..(Date().get(Calendar.YEAR) + 1)

        !validRange.contains(startYear) || !validRange.contains(endYear) || isInvalidDistance
    } else
        isInvalidDistance

    if (isInvalid)
        throw IllegalArgumentException("toStartEndDate: '$input'")
}

fun runCatchingIsSuccess(block: () -> Unit) = runCatching { block() }
        .onFailure { throwable -> throwable.printStackTrace() }.isSuccess

fun Intent.fillIntentArguments(params: Array<out Pair<String, Any?>>) {
    params.forEach { (key, value) ->
        when (value) {
            null -> putExtra(key, null as Serializable?)
            is Int -> putExtra(key, value)
            is Long -> putExtra(key, value)
            is CharSequence -> putExtra(key, value)
            is String -> putExtra(key, value)
            is Float -> putExtra(key, value)
            is Double -> putExtra(key, value)
            is Char -> putExtra(key, value)
            is Short -> putExtra(key, value)
            is Boolean -> putExtra(key, value)
            is Serializable -> putExtra(key, value)
            is Bundle -> putExtra(key, value)
            is Parcelable -> putExtra(key, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> putExtra(key, value)
                value.isArrayOf<String>() -> putExtra(key, value)
                value.isArrayOf<Parcelable>() -> putExtra(key, value)
                else -> throw IllegalArgumentException("Intent extra $key has wrong type ${value.javaClass.name}")
            }
            is IntArray -> putExtra(key, value)
            is LongArray -> putExtra(key, value)
            is FloatArray -> putExtra(key, value)
            is DoubleArray -> putExtra(key, value)
            is CharArray -> putExtra(key, value)
            is ShortArray -> putExtra(key, value)
            is BooleanArray -> putExtra(key, value)
            else -> throw IllegalArgumentException("Intent extra $key has wrong type ${value.javaClass.name}")
        }
    }
}