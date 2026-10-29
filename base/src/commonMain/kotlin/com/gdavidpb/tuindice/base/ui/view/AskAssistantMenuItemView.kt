package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.icon.assistantSparkleTint
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import org.jetbrains.compose.resources.stringResource
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.assistant_ask_about_this

/**
 * The «Preguntar sobre esto» item of a menu, which sends the question about the screen to the assistant. It leads with
 * the sparkle in the assistant's tint, as every way to the assistant does. It is drawn only when [onAsk] is given, so a
 * screen without a way to reach the assistant shows no item. Place it inside a DropdownMenu.
 */
@Composable
fun AskAssistantMenuItemView(
	onAsk: (() -> Unit)?,
	modifier: Modifier = Modifier
) {
	if (onAsk == null) return

	DropdownMenuItem(
		text = { Text(text = stringResource(Res.string.assistant_ask_about_this)) },
		onClick = onAsk,
		leadingIcon = {
			Icon(
				imageVector = rememberAssistantSparkleIcon(),
				contentDescription = null,
				modifier = Modifier
					.size(SPARKLE_SIZE_DP.dp)
					.testTag(BaseUiTags.askAssistant(SPARKLE_SUFFIX)),
				tint = assistantSparkleTint()
			)
		},
		modifier = modifier.testTag(BaseUiTags.askAssistant(MENU_ITEM_SUFFIX))
	)
}

private const val MENU_ITEM_SUFFIX = "menu_item"
private const val SPARKLE_SUFFIX = "menu_item_sparkle"
private const val SPARKLE_SIZE_DP = 18
