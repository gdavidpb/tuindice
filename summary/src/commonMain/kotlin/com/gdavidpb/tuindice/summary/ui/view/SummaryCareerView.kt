package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardIcon
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_pensum
import tuindice.summary.generated.resources.summary_career_invite_message
import tuindice.summary.generated.resources.summary_career_invite_title
import tuindice.summary.generated.resources.summary_link_pensum
import tuindice.summary.generated.resources.summary_section_career

/**
 * "Carrera": how much of the pensum the student has approved, in credits and as a share, with a bar. When no pensum was
 * ever calculated the card invites to open it, with the bar empty. Its link, the progress card and the invitation
 * itself, open Pensum.
 */
@Composable
fun SummaryCareerView(
	career: SummaryCareerItem,
	actions: SummaryHubActions,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.CareerSection),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		SummarySectionHeaderView(
			title = stringResource(Res.string.summary_section_career),
			linkText = stringResource(Res.string.summary_link_pensum),
			onLinkClick = { actions.onTargetClick(SummaryHubTarget.Pensum) },
			sectionTag = SummaryUiTags.CareerSection
		)

		when (career) {
			is SummaryCareerItem.Progress -> SummaryCareerProgressCardView(
				career = career,
				onClick = { actions.onTargetClick(SummaryHubTarget.Pensum) }
			)

			SummaryCareerItem.Invite -> SummaryCardView(
				item = SummaryCardItem(
					icon = SummaryCardIcon.Career,
					titleText = UiText.Resource(Res.string.summary_career_invite_title),
					messageText = UiText.Resource(Res.string.summary_career_invite_message),
					target = SummaryHubTarget.Pensum,
					targetLabel = UiText.Resource(Res.string.a11y_summary_open_pensum)
				),
				actions = actions,
				modifier = Modifier.testTag(SummaryUiTags.CareerInviteCard),
				footer = { SummaryCareerBarView(fraction = 0f, description = null) }
			)
		}
	}
}
