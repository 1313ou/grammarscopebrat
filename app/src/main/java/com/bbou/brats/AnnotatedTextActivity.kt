package com.bbou.brats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AnnotatedTextActivity : AppCompatActivity() {

    lateinit var textView: AnnotatedTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.annotated_text)
        val textControl = findViewById<AnnotatedTextControl>(R.id.annotated_text_control)
        textView = findViewById<AnnotatedTextView>(textControl.textViewId)
        textView.post {
            textView.prepare()
        }
    }
}
