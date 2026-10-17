package com.gdavidpb.tuindice.chatcontract.error

/** Codes carried in [AssistantErrorBody.code]. */
public object AssistantErrorCodes {
	public const val ASSISTANT_DISABLED: String = "assistant_disabled"
	public const val CONSENT_REQUIRED: String = "consent_required"
	public const val QUOTA_EXHAUSTED: String = "quota_exhausted"
	public const val TURN_IN_PROGRESS: String = "turn_in_progress"
	public const val SUSPENDED: String = "suspended"
	public const val GUIDED_ONLY: String = "guided_only"
	public const val INTENT_EXPIRED: String = "intent_expired"
	public const val INTENT_CONFLICT: String = "intent_conflict"
}
