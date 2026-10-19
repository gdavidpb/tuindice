package com.gdavidpb.tuindice.base.ui.keyboard

import androidx.compose.ui.text.input.KeyboardType

// Compose maps KeyboardType.Ascii to UIKeyboardTypeASCIICapable (1). It maps no value to the
// numbers-and-punctuation keyboard (2), the one that would show only digits.
actual val digitsKeyboardType: KeyboardType
	get() = KeyboardType.Ascii
