package com.gdavidpb.tuindice.assistant.ui

import com.gdavidpb.tuindice.chatcontract.feedback.ReportReason

object AssistantUiTags {
	const val ChatSheet = "assistant_chat_sheet"
	const val MessageList = "assistant_message_list"
	const val ContextChip = "assistant_context_chip"
	const val ContextRemove = "assistant_context_remove"
	const val Composer = "assistant_composer"
	const val SendButton = "assistant_send_button"
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
	const val ConsentView = "assistant_consent_view"
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

		fun reportReason(messageId: String, reason: ReportReason): String =
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
}
