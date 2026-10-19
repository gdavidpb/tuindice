package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AttentionCardItem
import com.gdavidpb.tuindice.assistant.presentation.model.ClashesItem
import com.gdavidpb.tuindice.assistant.presentation.model.IndexGoalItem
import com.gdavidpb.tuindice.assistant.presentation.model.IndexSeriesItem
import com.gdavidpb.tuindice.assistant.presentation.model.NextClassItem
import com.gdavidpb.tuindice.assistant.presentation.model.PaceItem
import com.gdavidpb.tuindice.assistant.presentation.model.ProfileSummaryItem
import com.gdavidpb.tuindice.assistant.presentation.model.RecordComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.RecordStatsItem
import com.gdavidpb.tuindice.assistant.presentation.model.SimulationItem
import com.gdavidpb.tuindice.assistant.presentation.model.SyncStatusItem
import com.gdavidpb.tuindice.assistant.presentation.model.TermItem
import com.gdavidpb.tuindice.assistant.presentation.model.WeekScheduleItem
import com.gdavidpb.tuindice.assistant.ui.component.horario.ClashesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.horario.NextClassComponentView
import com.gdavidpb.tuindice.assistant.ui.component.horario.WeekScheduleComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.IndexGoalComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.IndexSeriesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.PaceComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.RecordStatsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.SimulationComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.TermComponentView
import com.gdavidpb.tuindice.assistant.ui.component.perfil.ProfileSummaryComponentView
import com.gdavidpb.tuindice.assistant.ui.component.perfil.SyncStatusComponentView
import com.gdavidpb.tuindice.assistant.ui.component.resumen.AttentionComponentView

// Record cards: the components of the perfil, informe, horario and resumen catalogs.
@Composable
internal fun RecordComponentRenderer(
	item: RecordComponentItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (item) {
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
		is SyncStatusItem -> SyncStatusComponentView(item = item)
		is AttentionCardItem -> AttentionComponentView(item = item, onAction = onAction)
	}
}
