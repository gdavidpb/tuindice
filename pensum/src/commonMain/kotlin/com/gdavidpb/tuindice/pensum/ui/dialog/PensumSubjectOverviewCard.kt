package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.view.PensumElementShape
import com.gdavidpb.tuindice.pensum.ui.view.pensumGraphColors
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_subject_detail_credits
import tuindice.pensum.generated.resources.pensum_subject_detail_no_term
import tuindice.pensum.generated.resources.pensum_subject_detail_term

@Composable
fun PensumSubjectOverviewCard(
	node: PensumNodeItem
) {
	val graphColors = pensumGraphColors()
	val detail = node.detail

	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = PensumElementShape,
		color = graphColors.panelBackground,
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant
		)
	) {
		Column(
			modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Column(
				modifier = Modifier.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(10.dp)
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					PensumStyledSubjectCodeChip(
						modifier = Modifier.testTag(PensumUiTags.SubjectDetailCode),
						code = detail.code,
						visualStyle = node.visualStyle
					)
					PensumSubjectStatusBadge(status = detail.status)
				}
				Text(
					modifier = Modifier.testTag(PensumUiTags.SubjectDetailName),
					text = detail.name,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.SemiBold,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				PensumSubjectDetailMeta(
					modifier = Modifier.weight(1f),
					label = stringResource(Res.string.pensum_subject_detail_term),
					value = detail.termLabel ?: stringResource(Res.string.pensum_subject_detail_no_term),
					valueTag = PensumUiTags.SubjectDetailTermValue
				)
				PensumSubjectDetailMeta(
					modifier = Modifier.weight(1f),
					label = stringResource(Res.string.pensum_subject_detail_credits),
					value = detail.creditsText
				)
			}
		}
	}
}
