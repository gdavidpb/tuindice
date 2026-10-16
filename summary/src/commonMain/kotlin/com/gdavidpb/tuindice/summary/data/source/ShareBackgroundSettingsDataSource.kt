package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse
import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundsResponse
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundSettingsDataRepository
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

class ShareBackgroundSettingsDataSource(
	private val settings: Settings
) : ShareBackgroundSettingsDataRepository, SessionMemory {
	// The catalog that was saved last, read from the store once and written through on each save.
	private val lastCatalog = MutableStateFlow(readLastCatalog())

	override fun observeLastCatalog(): Flow<List<ShareBackgroundResponse>?> {
		return lastCatalog
	}

	override suspend fun saveLastCatalog(backgrounds: List<ShareBackgroundResponse>) {
		settings.putString(
			PreferencesKeys.SHARE_BACKGROUNDS_CATALOG,
			encodeShareBackgroundsCatalog(backgrounds)
		)
		lastCatalog.value = backgrounds
	}

	// The mirror goes back to what is stored, which the wipe has just emptied.
	override suspend fun clearSessionMemory() {
		lastCatalog.value = readLastCatalog()
	}

	private fun readLastCatalog(): List<ShareBackgroundResponse>? {
		return settings.getStringOrNull(PreferencesKeys.SHARE_BACKGROUNDS_CATALOG)
			?.let { content -> decodeShareBackgroundsCatalog(content) }
	}
}

// What is stored is the catalog as it was read. A stored text that no longer reads is no catalog at all.
internal fun decodeShareBackgroundsCatalog(content: String): List<ShareBackgroundResponse>? {
	return runCatching {
		catalogJson.decodeFromString(ShareBackgroundsResponse.serializer(), content).backgrounds
	}.getOrNull()
}

internal fun encodeShareBackgroundsCatalog(backgrounds: List<ShareBackgroundResponse>): String {
	return catalogJson.encodeToString(ShareBackgroundsResponse.serializer(), ShareBackgroundsResponse(backgrounds))
}

private val catalogJson = Json { ignoreUnknownKeys = true }
