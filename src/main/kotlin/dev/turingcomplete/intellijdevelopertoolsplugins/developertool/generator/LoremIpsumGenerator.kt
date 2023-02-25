@file:Suppress("UnstableApiUsage")

package dev.turingcomplete.intellijdevelopertoolsplugins.developertool.generator

import ai.grazie.utils.capitalize
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComboBoxPredicate
import dev.turingcomplete.intellijdevelopertoolsplugins.*
import dev.turingcomplete.intellijdevelopertoolsplugins.ValidateMinIntValueSide.MAX
import dev.turingcomplete.intellijdevelopertoolsplugins.ValidateMinIntValueSide.MIN
import dev.turingcomplete.intellijdevelopertoolsplugins.developertool.common.GeneralDeveloperTool
import dev.turingcomplete.intellijdevelopertoolsplugins.developertool.generator.LoremIpsumGenerator.TextMode.*
import java.security.SecureRandom
import java.util.*
import kotlin.math.max
import kotlin.math.min

class LoremIpsumGenerator :
  MultiLineTextGenerator(id = "lorem-ipsum", title = "Lorem Ipsum"), GeneralDeveloperTool {
  // -- Properties -------------------------------------------------------------------------------------------------- //

  private var textMode: TextMode by createProperty("generatedTextKind", PARAGRAPHS)
  private var numberOfValues: Int by createProperty("numberOfValues", 9)
  private var minWordsInParagraph: Int by createProperty("minWordsInParagraph", DEFAULT_MIN_PARAGRAPH_WORDS)
  private var maxWordsInParagraph: Int by createProperty("maxWordsInParagraph", DEFAULT_MAX_PARAGRAPH_WORDS)
  private var minWordsInBullet: Int by createProperty("minWordsInBullet", DEFAULT_MIN_BULLET_WORDS)
  private var maxWordsInBullet: Int by createProperty("maxWordsInBullet", DEFAULT_MAX_BULLET_WORDS)
  private var startWithLoremIpsum: Boolean by createProperty("startWithLoremIpsum", true)

  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun generate(): String = when (textMode) {
    WORDS -> generateWords()
    PARAGRAPHS -> generateParagraphs()
    BULLETS -> generateBullets()
  }

  override fun Panel.buildConfigurationUi(project: Project?, parentDisposable: Disposable) {
    lateinit var textModeComboBox: ComboBox<TextMode>
    row {
      textField().text(numberOfValues.toString()).columns(COLUMNS_TINY)
              .validateIntValue(IntRange(1, 999))
              .whenTextChangedFromUi { numberOfValues = it.toInt() }
              .gap(RightGap.SMALL)
      textModeComboBox = comboBox(TextMode.values().toList()).applyToComponent {
        selectedItem = textMode
        onChanged { textMode = it }
      }.component
    }

    row {
      textField().label("Minimum words in paragraph:").columns(COLUMNS_TINY)
              .text(minWordsInParagraph.toString())
              .validateIntValue(IntRange(1, 999))
              .validation(validateMinMaxValueRelation(MIN) { maxWordsInParagraph })
              .whenTextChangedFromUi { minWordsInParagraph = it.toIntOrNull() ?: DEFAULT_MIN_PARAGRAPH_WORDS }
              .gap(RightGap.SMALL)
      textField().label("Maximum:").columns(COLUMNS_TINY)
              .text(maxWordsInParagraph.toString())
              .validateIntValue(IntRange(1, 999))
              .validation(validateMinMaxValueRelation(MAX) { minWordsInParagraph })
              .whenTextChangedFromUi { maxWordsInParagraph = it.toIntOrNull() ?: DEFAULT_MAX_PARAGRAPH_WORDS }
    }.visibleIf(ComboBoxPredicate<TextMode>(textModeComboBox) { it == PARAGRAPHS })

    row {
      textField().label("Minimum words in bullet:").columns(COLUMNS_TINY)
              .text(minWordsInBullet.toString())
              .validateIntValue(IntRange(1, 999))
              .validation(validateMinMaxValueRelation(MIN) { maxWordsInBullet })
              .whenTextChangedFromUi { minWordsInBullet = it.toIntOrNull() ?: DEFAULT_MIN_BULLET_WORDS }
              .gap(RightGap.SMALL)
      textField().label("Maximum:").columns(COLUMNS_TINY)
              .text(maxWordsInBullet.toString())
              .validateIntValue(IntRange(1, 999))
              .validation(validateMinMaxValueRelation(MAX) { minWordsInBullet })
              .whenTextChangedFromUi { maxWordsInBullet = it.toIntOrNull() ?: DEFAULT_MAX_BULLET_WORDS }
    }.visibleIf(ComboBoxPredicate<TextMode>(textModeComboBox) { it == BULLETS })

    row {
      checkBox("<html>Start with iconic <i>Lorem ipsum dolor sit amet…</i></html>").applyToComponent {
        isSelected = startWithLoremIpsum
        onSelectionChanged { startWithLoremIpsum = it }
      }
    }
  }

  fun generateIconicText(atMostWords: Int, isSentence: Boolean): List<String> {
    val words = ICONIC_LOREM_IPSUM_SENTENCE.subList(0, min(ICONIC_LOREM_IPSUM_SENTENCE.size, atMostWords)).toMutableList()

    if (isSentence) {
      val wordsSize = words.size
      // Comma
      if (wordsSize > ICONIC_LOREM_IPSUM_SENTENCE_COMMA_INDEX + 1) {
        words[ICONIC_LOREM_IPSUM_SENTENCE_COMMA_INDEX] = "${words[ICONIC_LOREM_IPSUM_SENTENCE_COMMA_INDEX]},"
      }
      if (wordsSize >= 1) {
        // Capitalize first character
        words[0] = words[0].capitalize()
        // Full stop
        words[wordsSize - 1] = "${words[wordsSize - 1]}."
      }
    }

    return words
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun generateParagraphs() = IntRange(0, numberOfValues - 1).joinToString(PARAGRAPH_SEPARATOR) { paragraphIndex ->

    val totalWordsInParagraph = SECURE_RANDOM.nextInt(minWordsInParagraph, maxWordsInParagraph + 1)

    val initialWords = if (paragraphIndex == 0 && startWithLoremIpsum) {
      generateIconicText(totalWordsInParagraph, true)
    }
    else {
      emptyList()
    }

    createSentences(initialWords, totalWordsInParagraph).joinToString(WORDS_SEPARATOR)
  }

  private fun generateWords(): String {
    val words = mutableListOf<String>()

    if (startWithLoremIpsum) {
      words.addAll(generateIconicText(numberOfValues, false))
    }

    if (words.size < numberOfValues) {
      words.addAll(getRandomWords(numberOfValues - words.size))
    }

    return words.joinToString(WORDS_SEPARATOR)
  }

  private fun generateBullets() = IntRange(0, numberOfValues - 1).joinToString(BULLET_SEPARATOR) { bulletIndex ->
    val words = mutableListOf<String>()

    val totalWordsInBullet = SECURE_RANDOM.nextInt(minWordsInBullet, maxWordsInBullet)

    if (bulletIndex == 0 && startWithLoremIpsum) {
      words.addAll(generateIconicText(totalWordsInBullet, true))
    }

    // Avoid a single word sentence.
    words.addAll(createSentences(words, totalWordsInBullet))

    words[0] = "$BULLET_SYMBOL ${words[0]}"

    words.joinToString(WORDS_SEPARATOR)
  }

  private fun getRandomWords(words: Int): List<String> = IntRange(0, words - 1).asSequence()
          .map { getRandomWord() }
          .toList()

  private fun getRandomWord() = LOREM_IPSUM_WORDS[SECURE_RANDOM.nextInt(LOREM_IPSUM_WORDS.size)]

  private fun createSentence(words: List<String>): List<String> {
    // Divide sentence in n-1 fragments (the last fragment does not get a comma).
    val fragments = Math.floorDiv(words.size, TEXT_FRAGMENT_LENGTH) - 1
    val indiciesOfWordsWithCommas = IntRange(0, fragments - 1)
            // Randomly decide with a 2/3 change to put comma in fragment
            .filter { SECURE_RANDOM.nextInt(1, 4) != 3 }
            // Randomly decide index after the first word
            .map { fragmentIndex ->
              val commaIndexInFragment = SECURE_RANDOM.nextInt(1, TEXT_FRAGMENT_LENGTH)
              (TEXT_FRAGMENT_LENGTH * fragmentIndex) + commaIndexInFragment
            }
            .toSet()

    return words.mapIndexed { i: Int, rawWord: String ->
      var word = rawWord
      if (i == 0) {
        word = word.capitalize()
      }
      if (i == words.size - 1) {
        word = "$word."
      }
      if (indiciesOfWordsWithCommas.contains(i)) {
        word = "$word,"
      }
      word
    }
  }

  private fun createSentences(initialWords: List<String>, totalWords: Int): List<String> {
    val words = initialWords.toMutableList()

    while (words.size < totalWords) {
      val remainingWords = totalWords - words.size
      // With the max we ensue that there are at least `MIN_SENTENCE_WORDS` words.
      val minSentenceWords = max(min(MIN_SENTENCE_WORDS, remainingWords), MIN_SENTENCE_WORDS)
      val maxSentenceWords = max(min(MAX_SENTENCE_WORDS, remainingWords), MIN_SENTENCE_WORDS)
      val sentenceWords = if (minSentenceWords == maxSentenceWords) minSentenceWords else SECURE_RANDOM.nextInt(minSentenceWords, maxSentenceWords)
      val elements = createSentence(getRandomWords(sentenceWords))
      words.addAll(elements)
    }

    return words
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //

  private enum class TextMode(val title: String) {

    PARAGRAPHS("Paragraphs"),
    WORDS("Words"),
    BULLETS("Bullets");

    override fun toString(): String = title
  }

  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {

    private val SECURE_RANDOM = SecureRandom()
    private val ICONIC_LOREM_IPSUM_SENTENCE = listOf("lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit")
    private const val ICONIC_LOREM_IPSUM_SENTENCE_COMMA_INDEX = 4
    private val LOREM_IPSUM_WORDS: List<String> by lazy {
      LoremIpsumGenerator::class.java.getResource("/dev/turingcomplete/intellijdevelopertoolsplugin/lorem-ipsum.txt")!!.readText().lines()
    }
    private const val DEFAULT_MIN_PARAGRAPH_WORDS = 20
    private const val DEFAULT_MAX_PARAGRAPH_WORDS = 100
    private const val DEFAULT_MIN_BULLET_WORDS = 10
    private const val DEFAULT_MAX_BULLET_WORDS = 30
    private const val MIN_SENTENCE_WORDS = 3
    private const val MAX_SENTENCE_WORDS = 40
    private const val TEXT_FRAGMENT_LENGTH = 8
    private val PARAGRAPH_SEPARATOR = System.lineSeparator().repeat(2)
    private const val WORDS_SEPARATOR = " "
    private const val BULLET_SYMBOL = "-"
    private val BULLET_SEPARATOR = System.lineSeparator().repeat(2)
  }
}