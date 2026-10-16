package com.gdavidpb.tuindice.summary.ui.view

import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest

/**
 * One picture of the profile stack, keyed by its [tag] and loaded from its [request]. A layer that [showsPlaceholder]
 * wears the placeholder while it loads and when it fails, and one that [showsFallback] wears it when it has no picture
 * at all. [onState] hears how the loading of the layer goes, when it has a listener.
 */
class ProfilePictureLayer(
	val tag: String,
	val request: ImageRequest,
	val showsPlaceholder: Boolean = false,
	val showsFallback: Boolean = false,
	val onState: ((AsyncImagePainter.State) -> Unit)? = null
)
