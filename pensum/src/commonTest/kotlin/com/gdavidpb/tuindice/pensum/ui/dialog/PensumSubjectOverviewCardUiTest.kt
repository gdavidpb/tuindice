package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.pensum.testing.sampleBlockedPensumNode
import com.gdavidpb.tuindice.pensum.testing.sampleCurrentPensumNode
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumSubjectOverviewCardUiTest {
	@Test
	fun when_nodeHasTerm_then_showsCodeNameStatusTermAndCredits() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			PensumSubjectOverviewCard(node = sampleCurrentPensumNode())
		}

		onNodeWithTag(PensumUiTags.SubjectDetailCode).assertTextEquals("CI4325")
		onNodeWithTag(PensumUiTags.SubjectDetailName).assertTextEquals("Interfaces con el Usuario")
		onNodeWithTag(PensumUiTags.SubjectDetailStatus).assertTextEquals("En curso")
		onNodeWithText("Trimestre").assertExists()
		onNodeWithTag(PensumUiTags.SubjectDetailTermValue).assertTextEquals("1° trimestre")
		onNodeWithText("Unidades crédito").assertExists()
		onNodeWithText("4 UC").assertExists()
	}

	@Test
	fun when_nodeHasNoTerm_then_termValueSaysItIsUnassigned() = runTuIndiceUiTest {
		val node = sampleBlockedPensumNode().let { blocked ->
			blocked.copy(detail = blocked.detail.copy(termLabel = null))
		}

		setTuIndiceTestContent {
			PensumSubjectOverviewCard(node = node)
		}

		onNodeWithTag(PensumUiTags.SubjectDetailTermValue).assertTextEquals("Sin trimestre asignado")
		onNodeWithTag(PensumUiTags.SubjectDetailCode).assertTextEquals("CI9999")
		onNodeWithTag(PensumUiTags.SubjectDetailStatus).assertTextEquals("Bloqueada")
	}
}
