package com.gdavidpb.tuindice.assistant.ui.component.conocimiento

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.CitationItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_citation_label
import tuindice.assistant.generated.resources.assistant_card_b_citation_open

// conocimiento.cita/1: no figure. One entry per cited source: its document and article, its excerpt and, when the
// service brings one, a link to the official document, so the student can read it at the source.
@Composable
fun CitationComponentView(
	item: CitationItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val uriHandler = LocalUriHandler.current

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Knowledge.citation(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_citation_label))
		item.sources.forEachIndexed { index, source ->
			CardRowView(
				code = source.documentText.asString(),
				name = source.article,
				showsDivider = index > 0
			)
			Text(
				text = source.excerpt,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
			source.url?.let { url ->
				CardLinkView(
					text = stringResource(Res.string.assistant_card_b_citation_open),
					onClick = { runCatching { uriHandler.openUri(url) } }
				)
			}
		}
		item.recordLink?.let { link ->
			CardLinkView(
				text = link.text.asString(),
				onClick = { onAction(AssistantChat.Action.OpenNavTarget(link.target, link.entityRefs)) }
			)
		}
	}
}
