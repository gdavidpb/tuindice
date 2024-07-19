package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.base.presentation.navigation.Destination

fun Destination.toDestinationName(): String = when (this) {
	is Destination.Record -> "record"
	is Destination.Evaluations -> "evaluations"
	is Destination.About -> "about"
	else -> throw IllegalArgumentException()
}

fun String.toDestination(): Destination = when (this) {
	"record" -> Destination.Record
	"evaluations" -> Destination.Evaluations
	"about" -> Destination.About
	else -> throw IllegalArgumentException()
}