package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

inline fun <reified T : Any> config(crossinline block: ConfigRepository.() -> T) =
	lazy {
		val configRepository = ConfigKoinComponent.get<ConfigRepository>()

		block(configRepository)
	}

object ConfigKoinComponent : KoinComponent
