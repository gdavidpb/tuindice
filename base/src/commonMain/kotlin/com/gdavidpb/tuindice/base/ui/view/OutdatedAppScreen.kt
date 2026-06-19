package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.outdated_app_action
import tuindice.base.generated.resources.outdated_app_message
import tuindice.base.generated.resources.outdated_app_title

@Composable
fun OutdatedAppScreen(
	title: String = stringResource(Res.string.outdated_app_title),
	message: String = stringResource(Res.string.outdated_app_message),
	actionLabel: String = stringResource(Res.string.outdated_app_action),
	onUpdateClick: () -> Unit
) {
	Surface(
		modifier = Modifier
			.testTag(BaseUiTags.OutdatedAppScreen)
			.fillMaxSize(),
		color = MaterialTheme.colorScheme.background,
		contentColor = MaterialTheme.colorScheme.onBackground
	) {
		IllustratedMessageView(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 24.dp),
			title = title,
			message = message,
			actionLabel = actionLabel,
			onActionClick = onUpdateClick,
			verticalArrangement = Arrangement.Center,
			titleTestTag = BaseUiTags.OutdatedAppTitle,
			messageTestTag = BaseUiTags.OutdatedAppMessage,
			actionTestTag = BaseUiTags.OutdatedAppUpdateButton,
			headerContent = { OutdatedAppAnimationView() }
		)
	}
}
