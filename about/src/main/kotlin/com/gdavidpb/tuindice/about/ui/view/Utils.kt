package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.about.BuildConfig
import com.gdavidpb.tuindice.about.R

@Composable
@ReadOnlyComposable
fun getVersionName(): String {
	val environmentRes = if (BuildConfig.DEBUG) R.string.debug else R.string.release
	val environmentName = stringResource(environmentRes)

	return stringResource(
		R.string.app_version,
		environmentName,
		BuildConfig.VERSION_NAME,
		BuildConfig.VERSION_CODE
	)
}