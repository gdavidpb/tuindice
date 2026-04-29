package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail as SubjectDetailModel

@Composable
fun SubjectDetailHeaderView(
	detail: SubjectDetailModel
) {
	val colors = remember(detail.id) { CourseCodeColorGenerator.fromCode(detail.id) }

	Column(
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
		Text(
			text = detail.name,
			style = MaterialTheme.typography.headlineSmall,
			fontWeight = FontWeight.SemiBold
		)

		Row(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier
					.background(
						color = colors.containerColor,
						shape = RoundedCornerShape(10.dp)
					)
					.padding(horizontal = 12.dp, vertical = 6.dp),
				text = detail.id,
				color = colors.color,
				style = MaterialTheme.typography.labelLarge,
				fontWeight = FontWeight.SemiBold
			)

			Text(
				text = "${detail.credits} UC",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			if (detail.gradingMode == GradingMode.QUALITATIVE_PASS_FAIL) {
				Text(
					text = "Cualitativa",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}
