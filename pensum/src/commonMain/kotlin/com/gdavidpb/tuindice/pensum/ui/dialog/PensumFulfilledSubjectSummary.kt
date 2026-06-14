package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumFulfilledSubjectItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeVisualStyle
import com.gdavidpb.tuindice.pensum.ui.view.PensumElementShape
import com.gdavidpb.tuindice.pensum.ui.view.pensumGraphColors
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_subject_detail_fulfilled_by

@Composable
fun PensumFulfilledSubjectSummary(
	fulfilledSubject: PensumFulfilledSubjectItem,
	visualStyle: PensumNodeVisualStyle
) {
	val graphColors = pensumGraphColors()
	Surface(
		modifier = Modifier.fillMaxWidth(),
		shape = PensumElementShape,
		color = graphColors.panelBackground,
		border = BorderStroke(
			width = 1.dp,
			color = graphColors.current.copy(alpha = 0.34f)
		)
	) {
		Column(
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
			verticalArrangement = Arrangement.spacedBy(3.dp)
		) {
			Text(
				text = stringResource(Res.string.pensum_subject_detail_fulfilled_by),
				style = MaterialTheme.typography.labelSmall,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			PensumStyledSubjectCodeChip(
				code = fulfilledSubject.code,
				visualStyle = visualStyle
			)
			Text(
				text = fulfilledSubject.name,
				style = MaterialTheme.typography.bodySmall,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}
