package com.gdavidpb.tuindice.base.domain.model.assistant

enum class AssistantMode {
	ON,
	GUIDED,
	OFF;

	companion object {
		// Remote values are matched without regard to case. Anything else, null included, fails closed to OFF.
		fun fromRemoteValue(value: String?): AssistantMode {
			return when (value?.lowercase()) {
				"on" -> ON
				"guided" -> GUIDED
				else -> OFF
			}
		}
	}
}
