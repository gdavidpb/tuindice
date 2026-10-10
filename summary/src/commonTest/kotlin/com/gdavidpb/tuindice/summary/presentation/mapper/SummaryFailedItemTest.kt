package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.presentation.mapper.NewStudentNoRecordTexts
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryFailedItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryFailedKind
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_failed_message
import tuindice.summary.generated.resources.summary_failed_title
import kotlin.test.Test
import kotlin.test.assertEquals

class SummaryFailedItemTest {
	@Test
	fun resolveSummaryFailedItem_readsTheSharedNewStudentCopyForAnAccountWithoutRecord() {
		assertEquals(
			SummaryFailedItem(
				kind = SummaryFailedKind.NewStudentNoRecord,
				title = NewStudentNoRecordTexts.title,
				message = NewStudentNoRecordTexts.message
			),
			resolveSummaryFailedItem(SyncStatus.NewStudentNoRecord)
		)
	}

	@Test
	fun resolveSummaryFailedItem_readsThePlainFailureForEveryOtherStatus() {
		val failure = SummaryFailedItem(
			kind = SummaryFailedKind.Error,
			title = UiText.Resource(Res.string.summary_failed_title),
			message = UiText.Resource(Res.string.summary_failed_message)
		)

		SyncStatus.entries
			.filterNot { status -> status == SyncStatus.NewStudentNoRecord }
			.forEach { status ->
				assertEquals(failure, resolveSummaryFailedItem(status), "status=$status")
			}
	}
}
