package com.bbou.brats

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import grammarscope.SemanticGraphRenderer
import grammarscope.document.SampleDocument

class AnnotatedTextActivity : AppCompatActivity() {

    lateinit var textView: AnnotatedTextView

    lateinit var renderer: SemanticGraphRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.annotated_text)
        val textControl = findViewById<AnnotatedTextControl>(R.id.annotated_text_control)
        textView = findViewById<AnnotatedTextView>(textControl.textViewId)
        textView.post {
            textView.prepare()
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
