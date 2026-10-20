package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.CooldownItem
import com.gdavidpb.tuindice.assistant.presentation.model.OpenScreenItem
import com.gdavidpb.tuindice.assistant.presentation.model.SistemaComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.assistant.presentation.model.SupportItem
import com.gdavidpb.tuindice.assistant.presentation.model.SuspensionItem
import com.gdavidpb.tuindice.assistant.presentation.model.UsageNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.WarningItem
import com.gdavidpb.tuindice.assistant.ui.component.nav.OpenScreenComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.CooldownComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.StillAppliesComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.SupportComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.SuspensionComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.UsageNoticeComponentView
import com.gdavidpb.tuindice.assistant.ui.component.sistema.WarningComponentView

// Sistema components: the notices, the support, the warnings and the link of the chat.
@Composable
internal fun SistemaComponentRenderer(
	item: SistemaComponentItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (item) {
		is StillAppliesItem -> StillAppliesComponentView(item = item, onAction = onAction)
		is UsageNoticeItem -> UsageNoticeComponentView(item = item)
		is CooldownItem -> CooldownComponentView(item = item, onAction = onAction)
		is SuspensionItem -> SuspensionComponentView(item = item, onAction = onAction)
		is SupportItem -> SupportComponentView(item = item, onAction = onAction)
		is WarningItem -> WarningComponentView(item = item)
		is OpenScreenItem -> OpenScreenComponentView(item = item, onAction = onAction)
	}
}
