package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.recordMapperTexts
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class QuarterItemUiTest {
	@Test
	fun when_quartersMapped_then_createsQuarterItemsWithTextsAndSubjects() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val items = listOf(DEFAULT_RECORD_QUARTER).toQuarterItemList(
				viewMode = RecordViewMode.Simulation,
				texts = recordMapperTexts(),
				highlightColor = Color(0xFFB8860B)
			)
			val item = items.first()

			Column {
				Text(text = item.shortNameText)
				Text(text = item.gradeText.text)
				Text(text = item.gradeSumText.text)
				Text(text = item.creditsText.text)
				Text(text = item.subjects.first().nameText)
			}
		}

		onNodeWithText("2026-1").assertIsDisplayed()
		onNodeWithText("Δx 70.0").assertIsDisplayed()
		onNodeWithText("∑x 70.0").assertIsDisplayed()
		onNodeWithText("⦿ 6").assertIsDisplayed()
		onNodeWithText("Programacion").assertIsDisplayed()
	}

	@Test
	fun when_quarterValueAnnotated_then_preservesTextContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Text(
				text = "Δx 4.2104"
					.annotatedQuarterValue(highlightColor = Color(0xFFB8860B))
					.text
			)
		}

		onNodeWithText("Δx 4.2104").assertIsDisplayed()
	}

	@Test
	fun when_previousQuarterExists_then_mapsQuarterDeltasAgainstIt() = runTuIndiceUiTest {
		val previousQuarter = DEFAULT_RECORD_QUARTER.copy(
			id = "quarter-0",
			name = "2025-3",
			startDate = DEFAULT_RECORD_QUARTER.startDate - 100_000L,
			endDate = DEFAULT_RECORD_QUARTER.endDate - 100_000L,
			grade = 70.0,
			gradeSum = 69.5,
			credits = 5,
			isCurrent = false
		)
		val currentQuarter = DEFAULT_RECORD_QUARTER.copy(
			grade = 69.75,
			gradeSum = 70.0
		)

		setTuIndiceTestContent {
			val items = listOf(
				currentQuarter,
				previousQuarter
			).toQuarterItemList(
				viewMode = RecordViewMode.Simulation,
				texts = recordMapperTexts(),
				highlightColor = Color(0xFFB8860B)
			)
			val currentItem = items.first()
			val previousItem = items.last()

			Column {
				Text(text = currentItem.gradeDelta?.text ?: "")
				Text(text = currentItem.gradeSumDelta?.text ?: "")
				Text(text = currentItem.creditsDelta?.text ?: "")
				Text(text = previousItem.gradeDelta?.text ?: "Sin diff")
			}
		}

		onNodeWithText("▼ 0.2500").assertIsDisplayed()
		onNodeWithText("▲ 0.5000").assertIsDisplayed()
		onNodeWithText("▲ 1").assertIsDisplayed()
		onNodeWithText("Sin diff").assertIsDisplayed()
	}

	@Test
	fun when_quarterHasNoSubjects_then_hidesQuarterDeltas() = runTuIndiceUiTest {
		val previousQuarter = DEFAULT_RECORD_QUARTER.copy(
			id = "quarter-0",
			name = "2025-3",
			startDate = DEFAULT_RECORD_QUARTER.startDate - 100_000L,
			endDate = DEFAULT_RECORD_QUARTER.endDate - 100_000L,
			isCurrent = true
		)
		val futureQuarter = DEFAULT_RECORD_QUARTER.copy(
			id = "quarter-future",
			name = "2026-1",
			startDate = DEFAULT_RECORD_QUARTER.startDate + 100_000L,
			endDate = DEFAULT_RECORD_QUARTER.endDate + 100_000L,
			grade = 0.0,
			gradeSum = 70.0,
			credits = 0,
			isCurrent = false,
			subjects = emptyList()
		)
		var futureItemTextGrade = ""
		var futureItemTextGradeSum = ""
		var futureItemTextCredits = ""
		var futureGradeDeltaText: String? = null
		var futureGradeSumDeltaText: String? = null
		var futureCreditsDeltaText: String? = null

		setTuIndiceTestContent {
			val items = listOf(
				futureQuarter,
				previousQuarter
			).toQuarterItemList(
				viewMode = RecordViewMode.Simulation,
				texts = recordMapperTexts(),
				highlightColor = Color(0xFFB8860B)
			)
			val futureItem = items.first()
			futureItemTextGrade = futureItem.gradeText.text
			futureItemTextGradeSum = futureItem.gradeSumText.text
			futureItemTextCredits = futureItem.creditsText.text
			futureGradeDeltaText = futureItem.gradeDelta?.text
			futureGradeSumDeltaText = futureItem.gradeSumDelta?.text
			futureCreditsDeltaText = futureItem.creditsDelta?.text
		}

		assertEquals("Δx 0.0", futureItemTextGrade)
		assertEquals("∑x 70.0", futureItemTextGradeSum)
		assertEquals("⦿ 0", futureItemTextCredits)
		assertNull(futureGradeDeltaText)
		assertNull(futureGradeSumDeltaText)
		assertNull(futureCreditsDeltaText)
	}

	@Test
	fun when_simulationStatusExists_then_simulationViewUsesIt_withoutChangingOfficialView() = runTuIndiceUiTest {
		val historicalQuarter = Quarter(
			id = "quarter-1",
			name = "2025-3",
			startDate = 100L,
			endDate = 101L,
			grade = 2.0,
			gradeSum = 2.0,
			credits = 4,
			creditsSum = 4,
			simulationGrade = 0.0,
			simulationGradeSum = 0.0,
			simulationCredits = 0,
			simulationCreditsSum = 0,
			isCurrent = false,
			isReadOnly = true,
			subjects = listOf(
				Subject(
					id = "subject-history",
					quarterId = "quarter-1",
					code = "MA1111",
					name = "Matematicas I",
					credits = 4,
					grade = 2,
					simulationStatus = SubjectStatus.WITHOUT_EFFECT
				)
			)
		)
		var officialStatus: SubjectStatus? = SubjectStatus.WITHOUT_EFFECT
		var simulationStatus: SubjectStatus? = null

		setTuIndiceTestContent {
			val officialItem = listOf(historicalQuarter).toQuarterItemList(
				viewMode = RecordViewMode.Official,
				texts = recordMapperTexts(),
				highlightColor = Color(0xFFB8860B)
			).single()
				val simulationItem = listOf(historicalQuarter).toQuarterItemList(
					viewMode = RecordViewMode.Simulation,
					texts = recordMapperTexts(),
					highlightColor = Color(0xFFB8860B)
				).single()
				officialStatus = officialItem.subjects.single().status
				simulationStatus = simulationItem.subjects.single().status
			}

		assertNull(officialStatus)
		assertEquals(SubjectStatus.WITHOUT_EFFECT, simulationStatus)
	}
}
