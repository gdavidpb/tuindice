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

	/** An appeal needs a sanction in force to appeal against. */
	public const val APPEAL_NOT_ALLOWED: String = "appeal_not_allowed"

	/** The student already has an appeal waiting for review. */
	public const val APPEAL_OPEN: String = "appeal_open"

	/** The service could not take the request right now (for example, the turn could not be queued); retry later. */
	public const val UNAVAILABLE: String = "unavailable"
}
