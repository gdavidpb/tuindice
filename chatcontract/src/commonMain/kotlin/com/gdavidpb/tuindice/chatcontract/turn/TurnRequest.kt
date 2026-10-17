package com.gdavidpb.tuindice.chatcontract.turn

import com.gdavidpb.tuindice.chatcontract.ChatContractVersion
import com.gdavidpb.tuindice.chatcontract.context.ClientState
import com.gdavidpb.tuindice.chatcontract.context.ScreenContext
import kotlinx.serialization.Serializable

/** Body the client sends to start a turn. */
@Serializable
public data class TurnRequest(
	public val conversationId: String? = null,
	public val clientTurnId: String,
	public val message: UserMessage,
	public val screenContext: ScreenContext? = null,
	public val clientState: ClientState = ClientState(),
	public val supportedCatalogs: List<String>,
	public val contractVersion: Int = ChatContractVersion.VALUE
)
