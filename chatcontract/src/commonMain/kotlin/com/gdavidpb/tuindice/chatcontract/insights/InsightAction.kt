package com.gdavidpb.tuindice.chatcontract.insights

import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.Serializable

/**
 * A button of "Qué sigue": the screen it opens ([target]), its [label], and the records that screen
 * needs ([entityRefs]), for instance the TERM ref of the share screen. The service picks the actions
 * from fixed rules that depend on the moment of the report, so an action never asks for something the
 * moment does not fit.
 */
@Serializable
public data class InsightAction(
	public val target: NavTarget,
	public val label: String,
	public val entityRefs: List<EntityRef> = emptyList()
)
