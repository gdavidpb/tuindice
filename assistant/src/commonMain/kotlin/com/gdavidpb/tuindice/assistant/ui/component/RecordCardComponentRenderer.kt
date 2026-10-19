package com.gdavidpb.tuindice.assistant.ui.component

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.component.horario.ClashesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.horario.NextClassComponentView
import com.gdavidpb.tuindice.assistant.ui.component.horario.WeekScheduleComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.IndexGoalComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.IndexSeriesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.PaceComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.RecordStatsComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.SimulationComponentView
import com.gdavidpb.tuindice.assistant.ui.component.informe.TermComponentView
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import com.gdavidpb.tuindice.chatcontract.component.IndexGoalComponent
import com.gdavidpb.tuindice.chatcontract.component.IndexSeriesComponent
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
import com.gdavidpb.tuindice.chatcontract.component.PaceComponent
import com.gdavidpb.tuindice.chatcontract.component.RecordStatsComponent
import com.gdavidpb.tuindice.chatcontract.component.SimulationComponent
import com.gdavidpb.tuindice.chatcontract.component.TermComponent
import com.gdavidpb.tuindice.chatcontract.component.WeekScheduleComponent

// Draws the record cards: the components of the perfil, informe, horario and resumen catalogs. ChatComponentView sends
// this function only the types of its record-card branch.
@Composable
fun RecordCardComponentRenderer(
	component: ChatComponent,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (component) {
		is TermComponent -> TermComponentView(component = component, onAction = onAction)
		is IndexSeriesComponent -> IndexSeriesComponentView(component = component)
		is RecordStatsComponent -> RecordStatsComponentView(component = component)
		is PaceComponent -> PaceComponentView(component = component)
		is SimulationComponent -> SimulationComponentView(component = component)
		is IndexGoalComponent -> IndexGoalComponentView(component = component)
		is WeekScheduleComponent -> WeekScheduleComponentView(component = component)
		is NextClassComponent -> NextClassComponentView(component = component)
		is ClashesComponent -> ClashesComponentView(component = component)

		// Only the types of the record-card branch reach here, so no other component is drawn.
		else -> Unit
	}
}
