package com.gdavidpb.tuindice.utils.mappers

import android.content.Context
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.text.buildSpannedString
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.presentation.model.*
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import java.util.*

/* Presentation */

fun Subject.toSubjectItem(context: Context) = SubjectItem(
        id = id,
        nameText = name,
        codeText = spanSubjectCode(context),
        gradeText = grade.formatGrade(context),
        creditsText = credits.formatCredits(context),
        data = this
)

fun Quarter.toQuarterItem(context: Context): QuarterItem {
    val quarterColor = ResourcesManager.getColor(status.toQuarterColor(), context)

    return QuarterItem(
            id = id,
            color = quarterColor,
            startEndDateText = (startDate to endDate).formatQuarterTitle(),
            gradeDiffText = grade.formatGradeDiff(quarterColor, context),
            gradeSumText = gradeSum.formatGradeSum(quarterColor, context),
            creditsText = credits.formatCredits(quarterColor, context),
            subjectsItems = subjects.map { it.toSubjectItem(context) },
            data = this
    )
}

fun EvaluationItem.toEvaluation() = data

fun Evaluation.toEvaluationItem(context: Context) = EvaluationItem(
        id = id,
        typeText = type.formatEvaluationTypeName(context),
        notesText = if (notes.isNotEmpty()) notes else "─",
        gradeText = context.getString(R.string.evaluation_grade_max, grade, maxGrade),
        grade = grade,
        dateText = date.formatEvaluationDate(),
        date = date,
        color = ResourcesManager.getColor(if (isDone) R.color.color_retired else R.color.color_approved, context),
        isDone = isDone,
        isSwiping = false,
        data = this
)

fun Account.toSummarySubjectsItem() = SummarySubjectsItem(
        enrolledSubjects = enrolledSubjects,
        approvedSubjects = approvedSubjects,
        retiredSubjects = retiredSubjects,
        failedSubjects = failedSubjects
)

fun Account.toSummaryCreditsItem() = SummaryCreditsItem(
        enrolledCredits = enrolledCredits,
        approvedCredits = approvedCredits,
        retiredCredits = retiredCredits,
        failedCredits = failedCredits
)

/* Format */

fun Int.toQuarterColor() = when (this) {
    STATUS_QUARTER_CURRENT -> R.color.quarter_current
    STATUS_QUARTER_COMPLETED -> R.color.quarter_completed
    STATUS_QUARTER_GUESS -> R.color.quarter_guess
    STATUS_QUARTER_RETIRED -> R.color.quarter_retired
    else -> throw IllegalArgumentException("toQuarterColor: '$this'")
}

fun Int.formatGrade(context: Context) =
        if (this != 0)
            context.getString(R.string.subject_grade, this)
        else
            "—"

fun Int.formatCredits(context: Context) =
        context.getString(R.string.subject_credits, this)

fun Pair<Date, Date>.formatQuarterTitle(): String {
    val start = first.format("MMM")?.capitalize()
    val end = second.format("MMM")?.capitalize()
    val year = first.format("yyyy")

    return "$start - $end $year".replace("\\.".toRegex(), "")
}

fun Double.formatGradeDiff(color: Int, context: Context) =
        context.getString(R.string.quarter_grade_diff, this).spanGrade(color)

fun Double.formatGradeSum(color: Int, context: Context) =
        context.getString(R.string.quarter_grade_sum, this).spanGrade(color)

fun Int.formatCredits(color: Int, context: Context) =
        context.getString(R.string.quarter_credits, this).spanGrade(color)

fun Date.formatEvaluationDate(): String {
    val weeksLeft = weeksLeft()

    val now = Date()

    return when {
        time == 0L -> "Evaluación continua"
        isToday() -> "Hoy"
        isTomorrow() -> "Mañana"
        isYesterday() -> "Ayer"
        isThisWeek() -> {
            if (before(now))
                "El ${format("EEEE 'pasado —' dd/MM")}"
            else
                "Este ${format("EEEE '—' dd/MM")}"
        }
        isNextWeek() -> "El próximo ${format("EEEE '—' dd/MM")}"
        weeksLeft in 2..12 -> "En $weeksLeft semanas, ${format("EEEE '—' dd/MM")}"
        else -> format("EEEE '—' dd/MM/yy")?.capitalize()!!
    }
}

fun EvaluationType.formatEvaluationTypeName(context: Context) = context.getString(stringRes)

fun Int.formatSubjectStatusDescription() = when (this) {
    STATUS_SUBJECT_RETIRED -> "Retirada"
    STATUS_SUBJECT_NO_EFFECT -> "Sin efecto"
    else -> ""
}

fun String.formatSubjectStatusValue() = when (this) {
    "Retirada" -> STATUS_SUBJECT_RETIRED
    "RETIRADA" -> STATUS_SUBJECT_RETIRED
    "Sin Efecto" -> STATUS_SUBJECT_NO_EFFECT
    else -> STATUS_SUBJECT_OK
}

fun String.capitalize() =
        if (isNotEmpty() && this[0].isLowerCase())
            "${substring(0, 1).toUpperCase(DEFAULT_LOCALE)}${substring(1)}"
        else
            this

fun String.toUsbEmail() = "$this@usb.ve"

fun String.formatSubjectName(): String {
    var result = replace("^\"|\"$".toRegex(), "")
            .replace("(?<=\\w)\\.(?=\\w)".toRegex(), " ")

    ROMANS.forEach { (key, value) ->
        result = result.replace("(?<=\\b)$key(?=\\b|$)".toRegex(), value)
    }

    return result
}

/* Span */

fun Subject.spanSubjectCode(context: Context): CharSequence {
    val statusText = status.formatSubjectStatusDescription()

    return if (statusText.isEmpty())
        code
    else buildSpannedString {
        val content = context.getString(R.string.subject_title, code, statusText)
        val colorSecondary = ResourcesManager.getColor(R.color.color_secondary_text, context)

        append(content.substringBefore(' '))
        append(' ')
        append(content.substringAfter(' '),
                TypefaceSpan("sans-serif-light"),
                ForegroundColorSpan(colorSecondary))

    }
}

fun String.spanAbout(titleColor: Int, subtitleColor: Int): CharSequence {
    val (title, subtitle) = listOf(
            substringBefore('\n'),
            substringAfter('\n')
    )

    return buildSpannedString {
        append(title, ForegroundColorSpan(titleColor))
        append('\n')
        append(subtitle, ForegroundColorSpan(subtitleColor), TypefaceSpan("sans-serif-light"))
    }
}

private fun String.spanGrade(color: Int): Spanned {
    val (iconString, valueString, extraString) = split(' ')
            .toMutableList()
            .apply { if (size == 2) add("") }

    return buildSpannedString {
        append(iconString,
                AbsoluteSizeSpan(18, true),
                ForegroundColorSpan(color))
        append(' ')
        append(valueString)

        if (extraString.isNotBlank()) {
            append(' ')
            append(extraString)
        }
    }
}