package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
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
 * "Carrera": how much of the pensum the student has approved, in credits and as a share, with a
 * bar. When no pensum was ever calculated the card invites to open it, with the bar empty. Its link,
 * the progress card and the invitation itself, open Pensum.
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
			is SummaryCareerItem.Progress -> CareerProgressCard(
				career = career,
				onClick = { actions.onTargetClick(SummaryHubTarget.Pensum) }
			)

			SummaryCareerItem.Invite -> CareerInviteCard(onClick = { actions.onTargetClick(SummaryHubTarget.Pensum) })
		}
	}
}

@Composable
private fun CareerProgressCard(
	career: SummaryCareerItem.Progress,
	onClick: () -> Unit
) {
	SummaryHubCardView(
		modifier = Modifier
			.testTag(SummaryUiTags.CareerCard)
			.clickable(
				onClickLabel = stringResource(Res.string.a11y_summary_open_pensum),
				role = Role.Button,
				onClick = onClick
			)
	) {
		Column(
			modifier = Modifier.padding(TuIndiceSpacing.Screen),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
				verticalAlignment = Alignment.Top
			) {
				SummaryFigureText(
					modifier = Modifier.weight(1f).alignByBaseline(),
					figure = career.valueText,
					rest = career.restText.asString()
				)

				Text(
					modifier = Modifier.testTag(SummaryUiTags.CareerPercentText).alignByBaseline(),
					text = career.percentText.asString(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
				)
			}

			CareerBar(fraction = career.fraction, description = career.description.asString())
		}
	}
}

@Composable
private fun CareerInviteCard(onClick: () -> Unit) {
	SummaryHubCardView(
		modifier = Modifier
			.testTag(SummaryUiTags.CareerInviteCard)
			.clickable(
				onClickLabel = stringResource(Res.string.a11y_summary_open_pensum),
				role = Role.Button,
				onClick = onClick
			)
	) {
		Column(
			modifier = Modifier.padding(TuIndiceSpacing.Screen),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)
		) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					modifier = Modifier.size(TuIndiceComponentSizes.Icon),
					imageVector = Icons.Outlined.AccountTree,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)

				Column(
					modifier = Modifier.weight(1f),
					verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two)
				) {
					Text(
						text = stringResource(Res.string.summary_career_invite_title),
						style = MaterialTheme.typography.bodyLarge,
						fontWeight = FontWeight.SemiBold
					)

					Text(
						text = stringResource(Res.string.summary_career_invite_message),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
					)
				}

				Icon(
					modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
					imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Muted)
				)
			}

			CareerBar(fraction = 0f, description = null)
		}
	}
}

// The track with the share of it that is approved, in the green of "approved" everywhere else. With
// a description it is a progress element for a screen reader; without one (the empty bar of the
// invitation) it is only decoration.
@Composable
private fun CareerBar(fraction: Float, description: String?) {
	val shape = RoundedCornerShape(TuIndiceRadius.Full)
	val semantics = if (description != null) {
		Modifier
			.testTag(SummaryUiTags.CareerProgressBar)
			.clearAndSetSemantics {
				contentDescription = description
				progressBarRangeInfo = ProgressBarRangeInfo(current = fraction, range = 0f..1f)
			}
	} else {
		Modifier.clearAndSetSemantics { }
	}

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(CareerBarHeight)
			.clip(shape)
			.background(MaterialTheme.colorScheme.outlineVariant)
			.then(semantics)
	) {
		Box(
			modifier = Modifier
				.fillMaxHeight()
				.fillMaxWidth(fraction.coerceIn(0f, 1f))
				.background(AcademicStatusColors.approved())
		)
	}
}

private val CareerBarHeight = 6.dp
