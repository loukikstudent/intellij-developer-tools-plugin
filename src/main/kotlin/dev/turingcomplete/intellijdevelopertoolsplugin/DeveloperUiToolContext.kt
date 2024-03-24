package dev.turingcomplete.intellijdevelopertoolsplugin

/**
 * Context information from the extension point in the `plugin.xml`.
 */
data class DeveloperUiToolContext(
  val id: String,
  val prioritizeVerticalLayout: Boolean
) {
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exported Methods -------------------------------------------------------------------------------------------- //
  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
  // -- Companion Object -------------------------------------------------------------------------------------------- //
}