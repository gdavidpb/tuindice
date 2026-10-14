package com.gdavidpb.tuindice.summary.ui.image

import androidx.compose.ui.graphics.ImageBitmap

/** Encodes this bitmap as a PNG, the format of every shared image. */
expect fun ImageBitmap.toPngBytes(): ByteArray
