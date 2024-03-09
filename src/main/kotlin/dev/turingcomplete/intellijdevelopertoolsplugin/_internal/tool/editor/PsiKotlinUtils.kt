package dev.turingcomplete.intellijdevelopertoolsplugin._internal.tool.editor

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import dev.turingcomplete.intellijdevelopertoolsplugin._internal.common.EditorUtils.getSelectedText
import org.jetbrains.kotlin.lexer.KtTokens

/**
 * Some code parts of this class are only available of the optional dependency
 * `org.jetbrains.kotlin` is available.
 */
internal object PsiKotlinUtils {
  // -- Variables --------------------------------------------------------------------------------------------------- //

  private val textElementKtTokens = setOf(KtTokens.REGULAR_STRING_PART, KtTokens.IDENTIFIER)

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exported Methods -------------------------------------------------------------------------------------------- //

  fun getTextFromStringValueOrIdentifier(e: AnActionEvent): Pair<String, TextRange>? {
    val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return null
    val editor = e.getData(CommonDataKeys.EDITOR) ?: return null
    if (editor.getSelectedText() != null) {
      return null
    }

    val psiElement = psiFile.findElementAt(editor.caretModel.offset) ?: return null
    return getTextFromStringValueOrIdentifier(psiElement)?.let { it to psiElement.textRange }
  }

  fun getTextFromStringValueOrIdentifier(psiElement: PsiElement): String? {
    return if (textElementKtTokens.contains(psiElement.elementType)) {
      psiElement.text
    }
    else {
      null
    }
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}