package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.view.NoticeView

// What limits the chat right now, above the composer. A failed turn and a restriction let the student act; a quota
// and the guided mode only inform.
@Composable
fun AssistantChatNoticeView(
	notice: ChatNoticeItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	when (notice) {
		is ChatNoticeItem.TurnFailed -> AssistantTurnFailedNoticeView(
			notice = notice,
			onAction = onAction,
			modifier = modifier
		)

		is ChatNoticeItem.Restricted -> AssistantRestrictedNoticeView(
			notice = notice,
			onAction = onAction,
			modifier = modifier
		)

		is ChatNoticeItem.QuotaExhausted -> Box(modifier = modifier.testTag(AssistantUiTags.Notice)) {
			NoticeView(message = notice.message)
		}

		is ChatNoticeItem.Guided -> Box(modifier = modifier.testTag(AssistantUiTags.Notice)) {
			NoticeView(message = notice.message)
		}
	}
}
