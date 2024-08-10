package com.gdavidpb.tuindice.about.ui.view

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.about.R
import com.gdavidpb.tuindice.base.BuildConfig

@Composable
@ReadOnlyComposable
fun getVersionName(): String {
	val environmentRes = if (BuildConfig.DEBUG) R.string.debug else R.string.release
	val environmentName = stringResource(environmentRes)
	val context = LocalContext.current

	val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
	val versionName = packageInfo.versionName.toString()
	val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
		packageInfo.longVersionCode
	} else {
		@Suppress("DEPRECATION")
		packageInfo.versionCode.toLong()
	}

	return stringResource(
		R.string.app_version,
		environmentName,
		versionName,
		versionCode
	)
}