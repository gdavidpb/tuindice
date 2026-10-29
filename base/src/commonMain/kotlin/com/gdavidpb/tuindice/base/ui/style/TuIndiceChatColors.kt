package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tones of the student's bubble in the assistant chat: the top tones of the two-tone rule, which is the documented
 * exception to that rule (docs/design-system.md, «Chat del asistente»).
 *
 * The tones are ARGB constants, so no numeric literal sits in a property initializer.
 */
object TuIndiceChatColors {
	private const val UserBubbleLight = 0xFFE5E4DDL
	private const val UserBubbleOutlineLight = 0xFFD3D1C8L
	private const val UserBubbleDark = 0xFF2A2A26L
	private const val UserBubbleOutlineDark = 0xFF3D3A33L

	@Composable
	fun chatUserBubble(): Color =
		if (TuIndiceDarkTheme.isDark()) Color(UserBubbleDark) else Color(UserBubbleLight)

	@Composable
	fun chatUserBubbleOutline(): Color =
		if (TuIndiceDarkTheme.isDark()) Color(UserBubbleOutlineDark) else Color(UserBubbleOutlineLight)
}
