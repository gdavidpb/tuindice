package com.gdavidpb.tuindice.base.presentation.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.scene.DialogSceneStrategy

/* Non-inline on purpose: keeps navigation3-ui out of feature compile classpaths. */
fun dialogMetadata(properties: DialogProperties = DialogProperties()): Map<String, Any> =
	DialogSceneStrategy.dialog(dialogProperties = properties)
