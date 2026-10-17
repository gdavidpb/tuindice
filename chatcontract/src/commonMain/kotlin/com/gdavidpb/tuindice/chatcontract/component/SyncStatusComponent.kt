package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Sync state of the profile: last sync, each source, and the enrollment and annulment status. */
@Serializable
@SerialName("perfil.estado_sync/1")
public data class SyncStatusComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val lastSyncAtMillis: Long?,
	public val sources: List<SyncSourceRow>,
	public val enrollmentStatus: String?,
	public val annulmentCause: String?
) : ChatComponent
