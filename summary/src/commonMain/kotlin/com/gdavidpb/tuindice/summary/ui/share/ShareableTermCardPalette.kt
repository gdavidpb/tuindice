package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.ui.graphics.Color

// The card is an image: its ink and its bone are its own, whatever the theme and the font scale of the screen it is
// drawn on, so every part of the card reads them from here.
private const val CARD_INK = 0xFF22271A
private const val CARD_BONE = 0xFFF4EFE3

internal val CardInk = Color(CARD_INK)
internal val CardBone = Color(CARD_BONE)
