package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.gdavidpb.tuindice.base.R

@Composable
fun EmptyView(
	title: String,
	message: String,
	action: String? = null,
	onActionClick: () -> Unit = {}
) {
	val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.an_empty))

	Column(
		modifier = Modifier
			.padding(horizontal = dimensionResource(id = R.dimen.dp_24))
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally

	) {
		LottieAnimation(
			modifier = Modifier
				.size(256.dp),
			iterations = LottieConstants.IterateForever,
			composition = lottieComposition
		)

		Text(
			text = title,
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Medium
		)

		Text(
			modifier = Modifier
				.padding(vertical = dimensionResource(id = R.dimen.dp_16)),
			text = message,
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.bodyMedium
		)

		if (action != null)
			Button(onClick = onActionClick) {
				Text(
					text = action
				)
			}
	}
}