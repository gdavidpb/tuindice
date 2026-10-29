package com.gdavidpb.tuindice.base.ui.keyboard

import androidx.compose.ui.text.input.KeyboardType

/**
 * The keyboard of a field that takes digits and goes on to the next field from its return key. On Android, the number
 * pad, which has the IME's action key. On iOS, the ASCII keyboard: digits behind its 123 key, and a return key. The iOS
 * number pad has no return key, so from it the student could not go on to the password.
 */
expect val digitsKeyboardType: KeyboardType
