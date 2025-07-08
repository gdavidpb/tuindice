package com.gdavidpb.tuindice.about.data.repository

import android.content.Context
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.utils.extension.versionDescription

class AboutDataRepository(
	private val context: Context
) : AboutRepository {
	override suspend fun getVersionDescription(): String {
		return context.versionDescription()
	}
}