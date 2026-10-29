package com.gdavidpb.tuindice.chatcontract.intent

/**
 * Args that change the app's data. [mutationIds] is computed from the args and is never serialized. It does not extend
 * [IntentArgs], so the sealed hierarchy lists only the wire types; every implementation also implements [IntentArgs].
 */
public interface MutatingIntentArgs {
	public val mutationIds: List<String>
}
