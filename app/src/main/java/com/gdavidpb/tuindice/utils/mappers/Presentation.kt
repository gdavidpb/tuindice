package com.gdavidpb.tuindice.utils.mappers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.EvaluationType
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.presentation.model.CustomTypefaceSpan
import com.gdavidpb.tuindice.presentation.model.SummaryCredits
import com.gdavidpb.tuindice.presentation.model.SummarySubjects
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import org.jetbrains.anko.append
import org.jetbrains.anko.buildSpanned
import org.jetbrains.anko.foregroundColor
import java.util.*

fun Account.toSummarySubjects() = SummarySubjects(
        enrolledSubjects = enrolledSubjects,
        approvedSubjects = approvedSubjects,
        retiredSubjects = retiredSubjects,
        failedSubjects = failedSubjects
)

fun Account.toSummaryCredits() = SummaryCredits(
        enrolledCredits = enrolledCredits,
        approvedCredits = approvedCredits,
        retiredCredits = retiredCredits,
        failedCredits = failedCredits
)

fun Subject.toSubjectCode(context: Context) =
        if (status == STATUS_SUBJECT_OK && grade != 0)
            code
        else
            buildSpanned {
                val content = context.getString(R.string.subject_title, code, status.toSubjectStatusDescription())
                val colorSecondary = context.getCompatColor(R.color.color_secondary_text)

                append(content.substringBefore(' '))
                append(' ')
                append(content.substringAfter(' '),
                        TypefaceSpan("sans-serif-light"),
                        ForegroundColorSpan(colorSecondary))

            }

fun Subject.toSubjectGrade(context: Context) =
        if (grade != 0)
            context.getString(R.string.subject_grade, grade)
        else
            "-"

fun Subject.toSubjectCredits(context: Context) =
        context.getString(R.string.subject_credits, credits)

@SuppressLint("DefaultLocale")
fun Quarter.toQuarterTitle(): String {
    val start = startDate.format("MMM")?.capitalize()
    val end = endDate.format("MMM")?.capitalize()
    val year = startDate.format("yyyy")

    return "$start - $end $year".replace("\\.".toRegex(), "")
}

fun Quarter.toQuarterGradeDiff(color: Int, context: Context) =
        context.getString(R.string.quarter_grade_diff, grade).toSpanned(color)

fun Quarter.toQuarterGradeSum(color: Int, context: Context) =
        context.getString(R.string.quarter_grade_sum, gradeSum).toSpanned(color)

fun Quarter.toQuarterCredits(color: Int, font: Typeface, context: Context) =
        context.getString(R.string.quarter_credits, credits).toSpanned(color, font)

@SuppressLint("DefaultLocale")
fun Date.toEvaluationDate(): String {
    val weeksLeft = weeksLeft() + 1

    return when {
        Date().after(this) -> "${format("EEEE '—' dd/MM")?.capitalize()}"
        isToday() -> "Hoy"
        isTomorrow() -> "Mañana"
        isThisWeek() -> "Este ${format("EEEE '—' dd/MM")}"
        isNextWeek() -> "El próximo ${format("EEEE '—' dd/MM")}"
        weeksLeft > 1 -> "En $weeksLeft semanas, ${format("EEEE '—' dd/MM")}"
        else -> "-"
    }
}

fun EvaluationType.toEvaluationTypeName(context: Context) = context.getString(stringRes)

fun Int.toSubjectStatusDescription() = when (this) {
    STATUS_SUBJECT_RETIRED -> "Retirada"
    STATUS_SUBJECT_GAVE_UP -> "Retirada"
    STATUS_SUBJECT_NO_EFFECT -> "Sin efecto"
    else -> ""
}

fun String.toSubjectStatusValue() = when (this) {
    "Retirada" -> STATUS_SUBJECT_RETIRED
    "RETIRADA" -> STATUS_SUBJECT_GAVE_UP
    "Sin Efecto" -> STATUS_SUBJECT_NO_EFFECT
    else -> STATUS_SUBJECT_OK
}

fun String.toUsbEmail() = "$this@usb.ve"

fun String.toSubjectName(): String {
    var result = replace("^\"|\"$".toRegex(), "")
            .replace("(?<=\\w)\\.(?=\\w)".toRegex(), " ")

    ROMANS.forEach { (key, value) ->
        result = result.replace("(?<=\\b)$key(?=\\b|$)".toRegex(), value)
    }

    return result
}

private fun String.toSpanned(color: Int, font: Typeface? = null): Spanned {
    val (iconString, valueString, extraString) = split(' ')
            .toMutableList()
            .apply { if (size == 2) add("") }

    val typefaceSpan = font?.let(::CustomTypefaceSpan)
            ?: TypefaceSpan("sans-serif-medium")

    return buildSpanned {
        append(iconString,
                typefaceSpan,
                AbsoluteSizeSpan(18, true),
                foregroundColor(color))
        append(' ')
        append(valueString)

        if (extraString.isNotBlank()) {
            append(' ')
            append(extraString)
        }
    }
}