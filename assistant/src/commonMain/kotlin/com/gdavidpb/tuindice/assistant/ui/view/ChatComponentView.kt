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
import com.gdavidpb.tuindice.assistant.presentation.model.CitationItem
import com.gdavidpb.tuindice.assistant.presentation.model.ClashesItem
import com.gdavidpb.tuindice.assistant.presentation.model.CooldownItem
import com.gdavidpb.tuindice.assistant.presentation.model.EvaluationListItem
import com.gdavidpb.tuindice.assistant.presentation.model.HelpItem
import com.gdavidpb.tuindice.assistant.presentation.model.IndexGoalItem
import com.gdavidpb.tuindice.assistant.presentation.model.IndexSeriesItem
import com.gdavidpb.tuindice.assistant.presentation.model.NextClassItem
import com.gdavidpb.tuindice.assistant.presentation.model.OpenScreenItem
import com.gdavidpb.tuindice.assistant.presentation.model.PaceItem
import com.gdavidpb.tuindice.assistant.presentation.model.PensumComparisonItem
import com.gdavidpb.tuindice.assistant.presentation.model.PensumProgressItem
import com.gdavidpb.tuindice.assistant.presentation.model.ProfileSummaryItem
import com.gdavidpb.tuindice.assistant.presentation.model.RecommendedElectivesItem
import com.gdavidpb.tuindice.assistant.presentation.model.RecordStatsItem
import com.gdavidpb.tuindice.assistant.presentation.model.RequiredGradeItem
import com.gdavidpb.tuindice.assistant.presentation.model.RequirementsItem
import com.gdavidpb.tuindice.assistant.presentation.model.SimulationItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectImpactItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectListItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectPointsItem
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectStatsItem
import com.gdavidpb.tuindice.assistant.presentation.model.SupportItem
import com.gdavidpb.tuindice.assistant.presentation.model.SuspensionItem
import com.gdavidpb.tuindice.assistant.presentation.model.SyncStatusItem
import com.gdavidpb.tuindice.assistant.presentation.model.TermItem
import com.gdavidpb.tuindice.assistant.presentation.model.UsageNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.WarningItem
import com.gdavidpb.tuindice.assistant.presentation.model.WeekScheduleItem
import com.gdavidpb.tuindice.assistant.presentation.model.WeeklyLoadItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.AcademicCardComponentRenderer
import com.gdavidpb.tuindice.assistant.ui.component.conocimiento.CitationComponentView
import com.gdavidpb.tuindice.assistant.ui.component.conocimiento.HelpComponentView
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.EvaluationListComponentView
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.RequiredGradeComponentView
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.SubjectPointsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.evaluaciones.WeeklyLoadComponentView
import com.gdavidpb.tuindice.assistant.ui.component.horario.ClashesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.horario.NextClassComponentView
import com.gdavidpb.tuindice.assistant.ui.component.horario.WeekScheduleComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.IndexGoalComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.IndexSeriesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.PaceComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.RecordStatsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.SimulationComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.TermComponentView
import com.gdavidpb.tuindice.assistant.ui.component.materias.RecommendedElectivesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.materias.SubjectListComponentView
import com.gdavidpb.tuindice.assistant.ui.component.materias.SubjectStatsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.nav.OpenScreenComponentView
import com.gdavidpb.tuindice.assistant.ui.component.pensum.PensumComparisonComponentView
import com.gdavidpb.tuindice.assistant.ui.component.pensum.PensumProgressComponentView
import com.gdavidpb.tuindice.assistant.ui.component.pensum.RequirementsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.pensum.SubjectImpactComponentView
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
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanComparisonComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanEvaluationComponent
import com.gdavidpb.tuindice.chatcontract.component.PlanListComponent
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent

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
		is WeekScheduleItem -> WeekScheduleComponentView(item = item)
		is NextClassItem -> NextClassComponentView(item = item)
		is ClashesItem -> ClashesComponentView(item = item)
		is HelpItem -> HelpComponentView(item = item, onAction = onAction)
		is EvaluationListItem -> EvaluationListComponentView(item = item, onAction = onAction)
		is SubjectPointsItem -> SubjectPointsComponentView(item = item, onAction = onAction)
		is WeeklyLoadItem -> WeeklyLoadComponentView(item = item, onAction = onAction)
		is RequiredGradeItem -> RequiredGradeComponentView(item = item, onAction = onAction)
		is SubjectListItem -> SubjectListComponentView(item = item, onAction = onAction)
		is SubjectStatsItem -> SubjectStatsComponentView(item = item, onAction = onAction)
		is RecommendedElectivesItem -> RecommendedElectivesComponentView(item = item, onAction = onAction)
		is PensumProgressItem -> PensumProgressComponentView(item = item, onAction = onAction)
		is RequirementsItem -> RequirementsComponentView(item = item, onAction = onAction)
		is SubjectImpactItem -> SubjectImpactComponentView(item = item, onAction = onAction)
		is PensumComparisonItem -> PensumComparisonComponentView(item = item, onAction = onAction)
		is CitationItem -> CitationComponentView(item = item, onAction = onAction)
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
		// plan: drawn by AcademicCardComponentRenderer
		is PlanComparisonComponent, is PlanEvaluationComponent, is PlanListComponent ->
			AcademicCardComponentRenderer(component = component, onAction = onAction)

		// A component this build does not know shows the text the backend sent with it.
		is UnknownChatComponent -> ChatFallbackComponentView(fallbackText = component.fallbackText)

		// The catalogs ported to items are drawn from their items, so none of their components arrives here.
		else -> Unit
	}
}
