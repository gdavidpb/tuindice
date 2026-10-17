package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Where the app shows a component: inline in the message, or in a sheet. */
@Serializable
public enum class ComponentDisplay {
	@SerialName("inline")
	INLINE,

	@SerialName("sheet")
	SHEET
}
