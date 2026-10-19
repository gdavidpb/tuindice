package com.gdavidpb.tuindice.assistant.domain.model

/**
 * The locale and the time zone a screen context is sent with. They are the defaults of the chat contract's
 * ScreenContext, which the server reads when a context does not say otherwise. A test pins these values to those
 * defaults, so the app and the contract cannot drift apart without a failing test.
 */
internal object AssistantContextDefaults {
	const val LOCALE = "es-VE"
	const val TIME_ZONE = "America/Caracas"
}
