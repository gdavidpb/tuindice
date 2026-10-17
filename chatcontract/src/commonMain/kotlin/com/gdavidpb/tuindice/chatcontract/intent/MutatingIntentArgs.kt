package com.gdavidpb.tuindice.chatcontract.intent

/** Args that change the app's data. [mutationIds] is computed from the args and is never serialized. */
public interface MutatingIntentArgs : IntentArgs {
	public val mutationIds: List<String>
}
