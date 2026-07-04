// Fixture de semgrep --test para transition-purity.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// Regla con fixture propio: su patrón matchea cualquier declaración de tipo,
// así que compartir fixture con otras familias colisionaría.
package com.gdavidpb.tuindice.sample.presentation.transition

// ruleid: transition-files-pure
class RogueTransitionHolder {
	fun rows(): Int = 1
}

// ruleid: transition-files-pure
internal object RogueTransitionCache

// ok: transition-files-pure
fun sampleIdleTransitions(machine: Any): Any = machine
