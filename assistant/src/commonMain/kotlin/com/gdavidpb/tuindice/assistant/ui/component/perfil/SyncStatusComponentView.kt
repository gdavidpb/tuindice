package com.gdavidpb.tuindice.assistant.ui.component.perfil

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.SyncStatusItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_sync_empty
import tuindice.assistant.generated.resources.assistant_card_a_sync_label
import tuindice.assistant.generated.resources.assistant_card_a_sync_last

// perfil.estado_sync/1: the state of the sync as the headline, the moment it last ran as a row of its own, one row per
// source with its state, and the cause of an annulment when there is one. With no source, a line says so.
@Composable
fun SyncStatusComponentView(
	item: SyncStatusItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Profile.sync(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_sync_label))
		item.headlineText?.let { headline ->
			Text(
				text = headline.asString(),
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
		if (item.sources.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_a_sync_empty), muted = true)
		}
		item.lastSyncText?.let { lastSync ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_a_sync_last),
				trailing = { CardTrailingView(value = lastSync.asString(), chips = emptyList()) }
			)
		}
		item.sources.forEachIndexed { index, source ->
			CardRowView(
				name = source.name.asString(),
				showsDivider = index > 0 || item.lastSyncText != null,
				trailing = { CardTrailingView(value = null, chips = listOfNotNull(source.stateChip)) }
			)
		}
		item.annulmentCause?.let { cause ->
			CardRowView(name = cause, showsDivider = item.sources.isNotEmpty() || item.lastSyncText != null)
		}
	}
}
