package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.about.generated.AboutDependencyTexts
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.model.AboutActions
import org.jetbrains.compose.resources.painterResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.ic_compose
import tuindice.about.generated.resources.ic_firebase
import tuindice.about.generated.resources.ic_koin
import tuindice.about.generated.resources.ic_kotlin
import tuindice.about.generated.resources.ic_ktor

@Composable
fun AboutLibrariesItems(actions: AboutActions) {
	AboutItem(
		icon = painterResource(Res.drawable.ic_kotlin),
		text = AboutDependencyTexts.kotlinDescription,
		testTag = AboutUiTags.OpenKotlin,
		size = 48.dp,
		scale = 0.85f
	) { actions.onKotlinClick() }

	AboutItem(
		icon = painterResource(Res.drawable.ic_compose),
		text = AboutDependencyTexts.composeDescription,
		testTag = AboutUiTags.OpenCompose,
		size = 48.dp
	) { actions.onComposeClick() }

	AboutItem(
		icon = painterResource(Res.drawable.ic_firebase),
		text = AboutDependencyTexts.firebaseDescription,
		testTag = AboutUiTags.OpenFirebase,
		size = 48.dp
	) { actions.onFirebaseClick() }

	AboutItem(
		icon = painterResource(Res.drawable.ic_koin),
		text = AboutDependencyTexts.koinDescription,
		testTag = AboutUiTags.OpenKoin,
		size = 48.dp
	) { actions.onKoinClick() }

	AboutItem(
		icon = painterResource(Res.drawable.ic_ktor),
		text = AboutDependencyTexts.ktorDescription,
		testTag = AboutUiTags.OpenKtor,
		size = 48.dp
	) { actions.onKtorClick() }
}
