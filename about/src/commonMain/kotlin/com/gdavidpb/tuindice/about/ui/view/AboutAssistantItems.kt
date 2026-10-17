package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_assistant_consent
import tuindice.about.generated.resources.about_assistant_memory

@Composable
fun AboutAssistantItems(
	consentGranted: Boolean,
	onConsentChange: (granted: Boolean) -> Unit,
	onMemoryClick: () -> Unit
) {
	AboutSwitchItem(
		icon = rememberVectorPainter(rememberAssistantSparkleIcon()),
		text = stringResource(Res.string.about_assistant_consent),
		checked = consentGranted,
		onCheckedChange = onConsentChange,
		testTag = AboutUiTags.AssistantConsentSwitch,
		// The sparkle paints its own color, as it does in the tabs and the swipe actions.
		tint = Color.Unspecified
	)

	AboutItem(
		icon = rememberVectorPainter(Icons.Outlined.Psychology),
		text = stringResource(Res.string.about_assistant_memory),
		testTag = AboutUiTags.AssistantMemoryItem,
		tint = MaterialTheme.colorScheme.onSurfaceVariant
	) { onMemoryClick() }
}
