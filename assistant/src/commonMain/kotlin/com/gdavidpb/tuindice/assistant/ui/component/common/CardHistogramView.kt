package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.gdavidpb.tuindice.assistant.presentation.model.HistogramBarItem
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The grade histogram of a chat card: one row per grade, its grade, a bar of the data blue as long as its share of the
// longest bar, and the number of subjects in it. Each row reads as one sentence for a screen reader («Nota 4: 16
// materias»); the bar is decoration.
@Composable
fun CardHistogramView(
	bars: List<HistogramBarItem>,
	modifier: Modifier = Modifier
) {
	Column(modifier = modifier.fillMaxWidth()) {
		bars.forEach { bar ->
			val description = bar.description.asString()

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clearAndSetSemantics { contentDescription = description },
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = bar.grade,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				CardProgressView(
					fraction = bar.fraction,
					color = AcademicStatusColors.loadBandLight(),
					modifier = Modifier.weight(1f)
				)
				CardValueView(text = bar.count)
			}
		}
	}
}
