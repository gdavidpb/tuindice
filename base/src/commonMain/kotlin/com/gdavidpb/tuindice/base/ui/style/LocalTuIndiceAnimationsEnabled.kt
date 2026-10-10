package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.runtime.compositionLocalOf

/**
 * Whether looping animations may run. The hosts turn it off when the system asks for reduced
 * motion (iOS), when the animator duration scale is 0 (Android) and under E2E.
 *
 * Honoured by the sign-in pattern, the logging-in message flipper, the Lottie illustrations
 * (frozen on their first frame), the pulsing halo and the summary sync rotation. The Material
 * indeterminate progress indicators are deliberately not covered: they exist only while work is
 * in flight and the toolkit offers no static form of them.
 */
val LocalTuIndiceAnimationsEnabled = compositionLocalOf { true }
