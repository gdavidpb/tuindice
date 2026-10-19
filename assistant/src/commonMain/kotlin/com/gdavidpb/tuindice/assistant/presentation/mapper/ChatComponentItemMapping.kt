package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatFallbackItem
import com.gdavidpb.tuindice.chatcontract.component.AttentionComponent
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
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
import com.gdavidpb.tuindice.chatcontract.component.UsageNoticeComponent
import com.gdavidpb.tuindice.chatcontract.component.WarningComponent
import com.gdavidpb.tuindice.chatcontract.component.WeekScheduleComponent
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent

// The item a component is drawn from: one item for each component of the catalogs this build renders.
internal fun ChatComponent.toChatComponentItem(nowMillis: Long): ChatComponentItem {
	return when (this) {
		is StillAppliesComponent -> toStillAppliesItem()
		is UsageNoticeComponent -> toUsageNoticeItem()
		is CooldownComponent -> toCooldownItem()
		is SuspensionComponent -> toSuspensionItem()
		is SupportComponent -> toSupportItem()
		is WarningComponent -> toWarningItem()
		is OpenScreenComponent -> toOpenScreenItem()
		is ProfileSummaryComponent -> toProfileSummaryItem()
		is TermComponent -> toTermItem()
		is IndexSeriesComponent -> toIndexSeriesItem()
		is RecordStatsComponent -> toRecordStatsItem()
		is PaceComponent -> toPaceItem()
		is SimulationComponent -> toSimulationItem()
		is IndexGoalComponent -> toIndexGoalItem()
		is WeekScheduleComponent -> toWeekScheduleItem()
		is NextClassComponent -> toNextClassItem(nowMillis = nowMillis)
		is ClashesComponent -> toClashesItem()
		is CitationComponent -> toCitationItem()
		is EvaluationListComponent -> toEvaluationListItem()
		is SubjectPointsComponent -> toSubjectPointsItem()
		is WeeklyLoadComponent -> toWeeklyLoadItem()
		is RequiredGradeComponent -> toRequiredGradeItem()
		is SubjectListComponent -> toSubjectListItem()
		is SubjectStatsComponent -> toSubjectStatsItem()
		is RecommendedElectivesComponent -> toRecommendedElectivesItem()
		is PensumProgressComponent -> toPensumProgressItem()
		is RequirementsComponent -> toRequirementsItem()
		is SubjectImpactComponent -> toSubjectImpactItem()
		is PensumComparisonComponent -> toPensumComparisonItem()
		is PlanListComponent -> toPlanListItem()
		is PlanEvaluationComponent -> toPlanEvaluationItem()
		is PlanComparisonComponent -> toPlanComparisonItem()
		is HelpComponent -> toHelpItem()
		is SyncStatusComponent -> toSyncStatusItem()
		is AttentionComponent -> toAttentionCardItem()

		is UnknownChatComponent -> ChatFallbackItem(id = id, opensSheet = opensSheet(), fallbackText = fallbackText)
	}
}

// Whether the component opens on a sheet of its own.
internal fun ChatComponent.opensSheet(): Boolean = display == ComponentDisplay.SHEET
