package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.login.R
import kotlinx.coroutines.delay

@Composable
fun FlipperView(items: List<String>) {
	var targetIndex by remember {
		mutableStateOf(0)
	}

	LaunchedEffect(Unit) {
		while (true) {
			delay(2000)
			targetIndex = (targetIndex + 1) % items.size
		}
	}

	AnimatedContent(
		targetState = targetIndex,
		transitionSpec = {
			(slideInHorizontally { width -> width } + fadeIn())
				.togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
				.using(SizeTransform(clip = false))
		}
	) { index ->
		Box(
			modifier = Modifier
				.fillMaxWidth()
		) {
			Text(
				modifier = Modifier
					.padding(horizontal = dimensionResource(id = R.dimen.dp_16))
					.align(alignment = Alignment.Center),
				text = items[index],
				fontWeight = FontWeight.Medium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.primary
			)
		}
	}
}