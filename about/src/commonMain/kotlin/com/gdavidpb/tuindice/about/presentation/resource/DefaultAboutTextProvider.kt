package com.gdavidpb.tuindice.about.presentation.resource

import org.jetbrains.compose.resources.getString
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_version_unavailable

class DefaultAboutTextProvider : AboutTextProvider {
	override suspend fun versionUnavailable(): String = getString(Res.string.about_version_unavailable)
}
