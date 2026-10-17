package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_component_detail

// The row that opens a component shown on its own sheet: «Ver detalle» with its chevron.
@Composable
fun ChatComponentDetailRowView(
	messageId: String,
	componentId: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.testTag(AssistantUiTags.Component.detail(messageId, componentId))
			.clickable(role = Role.Button, onClick = onClick)
			.padding(vertical = TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = stringResource(Res.string.assistant_chat_component_detail),
			style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline),
			color = MaterialTheme.colorScheme.onSurface
		)
		Icon(
			imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}
