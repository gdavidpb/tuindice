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
 *
 * Per keystroke on a form screen (A7, accepted, no code change):
 *
 * - The machine loop (`StateMachineViewModel`) publishes two events: an `app_action` carrying
 *   only the screen and the action's class name (never the typed text), and an
 *   `app_transition` whose `from` and `to` are the same state name. The state event is
 *   published only when the state name changes, so typing adds none.
 * - Each publish is a non-blocking `trySend` into the `BufferedEventPublisher` channel: 64
 *   slots, drop-oldest, so a burst can lose the oldest events but never blocks typing. Both the
 *   publish and the consumer check usage-data consent, so nothing is queued or forwarded while
 *   it is off.
 * - The self-loop `app_transition` is dropped by this policy inside each remote subscriber
 *   (Firebase Analytics on Android, the iOS analytics bridge, the Crashlytics breadcrumb log); it
 *   still takes a slot in the channel. The `app_action` passes and reaches remote analytics and
 *   the breadcrumb log.
 * - On iOS debug builds the analytics and breadcrumb subscribers are no-ops; the debug
 *   subscriber only logs locally.
 */
fun AppEvent.isAnalyticsRelevant(): Boolean {
	return when (this) {
		is AppEvent.Transition -> !isSelfLoop
		else -> true
	}
}
