package com.gdavidpb.tuindice.wizard.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentReturned
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Science
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicProfile
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.asIcon
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationAttemptPickerItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationGradeSectionItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationTypePickerItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumCanvasItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumDisplayLayoutDefaults
import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumEdgeRelationshipType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumModalityItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusDisplay
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusIcon
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeVisualStyle
import com.gdavidpb.tuindice.pensum.presentation.model.PensumOptionItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumPointItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSelection
import com.gdavidpb.tuindice.pensum.presentation.model.PensumSubjectDetailItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumSubjectRelationItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTermItem
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.toCreateTermSubjectItem
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.wizard.presentation.contract.CURRENT_TERM_ID
import com.gdavidpb.tuindice.wizard.presentation.contract.HISTORICAL_TERM_ID

private const val SAMPLE_DATE = 1_776_902_400_000L
private const val SAMPLE_CAREER_NAME = "Ingeniería de Computación"
private const val SAMPLE_LAST_UPDATE_TEXT = "Última actualización: 28 de abril 2026"
private const val SAMPLE_PENSUM_TERM_COUNT = 6
private const val SAMPLE_PENSUM_NODE_WIDTH = 160.0

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

internal fun sampleCreateSyntheticTermState() = CreateSyntheticTerm.State(
	periodOptions = listOf(
		SyntheticTermPeriodOption(
			periodYear = 2026,
			periodCode = AcademicTermPeriod.JUL_AUG
		),
		SyntheticTermPeriodOption(
			periodYear = 2026,
			periodCode = AcademicTermPeriod.SEP_DEC
		),
		SyntheticTermPeriodOption(
			periodYear = 2027,
			periodCode = AcademicTermPeriod.JAN_MAR
		)
	),
	selectedPeriod = SyntheticTermPeriodOption(
		periodYear = 2026,
		periodCode = AcademicTermPeriod.JUL_AUG
	),
	suggestedSubjects = listOf(
		syntheticTermSubject(
			code = "EP1308",
			name = "Organización y Sistemas"
		),
		syntheticTermSubject(
			code = "EP5855",
			name = "Innovación y Emprendimiento"
		)
	).map { subject -> subject.toCreateTermSubjectItem() }
)

internal fun sampleSubjectDetailState(
	selectedTab: SubjectSegmentTab
) = com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail.State.Content(
	detail = SubjectDetailItem(
		id = "CI2611",
		name = "Algoritmos y Estructuras I",
		creditsText = "4 UC",
		gradingModeText = null,
		generatedAtText = "Actualizado 28/4/2026",
		selectedTab = selectedTab,
		hasSegmentTabs = true,
		chartMode = SubjectDetailItem.ChartMode.NUMERIC_GRADES,
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
	)
)

internal fun samplePensumState(): Pensum.State.Content {
	val terms = (1..SAMPLE_PENSUM_TERM_COUNT).map { term ->
		PensumTermItem(
			id = "T$term",
			label = "T$term",
			x = samplePensumTermX(term),
			width = PensumDisplayLayoutDefaults.TermWidth
		)
	}
	val edges = listOf(
		pensumRequirementEdge("ma1111", "ma1112"),
		pensumRequirementEdge("ma1112", "ma1113"),
		pensumRequirementEdge("lla111", "lla112"),
		pensumRequirementEdge("ci2611", "ci3611"),
		pensumRequirementEdge("ci3611", "ci4325"),
		pensumRequirementEdge("ci4325", "ep5406", isDisconnected = true),
		pensumRequirementEdge("ec5344", "ep5406", isDisconnected = true)
	)
	val nodes = samplePensumNodes().withDetailRelations(edges)

	return Pensum.State.Content(
		model = PensumScreenModel(
			careerName = SAMPLE_CAREER_NAME,
			selection = PensumScreenSelection(
				year = 2019,
				modalityId = "degree_project"
			),
			pensumOptions = listOf(
				pensumOption(year = 2016),
				pensumOption(year = 2017),
				pensumOption(year = 2018),
				pensumOption(year = 2019)
			),
			modalityOptions = sampleModalityOptions(),
			progressPercent = 75,
			approvedCredits = 153,
			totalCredits = 205,
			isCurrentFocusVisible = true,
			canvas = samplePensumCanvas(terms = terms, nodes = nodes),
			terms = terms,
			nodes = nodes,
			edges = edges
		)
	)
}

private fun samplePensumNodes(): List<PensumNodeItem> {
	val nextYByTerm = mutableMapOf<Int, Double>()
	return listOf(
		PensumSampleNodeSpec("ma1111", "MA1111", "Matemáticas I", 4, 1, PensumSampleNodeState.APPROVED),
		PensumSampleNodeSpec("lla111", "LLA111", "Lenguaje I", 3, 1, PensumSampleNodeState.APPROVED),
		PensumSampleNodeSpec("csa211", "CSA211", "Venezuela ante el Siglo XXI I", 3, 1, PensumSampleNodeState.APPROVED),
		PensumSampleNodeSpec("id1111", "ID1111", "Inglés I", 3, 1, PensumSampleNodeState.APPROVED),
		PensumSampleNodeSpec("ma1112", "MA1112", "Matemáticas II", 4, 2, PensumSampleNodeState.APPROVED),
		PensumSampleNodeSpec("lla112", "LLA112", "Lenguaje II", 3, 2, PensumSampleNodeState.APPROVED),
		PensumSampleNodeSpec("ci2611", "CI2611", "Algoritmos y Estructuras I", 4, 2, PensumSampleNodeState.APPROVED),
		PensumSampleNodeSpec("id1112", "ID1112", "Inglés II", 3, 2, PensumSampleNodeState.APPROVED),
		PensumSampleNodeSpec("ma1113", "MA1113", "Matemáticas III", 4, 3, PensumSampleNodeState.APPROVED),
		PensumSampleNodeSpec("ci3611", "CI3611", "Algoritmos y Estructuras II", 4, 3, PensumSampleNodeState.CURRENT),
		PensumSampleNodeSpec("ec5344", "EC5344", "Sistemas Digitales", 4, 4, PensumSampleNodeState.AVAILABLE),
		PensumSampleNodeSpec("ci4325", "CI4325", "Interfaces con el Usuario", 5, 4, PensumSampleNodeState.CURRENT),
		PensumSampleNodeSpec("ea1", "EA1", "Electiva de Área I", 4, 5, PensumSampleNodeState.AVAILABLE),
		PensumSampleNodeSpec("ep5406", "EP5406", "Proyecto de Grado A", 9, 5, PensumSampleNodeState.BLOCKED)
	).map { spec ->
		val height = spec.name.samplePensumNodeHeight()
		val y = nextYByTerm.getOrPut(spec.term) { PensumDisplayLayoutDefaults.FirstNodeTop }
		nextYByTerm[spec.term] = y + height + PensumDisplayLayoutDefaults.NodeVerticalGap
		spec.toPensumNode(y = y, height = height)
	}
}

private fun samplePensumCanvas(
	terms: List<PensumTermItem>,
	nodes: List<PensumNodeItem>
): PensumCanvasItem {
	val width = terms.maxOfOrNull { term -> term.x + term.width }.orZero() +
		PensumDisplayLayoutDefaults.CanvasRightPadding
	val height = nodes.maxOfOrNull { node -> node.y + node.height }.orZero() +
		PensumDisplayLayoutDefaults.CanvasBottomPadding

	return PensumCanvasItem(width = width, height = height)
}

private fun samplePensumTermX(term: Int): Double {
	return (term - 1) * PensumDisplayLayoutDefaults.TermWidth
}

private fun samplePensumNodeX(term: Int): Double {
	return samplePensumTermX(term) + (PensumDisplayLayoutDefaults.TermWidth - SAMPLE_PENSUM_NODE_WIDTH) / 2.0
}

private fun String.samplePensumNodeHeight(): Double {
	return if (length > PensumDisplayLayoutDefaults.NodeSingleLineNameLimit) {
		PensumDisplayLayoutDefaults.NodeMultiLineMinHeight
	} else {
		PensumDisplayLayoutDefaults.NodeSingleLineMinHeight
	}
}

private fun Double?.orZero(): Double = this ?: 0.0

private fun List<PensumNodeItem>.withDetailRelations(
	edges: List<PensumEdgeItem>
): List<PensumNodeItem> {
	val nodesById = associateBy(PensumNodeItem::id)

	return map { node ->
		val requirements = edges.incomingRelations(
			nodeId = node.id,
			relationshipType = PensumEdgeRelationshipType.REQUIREMENT,
			nodesById = nodesById
		)
		val corequisites = edges.incomingRelations(
			nodeId = node.id,
			relationshipType = PensumEdgeRelationshipType.COREQUISITE,
			nodesById = nodesById
		)
		val unlocks = edges.outgoingRelations(
			nodeId = node.id,
			nodesById = nodesById
		)

		node.copy(
			detail = node.detail.copy(
				requirements = requirements,
				corequisites = corequisites,
				unlocks = unlocks,
				blockingReasons = node.blockingReasons(
					requirements = requirements,
					corequisites = corequisites
				)
			)
		)
	}
}

private fun List<PensumEdgeItem>.incomingRelations(
	nodeId: String,
	relationshipType: PensumEdgeRelationshipType,
	nodesById: Map<String, PensumNodeItem>
): List<PensumSubjectRelationItem> {
	return filter { edge -> edge.toNodeId == nodeId && edge.relationshipType == relationshipType }
		.mapNotNull { edge -> nodesById[edge.fromNodeId]?.let { node -> edge to node } }
		.sortedByNodePosition()
		.map { (edge, node) -> node.toSubjectRelationItem(edge.relationshipType) }
}

private fun List<PensumEdgeItem>.outgoingRelations(
	nodeId: String,
	nodesById: Map<String, PensumNodeItem>
): List<PensumSubjectRelationItem> {
	return filter { edge -> edge.fromNodeId == nodeId }
		.mapNotNull { edge -> nodesById[edge.toNodeId]?.let { node -> edge to node } }
		.sortedByNodePosition()
		.map { (edge, node) -> node.toSubjectRelationItem(edge.relationshipType) }
}

private fun List<Pair<PensumEdgeItem, PensumNodeItem>>.sortedByNodePosition(): List<Pair<PensumEdgeItem, PensumNodeItem>> {
	return sortedWith(
		compareBy<Pair<PensumEdgeItem, PensumNodeItem>> { (_, node) -> node.x }
			.thenBy { (_, node) -> node.y }
			.thenBy { (_, node) -> node.displayCode }
	)
}

private fun PensumNodeItem.toSubjectRelationItem(
	relationshipType: PensumEdgeRelationshipType
): PensumSubjectRelationItem {
	return PensumSubjectRelationItem(
		nodeId = id,
		code = displayCode,
		name = displayName,
		status = status,
		visualStyle = visualStyle,
		relationshipType = relationshipType
	)
}

private fun PensumNodeItem.blockingReasons(
	requirements: List<PensumSubjectRelationItem>,
	corequisites: List<PensumSubjectRelationItem>
): List<PensumSubjectRelationItem> {
	if (!isBlocked) return emptyList()

	return requirements.filter { requirement ->
		requirement.status.type != PensumNodeStatusType.APPROVED
	} + corequisites.filter { corequisite ->
		corequisite.status.type !in setOf(PensumNodeStatusType.APPROVED, PensumNodeStatusType.CURRENT)
	}
}

private data class PensumSampleNodeSpec(
	val id: String,
	val code: String,
	val name: String,
	val credits: Int,
	val term: Int,
	val state: PensumSampleNodeState
)

private fun PensumSampleNodeSpec.toPensumNode(
	y: Double,
	height: Double
) = PensumNodeItem(
	id = id,
	displayCode = code,
	subjectCode = code,
	name = name,
	displayName = name,
	credits = credits,
	creditsText = "$credits UC",
	termId = "T$term",
	x = samplePensumNodeX(term),
	y = y,
	width = SAMPLE_PENSUM_NODE_WIDTH,
	height = height,
	visualStyle = state.toVisualStyle(),
	status = state.toStatusDisplay(),
	subjectStatsCode = code,
	fulfilledSubject = null,
	detail = PensumSubjectDetailItem(
		code = code,
		name = name,
		status = state.toStatusDisplay(),
		termLabel = "$term° trimestre",
		creditsText = "$credits UC",
		statsCode = code,
		fulfilledSubject = null
	)
)

private fun pensumOption(year: Int) = PensumOptionItem(
	id = year.toString(),
	year = year,
	modalityOptions = sampleModalityOptions(),
	text = year.toString()
)

private fun sampleModalityOptions() = listOf(
	PensumModalityItem(
		id = "degree_project",
		name = "Proyecto de Grado",
		isDefault = true,
		text = "Proyecto de Grado"
	),
	PensumModalityItem(
		id = "exclusive_degree_project",
		name = "Proyecto de Grado a Dedicación Exclusiva",
		isDefault = false,
		text = "Proyecto de Grado a Dedicación Exclusiva"
	),
	PensumModalityItem(
		id = "long_internship",
		name = "Pasantía Larga",
		isDefault = false,
		text = "Pasantía Larga"
	)
)

private fun pensumRequirementEdge(
	fromNodeId: String,
	toNodeId: String,
	isDisconnected: Boolean = false
) = PensumEdgeItem(
	id = "${fromNodeId}_to_$toNodeId",
	fromNodeId = fromNodeId,
	toNodeId = toNodeId,
	relationshipType = PensumEdgeRelationshipType.REQUIREMENT,
	isDisconnected = isDisconnected,
	points = listOf(
		PensumPointItem(x = 0.0, y = 0.0),
		PensumPointItem(x = 1.0, y = 1.0)
	)
)

private enum class PensumSampleNodeState {
	APPROVED,
	CURRENT,
	AVAILABLE,
	BLOCKED
}

private fun PensumSampleNodeState.toVisualStyle(): PensumNodeVisualStyle {
	return when (this) {
		PensumSampleNodeState.APPROVED -> PensumNodeVisualStyle(
			containerArgb = 0xFF171819,
			borderArgb = 0xFF8FE38C,
			chipArgb = 0xFFB8F4A8,
			chipTextArgb = 0xFF1D5B25,
			textArgb = 0xFFF7F7F7,
			secondaryTextArgb = 0xFF9C9EA3
		)
		PensumSampleNodeState.CURRENT -> PensumNodeVisualStyle(
			containerArgb = 0xFF171819,
			borderArgb = 0xFFFFC400,
			chipArgb = 0xFFF7E6A6,
			chipTextArgb = 0xFF5A4A00,
			textArgb = 0xFFF7F7F7,
			secondaryTextArgb = 0xFF9C9EA3
		)
		PensumSampleNodeState.AVAILABLE -> PensumNodeVisualStyle(
			containerArgb = 0xFF171819,
			borderArgb = 0xFF8A8F94,
			chipArgb = 0xFFEBDDA3,
			chipTextArgb = 0xFF534500,
			textArgb = 0xFFF7F7F7,
			secondaryTextArgb = 0xFF9C9EA3
		)
		PensumSampleNodeState.BLOCKED -> PensumNodeVisualStyle(
			containerArgb = 0xFF171819,
			borderArgb = 0xFF686B70,
			chipArgb = 0xFFB7B8BA,
			chipTextArgb = 0xFF383A3D,
			textArgb = 0xFFF7F7F7,
			secondaryTextArgb = 0xFF9C9EA3
		)
	}
}

private fun PensumSampleNodeState.toStatusDisplay(): PensumNodeStatusDisplay {
	val visualStyle = toVisualStyle()
	return when (this) {
		PensumSampleNodeState.APPROVED -> PensumNodeStatusDisplay(
			type = PensumNodeStatusType.APPROVED,
			icon = PensumNodeStatusIcon.CHECK,
			colorArgb = visualStyle.borderArgb
		)
		PensumSampleNodeState.CURRENT -> PensumNodeStatusDisplay(
			type = PensumNodeStatusType.CURRENT,
			icon = PensumNodeStatusIcon.CURRENT_ROUTE,
			colorArgb = visualStyle.borderArgb
		)
		PensumSampleNodeState.AVAILABLE -> PensumNodeStatusDisplay(
			type = PensumNodeStatusType.AVAILABLE,
			icon = PensumNodeStatusIcon.ADD,
			colorArgb = visualStyle.borderArgb
		)
		PensumSampleNodeState.BLOCKED -> PensumNodeStatusDisplay(
			type = PensumNodeStatusType.BLOCKED,
			icon = PensumNodeStatusIcon.LOCK,
			colorArgb = visualStyle.borderArgb
		)
	}
}

internal fun sampleEvaluationsState(): Evaluations.State.Content {
	val upcomingGroups = listOf(
			EvaluationsGroupItem(
				title = "Jueves 23 de Abril",
				items = listOf(
					sampleEvaluationItem(
						id = "evaluation_1",
						name = "Parcial 1",
						subjectCode = "CI2611",
						typeText = "Parcial",
						dateText = "Jueves - 23/04/26",
						gradesText = "Sin nota / 35.00",
						type = EvaluationType.TEST,
						state = EvaluationState.PENDING,
						maxGrade = 35.0
					),
					sampleEvaluationItem(
						id = "evaluation_2",
						name = "Taller 1",
						subjectCode = "EC5344",
						typeText = "Taller",
						dateText = "Jueves - 30/04/26",
						gradesText = "Sin nota / 40.00",
						type = EvaluationType.WORKSHOP,
						state = EvaluationState.PENDING,
						maxGrade = 40.0
					)
				)
			)
	)
	val historyGroups = listOf(
			EvaluationsGroupItem(
				title = "Miércoles 8 de Abril",
				items = listOf(
					sampleEvaluationItem(
						id = "evaluation_3",
						name = "Laboratorio 1",
						subjectCode = "CI2611",
						typeText = "Laboratorio",
						dateText = "Miércoles - 08/04/26",
						gradesText = "18.00 / 25.00",
						type = EvaluationType.LABORATORY,
						state = EvaluationState.COMPLETED,
						grade = 18.0,
						maxGrade = 25.0
					)
				)
		)
	)
	val evaluationGroups = upcomingGroups + historyGroups

	return Evaluations.State.Content(
		weekItem = sampleEvaluationsWeekItem(),
		weekItems = sampleEvaluationsWeekItems(),
		evaluationGroups = evaluationGroups,
		evaluationWeekGroups = listOf(sampleEvaluationsWeekGroupItem(evaluationGroups))
	)
}

private fun sampleEvaluationsWeekItems() = (1..12).map { weekNumber ->
	sampleEvaluationsWeekItem(weekNumber = weekNumber)
}

private fun sampleEvaluationsWeekItem(
	weekNumber: Int = 4
) = EvaluationsWeekItem(
	key = EvaluationsWeekKey.Academic(weekNumber),
	labelText = "Semana $weekNumber",
	days = sampleEvaluationsWeekDays(weekNumber = weekNumber)
)

private fun sampleEvaluationsWeekDays(
	weekNumber: Int
): List<EvaluationWeekDayItem> {
	val weekdayTexts = listOf("LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM")
	val startDay = 20 + ((weekNumber - 4) * 7)

	return weekdayTexts.mapIndexed { offset, weekdayText ->
		val isToday = weekNumber == 4 && offset == 3
		val hasEvaluations = weekNumber == 4 && offset in setOf(2, 3)

		EvaluationWeekDayItem(
			weekdayText = weekdayText,
			dayText = normalizedMonthDay(startDay + offset).toString(),
			isSelected = isToday,
			hasEvaluations = hasEvaluations
		)
	}
}

private fun normalizedMonthDay(day: Int): Int {
	val normalized = (day - 1).floorMod(MOCK_MONTH_DAYS)

	return normalized + 1
}

private fun Int.floorMod(other: Int): Int {
	val remainder = this % other

	return if (remainder < 0) remainder + other else remainder
}

private const val MOCK_MONTH_DAYS = 31

private fun sampleEvaluationsWeekGroupItem(
	groups: List<EvaluationsGroupItem>
) = EvaluationsWeekGroupItem(
	key = EvaluationsWeekKey.Academic(4),
	title = "Semana 4",
	groups = groups
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
				disabledContainerColor = colors.containerColor.copy(alpha = TuIndiceAlpha.Muted),
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
			maxGrade = 35.0,
			gradeSection = EvaluationGradeSectionItem(
				maxGradeTitleText = "Nota máxima",
				overdueTitleText = "Nota obtenida / máxima",
				gradeText = "0.00",
				maxGradeText = "35.00",
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
			id = "2024-3",
			periodYear = 2024,
			periodCode = AcademicTermPeriod.SEP_DEC,
			kind = TermKind.HISTORICAL,
			attempts = listOf(
				approvedAttempt("CI2511", "Lógica Simbólica", 4, 4),
				approvedAttempt("ID1111", "Inglés I", 3, 5)
			)
		),
		AcademicTerm(
			id = HISTORICAL_TERM_ID,
			periodYear = 2025,
			periodCode = AcademicTermPeriod.SEP_DEC,
			kind = TermKind.HISTORICAL,
			attempts = listOf(
				approvedAttempt("MA1111", "Matemáticas I", 5, 4),
				approvedAttempt("FS1111", "Física I", 4, 3),
				approvedAttempt("LC1111", "Lenguaje y Comunicación", 3, 5)
			)
		),
		AcademicTerm(
			id = CURRENT_TERM_ID,
			periodYear = 2026,
			periodCode = AcademicTermPeriod.JAN_MAR,
			kind = TermKind.CURRENT,
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
	academicScore = AttemptScore.numeric(score),
	academicOutcome = AttemptOutcome.APPROVED
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
	academicScore = AttemptScore.empty(),
	academicOutcome = AttemptOutcome.PENDING
)

private fun syntheticTermSubject(
	code: String,
	name: String,
	credits: Int = 3
) = SyntheticTermSubject(
	subjectCode = code,
	name = name,
	credits = credits,
	gradingMode = AttemptGradingMode.NUMERIC
)

private fun sampleSubjectSegment(
	students: Int,
	attempts: Int,
	approvalRate: Double,
	difficultyScore: Int
) = SubjectDetailItem.SegmentItem(
	studentsText = students.toString(),
	attemptsText = attempts.toString(),
	difficultyScoreText = "$difficultyScore / 100",
	difficultyBandText = "Alta",
	firstAttemptPassRateText = "61%",
	approvalRateText = "${(approvalRate * 100).toInt()}%",
	failureRateText = "18%",
	withdrawalRateText = "7%",
	latestApprovedCount = (students * approvalRate).toInt(),
	latestFailedCount = (students * 0.18).toInt(),
	latestRetiredCount = (students * 0.07).toInt(),
	latestUnreportedCount = (students * 0.03).toInt(),
	medianGrade = 4.0,
	stddevGrade = 0.84,
	latestGradeBins = listOf(
		SubjectDetailItem.GradeBinItem(1, 24),
		SubjectDetailItem.GradeBinItem(2, 58),
		SubjectDetailItem.GradeBinItem(3, 220),
		SubjectDetailItem.GradeBinItem(4, 310),
		SubjectDetailItem.GradeBinItem(5, 185)
	),
	attemptsToPassBins = listOf(
		SubjectDetailItem.AttemptBinItem("1", 640),
		SubjectDetailItem.AttemptBinItem("2", 210),
		SubjectDetailItem.AttemptBinItem("3_plus", 82)
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
		maxGrade: Double = 35.0
): EvaluationItem {
	val colors = CourseCodeColorGenerator.fromCode(subjectCode)
	val tone = when (state) {
		EvaluationState.COMPLETED -> EvaluationHighlightTone.Success
		EvaluationState.OVERDUE -> EvaluationHighlightTone.Error
		else -> EvaluationHighlightTone.Neutral
		}
		val gradeText = grade?.let {
			"${it.formatGrade(decimals = 0)} / ${maxGrade.formatGrade(decimals = 0)}"
		} ?: "-- / ${maxGrade.formatGrade(decimals = 0)}"

	return EvaluationItem(
		evaluationId = id,
		grade = grade,
		maxGrade = maxGrade,
		nameText = name,
		subjectNameText = subjectNameForCode(subjectCode),
		subjectCodeText = subjectCode,
		subjectCodeColor = colors.color,
		subjectCodeContainerColor = colors.containerColor,
		highlightTone = tone,
		statusText = when (state) {
			EvaluationState.COMPLETED -> "Completada"
			EvaluationState.OVERDUE -> "Pendiente"
			EvaluationState.CONTINUOUS -> "Continua"
			else -> "Programada"
		},
		statusTone = tone,
		typeText = typeText,
		typeNameText = name,
		typeIcon = when (type) {
			EvaluationType.TEST -> Icons.Outlined.FileCopy
			EvaluationType.WORKSHOP -> Icons.Outlined.Build
			EvaluationType.LABORATORY -> Icons.Outlined.Science
			else -> type.asIcon()
			},
			dateText = dateText,
			dateIcon = Icons.Outlined.CalendarToday,
			gradeText = gradeText,
		gradesText = gradesText,
		gradeActionText = gradeText,
		showsGradeAction = true,
		gradesIcon = if (state == EvaluationState.COMPLETED)
			Icons.Outlined.AssignmentTurnedIn
		else
			Icons.Outlined.AssignmentReturned,
		isOverdue = state == EvaluationState.OVERDUE,
		isClickable = true
	)
}

private fun subjectNameForCode(subjectCode: String): String = when (subjectCode) {
	"CI2611" -> "Algoritmos y Estructuras I"
	"EC5344" -> "Sistemas Digitales"
	else -> subjectCode
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
