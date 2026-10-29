package com.gdavidpb.tuindice.assistant.ui.component.materias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.RecommendedElectivesItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.model.visibleRows
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_electives_empty
import tuindice.assistant.generated.resources.assistant_card_b_electives_label

// materias.recomendadas/1: no figure. A row per recommended elective, its code and name, and under it the short reason
// the service gives for recommending it. With no elective, a line says so.
@Composable
fun RecommendedElectivesComponentView(
	item: RecommendedElectivesItem,
	rowLimit: Int?,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Subjects.electives(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_electives_label))
		if (item.rows.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_b_electives_empty), muted = true)
		}
		item.rows.visibleRows(rowLimit).forEachIndexed { index, elective ->
			Column(verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)) {
				CardRowView(code = elective.code, name = elective.name, showsDivider = index > 0)
				Text(
					text = elective.reason,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
