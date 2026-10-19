package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem
import com.gdavidpb.tuindice.chatcontract.component.AttentionComponent
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay
import com.gdavidpb.tuindice.chatcontract.component.CooldownComponent
import com.gdavidpb.tuindice.chatcontract.component.OpenScreenComponent
import com.gdavidpb.tuindice.chatcontract.component.ProfileSummaryComponent
import com.gdavidpb.tuindice.chatcontract.component.StillAppliesComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportComponent
import com.gdavidpb.tuindice.chatcontract.component.SyncStatusComponent
import com.gdavidpb.tuindice.chatcontract.component.SuspensionComponent
import com.gdavidpb.tuindice.chatcontract.component.UsageNoticeComponent
import com.gdavidpb.tuindice.chatcontract.component.WarningComponent

// The item a component is drawn from. Each catalog gets its items here as it is ported; a component of a catalog not
// yet ported travels as Unmigrated, and its view draws it from the wire component.
internal fun ChatComponent.toChatComponentItem(): ChatComponentItem {
	return when (this) {
		is StillAppliesComponent -> toStillAppliesItem()
		is UsageNoticeComponent -> toUsageNoticeItem()
		is CooldownComponent -> toCooldownItem()
		is SuspensionComponent -> toSuspensionItem()
		is SupportComponent -> toSupportItem()
		is WarningComponent -> toWarningItem()
		is OpenScreenComponent -> toOpenScreenItem()
		is ProfileSummaryComponent -> toProfileSummaryItem()
		is SyncStatusComponent -> toSyncStatusItem()
		is AttentionComponent -> toAttentionCardItem()

		else -> ChatComponentItem.Unmigrated(component = this)
	}
}

// Whether the component opens on a sheet of its own.
internal fun ChatComponent.opensSheet(): Boolean = display == ComponentDisplay.SHEET
