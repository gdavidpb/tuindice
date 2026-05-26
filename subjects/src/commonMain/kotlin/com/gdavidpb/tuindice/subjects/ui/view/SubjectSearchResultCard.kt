package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_search_result_content_description

@Composable
fun SubjectSearchResultCard(
	item: SubjectSearchResultItem,
	onClick: () -> Unit
) {
	val codeColors = remember(item.subjectCode) {
		CourseCodeColorGenerator.fromCode(item.subjectCode)
	}
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(SubjectsUiTags.searchResult(item.subjectCode))
			.clickable(onClick = onClick),
		shape = RoundedCornerShape(14.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier
					.background(codeColors.containerColor, RoundedCornerShape(10.dp))
					.padding(horizontal = 12.dp, vertical = 8.dp),
				text = item.subjectCode,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = codeColors.color,
				maxLines = 1
			)
			Column(
				modifier = Modifier
					.weight(1f)
					.padding(start = 16.dp, end = 12.dp)
			) {
				Text(
					text = item.name,
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = item.creditsText,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			IconButton(
				modifier = Modifier
					.size(40.dp)
					.testTag(SubjectsUiTags.searchResultStatsButton(item.subjectCode)),
				onClick = onClick
			) {
				Icon(
					modifier = Modifier.size(22.dp),
					imageVector = Icons.Outlined.BarChart,
					contentDescription = stringResource(
						Res.string.subjects_search_result_content_description,
						item.subjectCode
					),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}
