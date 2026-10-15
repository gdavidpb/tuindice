package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScheduleEvaluationButtonUiTest {
	@Test
	fun when_anEvaluationIsGiven_then_theBoxIsAButtonThatAsksForIt() = runTuIndiceUiTest {
		val asked = mutableListOf<String>()

		setTuIndiceTestContent {
			Box(
				modifier = Modifier
					.size(40.dp)
					.testTag(TestTag)
					.scheduleEvaluationButton(
						shape = RectangleShape,
						evaluationId = "ci5311-exam",
						onClick = { evaluationId -> asked += evaluationId }
					)
			)
		}

		val button = onNodeWithTag(TestTag)

		button.assertHasClickAction()
		button.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
		// What a screen reader announces for the action, besides the role.
		button.assert(
			SemanticsMatcher("announces what it does") { node ->
				node.config.getOrNull(SemanticsActions.OnClick)?.label == "Ver las evaluaciones de la semana"
			}
		)

		button.performClick()

		assertEquals(listOf("ci5311-exam"), asked)
	}

	@Test
	fun when_noEvaluationIsGiven_then_theBoxIsNotAButton_andATapAsksForNothing() = runTuIndiceUiTest {
		val asked = mutableListOf<String>()

		setTuIndiceTestContent {
			Box(
				modifier = Modifier
					.size(40.dp)
					.testTag(TestTag)
					.scheduleEvaluationButton(
						shape = RectangleShape,
						evaluationId = null,
						onClick = { evaluationId -> asked += evaluationId }
					)
			)
		}

		onNodeWithTag(TestTag).assertHasNoClickAction()

		onNodeWithTag(TestTag).performClick()

		assertEquals(emptyList(), asked)
	}

	private companion object {
		const val TestTag = "scheduleEvaluationButton"
	}
}
