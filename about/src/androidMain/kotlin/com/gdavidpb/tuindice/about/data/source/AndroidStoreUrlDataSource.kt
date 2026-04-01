package com.gdavidpb.tuindice.about.data.source

import android.content.Context
import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataRepository

class AndroidStoreUrlDataSource(
	private val context: Context
) : StoreUrlDataRepository {
	override fun getStoreUrl(): String {
		return "market://details?id=${context.packageName}"
	}
}
