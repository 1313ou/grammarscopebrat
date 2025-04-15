package com.bbou.brats

import android.content.Context
import android.text.TextUtils.TruncateAt
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.setPadding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AnnotatedTextControl(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    private val initialTextSize = 24f
    private val minTextSize = 12f
    private val maxTextSize = 36f
    private val textSizeIncrement = 2f
    private var currentTextSize = initialTextSize

    private val initialSpacing = 350f
    private val minSpacing = 100f
    private val maxSpacing = 500f
    private val spacingIncrement = 50f
    private val lineSpacingMultiplier = 1.0f
    private var currentSpacing = initialSpacing

    private val padding = 16

    private val textView: AppCompatTextView = AnnotatedTextView(context).apply {
        id = generateViewId()
        textSize = initialTextSize
        setLineSpacing(initialSpacing, lineSpacingMultiplier)
        setPadding(padding)
        ellipsize = TruncateAt.END

        text = "First line\nSecond line\nThird line\nFourth line"
    }

    val textViewId = textView.id

    private val increaseButton: FloatingActionButton = FloatingActionButton(context).apply {
        id = generateViewId()
        setImageResource(R.drawable.btn_expand)
    }

    private val decreaseButton: FloatingActionButton = FloatingActionButton(context).apply {
        id = generateViewId()
        setImageResource(R.drawable.btn_collapse)
    }

    init {
        addView(textView, LayoutParams(0, 0))
        addView(increaseButton, ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(decreaseButton, ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

        val constraintSet = ConstraintSet()
        constraintSet.apply {
            clone(this@AnnotatedTextControl)

            // TextView
            connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(textView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            connect(textView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
            connect(textView.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)

            // Increase Button
            connect(increaseButton.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 16)
            connect(increaseButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)

            // Decrease Button
            connect(decreaseButton.id, ConstraintSet.BOTTOM, increaseButton.id, ConstraintSet.TOP, 16)
            connect(decreaseButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)

            applyTo(this@AnnotatedTextControl)
        }

        increaseButton.setOnClickListener {
            increaseSpacing()
        }

        decreaseButton.setOnClickListener {
            decreaseSpacing()
        }
    }

    private fun increaseSpacing() {
        if (currentSpacing < maxSpacing) {
            currentSpacing += spacingIncrement
            textView.setLineSpacing(currentSpacing, lineSpacingMultiplier)
        }
    }

    private fun decreaseSpacing() {
        if (currentSpacing > minSpacing) {
            currentSpacing -= spacingIncrement
            textView.setLineSpacing(currentSpacing, lineSpacingMultiplier)
        }
    }

    private fun increaseTextSize() {
        if (currentTextSize < maxTextSize) {
            currentTextSize += textSizeIncrement
            textView.textSize = currentTextSize
        }
    }

    private fun decreaseTextSize() {
        if (currentTextSize > minTextSize) {
            currentTextSize -= textSizeIncrement
            textView.textSize = currentTextSize
        }
    }

    fun setText(text: String) {
        textView.text = text
    }
}