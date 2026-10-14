package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardIcon
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryCardViewUiTest {
	@Test
	fun when_theCardHasATarget_then_tappingItAsksForIt() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()
		val target = SummaryHubTarget.EvaluationsWeek(weekStart = LocalDate(2026, 10, 26), evaluationId = "e1")

		setTuIndiceTestContent {
			SummaryCardView(
				item = SummaryCardItem(
					icon = SummaryCardIcon.Done,
					titleText = UiText.Raw("Nada pendiente esta semana"),
					messageText = UiText.Raw("Próxima: Parcial 2 de CI5437 · lun 26"),
					target = target,
					targetLabel = UiText.Raw("Ver las evaluaciones de esa semana")
				),
				actions = SummaryHubActions(onTargetClick = { tapped -> targets += tapped }),
				modifier = Modifier.testTag("card")
			)
		}

		onNodeWithText("Próxima: Parcial 2 de CI5437 · lun 26").assertIsDisplayed()
		onNodeWithTag("card").assertHasClickAction()
		onNodeWithTag("card").performClick()
		assertEquals(listOf<SummaryHubTarget>(target), targets)
	}

	@Test
	fun when_theActionIsTapped_then_itAsksForItsTargetAndTheCardLeadsNowhere() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryCardView(
				item = SummaryCardItem(
					icon = SummaryCardIcon.Evaluations,
					titleText = UiText.Raw("Agrega tus evaluaciones"),
					messageText = UiText.Raw("Parciales, quices y proyectos: los verás aquí y llevarás tus puntos."),
					action = SummaryCardItem.Action(
						text = UiText.Raw("Agregar evaluación"),
						target = SummaryHubTarget.AddEvaluation
					)
				),
				actions = SummaryHubActions(onTargetClick = { tapped -> targets += tapped }),
				modifier = Modifier.testTag("card")
			)
		}

		onNodeWithTag("card").assertHasNoClickAction()
		onNodeWithTag(SummaryUiTags.CardAction).performClick()
		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.AddEvaluation), targets)
	}

	@Test
	fun when_itHasNoTarget_then_itIsInformativeAndAsksForNothing() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryCardView(
				item = SummaryCardItem(
					icon = SummaryCardIcon.Calendar,
					titleText = UiText.Raw("Sin clases esta semana"),
					messageText = UiText.Raw("Cuando inscribas, aquí verás tus clases y evaluaciones.")
				),
				actions = SummaryHubActions(onTargetClick = { tapped -> targets += tapped }),
				modifier = Modifier.testTag("card")
			)
		}

		onNodeWithTag("card").assertHasNoClickAction()
		onNodeWithText("Sin clases esta semana").assertIsDisplayed()
		assertEquals(emptyList(), targets)
	}

	@Test
	fun when_itHasAFooter_then_thatLineSitsUnderTheText() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryCardView(
				item = SummaryCardItem(
					icon = SummaryCardIcon.Career,
					titleText = UiText.Raw("Mira cuánto llevas"),
					messageText = UiText.Raw("Abre tu pensum para calcular tu avance.")
				),
				actions = SummaryHubActions(),
				footer = { Text("footer line") }
			)
		}

		onNodeWithText("Mira cuánto llevas").assertIsDisplayed()
		onNodeWithText("footer line").assertIsDisplayed()
	}
}
