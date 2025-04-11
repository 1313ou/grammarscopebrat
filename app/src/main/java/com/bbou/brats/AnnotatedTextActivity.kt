package com.bbou.brats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import grammarscope.SemanticGraphRenderer
import grammarscope.document.SampleDocument

class AnnotatedTextActivity : AppCompatActivity() {

    lateinit var textView: AnnotatedTextView

    lateinit var renderer: SemanticGraphRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.annotated_text)
        textView = findViewById<AnnotatedTextView>(R.id.annotated_text)

        val lineSpacingExtra = 200f
        val lineSpacingMultiplier = 1.0f // Default multiplier
        textView.setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)

        textView.post {
            prepare()
        }
    }

    fun prepare() {
        val document = SampleDocument()
        val text = document.text
        textView.text = text

        // Highlight root
        val rootPos = findWordPosition(text, "gave")
        if (rootPos != null) {
            textView.highlightWord(rootPos.first, rootPos.second, "#FFEB3B".toColorInt())
        }

        // renderer
        renderer = SemanticGraphRenderer(textView, true)
        renderer.layout(document, textView)
        //textView.annotations.addAll(renderer.boxes)
        textView.annotations.addAll(renderer.edges)
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
