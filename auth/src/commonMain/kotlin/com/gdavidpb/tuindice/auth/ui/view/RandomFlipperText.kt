package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

@Composable
fun RandomFlipperText(items: List<String>) {
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current

	if (!animationsEnabled) {
		FlipperMessage(text = remember(items) { items.random() })
		return
	}

	val randomTextFlow = remember {
		flow {
			while (true) {
				val text = items.random()
				emit(text)
				delay(items.size * 100L)
			}
		}
	}

	val textState = randomTextFlow
		.collectAsStateWithLifecycle(initialValue = items.random())

	AnimatedContent(
		targetState = textState.value,
		transitionSpec = {
			val enter = slideInHorizontally { x -> x } + fadeIn()
			val exit = slideOutHorizontally { x -> -x } + fadeOut()

			enter togetherWith exit
		}, label = "RandomFlipperTextAnimatedContent"
	) { text ->
		FlipperMessage(text = text)
	}
}

@Composable
private fun FlipperMessage(text: String) {
	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			modifier = Modifier
				.testTag(AuthUiTags.RandomFlipperText)
				.padding(horizontal = 16.dp)
				.align(alignment = Alignment.Center),
			text = text,
			fontWeight = FontWeight.Medium,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.primary
		)
	}
}
