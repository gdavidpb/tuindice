package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundResponse
import com.gdavidpb.tuindice.summary.data.model.ShareBackgroundsResponse
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundSettingsDataRepository
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

class ShareBackgroundSettingsDataSource(
	private val settings: Settings
) : ShareBackgroundSettingsDataRepository {
	override suspend fun getLastCatalog(): List<ShareBackgroundResponse>? {
		return settings.getStringOrNull(PreferencesKeys.SHARE_BACKGROUNDS_CATALOG)
			?.let { content -> decodeShareBackgroundsCatalog(content) }
	}

	override suspend fun saveLastCatalog(backgrounds: List<ShareBackgroundResponse>) {
		settings.putString(
			PreferencesKeys.SHARE_BACKGROUNDS_CATALOG,
			encodeShareBackgroundsCatalog(backgrounds)
		)
	}

	override suspend fun getPreferredBackgroundId(): String? {
		return settings.getStringOrNull(PreferencesKeys.SHARE_BACKGROUND_ID)
	}

	override suspend fun setPreferredBackgroundId(id: String) {
		settings.putString(PreferencesKeys.SHARE_BACKGROUND_ID, id)
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
