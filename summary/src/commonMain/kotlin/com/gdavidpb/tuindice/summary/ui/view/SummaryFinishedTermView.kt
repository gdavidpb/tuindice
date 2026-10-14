package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.icon.ShareIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryFinishedTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_record
import tuindice.summary.generated.resources.share_term_share_action

/**
 * The last term that is over: its period with "terminado" beside it as a figure, and under it two rows, its index as
 * Registro shows it and the credits it approved. The body leads to Registro. A term the share sheet has a card for also
 * offers "Compartir" beside the body, never inside it, which asks for the sheet on that term through [onShareClick].
 */
@Composable
fun SummaryFinishedTermView(
	item: SummaryFinishedTermItem,
	onClick: () -> Unit,
	onShareClick: (termKey: String) -> Unit,
	modifier: Modifier = Modifier
) {
	SummaryHubCardView(modifier = modifier) {
		// The padding is inside the body's touch area, so the whole top of the card leads to Registro.
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.testTag(SummaryUiTags.TermFinishedCard)
				.clickable(
					onClickLabel = stringResource(Res.string.a11y_summary_open_record),
					role = Role.Button,
					onClick = onClick
				)
				.padding(TuIndiceSpacing.Screen),
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

		if (item.isShareable) {
			FinishedTermShareButton(onClick = { onShareClick(item.termKey) })
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

// The button that shares the term: a sibling of the body, never inside it, with the share icon of the platform and the
// sheet's own words for sharing.
@Composable
private fun FinishedTermShareButton(onClick: () -> Unit) {
	OutlinedButton(
		modifier = Modifier
			.padding(horizontal = TuIndiceSpacing.Screen)
			.padding(bottom = TuIndiceSpacing.Screen)
			.testTag(SummaryUiTags.TermFinishedShare),
		onClick = onClick
	) {
		Icon(
			modifier = Modifier.size(TuIndiceComponentSizes.IconSmall),
			imageVector = ShareIcon,
			contentDescription = null
		)

		Text(
			modifier = Modifier.padding(start = TuIndiceSpacing.Medium),
			text = stringResource(Res.string.share_term_share_action)
		)
	}
}
