package com.gdavidpb.tuindice.assistant.ui.component

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.component.plan.PlanComparisonComponentView
import com.gdavidpb.tuindice.assistant.ui.component.plan.PlanEvaluationComponentView
import com.gdavidpb.tuindice.assistant.ui.component.plan.PlanListComponentView
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanEvaluationComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanListComponent

// Draws the cards of the plan catalog. ChatComponentView sends this function only the types of its plan branch.
@Composable
fun AcademicCardComponentRenderer(
	component: ChatComponent,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (component) {
		is PlanListComponent -> PlanListComponentView(component = component, onAction = onAction)
		is PlanEvaluationComponent -> PlanEvaluationComponentView(component = component, onAction = onAction)
		is PlanComparisonComponent -> PlanComparisonComponentView(component = component, onAction = onAction)

		// Only the types of the plan branch reach here, so no other component is drawn.
		else -> Unit
	}
}
