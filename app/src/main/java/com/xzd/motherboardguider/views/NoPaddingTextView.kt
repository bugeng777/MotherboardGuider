package com.xzd.motherboardguider.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.xzd.motherboardguider.R


class NoPaddingTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    
    private val textBounds = Rect()
    private var removeFontPadding = true

    init {
        includeFontPadding = false
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.NoPaddingTextView)
            removeFontPadding = typedArray.getBoolean(
                R.styleable.NoPaddingTextView_removeDefaultPadding, 
                true
            )
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (removeFontPadding && text.isNotEmpty()) {
            calculateTextBounds()
            val width = paddingLeft + textBounds.width() + paddingRight
            val height = paddingTop + textBounds.height() + paddingBottom
            setMeasuredDimension(
                resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec)
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (removeFontPadding && text.isNotEmpty()) {
            val layout = layout ?: return
            val textPaint = paint.apply {
                color = currentTextColor
            }
            
            // 计算文本的实际顶部偏移（textBounds.top 通常是负数）
            val topOffset = -textBounds.top
            
            // 绘制每一行
            for (i in 0 until layout.lineCount) {
                val lineStart = layout.getLineStart(i)
                val lineEnd = layout.getLineEnd(i)
                val lineText = text.subSequence(lineStart, lineEnd)
                
                val x = paddingLeft + layout.getLineLeft(i)
                val baselineY = layout.getLineBaseline(i)
                val y = paddingTop + baselineY + topOffset
                
                canvas.drawText(lineText.toString(), x, y.toFloat(), textPaint)
            }
        } else {
            super.onDraw(canvas)
        }
    }

    private fun calculateTextBounds() {
        val text = text.toString()
        if (text.isNotEmpty()) {
            paint.apply {
                textSize = this@NoPaddingTextView.textSize
                typeface = this@NoPaddingTextView.typeface
            }.getTextBounds(text, 0, text.length, textBounds)
        } else {
            textBounds.setEmpty()
        }
    }
}