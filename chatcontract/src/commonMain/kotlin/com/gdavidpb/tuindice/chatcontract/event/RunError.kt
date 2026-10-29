package com.gdavidpb.tuindice.chatcontract.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The turn failed with [code]; a code this build does not know decodes to OTHER.
 * [retryAfterSeconds], when set, is the wait before a retry.
 */
@Serializable
@SerialName("run_error")
public data class RunError(
	override val seq: Long,
	override val turnId: String,
	public val code: RunErrorCode = RunErrorCode.OTHER,
	public val retryable: Boolean,
	public val retryAfterSeconds: Int? = null
) : AssistantEvent
