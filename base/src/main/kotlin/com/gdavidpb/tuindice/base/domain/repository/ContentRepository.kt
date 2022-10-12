package com.gdavidpb.tuindice.base.domain.repository

import android.net.Uri
import java.io.InputStream

interface ContentRepository {
	suspend fun openInputStream(url: Uri): InputStream
}