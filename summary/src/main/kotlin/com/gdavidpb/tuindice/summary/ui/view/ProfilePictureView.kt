package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gdavidpb.tuindice.summary.R

data class ProfilePictureState(
	val url: String,
	val isLoading: Boolean
)

@Composable
fun rememberProfilePictureState(
	url: String,
	isLoading: Boolean = false
) = remember {
	mutableStateOf(ProfilePictureState(url, isLoading))
}

@Composable
fun ProfilePictureView(
	modifier: Modifier = Modifier,
	state: ProfilePictureState,
	onLoading: (isLoading: Boolean) -> Unit,
	onClick: () -> Unit
) {
	Box(
		modifier = modifier
	) {
		AsyncImage(
			modifier = Modifier
				.size(dimensionResource(id = R.dimen.size_profile))
				.clip(CircleShape),
			model = ImageRequest.Builder(LocalContext.current)
				.data(state.url.ifEmpty { null })
				.crossfade(true)
				.build(),
			error = painterResource(R.drawable.ic_launcher),
			placeholder = painterResource(R.drawable.ic_launcher),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			onLoading = { onLoading(true) },
			onSuccess = { onLoading(false) },
			onError = { onLoading(false) },
		)

		IconButton(
			modifier = Modifier
				.size(dimensionResource(id = R.dimen.dp_32))
				.align(Alignment.BottomEnd),
			enabled = !state.isLoading,
			colors = IconButtonDefaults.filledIconButtonColors(),
			onClick = onClick
		) {
			Icon(
				modifier = Modifier
					.padding(dimensionResource(id = R.dimen.dp_4)),
				imageVector = Icons.Outlined.Edit,
				tint = MaterialTheme.colorScheme.onPrimary,
				contentDescription = null
			)
		}

		AnimatedVisibility(
			modifier = Modifier
				.align(Alignment.Center),
			visible = state.isLoading
		) {
			CircularProgressIndicator()
		}
	}
}