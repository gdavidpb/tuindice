package com.gdavidpb.tuindice.assistant.ui.component

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.EvaluationListComponentView
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.RequiredGradeComponentView
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.SubjectPointsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.WeeklyLoadComponentView
import com.gdavidpb.tuindice.assistant.ui.component.materias.RecommendedElectivesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.materias.SubjectListComponentView
import com.gdavidpb.tuindice.assistant.ui.component.materias.SubjectStatsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.pensum.PensumComparisonComponentView
import com.gdavidpb.tuindice.assistant.ui.component.pensum.PensumProgressComponentView
import com.gdavidpb.tuindice.assistant.ui.component.pensum.RequirementsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.pensum.SubjectImpactComponentView
import com.gdavidpb.tuindice.assistant.ui.component.plan.PlanComparisonComponentView
import com.gdavidpb.tuindice.assistant.ui.component.plan.PlanEvaluationComponentView
import com.gdavidpb.tuindice.assistant.ui.component.plan.PlanListComponentView
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.PensumComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PensumProgressComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanEvaluationComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanListComponent
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElectivesComponent
import com.gdavidpb.tuindice.chatcontract.component.RequiredGradeComponent
import com.gdavidpb.tuindice.chatcontract.component.RequirementsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectImpactComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectListComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectStatsComponent
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent

// Draws the cards of the evaluaciones, pensum, materias, plan and conocimiento catalogs. ChatComponentView sends this
// function only the types of its academic branch. The cards are split in two halves, so each `when` stays small.
@Composable
fun AcademicCardComponentRenderer(
	component: ChatComponent,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (component) {
		is EvaluationListComponent -> EvaluationListComponentView(component = component, onAction = onAction)
		is SubjectPointsComponent -> SubjectPointsComponentView(component = component, onAction = onAction)
		is WeeklyLoadComponent -> WeeklyLoadComponentView(component = component, onAction = onAction)
		is RequiredGradeComponent -> RequiredGradeComponentView(component = component, onAction = onAction)
		is PensumProgressComponent -> PensumProgressComponentView(component = component, onAction = onAction)
		is RequirementsComponent -> RequirementsComponentView(component = component, onAction = onAction)
		is SubjectImpactComponent -> SubjectImpactComponentView(component = component, onAction = onAction)
		is PensumComparisonComponent -> PensumComparisonComponentView(component = component, onAction = onAction)

		// materias/1 and plan/1: drawn by the second half.
		is SubjectListComponent, is SubjectStatsComponent, is RecommendedElectivesComponent,
		is PlanListComponent, is PlanEvaluationComponent, is PlanComparisonComponent ->
			SubjectPlanCardComponentRenderer(component = component, onAction = onAction)

		// Only the types of the academic branch reach here, so no other component is drawn.
		else -> Unit
	}
}

@Composable
private fun SubjectPlanCardComponentRenderer(
	component: ChatComponent,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (component) {
		is SubjectListComponent -> SubjectListComponentView(component = component, onAction = onAction)
		is SubjectStatsComponent -> SubjectStatsComponentView(component = component, onAction = onAction)
		is RecommendedElectivesComponent -> RecommendedElectivesComponentView(component = component, onAction = onAction)
		is PlanListComponent -> PlanListComponentView(component = component, onAction = onAction)
		is PlanEvaluationComponent -> PlanEvaluationComponentView(component = component, onAction = onAction)
		is PlanComparisonComponent -> PlanComparisonComponentView(component = component, onAction = onAction)

		// Only the types of the second half reach here, so no other component is drawn.
		else -> Unit
	}
}
