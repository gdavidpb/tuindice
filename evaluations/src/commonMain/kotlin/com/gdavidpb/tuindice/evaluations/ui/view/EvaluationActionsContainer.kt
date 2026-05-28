package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BoxScope.EvaluationActionsContainer(
	modifier: Modifier = Modifier,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	Box(
		modifier = modifier,
		contentAlignment = Alignment.CenterEnd
	) {
		EvaluationActions(
			modifier = Modifier
				.width(ActionsWidth)
				.fillMaxHeight(),
			onEdit = onEdit,
			onDelete = onDelete
		)
	}
}
