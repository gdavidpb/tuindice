package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.evaluations.R

@Composable
fun EvaluationsEmptyView() {
	val message = stringResource(id = R.string.label_empty_evaluations)

	val annotatedString = remember {
		buildAnnotatedString {
			append(message)

			val startFirstLine = 0
			val endFirstLine = message.indexOf("\n")

			addStyle(
				style = SpanStyle(fontWeight = FontWeight.Medium),
				start = startFirstLine,
				end = endFirstLine
			)
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Image(
			imageVector = ImageVector.vectorResource(id = R.drawable.il_evaluations_empty),
			contentDescription = null
		)

		Text(
			modifier = Modifier
				.padding(dimensionResource(id = R.dimen.dp_24)),
			text = annotatedString,
			textAlign = TextAlign.Center,
			fontSize = MaterialTheme.typography.titleMedium.fontSize
		)
	}
}