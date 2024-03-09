package dev.turingcomplete.intellijdevelopertoolsplugin._internal.tool.editor.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import dev.turingcomplete.intellijdevelopertoolsplugin._internal.tool.editor.PsiKotlinUtils

/**
 * Some code parts of this class are only available of the optional dependency
 * `org.jetbrains.kotlin` is available.
 */
internal class TextCaseConverterKotlinCodeIntentionAction : TextCaseConverterIntentionAction() {
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exported Methods -------------------------------------------------------------------------------------------- //

  override fun getFamilyName(): String = "Convert text case of Kotlin string or identifier"

  override fun getText(): String = "Convert text case"

  override fun getSourceText(editor: Editor, file: PsiFile): Pair<String, TextRange>? {
    val psiElement = file.findElementAt(editor.caretModel.offset) ?: return null
    return PsiKotlinUtils.getTextFromStringValueOrIdentifier(psiElement)?.let { it to psiElement.textRange }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
  // -- Companion Object -------------------------------------------------------------------------------------------- //
}