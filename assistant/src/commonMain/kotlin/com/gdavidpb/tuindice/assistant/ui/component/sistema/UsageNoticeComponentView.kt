package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.model.UsageNoticeItem
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTitleView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_usage_title

// sistema.aviso_uso/1: the requests left in each window, and when the quota resets if the service knows it.
@Composable
fun UsageNoticeComponentView(
	item: UsageNoticeItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier) {
		CardTitleView(text = stringResource(Res.string.assistant_usage_title))
		CardBodyTextView(text = item.hourText.asString())
		CardBodyTextView(text = item.dayText.asString())
		CardBodyTextView(text = item.monthText.asString())
		item.resetsText?.let { resetsText ->
			CardBodyTextView(text = resetsText.asString(), muted = true)
		}
	}
}
