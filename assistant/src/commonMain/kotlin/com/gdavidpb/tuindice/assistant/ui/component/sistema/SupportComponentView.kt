package com.gdavidpb.tuindice.assistant.ui.component.sistema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.gdavidpb.tuindice.assistant.presentation.model.SupportItem
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_support_open_page
import tuindice.assistant.generated.resources.assistant_support_title

// sistema.apoyo/1: where to reach support, each line with its contact. It shows no diagnostics.
// A phone opens the dialer and a page opens the browser, so a student in a hard moment reaches help in one tap.
@Composable
fun SupportComponentView(
	item: SupportItem,
	modifier: Modifier = Modifier
) {
	val uriHandler = LocalUriHandler.current

	AssistantEmbeddedCardView(modifier = modifier) {
		Text(
			text = stringResource(Res.string.assistant_support_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		item.lines.forEach { line ->
			Text(
				text = line.label,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
			line.call?.let { call ->
				CardLinkView(
					text = call.text.asString(),
					onClick = { runCatching { uriHandler.openUri(call.telUri) } }
				)
			}
			line.pageUri?.let { pageUri ->
				CardLinkView(
					text = stringResource(Res.string.assistant_support_open_page),
					onClick = { runCatching { uriHandler.openUri(pageUri) } }
				)
			}
		}
	}
}
