package com.gdavidpb.tuindice.chatcontract.context

import kotlinx.serialization.Serializable

/** What the user is looking at when a turn starts. */
@Serializable
public data class ScreenContext(
	public val screen: ScreenId,
	public val entities: List<EntityRef> = emptyList(),
	public val visibleFields: Map<String, String> = emptyMap(),
	public val filters: Map<String, String> = emptyMap(),
	public val locale: String = "es-VE",
	public val timeZone: String = "America/Caracas"
)
