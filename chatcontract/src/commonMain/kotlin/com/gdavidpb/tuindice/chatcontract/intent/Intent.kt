package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.Serializable

/** Action the assistant proposes. The user confirms it with [confirmLabel] or cancels it with [cancelLabel]. */
@Serializable
public data class Intent(
	public val id: String,
	public val args: IntentArgs,
	public val title: String,
	public val effect: List<IntentEffectLine>,
	public val risk: IntentRisk,
	public val editable: List<String> = emptyList(),
	public val binding: IntentBinding,
	public val expiresAtMillis: Long,
	public val status: IntentStatus = IntentStatus.PROPOSED,
	public val confirmLabel: String,
	public val cancelLabel: String
)
