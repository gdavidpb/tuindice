package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.runtime.compositionLocalOf

/**
 * True while the screen that owns the form is waiting on the view model (signing in, updating the
 * password) and the view model drops the edits it receives. A key typed just before the wait reaches
 * the screen, or while the content is still on screen, is shown by the field but dropped, so the text
 * fields readopt the state's text when it turns on and when it turns off. It changes nothing when no
 * key was dropped.
 */
internal val LocalFormWaiting = compositionLocalOf { false }
