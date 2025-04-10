package com.bbou.brats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt

class AnnotatedTextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.annotated_text)
        val textView = findViewById<AnnotatedTextView>(R.id.annotated_text)
        val lineSpacingExtra = 200f
        val lineSpacingMultiplier = 1.0f // Default multiplier

        textView.setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
        fill(textView)
    }

    fun fill(textView: AnnotatedTextView) {
        val text = """
            This is the first line of text.
            Here is another line with some important concepts.
            The third line shows relationships between words.
            This is the final line of our example.""".trimIndent()
        textView.text = text

        // Find word positions
        val firstLineWordPos = findWordPosition(text, "first")
        val importantWordPos = findWordPosition(text, "important")
        val conceptsWordPos = findWordPosition(text, "concepts")
        val relationshipsWordPos = findWordPosition(text, "relationships")
        val wordsWordPos = findWordPosition(text, "words")

        // Add arrow from "important" to "relationships" (across multiple lines)
        if (importantWordPos != null && relationshipsWordPos != null) {
            textView.addArrow(
                importantWordPos.first, importantWordPos.second,
                relationshipsWordPos.first, relationshipsWordPos.second
            )
        }

        // Add circle icon below "concepts"
        if (conceptsWordPos != null) {
            textView.addIcon(
                conceptsWordPos.first, conceptsWordPos.second,
                IconType.CIRCLE
            )
        }

        // Add star icon below "first"
        if (firstLineWordPos != null) {
            textView.addIcon(
                firstLineWordPos.first, firstLineWordPos.second,
                IconType.STAR
            )
        }

        // Add arrow down icon below "words"
        if (wordsWordPos != null) {
            textView.addIcon(
                wordsWordPos.first, wordsWordPos.second,
                IconType.ARROW_DOWN
            )
        }

        // Highlight some words
        if (importantWordPos != null) {
            textView.highlightWord(importantWordPos.first, importantWordPos.second, "#FFEB3B".toColorInt())
        }

        if (relationshipsWordPos != null) {
            textView.highlightWord(relationshipsWordPos.first, relationshipsWordPos.second, "#B3E5FC".toColorInt())
        }
    }

    /**
     * Helper method to find a word's position in text
     */
    private fun findWordPosition(text: String, word: String): Pair<Int, Int>? {
        val index = text.indexOf(word)
        if (index != -1) {
            return Pair(index, index + word.length)
        }
        return null
    }
}
