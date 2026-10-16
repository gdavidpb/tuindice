package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryFinishedTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_record

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

			SummaryFinishedTermRowView(
				label = item.indexLabel.asString(),
				value = item.indexText,
				valueTag = SummaryUiTags.TermFinishedIndex
			)

			SummaryFinishedTermRowView(
				label = item.creditsLabel.asString(),
				value = item.creditsText.asString(),
				valueTag = SummaryUiTags.TermFinishedCredits
			)
		}

		if (item.isShareable) {
			SummaryFinishedTermShareButton(onClick = { onShareClick(item.termKey) })
		}
	}
}
