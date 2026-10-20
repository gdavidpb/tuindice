package com.gdavidpb.tuindice.assistant.ui.model

// The tone of a card's title. A card that limits the student (a warning, a pause, a suspension) gives its title the
// tone of its gravity; the card around it stays neutral.
enum class CardTitleTone {
	Neutral,
	Warning,
	Error
}
