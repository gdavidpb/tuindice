package com.gdavidpb.tuindice.about.data.source

import android.content.Context
import com.gdavidpb.tuindice.about.domain.repository.StoreUrlRepository

class AndroidStoreUrlDataSource(
	private val context: Context
) : StoreUrlRepository {
	override fun getStoreUrl(): String {
		return "market://details?id=${context.packageName}"
	}
}
