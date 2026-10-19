package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AcademicComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.CitationItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationListItem
import com.gdavidpb.tuindice.assistant.presentation.model.HelpItem
import com.gdavidpb.tuindice.assistant.presentation.model.RecommendedElectivesItem
import com.gdavidpb.tuindice.assistant.presentation.model.RequiredGradeItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectListItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectPointsItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectStatsItem
import com.gdavidpb.tuindice.assistant.presentation.model.WeeklyLoadItem
import com.gdavidpb.tuindice.assistant.ui.component.conocimiento.CitationComponentView
import com.gdavidpb.tuindice.assistant.ui.component.conocimiento.HelpComponentView
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.EvaluationListComponentView
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.RequiredGradeComponentView
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.SubjectPointsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.WeeklyLoadComponentView
import com.gdavidpb.tuindice.assistant.ui.component.materias.RecommendedElectivesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.materias.SubjectListComponentView
import com.gdavidpb.tuindice.assistant.ui.component.materias.SubjectStatsComponentView

// Academic cards: the components of the conocimiento, evaluaciones and materias catalogs.
@Composable
internal fun AcademicComponentRenderer(
	item: AcademicComponentItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (item) {
		is HelpItem -> HelpComponentView(item = item, onAction = onAction)
		is EvaluationListItem -> EvaluationListComponentView(item = item, onAction = onAction)
		is SubjectPointsItem -> SubjectPointsComponentView(item = item, onAction = onAction)
		is WeeklyLoadItem -> WeeklyLoadComponentView(item = item, onAction = onAction)
		is RequiredGradeItem -> RequiredGradeComponentView(item = item, onAction = onAction)
		is SubjectListItem -> SubjectListComponentView(item = item, onAction = onAction)
		is SubjectStatsItem -> SubjectStatsComponentView(item = item, onAction = onAction)
		is RecommendedElectivesItem -> RecommendedElectivesComponentView(item = item, onAction = onAction)
		is CitationItem -> CitationComponentView(item = item, onAction = onAction)
	}
}
