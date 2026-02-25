package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.domain.repository.ConfigGateway
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

inline fun <reified T : Any> config(crossinline block: ConfigGateway.() -> T) =
	lazy {
		val configRepository = ConfigKoinComponent.get<ConfigGateway>()

		block(configRepository)
	}

object ConfigKoinComponent : KoinComponent
