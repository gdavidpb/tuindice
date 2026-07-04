// Fixture de semgrep --test para kmp-portability.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
package com.gdavidpb.tuindice.sample.presentation

// ruleid: kmp-no-android-import
import android.content.Context
// ok: kmp-no-android-import
import androidx.lifecycle.ViewModel
// ruleid: kmp-no-java-import
import java.io.InputStream
// ok: kmp-no-java-import
import kotlinx.io.Buffer
// ruleid: kmp-no-koin-androidx-import
import org.koin.androidx.viewmodel.dsl.viewModelOf
// ok: kmp-no-koin-androidx-import
import org.koin.compose.viewmodel.koinViewModel
// ruleid: kmp-no-firebase-import
import com.google.firebase.analytics.FirebaseAnalytics
// ruleid: kmp-no-firebase-import
import dev.gitlive.firebase.Firebase
// ruleid: kmp-no-buildconfig
import com.gdavidpb.tuindice.BuildConfig

fun sampleVersion(): String {
	// ruleid: kmp-no-buildconfig
	return BuildConfig.VERSION_NAME
}

fun sampleEnvironment(environment: String): String {
	// ok: kmp-no-buildconfig
	return environment
}
