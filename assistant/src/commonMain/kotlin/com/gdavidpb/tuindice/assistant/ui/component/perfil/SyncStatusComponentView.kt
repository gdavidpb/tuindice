package com.gdavidpb.tuindice.assistant.ui.component.perfil

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.SyncStatusItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString

// perfil.estado_sync/1: the state of the sync as the headline, one row per source with its state, and the cause of an
// annulment when there is one. The label says when the sync last ran.
@Composable
fun SyncStatusComponentView(
	item: SyncStatusItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Profile.sync(item.id))) {
		CardLabelView(text = item.labelText.asString())
		item.headlineText?.let { headline ->
			Text(
				text = headline.asString(),
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
		item.sources.forEachIndexed { index, source ->
			CardRowView(
				name = source.name.asString(),
				showsDivider = index > 0,
				trailing = { source.stateText?.let { text -> CardChipView(text = text.asString()) } }
			)
		}
		item.annulmentCause?.let { cause ->
			CardRowView(name = cause, showsDivider = item.sources.isNotEmpty())
		}
	}
}
