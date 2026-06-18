package com.gdavidpb.tuindice.data.repository.playcore

import com.gdavidpb.tuindice.data.model.playcore.PlayCoreSurface

interface PlayCoreEnvironmentDataRepository {
	fun getGooglePlayServicesStatus(): Int
	fun isPlayStoreAvailable(): Boolean
	fun isPlayCoreServiceAvailable(surface: PlayCoreSurface): Boolean
}
