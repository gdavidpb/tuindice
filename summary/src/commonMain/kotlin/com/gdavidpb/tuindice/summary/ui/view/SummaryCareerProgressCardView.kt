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
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_pensum

/**
 * The card of the career with the pensum in progress: the approved credits in figures with the share beside them, and
 * the [SummaryCareerBarView] under them. The whole card leads to Pensum through [onClick].
 */
@Composable
fun SummaryCareerProgressCardView(
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

			SummaryCareerBarView(fraction = career.fraction, description = career.description.asString())
		}
	}
}
