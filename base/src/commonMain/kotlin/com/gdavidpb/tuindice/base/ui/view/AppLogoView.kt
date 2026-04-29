package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.ic_app_logo

@Composable
fun AppLogoView(
	modifier: Modifier = Modifier,
	contentDescription: String? = null
) {
	Image(
		modifier = modifier,
		painter = painterResource(Res.drawable.ic_app_logo),
		contentDescription = contentDescription
	)
}
