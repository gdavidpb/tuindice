package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The next class, or the one in progress. */
@Serializable
@SerialName("horario.proxima/1")
public data class NextClassComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val subjectCode: String,
	public val subjectName: String,
	public val dayOfWeek: Int,
	public val startMinute: Int,
	public val endMinute: Int,
	public val startBlock: Int,
	public val endBlock: Int,
	public val room: String? = null,
	// The server's reading when the turn ran. A card read later counts the class from the clock instead.
	public val inProgress: Boolean,
	public val dateMillis: Long
) : ChatComponent
