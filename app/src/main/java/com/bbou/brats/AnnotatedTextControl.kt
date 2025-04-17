package com.bbou.brats

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils.TruncateAt
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AnnotatedTextControl(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    private val initialTextSize = 24f
    private val minTextSize = 12f
    private val maxTextSize = 50f
    private val textSizeIncrement = 2f
    private var currentTextSize = initialTextSize

    private val initialSpacing = 400f
    private val minSpacing = 100f
    private val maxSpacing = 750f
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
    }

    val textViewId = textView.id

    private val increaseSpacingButton: FloatingActionButton = createTransparentFab(context, R.drawable.btn_plus).apply {
        id = generateViewId()
    }

    private val decreaseSpacingButton: FloatingActionButton = createTransparentFab(context, R.drawable.btn_minus).apply {
        id = generateViewId()
    }

    private val increaseTextSizeButton: FloatingActionButton = createTransparentFab(context, R.drawable.btn_expand).apply {
        id = generateViewId()
    }

    private val decreaseTextSizeButton: FloatingActionButton = createTransparentFab(context, R.drawable.btn_collapse).apply {
        id = generateViewId()
    }

    init {
        addView(textView, LayoutParams(0, 0))
        addView(increaseSpacingButton, ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(decreaseSpacingButton, ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(increaseTextSizeButton, ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(decreaseTextSizeButton, ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

        val constraintSet = ConstraintSet()
        constraintSet.apply {
            clone(this@AnnotatedTextControl)

            // TextView
            connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(textView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            connect(textView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
            connect(textView.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)

            // Increase Spacing Button
            connect(increaseSpacingButton.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 16)
            connect(increaseSpacingButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)

            // Decrease Spacing Button
            connect(decreaseSpacingButton.id, ConstraintSet.BOTTOM, increaseSpacingButton.id, ConstraintSet.TOP, 16)
            connect(decreaseSpacingButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)

            // Increase TextSize Button
            connect(increaseTextSizeButton.id, ConstraintSet.BOTTOM, decreaseSpacingButton.id, ConstraintSet.TOP, 16)
            connect(increaseTextSizeButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)

            // Decrease TextSize Button
            connect(decreaseTextSizeButton.id, ConstraintSet.BOTTOM, increaseTextSizeButton.id, ConstraintSet.TOP, 16)
            connect(decreaseTextSizeButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)

            applyTo(this@AnnotatedTextControl)
        }

        increaseSpacingButton.setOnClickListener {
            increaseSpacing()
        }

        decreaseSpacingButton.setOnClickListener {
            decreaseSpacing()
        }

        increaseTextSizeButton.setOnClickListener {
            increaseTextSize()
        }

        decreaseTextSizeButton.setOnClickListener {
            decreaseTextSize()
        }
    }

    fun createTransparentFab0(context: Context, icon: Int): FloatingActionButton {
        val fab = FloatingActionButton(context).apply {
            // Set the icon
            setImageDrawable(ContextCompat.getDrawable(context, icon))

            // Remove background tint
            backgroundTintList = null

            // Remove elevation (shadow)
            elevation = 0f

            // Remove the background
            background = null

            // Add padding to prevent the icon from being cut off.
            val padding = context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 2
            setPadding(padding, padding, padding, padding)
        }
        return fab
    }

    fun createTransparentFab(context: Context, icon: Int): FloatingActionButton {

        // Create a ContextThemeWrapper with your style
        val contextThemeWrapper: Context = ContextThemeWrapper(context, R.style.TransparentFab)
        val fab = FloatingActionButton(contextThemeWrapper).apply {

            // Set transparent background
            backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            compatElevation = 0f
            elevation = 0f

            // Set icon
            setImageResource(icon)

            imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.btn_color))
        }
        return fab
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