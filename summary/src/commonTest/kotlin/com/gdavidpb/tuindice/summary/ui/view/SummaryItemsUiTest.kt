package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.testing.summaryContentState
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryItemsUiTest {
	@Test
	fun when_summaryItemsAreRemembered_then_buildsLocalizedHeadersAndLabels() = runTuIndiceUiTest {
		val contentState = summaryContentState().copy(
			enrolledSubjects = 1,
			enrolledCredits = 2
		)

		setTuIndiceTestContent {
			val items = rememberSummaryItems(state = contentState)

			Column {
				items.forEach { item ->
					Text(text = item.header)
					item.entries.forEach { entry ->
						Text(text = "${entry.label}:${entry.value}")
					}
				}
			}
		}

		onNodeWithText("1 materia inscrita").assertIsDisplayed()
		onNodeWithText("2 créditos inscritos").assertIsDisplayed()
		onNodeWithText("Aprobadas:3").assertIsDisplayed()
		onNodeWithText("Reprobadas:1").assertIsDisplayed()
		onNodeWithText("Retiradas:1").assertIsDisplayed()
		onNodeWithText("Aprobados:18").assertIsDisplayed()
		onNodeWithText("Reprobados:1").assertIsDisplayed()
		onNodeWithText("Retirados:2").assertIsDisplayed()
	}

	@Test
	fun when_buildSummaryItemsCalled_then_mapsHeadersValuesAndColors() {
		val state = summaryContentState().copy(
			approvedSubjects = 7,
			failedSubjects = 2,
			retiredSubjects = 1,
			approvedCredits = 20,
			failedCredits = 4,
			retiredCredits = 3
		)
		val approvedColor = Color(0xFF2E7D32)
		val failedColor = Color(0xFFC62828)
		val retiredColor = Color(0xFF616161)

		val items = buildSummaryItems(
			state = state,
			labels = SummaryItemsLabels(
				subjectsHeader = "Materias",
				subjectsApprovedLabel = "Aprobadas",
				subjectsFailedLabel = "Reprobadas",
				subjectsRetiredLabel = "Retiradas",
				creditsHeader = "Creditos",
				creditsApprovedLabel = "Aprobados",
				creditsFailedLabel = "Reprobados",
				creditsRetiredLabel = "Retirados"
			),
			colors = SummaryItemsColors(
				approved = approvedColor,
				failed = failedColor,
				retired = retiredColor
			)
		)

		assertEquals(2, items.size)
		assertEquals("Materias", items[0].header)
		assertEquals(3, items[0].entries.size)
		assertEquals("Aprobadas", items[0].entries[0].label)
		assertEquals(7, items[0].entries[0].value)
		assertEquals(approvedColor, items[0].entries[0].color)
		assertEquals("Creditos", items[1].header)
		assertEquals("Reprobados", items[1].entries[1].label)
		assertEquals(4, items[1].entries[1].value)
		assertEquals(failedColor, items[1].entries[1].color)
		assertEquals("Retirados", items[1].entries[2].label)
		assertEquals(3, items[1].entries[2].value)
		assertEquals(retiredColor, items[1].entries[2].color)
	}
}
