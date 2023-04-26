package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.capitalize
import com.gdavidpb.tuindice.base.utils.extension.format
import com.gdavidpb.tuindice.base.utils.extension.isToday
import com.gdavidpb.tuindice.base.utils.extension.isYesterday
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryViewState
import java.util.Date
import java.util.concurrent.TimeUnit

fun Account.toSummaryViewState(resourceResolver: ResourceResolver) = SummaryViewState(
	name = toShortName(),
	lastUpdate = resourceResolver.getString(
		R.string.text_last_update,
		lastUpdate.formatLastUpdate()
	),
	careerName = careerName,
	grade = grade.toFloat(),
	profilePictureUrl = pictureUrl,
	items = listOf(
		toSubjectsSummaryItem(resourceResolver),
		toCreditsSummaryItem(resourceResolver)
	),
	isGradeVisible = (grade > 0.0),
	isProfilePictureLoading = false,
	isLoading = false,
	isUpdated = true,
	isUpdating = false
)

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

fun Account.toSubjectsSummaryItem(resourceResolver: ResourceResolver) = SummaryItem(
	headerText = resourceResolver.getQuantityString(
		R.plurals.text_subjects_header,
		enrolledSubjects,
		enrolledSubjects
	),
	enrolled = enrolledSubjects,
	approved = approvedSubjects,
	retired = retiredSubjects,
	failed = failedSubjects
)

fun Account.toCreditsSummaryItem(resourceResolver: ResourceResolver) = SummaryItem(
	headerText = resourceResolver.getQuantityString(
		R.plurals.text_credits_header,
		enrolledCredits,
		enrolledCredits
	),
	enrolled = enrolledCredits,
	approved = approvedCredits,
	retired = retiredCredits,
	failed = failedCredits
)