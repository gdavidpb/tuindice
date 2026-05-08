package com.gdavidpb.tuindice.wizard.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentReturned
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Science
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicProfile
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationCourseFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateGroup
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.asIcon
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationFilterGroupItemList
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationAttemptPickerItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationGradeSectionItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationTypePickerItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.subjects.domain.model.SubjectAttemptBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDifficultyBand
import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.wizard.presentation.contract.CURRENT_TERM_ID
import com.gdavidpb.tuindice.wizard.presentation.contract.HISTORICAL_TERM_ID

private const val CURRENT_TERM_START = 1_775_001_600_000L
private const val CURRENT_TERM_END = 1_785_456_000_000L
private const val HISTORICAL_TERM_START = 1_759_276_800_000L
private const val HISTORICAL_TERM_END = 1_766_102_400_000L
private const val SAMPLE_DATE = 1_776_902_400_000L
private const val SAMPLE_CAREER_NAME = "Ingeniería de Computación"
private const val SAMPLE_LAST_UPDATE_TEXT = "Última actualización: 28 de abril 2026"

internal fun sampleSummaryState() = Summary.State.Content(
	name = "Andrea Pérez",
	lastUpdate = SAMPLE_LAST_UPDATE_TEXT,
	careerName = SAMPLE_CAREER_NAME,
	grade = 4.2308f,
	enrolledSubjects = 4,
	enrolledCredits = 16,
	approvedSubjects = 22,
	approvedCredits = 88,
	retiredSubjects = 1,
	retiredCredits = 3,
	failedSubjects = 2,
	failedCredits = 7,
	profilePictureUrl = "",
	isProfilePictureLoading = false,
	isUserRefreshing = false
)

internal fun sampleRecordState(
	viewMode: RecordViewMode,
	selectedTermId: String
) = Record.State.Content(
	viewMode = viewMode,
	record = sampleAcademicRecord(),
	selectedTermId = selectedTermId
)

internal fun sampleSubjectDetailState(
	selectedTab: SubjectSegmentTab
) = com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail.State.Content(
	detail = SubjectDetail(
		id = "CI2611",
		name = "Algoritmos y Estructuras I",
		credits = 4,
		gradingMode = GradingMode.NUMERIC,
		generatedAt = SAMPLE_DATE,
		expiresAt = SAMPLE_DATE + 604_800_000L,
		careerSegment = sampleSubjectSegment(
			students = 842,
			attempts = 1_180,
			approvalRate = 0.72,
			difficultyScore = 68
		),
		globalSegment = sampleSubjectSegment(
			students = 1_420,
			attempts = 2_015,
			approvalRate = 0.69,
			difficultyScore = 71
		)
	),
	selectedTab = selectedTab
)

internal fun samplePensumState() = Pensum.State.Content(
	model = PensumScreenModel(
		selection = PensumScreenModel.Selection(
			careerCode = 15,
			year = 2019,
			modalityId = "degree_project"
		),
		pensumOptions = listOf(
			pensumOption(year = 2014),
			pensumOption(year = 2017),
			pensumOption(year = 2019)
		),
		modalityOptions = listOf(
			PensumScreenModel.ModalityItem(
				id = "degree_project",
				name = "Proyecto de Grado",
				isDefault = true,
				text = "Proyecto de Grado"
			),
			PensumScreenModel.ModalityItem(
				id = "project_de",
				name = "Proyecto DE",
				isDefault = false,
				text = "Proyecto DE"
			),
			PensumScreenModel.ModalityItem(
				id = "long_internship",
				name = "Pasantía Larga",
				isDefault = false,
				text = "Pasantía Larga"
			)
		),
		progressPercent = 75,
		approvedCredits = 153,
		totalCredits = 205,
		canvas = PensumScreenModel.Canvas(width = 1800.0, height = 1340.0),
		terms = (1..6).map { term ->
			PensumScreenModel.Term(
				id = "T$term",
				label = "T$term",
				x = ((term - 1) * 300).toDouble(),
				width = 300.0
			)
		},
		nodes = listOf(
			pensumNode("ma1111", "MA1111", "Matemáticas I", 4, "T1", 40.0, 150.0, PensumNodeStatus.APPROVED),
			pensumNode("lla111", "LLA111", "Lenguaje I", 3, "T1", 40.0, 350.0, PensumNodeStatus.APPROVED),
			pensumNode("csa211", "CSA211", "Venezuela ante el Siglo XXI I", 3, "T1", 40.0, 550.0, PensumNodeStatus.APPROVED),
			pensumNode("id1111", "ID1111", "Inglés I", 3, "T1", 40.0, 780.0, PensumNodeStatus.APPROVED),
			pensumNode("ma1112", "MA1112", "Matemáticas II", 4, "T2", 340.0, 150.0, PensumNodeStatus.APPROVED),
			pensumNode("lla112", "LLA112", "Lenguaje II", 3, "T2", 340.0, 350.0, PensumNodeStatus.APPROVED),
			pensumNode("ci2611", "CI2611", "Algoritmos y Estructuras I", 4, "T2", 340.0, 550.0, PensumNodeStatus.APPROVED),
			pensumNode("id1112", "ID1112", "Inglés II", 3, "T2", 340.0, 780.0, PensumNodeStatus.APPROVED),
			pensumNode("ma1113", "MA1113", "Matemáticas III", 4, "T3", 640.0, 150.0, PensumNodeStatus.APPROVED),
			pensumNode("ci3611", "CI3611", "Algoritmos y Estructuras II", 4, "T3", 640.0, 550.0, PensumNodeStatus.CURRENT),
			pensumNode("ec5344", "EC5344", "Sistemas Digitales", 4, "T4", 940.0, 350.0, PensumNodeStatus.AVAILABLE),
			pensumNode("ci4325", "CI4325", "Interfaces con el Usuario", 5, "T4", 940.0, 550.0, PensumNodeStatus.CURRENT),
			pensumNode("ea1", "EA1", "Electiva de Área I", 4, "T5", 1240.0, 350.0, PensumNodeStatus.AVAILABLE),
			pensumNode("ep5406", "EP5406", "Proyecto de Grado A", 9, "T5", 1240.0, 550.0, PensumNodeStatus.BLOCKED)
		),
		edges = listOf(
			pensumRequirementEdge("ma1111", "ma1112"),
			pensumRequirementEdge("ma1112", "ma1113"),
			pensumRequirementEdge("lla111", "lla112"),
			pensumRequirementEdge("ci2611", "ci3611"),
			pensumRequirementEdge("ci3611", "ci4325"),
			pensumRequirementEdge("ci4325", "ep5406"),
			pensumRequirementEdge("ec5344", "ep5406")
		)
	)
)

private fun pensumOption(year: Int) = PensumScreenModel.PensumOptionItem(
	id = "computacion-$year",
	careerCode = 15,
	careerName = "Computación",
	year = year,
	text = "$year - Computación"
)

private fun pensumNode(
	id: String,
	code: String,
	name: String,
	credits: Int,
	termId: String,
	x: Double,
	y: Double,
	status: PensumNodeStatus
) = PensumScreenModel.Node(
	id = id,
	displayCode = code,
	name = name,
	credits = credits,
	termId = termId,
	x = x,
	y = y,
	width = 220.0,
	height = 148.0,
	status = status
)

private fun pensumRequirementEdge(
	fromNodeId: String,
	toNodeId: String
) = PensumScreenModel.Edge(
	id = "${fromNodeId}_to_$toNodeId",
	fromNodeId = fromNodeId,
	toNodeId = toNodeId,
	relationshipType = PensumRelationshipType.REQUIREMENT,
	points = listOf(
		PensumScreenModel.Point(x = 0.0, y = 0.0),
		PensumScreenModel.Point(x = 1.0, y = 1.0)
	)
)

private fun sampleEvaluationFilters() = listOf(
	EvaluationStateFilter(
		label = "Pendientes"
	) { evaluation -> evaluation.state == EvaluationState.PENDING },
	EvaluationStateFilter(
		label = "Completadas"
	) { evaluation -> evaluation.state == EvaluationState.COMPLETED },
	EvaluationStateFilter(
		label = "Sin nota"
	) { evaluation -> evaluation.state == EvaluationState.OVERDUE },
	EvaluationCourseFilter("CI2611"),
	EvaluationCourseFilter("EC5344"),
	EvaluationCourseFilter("MA1111"),
	EvaluationDateFilter(
		group = EvaluationDateGroup.Yesterday,
		label = "Ayer"
	) { evaluation -> evaluation.date == SAMPLE_DATE },
	EvaluationDateFilter(
		group = EvaluationDateGroup.Tomorrow,
		label = "Mañana"
	) { evaluation -> evaluation.date == SAMPLE_DATE + 604_800_000L },
	EvaluationDateFilter(
		group = EvaluationDateGroup.WeeksAhead(weeks = 2),
		label = "En 2 semanas"
	) { evaluation -> evaluation.date == SAMPLE_DATE - 1_296_000_000L }
)

internal fun sampleEvaluationsState() = Evaluations.State.Content(
	filterGroups = sampleEvaluationFilters().toEvaluationFilterGroupItemList(activeFilters = emptyList()),
	evaluationGroups = listOf(
		EvaluationsGroupItem(
			title = "Jueves - 23/04/26",
			items = listOf(
				sampleEvaluationItem(
					id = "evaluation_1",
					name = "Parcial 1",
					subjectCode = "CI2611",
					typeText = "Parcial",
					dateText = "Jueves - 23/04/26",
					gradesText = "Sin nota / 100.00",
					type = EvaluationType.TEST,
					state = EvaluationState.PENDING
				),
				sampleEvaluationItem(
					id = "evaluation_2",
					name = "Taller 1",
					subjectCode = "EC5344",
					typeText = "Taller",
					dateText = "Jueves - 30/04/26",
					gradesText = "Sin nota / 20.00",
					type = EvaluationType.WORKSHOP,
					state = EvaluationState.PENDING
				)
			)
		),
		EvaluationsGroupItem(
			title = "Completadas",
			items = listOf(
				sampleEvaluationItem(
					id = "evaluation_3",
					name = "Laboratorio 1",
					subjectCode = "CI2611",
					typeText = "Laboratorio",
					dateText = "Miércoles - 08/04/26",
					gradesText = "18.00 / 20.00",
					type = EvaluationType.LABORATORY,
					state = EvaluationState.COMPLETED,
					grade = 18.0,
					maxGrade = 20.0
				)
			)
		)
	),
	activeFilters = emptyList()
)

internal fun sampleEvaluationFormState(): Evaluation.State.Content {
	val attempts = sampleEditableAttempts()
	val selectedAttempt = attempts.first()
	val selectedType = EvaluationType.TEST

	return Evaluation.State.Content(
		attemptItems = attempts.map { attempt ->
			val colors = CourseCodeColorGenerator.fromCode(attempt.code)
			EvaluationAttemptPickerItem(
				attempt = attempt,
				labelText = attempt.code,
				isSelected = attempt == selectedAttempt,
				isVisible = true,
				containerColor = colors.containerColor,
				contentColor = colors.color,
				disabledContainerColor = colors.containerColor.copy(alpha = 0.55f),
				disabledContentColor = colors.color.copy(alpha = 0.38f)
			)
		},
		selectedAttempt = selectedAttempt,
		type = selectedType,
		typeItems = listOf(
			EvaluationType.TEST,
			EvaluationType.WORKSHOP,
			EvaluationType.PROJECT,
			EvaluationType.LABORATORY
		).map { type ->
			EvaluationTypePickerItem(
				type = type,
				labelText = type.toWizardLabel(),
				icon = type.asIcon(),
				isSelected = type == selectedType,
				isVisible = true
			)
		},
		scheduleMode = EvaluationScheduleMode.DATED,
		date = SAMPLE_DATE,
		maxGrade = 100.0,
		gradeSection = EvaluationGradeSectionItem(
			maxGradeTitleText = "Nota máxima",
			overdueTitleText = "Nota",
			gradeText = "0.00",
			maxGradeText = "100.00",
			showsGradeChip = false
		)
	)
}

internal fun sampleAboutState() = About.State.Content(
	versionText = "TuIndice 6.0"
)

private fun sampleAcademicRecord() = AcademicRecord(
	id = "wizard_record",
	profile = AcademicProfile(
		userId = "wizard_user",
		email = "andrea@usb.ve",
		firstNames = "Andrea",
		lastNames = "Pérez",
		careerName = SAMPLE_CAREER_NAME
	),
	terms = listOf(
		AcademicTerm(
			id = HISTORICAL_TERM_ID,
			startAtMillis = HISTORICAL_TERM_START,
			endAtMillis = HISTORICAL_TERM_END,
			kind = TermKind.OFFICIAL_HISTORICAL,
			attempts = listOf(
				approvedAttempt("MA1111", "Matemáticas I", 5, 4),
				approvedAttempt("FS1111", "Física I", 4, 3),
				approvedAttempt("LC1111", "Lenguaje y Comunicación", 3, 5)
			)
		),
		AcademicTerm(
			id = CURRENT_TERM_ID,
			startAtMillis = CURRENT_TERM_START,
			endAtMillis = CURRENT_TERM_END,
			kind = TermKind.OFFICIAL_CURRENT,
			attempts = listOf(
				currentAttempt("CI2611", "Algoritmos y Estructuras I", 4),
				currentAttempt("EC5344", "Sistemas Digitales", 4),
				currentAttempt("EAD212", "Diseño de Interfaces", 4),
				currentAttempt("PS5315", "Gestión de Proyectos", 4)
			)
		)
	)
)

private fun approvedAttempt(
	code: String,
	name: String,
	credits: Int,
	score: Int
) = AcademicAttempt(
	id = "${code.lowercase()}_historical",
	subjectCode = code,
	subjectName = name,
	credits = credits,
	officialScore = AttemptScore.numeric(score),
	officialOutcome = AttemptOutcome.APPROVED
)

private fun currentAttempt(
	code: String,
	name: String,
	credits: Int
) = AcademicAttempt(
	id = "${code.lowercase()}_current",
	subjectCode = code,
	subjectName = name,
	credits = credits,
	officialScore = AttemptScore.empty(),
	officialOutcome = AttemptOutcome.PENDING
)

private fun sampleSubjectSegment(
	students: Int,
	attempts: Int,
	approvalRate: Double,
	difficultyScore: Int
) = SubjectStatsSegment(
	sampleStudents = students,
	closedAttempts = attempts,
	numericLatestStudents = students - 36,
	latestApprovedCount = (students * approvalRate).toInt(),
	latestFailedCount = (students * 0.18).toInt(),
	latestRetiredCount = (students * 0.07).toInt(),
	latestUnreportedCount = (students * 0.03).toInt(),
	averageGrade = 3.72,
	medianGrade = 4.0,
	stddevGrade = 0.84,
	firstAttemptPassRate = 0.61,
	approvalRate = approvalRate,
	latestFailureRate = 0.18,
	latestWithdrawalRate = 0.07,
	retakeRate = 0.24,
	avgAttemptsToPass = 1.34,
	medianAttemptsToPass = 1.0,
	difficultyScore = difficultyScore,
	difficultyBand = SubjectDifficultyBand.HIGH,
	firstClosedTermStartAt = 1_672_531_200_000L,
	lastClosedTermStartAt = HISTORICAL_TERM_END,
	latestGradeBins = listOf(
		SubjectGradeBin(1, 24),
		SubjectGradeBin(2, 58),
		SubjectGradeBin(3, 220),
		SubjectGradeBin(4, 310),
		SubjectGradeBin(5, 185)
	),
	allGradeBins = listOf(
		SubjectGradeBin(1, 88),
		SubjectGradeBin(2, 142),
		SubjectGradeBin(3, 410),
		SubjectGradeBin(4, 520),
		SubjectGradeBin(5, 275)
	),
	attemptsToPassBins = listOf(
		SubjectAttemptBin("1", 640),
		SubjectAttemptBin("2", 210),
		SubjectAttemptBin("3+", 82)
	)
)

private fun sampleEvaluationItem(
	id: String,
	name: String,
	subjectCode: String,
	typeText: String,
	dateText: String,
	gradesText: String,
	type: EvaluationType,
	state: EvaluationState,
	grade: Double? = null,
	maxGrade: Double = 100.0
): EvaluationItem {
	val colors = CourseCodeColorGenerator.fromCode(subjectCode)
	return EvaluationItem(
		evaluationId = id,
		grade = grade,
		maxGrade = maxGrade,
		nameText = name,
		subjectCodeText = subjectCode,
		subjectCodeColor = colors.color,
		subjectCodeContainerColor = colors.containerColor,
		highlightTone = when (state) {
			EvaluationState.COMPLETED -> EvaluationHighlightTone.Success
			EvaluationState.OVERDUE -> EvaluationHighlightTone.Error
			else -> EvaluationHighlightTone.Neutral
		},
		typeText = typeText,
		typeIcon = when (type) {
			EvaluationType.TEST -> Icons.Outlined.FileCopy
			EvaluationType.WORKSHOP -> Icons.Outlined.Build
			EvaluationType.LABORATORY -> Icons.Outlined.Science
			else -> type.asIcon()
		},
		dateText = dateText,
		dateIcon = if (state == EvaluationState.COMPLETED) Icons.Outlined.EventAvailable else Icons.Outlined.Event,
		gradesText = gradesText,
		gradeActionText = grade?.formatGrade(decimals = 2) ?: "Sin nota",
		showsGradeAction = state != EvaluationState.PENDING,
		gradesIcon = if (state == EvaluationState.COMPLETED)
			Icons.Outlined.AssignmentTurnedIn
		else
			Icons.Outlined.AssignmentReturned,
		isOverdue = state == EvaluationState.OVERDUE,
		isClickable = state != EvaluationState.PENDING
	)
}

private fun sampleEditableAttempts() = listOf(
	EditableAttemptDescriptor(
		id = "ci2611_current",
		termId = CURRENT_TERM_ID,
		code = "CI2611",
		name = "Algoritmos y Estructuras I",
		credits = 4,
		grade = 5
	),
	EditableAttemptDescriptor(
		id = "ec5344_current",
		termId = CURRENT_TERM_ID,
		code = "EC5344",
		name = "Sistemas Digitales",
		credits = 4,
		grade = 5
	)
)

private fun EvaluationType.toWizardLabel(): String = when (this) {
	EvaluationType.TEST -> "Parcial"
	EvaluationType.WORKSHOP -> "Taller"
	EvaluationType.PROJECT -> "Proyecto"
	EvaluationType.LABORATORY -> "Laboratorio"
	else -> name.lowercase()
}
