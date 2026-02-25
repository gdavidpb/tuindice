package com.gdavidpb.tuindice.login.ui.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.ic_launcher

@Composable
fun SignInHeader() {
	Image(
		modifier = Modifier.padding(vertical = 32.dp),
		painter = painterResource(Res.drawable.ic_launcher),
		contentDescription = null
	)
}
