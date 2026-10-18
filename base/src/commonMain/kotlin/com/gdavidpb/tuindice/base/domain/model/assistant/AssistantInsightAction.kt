package com.gdavidpb.tuindice.base.domain.model.assistant

/**
 * A button of "Qué sigue": the screen it opens ([target]), its [label] and the records that screen needs ([entityRefs]).
 * The service chooses the buttons by fixed rules, so an action never asks for a screen the moment does not fit.
 */
data class AssistantInsightAction(
	val target: AssistantNavTarget,
	val label: String,
	val entityRefs: List<AssistantEntityRef> = emptyList()
)
