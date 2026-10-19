package com.gdavidpb.tuindice.assistant.ui.component.perfil

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.mapper.isSyncProblem
import com.gdavidpb.tuindice.assistant.presentation.mapper.syncSourceText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.chatcontract.component.SyncStatusComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_joined
import tuindice.assistant.generated.resources.assistant_card_a_sync_label
import tuindice.assistant.generated.resources.assistant_card_a_sync_last
import tuindice.assistant.generated.resources.assistant_card_a_sync_ok
import tuindice.assistant.generated.resources.assistant_card_a_sync_problems

// perfil.estado_sync/1: the state of the sync as the headline, one row per source with its state, and the cause of an
// annulment when there is one. The label says when the sync last ran.
@Composable
fun SyncStatusComponentView(
	component: SyncStatusComponent,
	modifier: Modifier = Modifier
) {
	val lastSync = component.lastSyncAtMillis?.let { millis ->
		UiText.Resource(
			Res.string.assistant_card_a_sync_last,
			listOf(millis.formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE))
		)
	}
	val hasProblem = component.sources.any { source -> isSyncProblem(source.status) }

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Profile.sync(component.id))) {
		CardLabelView(text = lastSync?.asString() ?: stringResource(Res.string.assistant_card_a_sync_label))

		if (component.sources.isNotEmpty()) {
			Text(
				text = stringResource(
					if (hasProblem) Res.string.assistant_card_a_sync_problems else Res.string.assistant_card_a_sync_ok
				),
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface
			)
		}

		component.sources.forEachIndexed { index, source ->
			val name = source.detail?.let { detail ->
				UiText.Resource(Res.string.assistant_card_a_joined, listOf(source.name, detail)).asString()
			}

			CardRowView(
				name = name ?: source.name,
				showsDivider = index > 0,
				trailing = { syncSourceText(source.status)?.let { text -> CardChipView(text = text.asString()) } }
			)
		}

		component.annulmentCause?.let { cause ->
			CardRowView(name = cause, showsDivider = component.sources.isNotEmpty())
		}
	}
}
