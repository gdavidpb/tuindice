package com.gdavidpb.tuindice.assistant.presentation.machine

// The two groups of change proposals the assistant makes, grouped because the chat confirms either one the same way:
// the student's records and the enrollment proof ([intents]), and the plans and the projection ([planIntents]).
class AssistantChatProposals(
	val intents: AssistantChatIntents,
	val planIntents: AssistantChatPlanIntents
)
