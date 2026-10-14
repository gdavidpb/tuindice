package com.gdavidpb.tuindice.summary.data.source

/**
 * Directory, inside the platform's temporary storage, where a shared image is written. The FileProvider
 * of the app grants the same name in `res/xml/paths.xml`. Sign-out clears that storage, so a shared image
 * does not outlive the session.
 */
const val IMAGE_SHARE_DIRECTORY_NAME = "statistics_share"
