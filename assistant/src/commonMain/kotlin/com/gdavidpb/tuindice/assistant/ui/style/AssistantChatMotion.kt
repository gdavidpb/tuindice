package com.gdavidpb.tuindice.assistant.ui.style

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAnimation
import kotlin.math.min

/**
 * The motion of the assistant chat (variant «Escritura»): what enters, how long it takes and how the parts of an
 * answer follow one another. Every duration is in milliseconds. Only what arrives while the sheet is open moves; with
 * `LocalTuIndiceAnimationsEnabled` off nothing moves at all.
 */
object AssistantChatMotion {
	/** A message entering: the student's bubble and each part of an answer fade in while rising [MessageRise]. */
	const val MessageEnterMillis = TuIndiceAnimation.StandardMillis

	/** A small item entering: the notice, the offer, the context chip, the progress label. */
	const val SmallEnterMillis = TuIndiceAnimation.CrossfadeMillis

	/** Anything leaving, and the swap of an icon or a tint. */
	const val ExitMillis = 150

	/** The gap between the parts of an answer (text, cards, actions). */
	const val StaggerMillis = 60

	/** The gap between the suggestion chips. */
	const val SuggestionStaggerMillis = 50

	/** The pop of a thumb or a flag that becomes selected: 1 → [PopScale] → 1. */
	const val SelectionPopMillis = 260
	const val PopScale = 1.25f

	/** An answer's text appears word by word: one word every [WordStepMillis], each fading in over [WordFadeMillis]. */
	const val WordStepMillis = 30
	const val WordFadeMillis = 120

	/** The whole reveal of a text, last fade included, never takes longer than this. */
	const val RevealMaxMillis = 1200

	val MessageRise: Dp = 8.dp
	val SuggestionRise: Dp = 6.dp
}

/** The spans of the words of [text]: each run of characters that are not whitespace. */
internal fun wordRanges(text: String): List<IntRange> {
	val ranges = mutableListOf<IntRange>()
	var start = -1

	text.forEachIndexed { index, char ->
		if (char.isWhitespace()) {
			if (start >= 0) ranges.add(start until index)
			start = -1
		} else if (start < 0) {
			start = index
		}
	}
	if (start >= 0) ranges.add(start until text.length)

	return ranges
}

/** The step between two words of a text of [wordCount] words: 30 ms, or less when it would overrun the cap. */
internal fun revealStepMillis(wordCount: Int): Float =
	if (wordCount <= 0) {
		0f
	} else {
		min(
			AssistantChatMotion.WordStepMillis.toFloat(),
			(AssistantChatMotion.RevealMaxMillis - AssistantChatMotion.WordFadeMillis).toFloat() / wordCount
		)
	}

/** How long the reveal of a text of [wordCount] words lasts, from its first word starting to its last one finishing. */
internal fun revealTotalMillis(wordCount: Int): Float =
	if (wordCount <= 0) 0f else revealStepMillis(wordCount) * (wordCount - 1) + AssistantChatMotion.WordFadeMillis
