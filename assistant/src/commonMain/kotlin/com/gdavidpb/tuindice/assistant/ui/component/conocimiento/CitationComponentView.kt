package com.gdavidpb.tuindice.assistant.ui.component.conocimiento

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.chatcontract.component.CitationComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_citation_document
import tuindice.assistant.generated.resources.assistant_card_b_citation_label

// conocimiento.cita/1: no figure. One entry per cited source: its document and article, its excerpt and, when the
// service brings one, its address as text. The chat has no way to open a web page, so the address is not a link.
@Composable
fun CitationComponentView(
	component: CitationComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Knowledge.citation(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_citation_label))
		component.sources.forEachIndexed { index, source ->
			CardRowView(
				code = stringResource(Res.string.assistant_card_b_citation_document, source.document),
				name = source.article,
				showsDivider = index > 0
			)
			Text(
				text = source.excerpt,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
			source.url?.let { url ->
				Text(
					text = url,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
