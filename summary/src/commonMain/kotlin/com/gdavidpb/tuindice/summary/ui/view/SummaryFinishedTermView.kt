package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryFinishedTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_record

/**
 * The last term that is over: its period with "terminado" beside it as a figure, and under it two rows, its index as
 * Registro shows it and the credits it approved. The whole card leads to Registro.
 */
@Composable
fun SummaryFinishedTermView(
	item: SummaryFinishedTermItem,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	SummaryHubCardView(
		modifier = modifier
			.testTag(SummaryUiTags.TermFinishedCard)
			.clickable(
				onClickLabel = stringResource(Res.string.a11y_summary_open_record),
				role = Role.Button,
				onClick = onClick
			)
	) {
		Column(
			modifier = Modifier.padding(TuIndiceSpacing.Screen),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)
		) {
			SummaryFigureText(
				modifier = Modifier.testTag(SummaryUiTags.TermFinishedName),
				figure = item.nameText.asString(),
				rest = item.stateText.asString()
			)

			FinishedTermRow(
				label = item.indexLabel.asString(),
				value = item.indexText,
				valueTag = SummaryUiTags.TermFinishedIndex
			)

			FinishedTermRow(
				label = item.creditsLabel.asString(),
				value = item.creditsText.asString(),
				valueTag = SummaryUiTags.TermFinishedCredits
			)
		}
	}
}

// A label on the left, lighter, and its value on the right: the line of a figure that is not the card's headline.
@Composable
private fun FinishedTermRow(
	label: String,
	value: String,
	valueTag: String
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
		)

		Text(
			modifier = Modifier.testTag(valueTag),
			text = value,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface
		)
	}
}
