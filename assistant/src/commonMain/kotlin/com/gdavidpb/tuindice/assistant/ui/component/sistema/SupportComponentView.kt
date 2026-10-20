package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.gdavidpb.tuindice.assistant.presentation.model.SupportItem
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTitleView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_support_open_page
import tuindice.assistant.generated.resources.assistant_support_title

// sistema.apoyo/1: where to reach support, each line with its contact. It shows no diagnostics. A phone opens the
// dialer and a page opens the browser, so a student in a hard moment reaches help in one tap. Both links leave the app,
// and they carry the mark that says so.
@Composable
fun SupportComponentView(
	item: SupportItem,
	modifier: Modifier = Modifier
) {
	val uriHandler = LocalUriHandler.current

	AssistantEmbeddedCardView(modifier = modifier) {
		CardTitleView(text = stringResource(Res.string.assistant_support_title))
		item.lines.forEach { line ->
			CardBodyTextView(text = line.label)
			line.call?.let { call ->
				CardLinkView(
					text = call.text.asString(),
					onClick = { runCatching { uriHandler.openUri(call.telUri) } },
					external = true
				)
			}
			line.pageUri?.let { pageUri ->
				CardLinkView(
					text = stringResource(Res.string.assistant_support_open_page),
					onClick = { runCatching { uriHandler.openUri(pageUri) } },
					external = true
				)
			}
		}
	}
}
