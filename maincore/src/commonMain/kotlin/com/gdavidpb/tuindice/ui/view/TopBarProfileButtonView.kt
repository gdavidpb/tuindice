package com.gdavidpb.tuindice.ui.view

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.summary.ui.view.ProfileAvatarView
import com.gdavidpb.tuindice.ui.MaincoreUiTags

/**
 * The last place of the top bar of a main section: the student's round avatar, which opens Settings. The image is
 * 32 dp inside the 48 dp touch target of an icon button. It shows the picture the app already holds ([identity]);
 * without one, or before the user is stored, it is the neutral person icon. [contentDescription] is what a screen
 * reader says for it.
 */
@Composable
fun TopBarProfileButtonView(
	identity: ProfileIdentity?,
	contentDescription: String,
	modifier: Modifier = Modifier,
	onClick: () -> Unit
) {
	IconButton(
		onClick = onClick,
		modifier = modifier
			.testTag(MaincoreUiTags.TuIndiceTopBarSettingsButton)
			.semantics {
				this.contentDescription = contentDescription
			}
	) {
		ProfileAvatarView(
			pictureUrl = identity?.pictureUrl.orEmpty(),
			pictureVersion = identity?.pictureVersion ?: 0,
			size = TuIndiceComponentSizes.AvatarSmall
		)
	}
}
