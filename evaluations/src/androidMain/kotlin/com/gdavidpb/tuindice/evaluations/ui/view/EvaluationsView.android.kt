package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.lazy.LazyListState
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.utils.THRESHOLD_EVALUATION_SWIPE
import kotlinx.coroutines.launch

@Composable
fun EvaluationsView(
	lazyListState: LazyListState,
	evaluations: List<EvaluationsGroupItem>,
	onEvaluationClick: (evaluationId: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit
) {
	EvaluationsView(
		lazyListState = lazyListState,
		evaluations = evaluations,
		onEvaluationClick = onEvaluationClick,
		onEvaluationEdit = onEvaluationEdit,
		onEvaluationDelete = onEvaluationDelete
	) { _, edit, delete, content ->
		val coroutineScope = rememberCoroutineScope()

		val dismissState = rememberSwipeToDismissBoxState(
			positionalThreshold = { totalDistance ->
				totalDistance * THRESHOLD_EVALUATION_SWIPE
			}
		)

		EvaluationSwipeToDismiss(
			state = dismissState,
			onDismiss = { dismissDirection ->
				when (dismissDirection) {
					SwipeToDismissBoxValue.StartToEnd -> edit()
					SwipeToDismissBoxValue.EndToStart -> delete()
					else -> {}
				}

				coroutineScope.launch {
					dismissState.reset()
				}
			}
		) {
			content()
		}
	}
}
