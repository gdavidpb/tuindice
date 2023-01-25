package com.gdavidpb.tuindice.summary.mapping

import android.content.Context
import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.utils.extension.capitalize
import com.gdavidpb.tuindice.base.utils.extension.format
import com.gdavidpb.tuindice.base.utils.extension.isToday
import com.gdavidpb.tuindice.base.utils.extension.isYesterday
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem
import java.util.*
import java.util.concurrent.TimeUnit

fun Account.toShortName(): String {
	val firstName = firstNames.substringBefore(' ')
	val lastName = lastNames.substringBefore(' ')

	return "$firstName $lastName"
}

fun Date.formatLastUpdate(): String {
	val now = Date()
	val diff = now.time - time
	val days = TimeUnit.MILLISECONDS.toDays(diff)

	return when {
		time == 0L -> "Nunca"
		isToday() -> format("'Hoy,' hh:mm aa")
		isYesterday() -> format("'Ayer,' hh:mm aa")
		days < 7 -> format("EEEE',' hh:mm aa")
		else -> format("dd 'de' MMMM yyyy")
	}?.capitalize() ?: "-"
}

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