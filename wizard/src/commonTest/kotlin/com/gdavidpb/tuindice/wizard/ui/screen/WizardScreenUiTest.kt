package com.gdavidpb.tuindice.wizard.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.ui.WizardUiTags
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class WizardScreenUiTest {
	@Test
	fun when_welcomeStepIsRendered_then_showsContextAndActions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content(),
				onBack = {},
				onSkip = {},
				onNext = {},
				onFinish = {},
				onOpenSubjectDetail = {},
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {}
			)
		}

		onNodeWithText("Bienvenido a TuIndice").assertExists()
		onNodeWithText("Comenzar recorrido").assertExists()
		onNodeWithText("Omitir").assertExists()
		assertNodeVisible(WizardUiTags.WelcomeScreen)
	}

	@Test
	fun when_summaryStepIsRendered_then_showsRealSummaryAndGuideBar() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content(currentIndex = 1),
				onBack = {},
				onSkip = {},
				onNext = {},
				onFinish = {},
				onOpenSubjectDetail = {},
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {}
			)
		}

		onNodeWithText("Andrea Pérez").assertExists()
		onNodeWithText("Ingeniería de Computación").assertExists()
		onNodeWithText("Última actualización: 28 de abril 2026").assertExists()
		onAllNodesWithText("Sincronizado hace 2 min").assertCountEquals(0)
		onNodeWithText("Resumen académico").assertExists()
		onNodeWithText("Paso 1 de 9").assertExists()
		assertNodeVisible(WizardUiTags.BackButton)
		assertNodeVisible(WizardUiTags.SkipButton)
		assertNodeVisible(WizardUiTags.PrimaryButton)
		onAllNodesWithTag(WizardUiTags.FocusOverlay).assertCountEquals(0)
	}

	@Test
	fun when_recordStepIsRendered_then_focusesTermGrades() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content(currentIndex = 3),
				onBack = {},
				onSkip = {},
				onNext = {},
				onFinish = {},
				onOpenSubjectDetail = {},
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {}
			)
		}

		onAllNodesWithText("Índices del trimestre").assertCountEquals(2)
		onNodeWithText("Paso 3 de 9").assertExists()
		onNodeWithText(
			"Este bloque resume el trimestre seleccionado: índice del trimestre, índice acumulado y cantidad de créditos.",
			substring = true
		).assertExists()
		assertNodeVisible(WizardUiTags.FocusOverlay)
	}

	@Test
	fun when_recordActionsStepIsRendered_then_explainsTopButtons() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content(currentIndex = 2),
				onBack = {},
				onSkip = {},
				onNext = {},
				onFinish = {},
				onOpenSubjectDetail = {},
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {}
			)
		}

		onNodeWithText("Acciones del trimestre").assertExists()
		onNodeWithText("Paso 2 de 9").assertExists()
		onNodeWithText("Botones superiores").assertExists()
		onNodeWithText(
			"El botón de modo alterna entre Modo Universidad y Modo Proyección.",
			substring = true
		).assertExists()
		onNodeWithText(
			"Cuando selecciones el trimestre actual, verás el botón de comprobante para descargarlo.",
			substring = true
		).assertExists()
		assertNodeVisible(WizardUiTags.FocusOverlay)
	}

	@Test
	fun when_recordSubjectEntryStepIsRendered_then_explainsHowToOpenStats() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content(currentIndex = 4),
				onBack = {},
				onSkip = {},
				onNext = {},
				onFinish = {},
				onOpenSubjectDetail = {},
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {}
			)
		}

		onNodeWithText("Abrir estadísticas de materia").assertExists()
		onNodeWithText("Paso 4 de 9").assertExists()
		onNodeWithText(
			"Puedes tocar el código de cualquier materia para abrir sus estadísticas",
			substring = true
		).assertExists()
		onAllNodesWithTag(WizardUiTags.FocusOverlay).assertCountEquals(0)
	}

	@Test
	fun when_subjectDetailStepIsRendered_then_explainsHeaderAndIndicators() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content(currentIndex = 5),
				onBack = {},
				onSkip = {},
				onNext = {},
				onFinish = {},
				onOpenSubjectDetail = {},
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {}
			)
		}

		onNodeWithText("Estadísticas de materia").assertExists()
		onNodeWithText("Paso 5 de 9").assertExists()
		onNodeWithText("Algoritmos y Estructuras I").assertExists()
		onNodeWithText(
			"Arriba ves el nombre de la materia, su código y UC.",
			substring = true
		).assertExists()
		onNodeWithText(
			"indicadores de dificultad, aprobación, reprobación, retiros y primer intento.",
			substring = true
		).assertExists()
		onAllNodesWithTag(WizardUiTags.FocusOverlay).assertCountEquals(0)
	}

	@Test
	fun when_subjectChartsStepIsRendered_then_explainsCharts() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content(currentIndex = 6),
				onBack = {},
				onSkip = {},
				onNext = {},
				onFinish = {},
				onOpenSubjectDetail = {},
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {}
			)
		}

		onNodeWithText("Gráficos de materia").assertExists()
		onNodeWithText("Paso 6 de 9").assertExists()
		onNodeWithText("Distribución de nota").assertExists()
		onNodeWithText("Intentos para aprobar").assertExists()
		onNodeWithText(
			"Los gráficos resumen cómo se distribuyeron las notas",
			substring = true
		).assertExists()
		onAllNodesWithTag(WizardUiTags.FocusOverlay).assertCountEquals(0)
	}

	@Test
	fun when_evaluationsStepIsRendered_then_showsStateSubjectAndDateFilters() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content(currentIndex = 7),
				onBack = {},
				onSkip = {},
				onNext = {},
				onFinish = {},
				onOpenSubjectDetail = {},
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {}
			)
		}

		onNodeWithText("Evaluaciones").assertExists()
		onNodeWithText("Paso 7 de 9").assertExists()
		onNodeWithTag(EvaluationsUiTags.filterChip("Pendientes")).assertExists()
		onNodeWithTag(EvaluationsUiTags.filterChip("CI2611")).assertExists()
		onNodeWithTag(EvaluationsUiTags.filterChip("Mañana")).assertExists()
		onNodeWithText(
			"Puedes filtrar por estado, materia y fecha",
			substring = true
		).assertExists()
		assertNodeVisible(WizardUiTags.FocusOverlay)
	}

	@Test
	fun when_aboutStepIsRendered_then_hasNoFocusOverlay() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content(currentIndex = 9),
				onBack = {},
				onSkip = {},
				onNext = {},
				onFinish = {},
				onOpenSubjectDetail = {},
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {}
			)
		}

		onNodeWithText("Ayuda e información").assertExists()
		onNodeWithText("Paso 9 de 9").assertExists()
		onAllNodesWithTag(WizardUiTags.FocusOverlay).assertCountEquals(0)
	}

	@Test
	fun when_primaryButtonIsTapped_then_invokesNextOrFinish() = runTuIndiceUiTest {
		var nextCalls = 0
		var finishCalls = 0

		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content(currentIndex = 1),
				onBack = {},
				onSkip = {},
				onNext = { nextCalls++ },
				onFinish = { finishCalls++ },
				onOpenSubjectDetail = {},
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {}
			)
		}

		onNodeWithTag(WizardUiTags.PrimaryButton).performClick()

		assertEquals(1, nextCalls)
		assertEquals(0, finishCalls)
	}
}
