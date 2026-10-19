package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.summary.presentation.model.TermReportActionIconKind

// The mark of each screen an action can open, the same screens the chat's actions open.
internal fun AssistantNavTarget.toActionIconKind(): TermReportActionIconKind {
	return when (this) {
		AssistantNavTarget.RECORD_PROJECTION -> TermReportActionIconKind.TREND
		AssistantNavTarget.RECORD_SCHEDULE, AssistantNavTarget.SUBJECTS_SEARCH -> TermReportActionIconKind.TREE
		AssistantNavTarget.SHARE_TERM -> TermReportActionIconKind.SHARE
		AssistantNavTarget.STATISTICS -> TermReportActionIconKind.BAR_CHART
		AssistantNavTarget.PENSUM, AssistantNavTarget.SUBJECTS_DETAIL -> TermReportActionIconKind.SCHOOL
		AssistantNavTarget.RECORD, AssistantNavTarget.RECORD_CREATE_TERM -> TermReportActionIconKind.TABLE
		AssistantNavTarget.EVALUATIONS, AssistantNavTarget.EVALUATIONS_ADD, AssistantNavTarget.EVALUATIONS_DETAIL ->
			TermReportActionIconKind.ASSIGNMENT
		AssistantNavTarget.ENROLLMENT_PROOF -> TermReportActionIconKind.FIND_IN_PAGE
		AssistantNavTarget.SUMMARY -> TermReportActionIconKind.DASHBOARD
		AssistantNavTarget.ABOUT -> TermReportActionIconKind.INFO
		AssistantNavTarget.SIGN_IN, AssistantNavTarget.UPDATE_PASSWORD -> TermReportActionIconKind.LOCK
	}
}
