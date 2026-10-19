package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent

// The item a component is drawn from. Each catalog gets its items here as it is ported.
internal fun ChatComponent.toChatComponentItem(): ChatComponentItem = ChatComponentItem.Unmigrated(component = this)
