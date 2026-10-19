package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.assistant.ui.style.revealStepMillis
import com.gdavidpb.tuindice.assistant.ui.style.revealTotalMillis
import com.gdavidpb.tuindice.assistant.ui.style.wordRanges
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import kotlinx.coroutines.delay

// The text of an answer that is typed in: the words appear one after another, each fading in, starting
// [delayMillis] after it is composed, in the colour of [style]. The whole text is laid out and exposed
// to accessibility from the first frame; the words that have not appeared are drawn in a transparent
// colour inside the same string, so nothing reflows while it plays. With [animate] false, or with
// animations off, it is plain text.
@Composable
fun AssistantRevealedTextView(
	text: String,
	animate: Boolean,
	modifier: Modifier = Modifier,
	delayMillis: Int = 0,
	style: TextStyle = LocalTextStyle.current
) {
	val words = remember(text) { wordRanges(text) }
	val color = style.color.takeOrElse { MaterialTheme.colorScheme.onSurface }

	if (!animate || !LocalTuIndiceAnimationsEnabled.current || words.isEmpty()) {
		Text(text = text, modifier = modifier, style = style)
		return
	}

	val step = revealStepMillis(words.size)
	val total = revealTotalMillis(words.size)
	val elapsed = remember { Animatable(0f) }

	LaunchedEffect(Unit) {
		delay(delayMillis.toLong())
		elapsed.animateTo(
			targetValue = total,
			animationSpec = tween(durationMillis = total.toInt(), easing = LinearEasing)
		)
	}

	Text(
		text = revealedText(text = text, words = words, step = step, elapsed = elapsed.value, color = color),
		modifier = modifier,
		style = style
	)
}

private fun revealedText(
	text: String,
	words: List<IntRange>,
	step: Float,
	elapsed: Float,
	color: Color
): AnnotatedString =
	buildAnnotatedString {
		append(text)
		words.forEachIndexed { index, range ->
			val alpha = ((elapsed - index * step) / AssistantChatMotion.WordFadeMillis).coerceIn(0f, 1f)

			if (alpha < 1f) {
				addStyle(SpanStyle(color = color.copy(alpha = alpha)), range.first, range.last + 1)
			}
		}
	}
