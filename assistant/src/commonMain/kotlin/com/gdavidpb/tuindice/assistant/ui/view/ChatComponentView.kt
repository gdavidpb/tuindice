package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AttentionCardItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.CooldownItem
import com.gdavidpb.tuindice.assistant.presentation.model.IndexGoalItem
import com.gdavidpb.tuindice.assistant.presentation.model.IndexSeriesItem
import com.gdavidpb.tuindice.assistant.presentation.model.OpenScreenItem
import com.gdavidpb.tuindice.assistant.presentation.model.PaceItem
import com.gdavidpb.tuindice.assistant.presentation.model.ProfileSummaryItem
import com.gdavidpb.tuindice.assistant.presentation.model.RecordStatsItem
import com.gdavidpb.tuindice.assistant.presentation.model.SimulationItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.assistant.presentation.model.SupportItem
import com.gdavidpb.tuindice.assistant.presentation.model.SuspensionItem
import com.gdavidpb.tuindice.assistant.presentation.model.SyncStatusItem
import com.gdavidpb.tuindice.assistant.presentation.model.TermItem
import com.gdavidpb.tuindice.assistant.presentation.model.UsageNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.WarningItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.AcademicCardComponentRenderer
import com.gdavidpb.tuindice.assistant.ui.component.RecordCardComponentRenderer
import com.gdavidpb.tuindice.assistant.ui.component.informe.IndexGoalComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.IndexSeriesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.PaceComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.RecordStatsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.SimulationComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.TermComponentView
import com.gdavidpb.tuindice.assistant.ui.component.nav.OpenScreenComponentView
import com.gdavidpb.tuindice.assistant.ui.component.perfil.ProfileSummaryComponentView
import com.gdavidpb.tuindice.assistant.ui.component.perfil.SyncStatusComponentView
import com.gdavidpb.tuindice.assistant.ui.component.resumen.AttentionComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.CooldownComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.StillAppliesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.SupportComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.SuspensionComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.UsageNoticeComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.WarningComponentView
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.CitationComponent
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import com.gdavidpb.tuindice.chatcontract.component.EvaluationListComponent
import com.gdavidpb.tuindice.chatcontract.component.HelpComponent
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
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
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.component.WeekScheduleComponent
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent

// One component of an agent message. A component that opens on a sheet adds its «Ver detalle» row, except on its own
// sheet, where [showsDetailRow] is false.
@Composable
fun ChatComponentView(
	messageId: String,
	item: ChatComponentItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier,
	showsDetailRow: Boolean = true
) {
	Column(
		modifier = modifier.fillMaxWidth().testTag(AssistantUiTags.Component.root(item.id)),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		ComponentRenderer(item = item, onAction = onAction)

		if (showsDetailRow && item.opensSheet) {
			ChatComponentDetailRowView(
				messageId = messageId,
				componentId = item.id,
				onClick = { onAction(AssistantChat.Action.OpenComponentSheet(messageId, item.id)) }
			)
		}
	}
}

// Exhaustive on purpose: a new item fails to compile until it has a branch here.
@Composable
private fun ComponentRenderer(
	item: ChatComponentItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (item) {
		is StillAppliesItem -> StillAppliesComponentView(item = item, onAction = onAction)
		is ProfileSummaryItem -> ProfileSummaryComponentView(item = item)
		is TermItem -> TermComponentView(item = item, onAction = onAction)
		is IndexSeriesItem -> IndexSeriesComponentView(item = item)
		is RecordStatsItem -> RecordStatsComponentView(item = item)
		is PaceItem -> PaceComponentView(item = item)
		is SimulationItem -> SimulationComponentView(item = item)
		is IndexGoalItem -> IndexGoalComponentView(item = item)
		is SyncStatusItem -> SyncStatusComponentView(item = item)
		is AttentionCardItem -> AttentionComponentView(item = item, onAction = onAction)
		is UsageNoticeItem -> UsageNoticeComponentView(item = item)
		is CooldownItem -> CooldownComponentView(item = item, onAction = onAction)
		is SuspensionItem -> SuspensionComponentView(item = item, onAction = onAction)
		is SupportItem -> SupportComponentView(item = item)
		is WarningItem -> WarningComponentView(item = item)
		is OpenScreenItem -> OpenScreenComponentView(item = item, onAction = onAction)

		is ChatComponentItem.Unmigrated -> WireComponentRenderer(component = item.component, onAction = onAction)
	}
}

// The renderers of the catalogs that still take their wire components. Each one leaves this function when its catalog
// has its items.
@Composable
private fun WireComponentRenderer(
	component: ChatComponent,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (component) {
		// horario/1: its cards are drawn by RecordCardComponentRenderer
		is WeekScheduleComponent, is NextClassComponent, is ClashesComponent ->
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

		// The catalogs ported to items are drawn from their items, so none of their components arrives here.
		else -> Unit
	}
}
