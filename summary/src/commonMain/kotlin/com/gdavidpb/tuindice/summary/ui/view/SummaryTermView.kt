package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_section_term

/**
 * "Trimestre": which week of the term it is, with the track of its twelve weeks and what is left of them, how many
 * subjects it has and their credits, and the points of each subject. Its link, the name of the term, opens the Record;
 * each subject's row opens the Record on that subject's attempt. Outside weeks 1 to 12 the week, what is left of it and
 * its track are not there, and the subjects remain with their line. With no term in progress the link leads to the
 * Record, the last term that is over comes first and then the invitation to plan the next one, or the first one to
 * come. A term with nothing to show yet says so in a card of its own. The last term that is over has a button that
 * opens the share sheet on that term, when the sheet has a card for it.
 */
@Composable
fun SummaryTermView(
	term: SummaryTermItem,
	actions: SummaryHubActions,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.TermSection),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		SummarySectionHeaderView(
			title = stringResource(Res.string.summary_section_term),
			linkText = term.linkText?.asString(),
			onLinkClick = { actions.onTargetClick(SummaryHubTarget.Record) },
			sectionTag = SummaryUiTags.TermSection
		)

		term.finished?.let { finished ->
			SummaryFinishedTermView(
				item = finished,
				onClick = { actions.onTargetClick(SummaryHubTarget.Record) },
				onShareClick = { termKey ->
					actions.onTargetClick(SummaryHubTarget.ShareTerm(termKey = termKey))
				}
			)
		}

		if (term.card == null) {
			SummaryTermCardView(term = term, actions = actions)
		}

		term.card?.let { card ->
			SummaryCardView(
				item = card,
				actions = actions,
				modifier = Modifier.testTag(SummaryUiTags.TermCard)
			)
		}
	}
}
