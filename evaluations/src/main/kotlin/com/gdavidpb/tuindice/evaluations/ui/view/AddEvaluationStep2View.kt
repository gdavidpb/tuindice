package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation

@Composable
fun AddEvaluationStep2View(
	modifier: Modifier = Modifier,
	state: AddEvaluation.State.Step2,
	onDateChange: (date: Long) -> Unit,
	onNextStepClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
	) {
		Column(
			modifier = modifier
				.padding(dimensionResource(id = R.dimen.dp_16))
		) {
			Text(
				text = stringResource(id = R.string.label_add_evaluation_date),
				style = MaterialTheme.typography.headlineLarge
			)

			EvaluationDatePicker(
				modifier = modifier
					.padding(top = dimensionResource(id = R.dimen.dp_16)),
				selectedDate = state.date,
				onDateChange = onDateChange
			)
		}

		FloatingActionButton(
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(dimensionResource(id = R.dimen.dp_24)),
			containerColor = MaterialTheme.colorScheme.primary,
			onClick = onNextStepClick
		) {
			Icon(
				imageVector = Icons.Outlined.Done,
				contentDescription = null
			)
		}
	}
}