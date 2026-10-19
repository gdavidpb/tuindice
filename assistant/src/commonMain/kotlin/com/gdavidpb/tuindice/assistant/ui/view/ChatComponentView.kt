package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.AcademicCardComponentRenderer
import com.gdavidpb.tuindice.assistant.ui.component.RecordCardComponentRenderer
import com.gdavidpb.tuindice.assistant.ui.component.nav.OpenScreenComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.CooldownComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.StillAppliesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.SupportComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.SuspensionComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.UsageNoticeComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.WarningComponentView
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.AttentionComponent
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.CitationComponent
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay
import com.gdavidpb.tuindice.chatcontract.component.CooldownComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.HelpComponent
import com.gdavidpb.tuindice.chatcontract.component.IndexGoalComponent
import com.gdavidpb.tuindice.chatcontract.component.IndexSeriesComponent
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
import com.gdavidpb.tuindice.chatcontract.component.OpenScreenComponent
import com.gdavidpb.tuindice.chatcontract.component.PaceComponent
import com.gdavidpb.tuindice.chatcontract.component.PensumComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PensumProgressComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanEvaluationComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanListComponent
import com.gdavidpb.tuindice.chatcontract.component.ProfileSummaryComponent
import com.gdavidpb.tuindice.chatcontract.component.RecommendedElectivesComponent
import com.gdavidpb.tuindice.chatcontract.component.RecordStatsComponent
import com.gdavidpb.tuindice.chatcontract.component.RequiredGradeComponent
import com.gdavidpb.tuindice.chatcontract.component.RequirementsComponent
import com.gdavidpb.tuindice.chatcontract.component.SimulationComponent
import com.gdavidpb.tuindice.chatcontract.component.StillAppliesComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectImpactComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectListComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectStatsComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportComponent
import com.gdavidpb.tuindice.chatcontract.component.SuspensionComponent
import com.gdavidpb.tuindice.chatcontract.component.SyncStatusComponent
import com.gdavidpb.tuindice.chatcontract.component.TermComponent
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.component.UsageNoticeComponent
import com.gdavidpb.tuindice.chatcontract.component.WarningComponent
import com.gdavidpb.tuindice.chatcontract.component.WeekScheduleComponent
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent

// One component of an agent message. A SHEET component adds its «Ver detalle» row, except on its own sheet, where
// [showsDetailRow] is false. Every catalog this build supports has its renderers (sistema, nav, perfil, informe,
// horario, resumen, evaluaciones, materias, pensum, plan and conocimiento); only a component this build does not
// know falls back to the text it carries.
@Composable
fun ChatComponentView(
	messageId: String,
	component: ChatComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier,
	showsDetailRow: Boolean = true
) {
	Column(
		modifier = modifier.fillMaxWidth().testTag(AssistantUiTags.Component.root(component.id)),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		ComponentRenderer(component = component, onAction = onAction)

		if (showsDetailRow && component.display == ComponentDisplay.SHEET) {
			ChatComponentDetailRowView(
				messageId = messageId,
				componentId = component.id,
				onClick = { onAction(AssistantChat.Action.OpenComponentSheet(messageId, component.id)) }
			)
		}
	}
}

// Exhaustive on purpose: a new ChatComponent subclass fails to compile until it has a branch here.
@Composable
private fun ComponentRenderer(
	component: ChatComponent,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (component) {
		is StillAppliesComponent -> StillAppliesComponentView(component = component, onAction = onAction)
		is UsageNoticeComponent -> UsageNoticeComponentView(component = component)
		is CooldownComponent -> CooldownComponentView(component = component, onAction = onAction)
		is SuspensionComponent -> SuspensionComponentView(component = component, onAction = onAction)
		is SupportComponent -> SupportComponentView(component = component)
		is WarningComponent -> WarningComponentView(component = component)
		is OpenScreenComponent -> OpenScreenComponentView(component = component, onAction = onAction)

		// perfil/1, informe/1, horario/1 and resumen/1: their cards are drawn by RecordCardComponentRenderer
		is ProfileSummaryComponent, is SyncStatusComponent, is TermComponent, is IndexSeriesComponent,
		is RecordStatsComponent, is PaceComponent, is SimulationComponent, is IndexGoalComponent,
		is AttentionComponent, is WeekScheduleComponent, is NextClassComponent, is ClashesComponent ->
			RecordCardComponentRenderer(component = component, onAction = onAction)

		// conocimiento, evaluaciones, materias, pensum and plan: drawn by AcademicCardComponentRenderer
		is HelpComponent, is CitationComponent,
		is WeeklyLoadComponent, is EvaluationListComponent, is RequiredGradeComponent, is SubjectPointsComponent,
		is SubjectStatsComponent, is SubjectListComponent, is RecommendedElectivesComponent,
		is PensumProgressComponent, is PensumComparisonComponent, is SubjectImpactComponent, is RequirementsComponent,
		is PlanComparisonComponent, is PlanEvaluationComponent, is PlanListComponent ->
			AcademicCardComponentRenderer(component = component, onAction = onAction)

		// A component this build does not know shows the text the backend sent with it.
		is UnknownChatComponent -> ChatFallbackComponentView(fallbackText = component.fallbackText)
	}
}
