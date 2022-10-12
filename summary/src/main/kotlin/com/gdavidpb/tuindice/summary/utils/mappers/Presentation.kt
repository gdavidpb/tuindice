package com.gdavidpb.tuindice.summary.utils.mappers

import android.content.Context
import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem

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