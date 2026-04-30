package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.utils.THRESHOLD_EVALUATION_SWIPE
import kotlinx.coroutines.launch

@Composable
fun EvaluationsView(
	lazyListState: LazyListState,
	evaluations: List<EvaluationsGroupItem>,
	onEvaluationClick: (evaluationId: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit,
	scrollEnabled: Boolean = true
) {
	LazyColumn(
		modifier = Modifier.testTag(EvaluationsUiTags.EvaluationsList),
		state = lazyListState,
		userScrollEnabled = scrollEnabled
	) {
		evaluations.forEach { (title, items) ->
			stickyHeader {
				EvaluationHeaderView(label = title)
			}

			items(
				items = items,
				key = { evaluation -> evaluation.evaluationId }
			) { evaluation ->
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
							SwipeToDismissBoxValue.StartToEnd ->
								onEvaluationEdit(evaluation.evaluationId)

							SwipeToDismissBoxValue.EndToStart ->
								onEvaluationDelete(evaluation.evaluationId)

							else -> Unit
						}

						coroutineScope.launch {
							dismissState.reset()
						}
					}
				) {
					EvaluationItemView(
						modifier = Modifier
							.clickable {
								if (evaluation.isClickable)
									onEvaluationClick(
										evaluation.evaluationId
									)
							}
							.animateItem(),
						item = evaluation
					)
				}
			}
		}
	}
}
