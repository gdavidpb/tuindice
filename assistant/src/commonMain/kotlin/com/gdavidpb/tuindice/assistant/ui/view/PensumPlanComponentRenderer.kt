package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.PensumComparisonItem
import com.gdavidpb.tuindice.assistant.presentation.model.PensumPlanComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.PensumProgressItem
import com.gdavidpb.tuindice.assistant.presentation.model.PlanComparisonItem
import com.gdavidpb.tuindice.assistant.presentation.model.PlanEvaluationItem
import com.gdavidpb.tuindice.assistant.presentation.model.PlanListItem
import com.gdavidpb.tuindice.assistant.presentation.model.RequirementsItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectImpactItem
import com.gdavidpb.tuindice.assistant.ui.component.pensum.PensumComparisonComponentView
import com.gdavidpb.tuindice.assistant.ui.component.pensum.PensumProgressComponentView
import com.gdavidpb.tuindice.assistant.ui.component.pensum.RequirementsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.pensum.SubjectImpactComponentView
import com.gdavidpb.tuindice.assistant.ui.component.plan.PlanComparisonComponentView
import com.gdavidpb.tuindice.assistant.ui.component.plan.PlanEvaluationComponentView
import com.gdavidpb.tuindice.assistant.ui.component.plan.PlanListComponentView

// Pensum and plan cards: the components of the pensum and plan catalogs.
@Composable
internal fun PensumPlanComponentRenderer(
	item: PensumPlanComponentItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (item) {
		is PensumProgressItem -> PensumProgressComponentView(item = item, onAction = onAction)
		is RequirementsItem -> RequirementsComponentView(item = item, onAction = onAction)
		is SubjectImpactItem -> SubjectImpactComponentView(item = item, onAction = onAction)
		is PensumComparisonItem -> PensumComparisonComponentView(item = item, onAction = onAction)
		is PlanListItem -> PlanListComponentView(item = item, onAction = onAction)
		is PlanEvaluationItem -> PlanEvaluationComponentView(item = item, onAction = onAction)
		is PlanComparisonItem -> PlanComparisonComponentView(item = item, onAction = onAction)
	}
}
