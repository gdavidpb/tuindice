package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardProgressView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.RecordStatsComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_record_approved_caption
import tuindice.assistant.generated.resources.assistant_card_a_record_approved_description
import tuindice.assistant.generated.resources.assistant_card_a_record_failed
import tuindice.assistant.generated.resources.assistant_card_a_record_first_try
import tuindice.assistant.generated.resources.assistant_card_a_record_histogram
import tuindice.assistant.generated.resources.assistant_card_a_record_label
import tuindice.assistant.generated.resources.assistant_card_a_record_repeated
import tuindice.assistant.generated.resources.assistant_card_a_record_retired

// informe.estadisticas/1: the approved subjects as the figure, the other counts as rows, and the grade histogram as
// bars in the accent color, one bar per grade from 1 to 5.
@Composable
fun RecordStatsComponentView(
	component: RecordStatsComponent,
	modifier: Modifier = Modifier
) {
	val counts = listOf(
		Res.string.assistant_card_a_record_failed to component.failed,
		Res.string.assistant_card_a_record_retired to component.retired,
		Res.string.assistant_card_a_record_first_try to component.approvedFirstTry,
		Res.string.assistant_card_a_record_repeated to component.repeated
	)
	val approved = component.approved.toString()
	val largest = component.histogram.maxOrNull() ?: 0

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.recordStats(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_record_label))
		CardFiguresView(
			primary = CardFigure(
				value = approved,
				caption = stringResource(Res.string.assistant_card_a_record_approved_caption),
				contentDescription = stringResource(Res.string.assistant_card_a_record_approved_description, approved)
			)
		)
		counts.forEachIndexed { index, (label, count) ->
			CardRowView(
				name = stringResource(label),
				showsDivider = index > 0,
				trailing = { CardValueView(text = count.toString()) }
			)
		}
		if (component.histogram.isNotEmpty()) {
			CardLabelView(text = stringResource(Res.string.assistant_card_a_record_histogram))
			component.histogram.forEachIndexed { index, count ->
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = (index + 1).toString(),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					CardProgressView(
						fraction = if (largest == 0) 0f else count.toFloat() / largest,
						modifier = Modifier.weight(1f)
					)
					CardValueView(text = count.toString())
				}
			}
		}
	}
}
