package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.utils.THRESHOLD_EVALUATION_SWIPE

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EvaluationsView(
	lazyListState: LazyListState,
	evaluations: List<EvaluationsGroupItem>,
	onEvaluationClick: (evaluationId: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit
) {
	LazyColumn(
		state = lazyListState
	) {
		evaluations.forEach { (title, items) ->
			stickyHeader {
				EvaluationHeaderView(label = title)
			}

			items(
				items = items,
				key = { evaluation -> evaluation.evaluationId }
			) { evaluation ->
				val dismissProgress = remember { mutableFloatStateOf(0f) }

				val dismissState = rememberDismissState(
					positionalThreshold = { _ -> 0f },
					confirmValueChange = { confirmValue ->
						if (dismissProgress.floatValue > THRESHOLD_EVALUATION_SWIPE)
							when (confirmValue) {
								DismissValue.DismissedToEnd ->
									onEvaluationEdit(
										evaluation.evaluationId
									)

								DismissValue.DismissedToStart -> {
									onEvaluationDelete(
										evaluation.evaluationId
									)
								}

								else -> {}
							}

						false
					}
				)

				dismissProgress.floatValue = dismissState.progress

				EvaluationSwipeToDismiss(
					state = dismissState
				) {
					EvaluationItemView(
						modifier = Modifier
							.clickable {
								if (evaluation.isClickable)
									onEvaluationClick(
										evaluation.evaluationId
									)
							}
							.animateItemPlacement(),
						item = evaluation
					)
				}
			}
		}
	}
}