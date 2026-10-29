package com.gdavidpb.tuindice.about.presentation.resource

// The text the about machine puts in its internal events. The machine reads it through this seam, so its walk runs on
// any host without platform resources.
interface AboutTextProvider {
	suspend fun versionUnavailable(): String
}
