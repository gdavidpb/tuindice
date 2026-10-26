package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.assistant.ui.style.revealStepMillis
import com.gdavidpb.tuindice.assistant.ui.style.revealTotalMillis
import com.gdavidpb.tuindice.assistant.ui.style.wordRanges
import com.gdavidpb.tuindice.base.ui.ReportComposition
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import kotlinx.coroutines.delay

// The text of an answer that is typed in: the words appear one after another, each fading in, starting
// [delayMillis] after it is composed, in the colour of [style]. The whole text is laid out and exposed to
// accessibility from the first frame. While the words fade in, the text is drawn once, in its final colour, and
// each word that has not fully appeared is erased from it in the draw phase: no frame of the reveal recomposes the
// view or lays the text out again. With [animate] false, or with animations off, it is plain text.
@Composable
fun AssistantRevealedTextView(
	text: String,
	animate: Boolean,
	modifier: Modifier = Modifier,
	delayMillis: Int = 0,
	style: TextStyle = LocalTextStyle.current
) {
	ReportComposition("revealed-text")

	val words = remember(text) { wordRanges(text) }

	if (!animate || !LocalTuIndiceAnimationsEnabled.current || words.isEmpty()) {
		Text(text = text, modifier = modifier, style = style)
		return
	}

	val step = revealStepMillis(words.size)
	val total = revealTotalMillis(words.size)
	val elapsed = remember { Animatable(0f) }
	// Changes once, when the reveal ends: the view recomposes then, and only then, to show the plain text.
	val isRevealed by remember(total) { derivedStateOf { elapsed.value >= total } }
	var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

	LaunchedEffect(Unit) {
		delay(delayMillis.toLong())
		elapsed.animateTo(
			targetValue = total,
			animationSpec = tween(durationMillis = total.toInt(), easing = LinearEasing)
		)
	}

	if (isRevealed) {
		Text(text = text, modifier = modifier, style = style)
		return
	}

	Text(
		text = text,
		modifier = modifier
			.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
			.drawWithCache {
				val paths = wordPaths(layout = layout, words = words)

				onDrawWithContent {
					drawContent()
					eraseHiddenWords(paths = paths, elapsed = elapsed.value, step = step)
				}
			},
		style = style,
		onTextLayout = { layout = it }
	)
}

// The outline of each word in [layout]; none until the layout is known, so nothing is erased before then.
private fun wordPaths(layout: TextLayoutResult?, words: List<IntRange>): List<Path> =
	if (layout == null) {
		emptyList()
	} else {
		words.map { range -> layout.getPathForRange(range.first, range.last + 1) }
	}

// Erases from the text just drawn the part of each word still hidden at [elapsed] milliseconds of the reveal.
private fun DrawScope.eraseHiddenWords(paths: List<Path>, elapsed: Float, step: Float) {
	paths.forEachIndexed { index, path ->
		val alpha = wordAlpha(elapsed = elapsed, index = index, step = step)

		if (alpha < 1f) {
			drawPath(path, Color.Black, alpha = 1f - alpha, blendMode = BlendMode.DstOut)
		}
	}
}

// How far the word at [index] has faded in at [elapsed] milliseconds of the reveal: 0 before it starts, 1 once it
// is fully visible.
private fun wordAlpha(elapsed: Float, index: Int, step: Float): Float =
	((elapsed - index * step) / AssistantChatMotion.WordFadeMillis).coerceIn(0f, 1f)
