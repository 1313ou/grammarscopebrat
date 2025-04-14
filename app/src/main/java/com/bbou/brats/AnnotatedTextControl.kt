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

    private val textView: AppCompatTextView = AnnotatedTextView(context).apply {
        id = generateViewId()
        textSize = 24f
        setPadding(16)
        ellipsize = TruncateAt.END
        text="First line\nSecond line\nThird line\nFourth line"
    }

    val textViewId = textView.id

    private val increaseButton: FloatingActionButton = FloatingActionButton(context).apply {
        id = generateViewId()
        setImageResource(android.R.drawable.btn_plus)
    }

    private val decreaseButton: FloatingActionButton = FloatingActionButton(context).apply {
        id = generateViewId()
        setImageResource(android.R.drawable.btn_minus)
    }

    private var currentTextSize = 24f // Initial text size
    private val minTextSize = 12f
    private val maxTextSize = 36f
    private val textSizeIncrement = 2f

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
            increaseTextSize()
        }

        decreaseButton.setOnClickListener {
            decreaseTextSize()
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

    fun setText(text : String)
    {
        textView.text = text
    }
}