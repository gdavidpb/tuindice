package com.gdavidpb.tuindice.utils.mappers

import android.graphics.Color
import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.model.exception.ParseException
import com.gdavidpb.tuindice.domain.model.exception.UnauthenticatedException
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.utils.REF_BASE
import com.gdavidpb.tuindice.utils.extensions.get
import com.gdavidpb.tuindice.utils.extensions.parse
import com.gdavidpb.tuindice.utils.extensions.trimAll
import com.google.firebase.auth.FirebaseUser
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

fun Credentials.toDstCredentials(serviceUrl: String) = DstCredentials(
        usbId = usbId,
        password = password,
        serviceUrl = serviceUrl
)

fun FirebaseUser.toAuth() = Auth(
        uid = uid,
        email = email ?: throw UnauthenticatedException()
)

infix fun Int.distanceTo(x: Int): Double {
    val (a, b, c) = arrayOf(
            (Color.red(x) - Color.red(this)).toDouble(),
            (Color.green(x) - Color.green(this)).toDouble(),
            (Color.blue(x) - Color.blue(this)).toDouble()
    )

    return sqrt(a.pow(2.0) + b.pow(2.0) + c.pow(2.0))
}

fun String.toStartEndDate(refYear: Int): List<Date> {
    val normalizedText = "\\w+\\s*-\\s*\\w+\\s*\\d{4}".toRegex().find(this)?.value

    checkNotNull(normalizedText) { throw ParseException("toStartEndDate: $this") }

    val year = normalizedText.substringAfterLast(' ').trimAll().toIntOrNull() ?: 0
    val months = normalizedText.substringBeforeLast(' ').trimAll()

    return months
            .split("\\s*-\\s*".toRegex())
            .mapNotNull { month -> "$month $year".parse("MMMM yyyy") }
            .also { output ->
                checkIntegrity(input = this, output = output, refYear = refYear)
            }
}

fun String.toRefYear(): Int {
    val yearText = "^[^-]+".toRegex().find(this)?.value

    checkNotNull(yearText) { throw ParseException("toRefYear: $this") }

    return yearText.toInt() + REF_BASE
}

private fun checkIntegrity(input: String, output: List<Date>, refYear: Int) {
    check(output.size == 2) { throw ParseException("checkIntegrity: $input") }

    val startYear = output[0].get(Calendar.YEAR)
    val endYear = output[1].get(Calendar.YEAR)

    val isInvalidDistance = abs(startYear - endYear) > 1

    val isInvalid = if (refYear != -1) {
        val validRange = (refYear - 1)..(Date().get(Calendar.YEAR) + 1)

        !validRange.contains(startYear) || !validRange.contains(endYear) || isInvalidDistance
    } else
        isInvalidDistance

    check(!isInvalid) { throw ParseException("checkIntegrity: $input") }
}

fun runCatchingIsSuccess(block: () -> Unit) = runCatching { block() }
        .onFailure { throwable -> throwable.printStackTrace() }.isSuccess