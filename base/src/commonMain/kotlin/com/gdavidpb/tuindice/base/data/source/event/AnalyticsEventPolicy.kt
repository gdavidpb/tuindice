package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent

/**
 * Which events are worth forwarding to remote analytics. Self-loop transitions
 * (rows that stay in the same state — one per keystroke on form screens) are
 * debug-level detail: they roughly double the per-input event volume while
 * app_action/app_state already cover usage analytics. State-changing transitions
 * keep the navigation narrative with its triggering event, and invalid
 * transitions are the anomaly signal that flags table holes in production; both
 * stay remote. Debug subscribers ignore this policy and log everything.
 */
fun AppEvent.isAnalyticsRelevant(): Boolean {
	return when (this) {
		is AppEvent.Transition -> !isSelfLoop
		else -> true
	}
}
