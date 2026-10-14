package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.ui.view.arrivalHighlight
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.AttemptArrivalHighlight

@Composable
fun AttemptCardItemView(
	modifier: Modifier = Modifier,
	item: AttemptItem,
	gradeState: MutableIntState? = null,
	onGradeChange: (newGrade: Int?, newOutcome: AttemptOutcome?, isSelected: Boolean) -> Unit,
	// The arrival highlight a jump plays on this card, while it plays.
	arrivalHighlight: AttemptArrivalHighlight? = null
) {
	// The margin is outside the card, so the highlight frames the card's own shape and not the space around it.
	Box(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = 16.dp,
				vertical = 6.dp
			)
	) {
		Card(
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant,
				contentColor = MaterialTheme.colorScheme.onSurface
			),
			modifier = Modifier
				.fillMaxWidth()
				.testTag(RecordUiTags.attemptCard(item.attemptId))
				.arrivalHighlight(
					trigger = arrivalHighlight?.trigger,
					shape = CardDefaults.shape,
					onFinished = { arrivalHighlight?.onFinished?.invoke() }
				)
		) {
			AttemptItemView(
				item = item,
				gradeState = gradeState,
				onGradeChange = onGradeChange
			)
		}
	}
}
