package com.gdavidpb.tuindice.data.repository.playcore

import com.gdavidpb.tuindice.data.model.playcore.PlayCoreSurface

interface PlayCoreAvailabilityDataRepository {
	fun isAvailable(surface: PlayCoreSurface): Boolean
}
