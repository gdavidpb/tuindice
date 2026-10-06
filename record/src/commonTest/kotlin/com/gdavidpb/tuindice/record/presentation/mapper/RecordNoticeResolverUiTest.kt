package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncReportSources
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.record.domain.model.ObservedRecord
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertNotNull

// The Spanish the stale-enrollment notice reads as, resolved the way the screen does it:
// asString() inside a composable.
@OptIn(ExperimentalTestApi::class)
class RecordNoticeResolverUiTest {
	@Test
	fun when_enrollmentIsStaleAndItsLastReadIsKnown_then_messageReadsTheDayAndTheShortMonth() = runTuIndiceUiTest {
		assertResolvedTexts(
			"Actualizado por última vez el 5 ene." to staleMessage(readAt = middayOf(month = 1, day = 5)),
			"Actualizado por última vez el 22 sep." to staleMessage(readAt = middayOf(month = 9, day = 22))
		)
	}

	@Test
	fun when_enrollmentIsStaleAndItsLastReadIsUnknown_then_messageDoesNotNameADate() = runTuIndiceUiTest {
		assertResolvedTexts(
			"No pudimos actualizar tu inscripción. Mostramos la última guardada." to staleMessage(readAt = null)
		)
	}

	private fun staleMessage(readAt: Long?): UiText {
		val notice = resolveRecordNotice(
			ObservedRecord(
				record = AcademicRecord(
					id = "record",
					terms = listOf(academicTerm(id = "term", kind = TermKind.CURRENT))
				),
				viewMode = RecordViewMode.Projection,
				selectedTermId = "term",
				hasSyncedRecord = true,
				syncStatus = SyncStatus.Healthy,
				syncReport = SyncReport(
					status = SyncReportStatus.Success,
					sources = SyncReportSources(
						record = SyncSourceReport(SyncSourceStatus.Success),
						enrollment = SyncSourceReport(status = SyncSourceStatus.Unavailable)
					),
					enrollmentReadAt = readAt
				)
			)
		)

		return assertNotNull(notice).message
	}

	// The mapper reads the instant in the zone of the device, so it is built in that zone.
	private fun middayOf(month: Int, day: Int): Long {
		return LocalDateTime(year = 2026, month = month, day = day, hour = 12, minute = 0)
			.toInstant(TimeZone.currentSystemDefault())
			.toEpochMilliseconds()
	}

	private fun ComposeUiTest.assertResolvedTexts(vararg cases: Pair<String, UiText>) {
		setTuIndiceTestContent {
			cases.forEachIndexed { index, (_, text) ->
				Text(
					modifier = Modifier.testTag(resolvedTextTag(index)),
					text = text.asString()
				)
			}
		}

		cases.forEachIndexed { index, (expected, _) ->
			// Resources resolve asynchronously off Android: wait for the text, then read the node.
			waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
				onAllNodesWithText(expected).fetchSemanticsNodes().isNotEmpty()
			}

			onNodeWithTag(resolvedTextTag(index)).assertTextEquals(expected)
		}
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L

		fun resolvedTextTag(index: Int) = "record_notice_text_resolved_$index"
	}
}
