package com.gdavidpb.tuindice.base.presentation.model

enum class SyncedContentResolution {
	Content,
	Empty,
	Loading,
	KeepCurrent
}

fun resolveSyncedContentResolution(
	hasContent: Boolean,
	hasSynced: Boolean,
	keepCurrentWhileWaiting: Boolean
): SyncedContentResolution {
	return when {
		hasContent -> SyncedContentResolution.Content
		hasSynced -> SyncedContentResolution.Empty
		keepCurrentWhileWaiting -> SyncedContentResolution.KeepCurrent
		else -> SyncedContentResolution.Loading
	}
}
