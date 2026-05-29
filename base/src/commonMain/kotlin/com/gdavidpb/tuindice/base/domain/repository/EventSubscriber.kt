package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent

interface EventSubscriber {
	val id: String
	val isEnabled: Boolean

	fun onEvent(event: AppEvent)
}
