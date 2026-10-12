package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.utils.extension.attempt
import io.github.vinceglb.filekit.PlatformFile

// What the wipe of the real platforms does (AndroidApplicationDataSource, IosApplicationDataSource), in
// the same order: the session memory is released first and again at the end, whatever happens, and in
// between the database ([database] is where a test stands for it), the settings and the secure stores
// are emptied. That is what makes the session gone from its first step on, which a double that only
// counts calls hides.
internal class ProductionLikeApplicationRepository(
	private val settings: SettingsRepository,
	private val stores: List<SecureKeyValueDataRepository>,
	private val memory: List<SessionMemory>,
	private val database: suspend () -> Unit = {}
) : ApplicationRepository {
	var clearCalls = 0
		private set

	override suspend fun clearData() {
		clearCalls++

		releaseSessionMemory()

		try {
			database()
			settings.clear()
			stores.forEach { store -> attempt { store.clear() } }
		} finally {
			releaseSessionMemory()
		}
	}

	override suspend fun clearSessionResidue() = Unit

	override suspend fun canOpen(file: PlatformFile): Boolean = true

	private suspend fun releaseSessionMemory() {
		memory.forEach { holder -> attempt { holder.clearSessionMemory() } }
	}
}
