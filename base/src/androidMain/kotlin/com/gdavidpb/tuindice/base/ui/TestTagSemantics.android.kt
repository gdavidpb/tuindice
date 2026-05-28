package com.gdavidpb.tuindice.base.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

actual fun Modifier.exposeTestTagsAsResourceId(): Modifier =
	semantics { testTagsAsResourceId = true }
