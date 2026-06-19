package com.gdavidpb.tuindice.wizard.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.presentation.model.WizardStepId
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
				state = Wizard.State.Content().goTo(WizardStepId.Summary),
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
		onNodeWithText("Última sincronización: 28 de abril 2026").assertExists()
		onAllNodesWithText("Sincronizado hace 2 min").assertCountEquals(0)
		onNodeWithText("Resumen académico").assertExists()
		onNodeWithText("Paso 1 de 11").assertExists()
		assertNodeVisible(WizardUiTags.BackButton)
		assertNodeVisible(WizardUiTags.SkipButton)
		assertNodeVisible(WizardUiTags.PrimaryButton)
		onAllNodesWithTag(WizardUiTags.FocusOverlay).assertCountEquals(0)
	}

	@Test
	fun when_recordStepIsRendered_then_focusesTermGrades() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content().goTo(WizardStepId.Record),
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
		onNodeWithText("Paso 2 de 11").assertExists()
		onNodeWithText(
			"Este bloque resume el trimestre seleccionado: índice del trimestre, índice acumulado y cantidad de créditos.",
			substring = true
		).assertExists()
		assertNodeVisible(WizardUiTags.FocusOverlay)
	}

	@Test
	fun when_recordActionsStepIsRendered_then_explainsTermSelectionAndFloatingActions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content().goTo(WizardStepId.RecordActions),
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
		onNodeWithText("Paso 3 de 11").assertExists()
		onNodeWithText("Calendario y acciones").assertExists()
		onNodeWithText(
			"El botón de calendario abre todos los trimestres",
			substring = true
		).assertExists()
		onNodeWithText(
			"el botón de modo alterna entre Histórico y Proyección",
			substring = true
		).assertExists()
		assertNodeHidden(BaseUiTags.topBarActionButton(TopBarAction.FetchEnrollmentProofAction))
		assertNodeVisible(WizardUiTags.FocusOverlay)
	}

	@Test
	fun when_recordTermSelectionIsVisible_then_showsSheetAndDismissesFromUi() = runTuIndiceUiTest {
		var dismissCalls = 0

		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content(
					isRecordTermSelectionVisible = true
				).goTo(WizardStepId.RecordActions),
				onBack = {},
				onSkip = {},
				onNext = {},
				onFinish = {},
				onOpenSubjectDetail = {},
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {},
				onDismissRecordTermSelection = { dismissCalls++ }
			)
		}

		assertNodeVisible(RecordUiTags.TermSelectionSheet)
		assertNodeVisible(RecordUiTags.TermSelectionList)
		assertNodeVisible(RecordUiTags.termSelectionOption("2026-2"))
		onNodeWithTag(RecordUiTags.termSelectionOption("2026-2")).performClick()
		assertEquals(1, dismissCalls)
	}

	@Test
	fun when_createSyntheticTermStepIsRendered_then_showsCreationScreenContext() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content().goTo(WizardStepId.CreateSyntheticTerm),
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

		onNodeWithText("Planificar trimestre").assertExists()
		onNodeWithText("Paso 4 de 11").assertExists()
		onNodeWithText("Jul - Ago 2026").assertExists()
		onNodeWithText("0 materias seleccionadas").assertExists()
		onNodeWithText("Sugeridas por tu pensum").assertExists()
		onNodeWithText("EP1308").assertExists()
		onNodeWithText("EP5855").assertExists()
		onNodeWithText(
			"Aquí eliges el periodo, agregas materias sugeridas",
			substring = true
		).assertExists()
		onNodeWithText(
			"Al guardar, queda como proyección y no cambia tu historial académico.",
			substring = true
		).assertExists()
		assertNodeVisible(RecordUiTags.CreateSyntheticTermScreen)
		onAllNodesWithTag(WizardUiTags.FocusOverlay).assertCountEquals(0)
	}

	@Test
	fun when_pensumStepIsRendered_then_showsGraphProgressAndOpensStats() = runTuIndiceUiTest {
		var didOpenSubjectDetail = false

		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content().goTo(WizardStepId.Pensum),
				onBack = {},
				onSkip = {},
				onNext = {},
				onFinish = {},
				onOpenSubjectDetail = { didOpenSubjectDetail = true },
				onOpenEvaluationForm = {},
				onSubjectTabSelected = {},
				onSelectedTermChange = {},
				onSubjectChartsVisibilityChange = {}
			)
		}

		onNodeWithText("Mapa del pensum").assertExists()
		onNodeWithText("Paso 5 de 11").assertExists()
		onNodeWithText("75% avance").assertExists()
		onNodeWithText("Ingeniería de Computación").assertExists()
		onNodeWithText(
			"Toca una materia para enfocar sus requisitos,",
			substring = true
		).assertExists()
		onNodeWithText(
			"abrir su detalle.",
			substring = true
		).assertExists()
		assertNodeVisible(PensumUiTags.PensumScreen)
		onNodeWithTag(PensumUiTags.node("ci4325")).assertExists()
		onNodeWithTag(PensumUiTags.node("ma1111")).performClick()
		onNodeWithTag(PensumUiTags.focusedNode("ma1111"), useUnmergedTree = true).assertExists()
		assertNodeVisible(PensumUiTags.SubjectDetailSheet)
		onNodeWithTag(PensumUiTags.SubjectDetailTermValue).assertExists()
		onNodeWithText("Ver estadísticas").performClick()
		assertEquals(true, didOpenSubjectDetail)
		onAllNodesWithTag(WizardUiTags.FocusOverlay).assertCountEquals(0)
	}

	@Test
	fun when_subjectDetailStepIsRendered_then_explainsHeaderAndIndicators() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content().goTo(WizardStepId.SubjectDetail),
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
		onNodeWithText("Paso 6 de 11").assertExists()
		onNodeWithText("Algoritmos y Estructuras I").assertExists()
		onNodeWithText(
			"Aquí ves nombre, código y UC.",
			substring = true
		).assertExists()
		onNodeWithText(
			"Cambia entre tu carrera y general para ver dificultad, aprobación, retiros y primer intento.",
			substring = true
		).assertExists()
		onAllNodesWithTag(WizardUiTags.FocusOverlay).assertCountEquals(0)
	}

	@Test
	fun when_subjectChartsStepIsRendered_then_explainsCharts() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content().goTo(WizardStepId.SubjectCharts),
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
		onNodeWithText("Paso 7 de 11").assertExists()
		onNodeWithText("Distribución de nota").assertExists()
		onNodeWithText("Intentos para aprobar").assertExists()
		onNodeWithText(
			"Los gráficos resumen cómo se distribuyeron las notas",
			substring = true
		).assertExists()
		onAllNodesWithTag(WizardUiTags.FocusOverlay).assertCountEquals(0)
	}

	@Test
	fun when_evaluationsStepIsRendered_then_showsWeeklyAgenda() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content().goTo(WizardStepId.Evaluations),
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
		onNodeWithText("Paso 8 de 11").assertExists()
		assertNodeVisible(EvaluationsUiTags.EvaluationsWeekStrip)
		onNodeWithTag(EvaluationsUiTags.evaluationsWeekChip(4)).assertExists()
		onNodeWithText(
			"Desliza entre semanas",
			substring = true
		).assertExists()
		assertNodeVisible(WizardUiTags.FocusOverlay)
	}

	@Test
	fun when_aboutStepIsRendered_then_hasNoFocusOverlay() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content().goTo(WizardStepId.About),
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
		onNodeWithText("Paso 11 de 11").assertExists()
		onAllNodesWithTag(WizardUiTags.FocusOverlay).assertCountEquals(0)
	}

	@Test
	fun when_primaryButtonIsTapped_then_invokesNextOrFinish() = runTuIndiceUiTest {
		var nextCalls = 0
		var finishCalls = 0

		setTuIndiceTestContent {
			WizardScreen(
				state = Wizard.State.Content().goTo(WizardStepId.Summary),
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
