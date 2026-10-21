package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import org.jetbrains.compose.resources.stringResource
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.a11y_more_options

/**
 * The ⋮ button of the top bar and the menu it opens with the actions that do not fit inline.
 *
 * The text of each item is the content description of its action. An action whose description is null has no text to
 * show, so its item is not drawn: whoever passes overflow actions must give a description to every one of them.
 */
@Composable
fun TopAppBarOverflowMenuView(
	actions: List<TopBarAction>,
	onAction: (action: TopBarAction) -> Unit,
	actionContentDescription: @Composable (action: TopBarAction) -> String?
) {
	val expanded = remember { mutableStateOf(false) }

	// The menu is anchored to the box around the button, so it opens under the ⋮ and not under the whole row.
	Box {
		IconButton(
			onClick = {
				expanded.value = true
			},
			modifier = Modifier.testTag(BaseUiTags.TopAppBarOverflowButton)
		) {
			Icon(
				imageVector = Icons.Outlined.MoreVert,
				contentDescription = stringResource(Res.string.a11y_more_options)
			)
		}

		DropdownMenu(
			modifier = Modifier.exposeTestTagsAsResourceId(),
			expanded = expanded.value,
			onDismissRequest = {
				expanded.value = false
			}
		) {
			actions.forEach { action ->
				key(action) {
					val label = actionContentDescription(action)

					if (label != null) {
						DropdownMenuItem(
							text = { Text(text = label) },
							onClick = {
								expanded.value = false
								onAction(action)
							},
							modifier = Modifier.testTag(BaseUiTags.topBarOverflowItem(action))
						)
					}
				}
			}
		}
	}
}
