package com.gdavidpb.tuindice.utils.mappers

import android.content.Context
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.text.buildSpannedString
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.presentation.model.QuarterItem
import com.gdavidpb.tuindice.presentation.model.SubjectItem
import com.gdavidpb.tuindice.presentation.model.SummaryItem
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import java.util.*

/* Presentation */

fun Subject.toSubjectItem(context: Context) = SubjectItem(
        uid = id.hashCode().toLong(),
        id = id,
        code = code,
        isRetired = (status == STATUS_SUBJECT_RETIRED),
        nameText = name,
        codeText = spanSubjectCode(context),
        gradeText = grade.formatGrade(context),
        creditsText = credits.formatCredits(context),
        data = this
)

fun Quarter.toQuarterItem(context: Context): QuarterItem {
    val quarterColor = context.getCompatColor(status.toQuarterColor())

    return QuarterItem(
            uid = id.hashCode().toLong(),
            id = id,
            color = quarterColor,
            isMock = (status == STATUS_QUARTER_MOCK),
            isEditable = (status == STATUS_QUARTER_CURRENT) || (status == STATUS_QUARTER_MOCK),
            TitleText = name,
            isCurrent = (status == STATUS_QUARTER_CURRENT),
            gradeDiffText = grade.formatGradeDiff(quarterColor, context),
            gradeSumText = gradeSum.formatGradeSum(quarterColor, context),
            creditsText = credits.formatCredits(quarterColor, context),
            subjectsItems = subjects.map { it.toSubjectItem(context) },
            data = this
    )
}

fun Evaluation.toEvaluationItem(context: Context) = EvaluationItem(
        uid = id.hashCode().toLong(),
        id = id,
        notesText = if (notes.isNotEmpty()) notes else "─",
        grade = grade,
        maxGrade = maxGrade,
        dateText = date.formatEvaluationDate(),
        typeText = type.formatEvaluationTypeName(context),
        date = date,
        isDone = isDone,
        data = this
)

fun Account.toSubjectsSummaryItem(context: Context) = SummaryItem(
        headerText = context.resources.getQuantityString(
                R.plurals.text_subjects_header,
                enrolledSubjects,
                enrolledSubjects
        ),
        enrolled = enrolledSubjects,
        approved = approvedSubjects,
        retired = retiredSubjects,
        failed = failedSubjects
)

fun Account.toCreditsSummaryItem(context: Context) = SummaryItem(
        headerText = context.resources.getQuantityString(
                R.plurals.text_credits_header,
                enrolledCredits,
                enrolledCredits
        ),
        enrolled = enrolledCredits,
        approved = approvedCredits,
        retired = retiredCredits,
        failed = failedCredits
)

/* Format */

fun Int.toQuarterColor() = when (this) {
    STATUS_QUARTER_CURRENT -> R.color.quarter_current
    STATUS_QUARTER_COMPLETED -> R.color.quarter_completed
    STATUS_QUARTER_MOCK -> R.color.quarter_mock
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

fun String.capitalize() =
        if (isNotEmpty() && this[0].isLowerCase())
            "${substring(0, 1).uppercase(DEFAULT_LOCALE)}${substring(1)}"
        else
            this

fun String.asUsbId() = removeSuffix("@usb.ve")

/* Span */

fun Subject.spanSubjectCode(context: Context): CharSequence {
    val statusText = status.formatSubjectStatusDescription()

    return if (statusText.isEmpty())
        code
    else buildSpannedString {
        val content = context.getString(R.string.subject_title, code, statusText)
        val colorSecondary = context.getCompatColor(R.color.color_secondary_text)

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