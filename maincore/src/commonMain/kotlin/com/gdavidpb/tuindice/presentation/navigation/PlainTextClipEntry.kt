package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.ui.platform.ClipEntry

// The clipboard entry of a plain text, as each platform builds it.
internal expect fun plainTextClipEntry(text: String): ClipEntry
