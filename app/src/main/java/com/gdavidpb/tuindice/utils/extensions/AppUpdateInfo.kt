package com.gdavidpb.tuindice.utils.extensions

import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.UpdateAvailability

val AppUpdateInfo.isUpdateAvailable: Boolean
    get() = (updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE)

val AppUpdateInfo.isUpdateStalled: Boolean
    get() = (updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)