package com.gdavidpb.tuindice.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.presentation.model.TopBarSlot

/**
 * The one priority table of the top bar. The assistant leads whenever the screen declares a context and the assistant
 * is on; Record's view switch comes next, and then the actions of the screen in their own order.
 *
 * With four or more places, two stay in view and the rest go to the ⋮ menu, so the bar takes three places in all. With
 * three or fewer, every place is in view. The menu only takes actions: the assistant and the switch are always in view.
 */
fun arrangeTopBarActions(
	actions: List<TopBarAction>,
	hasAssistant: Boolean,
	hasViewModeSelector: Boolean
): Pair<List<TopBarSlot>, List<TopBarAction>> {
	val places = buildList {
		if (hasAssistant) add(TopBarSlot.Assistant)
		if (hasViewModeSelector) add(TopBarSlot.ViewModeSelector)
		actions.forEach { action -> add(TopBarSlot.Action(action)) }
	}

	if (places.size <= MAX_PLACES_WITHOUT_OVERFLOW) return places to emptyList()

	val inline = places.take(INLINE_PLACES_WITH_OVERFLOW)
	val overflow = places
		.drop(INLINE_PLACES_WITH_OVERFLOW)
		.filterIsInstance<TopBarSlot.Action>()
		.map { slot -> slot.action }

	return inline to overflow
}

private const val MAX_PLACES_WITHOUT_OVERFLOW = 3
private const val INLINE_PLACES_WITH_OVERFLOW = 2
