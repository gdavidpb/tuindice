package com.gdavidpb.tuindice.assistant.ui

import com.gdavidpb.tuindice.assistant.presentation.model.AssistantReportReason

object AssistantUiTags {
	const val ChatSheet = "assistant_chat_sheet"
	const val ChatTitle = "assistant_chat_title"
	const val ConsentTitle = "assistant_consent_title"
	const val TitleSparkle = "assistant_title_sparkle"
	const val MessageList = "assistant_message_list"
	const val ContextChip = "assistant_context_chip"
	const val ContextRemove = "assistant_context_remove"
	const val Composer = "assistant_composer"
	const val StopButton = "assistant_stop_button"
	const val SendButton = "assistant_send_button"
	const val PhotoButton = "assistant_photo_button"
	const val PhotoTake = "assistant_photo_take"
	const val PhotoPick = "assistant_photo_pick"
	const val ProgressContainer = "assistant_progress_container"
	const val ProgressAtom = "assistant_progress_atom"
	const val ProgressLabel = "assistant_progress_label"
	const val Notice = "assistant_notice"
	const val NoticeTurnFailed = "assistant_notice_turn_failed"
	const val NoticeRetry = "assistant_notice_retry"
	const val AppealAction = "assistant_appeal_action"
	const val AppealText = "assistant_appeal_text"
	const val AppealCounter = "assistant_appeal_counter"
	const val AppealSend = "assistant_appeal_send"
	const val NotificationOffer = "assistant_notification_offer"
	const val NotificationOfferActivate = "assistant_notification_offer_activate"
	const val NotificationOfferLater = "assistant_notification_offer_later"
	const val ConsentView = "assistant_consent_view"
	const val ConsentFailed = "assistant_consent_failed"
	const val ConsentAccept = "assistant_consent_accept"
	const val ConsentDecline = "assistant_consent_decline"
	const val ConsentPrivacy = "assistant_consent_privacy"
	const val MemoryScreen = "assistant_memory_screen"
	const val MemoryLoading = "assistant_memory_loading"
	const val MemoryEmpty = "assistant_memory_empty"
	const val MemoryFailed = "assistant_memory_failed"
	const val MemoryContent = "assistant_memory_content"
	const val MemoryClearAll = "assistant_memory_clear_all"
	const val ClearAllDialog = "assistant_clear_all_dialog"
	const val ClearAllConfirm = "assistant_clear_all_confirm"
	const val ClearAllCancel = "assistant_clear_all_cancel"

	// Tags that depend on an id, grouped by what they name. Each group keeps its own function count small.
	object Chat {
		fun suggestion(suggestionId: String): String = "assistant_suggestion_$suggestionId"
	}

	object Message {
		fun bubble(messageId: String): String = "assistant_user_bubble_$messageId"

		fun bubbleError(messageId: String): String = "assistant_user_bubble_error_$messageId"

		fun agent(messageId: String): String = "assistant_agent_message_$messageId"

		fun copy(messageId: String): String = "assistant_message_copy_$messageId"

		fun useful(messageId: String): String = "assistant_message_useful_$messageId"

		fun notUseful(messageId: String): String = "assistant_message_not_useful_$messageId"

		fun report(messageId: String): String = "assistant_message_report_$messageId"

		fun reportReason(messageId: String, reason: AssistantReportReason): String =
			"assistant_message_report_${reason.name.lowercase()}_$messageId"
	}

	object Component {
		fun root(componentId: String): String = "assistant_component_$componentId"

		fun detail(messageId: String, componentId: String): String =
			"assistant_component_detail_${messageId}_$componentId"

		fun openScreen(componentId: String): String = "assistant_open_screen_$componentId"
	}

	object Intent {
		fun card(intentId: String): String = "assistant_intent_card_$intentId"

		fun confirm(intentId: String): String = "assistant_intent_confirm_$intentId"

		fun cancel(intentId: String): String = "assistant_intent_cancel_$intentId"

		fun progress(intentId: String): String = "assistant_intent_progress_$intentId"

		fun receipt(intentId: String): String = "assistant_intent_receipt_$intentId"

		fun draft(intentId: String, mutationId: String): String = "assistant_intent_draft_${intentId}_$mutationId"
	}

	object Memory {
		fun entry(memoryId: String): String = "assistant_memory_entry_$memoryId"

		fun delete(memoryId: String): String = "assistant_memory_delete_$memoryId"

		fun stillAppliesYes(memoryId: String): String = "assistant_still_applies_yes_$memoryId"

		fun stillAppliesNo(memoryId: String): String = "assistant_still_applies_no_$memoryId"
	}

	// The cards of the profile, the report, the summary and the schedule: one tag per card, named by its catalog.
	object Profile {
		fun summary(componentId: String): String = "assistant_card_a_profile_summary_$componentId"

		fun sync(componentId: String): String = "assistant_card_a_profile_sync_$componentId"
	}

	object Report {
		fun term(componentId: String): String = "assistant_card_a_report_term_$componentId"

		fun indexSeries(componentId: String): String = "assistant_card_a_report_series_$componentId"

		fun recordStats(componentId: String): String = "assistant_card_a_report_stats_$componentId"

		fun pace(componentId: String): String = "assistant_card_a_report_pace_$componentId"

		fun simulation(componentId: String): String = "assistant_card_a_report_simulation_$componentId"

		fun indexGoal(componentId: String): String = "assistant_card_a_report_goal_$componentId"
	}

	object Summary {
		fun attention(componentId: String): String = "assistant_card_a_summary_attention_$componentId"
	}

	object Schedule {
		fun week(componentId: String): String = "assistant_card_a_schedule_week_$componentId"

		fun nextClass(componentId: String): String = "assistant_card_a_schedule_next_$componentId"

		fun clashes(componentId: String): String = "assistant_card_a_schedule_clashes_$componentId"
	}

	// The cards of the evaluations, the pensum, the subjects, the plans and the knowledge: one tag per card, by catalog.
	object Evaluations {
		fun list(componentId: String): String = "assistant_card_b_evaluations_list_$componentId"

		fun points(componentId: String): String = "assistant_card_b_evaluations_points_$componentId"

		fun load(componentId: String): String = "assistant_card_b_evaluations_load_$componentId"

		fun requiredGrade(componentId: String): String = "assistant_card_b_evaluations_required_$componentId"
	}

	object Pensum {
		fun progress(componentId: String): String = "assistant_card_b_pensum_progress_$componentId"

		fun requirements(componentId: String): String = "assistant_card_b_pensum_requirements_$componentId"

		fun impact(componentId: String): String = "assistant_card_b_pensum_impact_$componentId"

		fun comparison(componentId: String): String = "assistant_card_b_pensum_comparison_$componentId"
	}

	object Subjects {
		fun list(componentId: String): String = "assistant_card_b_subjects_list_$componentId"

		fun stats(componentId: String): String = "assistant_card_b_subjects_stats_$componentId"

		fun electives(componentId: String): String = "assistant_card_b_subjects_electives_$componentId"
	}

	object Plan {
		fun list(componentId: String): String = "assistant_card_b_plan_list_$componentId"

		fun evaluation(componentId: String): String = "assistant_card_b_plan_evaluation_$componentId"

		fun comparison(componentId: String): String = "assistant_card_b_plan_comparison_$componentId"
	}

	object Knowledge {
		fun citation(componentId: String): String = "assistant_card_b_knowledge_citation_$componentId"

		fun help(componentId: String): String = "assistant_card_b_knowledge_help_$componentId"

		fun helpOpen(componentId: String): String = "assistant_card_b_knowledge_help_open_$componentId"
	}
}
