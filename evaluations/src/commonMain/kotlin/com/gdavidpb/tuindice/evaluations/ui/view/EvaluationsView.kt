package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem

@Composable
fun EvaluationsView(
	lazyListState: LazyListState,
	evaluations: List<EvaluationsGroupItem>,
	onEvaluationClick: (evaluationId: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit,
	itemContainer: @Composable (
		evaluation: EvaluationItem,
		onEvaluationEdit: () -> Unit,
		onEvaluationDelete: () -> Unit,
		content: @Composable () -> Unit
	) -> Unit
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
				itemContainer(
					evaluation,
					{
						onEvaluationEdit(evaluation.evaluationId)
					},
					{
						onEvaluationDelete(evaluation.evaluationId)
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
