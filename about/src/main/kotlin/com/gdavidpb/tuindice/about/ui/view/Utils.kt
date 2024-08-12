package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.about.R
import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.versionCode
import com.gdavidpb.tuindice.base.utils.extension.versionName

@Composable
@ReadOnlyComposable
fun getVersionName(): String {
	val environmentRes = if (BuildConfig.DEBUG) R.string.debug else R.string.release
	val environmentName = stringResource(environmentRes)
	val context = LocalContext.current

	val versionName = context.versionName()
	val versionCode = context.versionCode()

	return stringResource(
		R.string.app_version,
		environmentName,
		versionName,
		versionCode
	)
}