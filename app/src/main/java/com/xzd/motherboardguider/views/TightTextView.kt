package com.xzd.motherboardguider.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

class TightTextView(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    override fun onDraw(canvas: Canvas) {
        // 调整绘制的Y轴偏移，减少顶部/底部空白
        val layout = layout
        val textPaint = paint
        textPaint.color = currentTextColor
        for (i in 0 until layout.lineCount) {
            val lineBaseline = layout.getLineBaseline(i)
            val lineText = layout.text.subSequence(layout.getLineStart(i), layout.getLineEnd(i))
            // 直接绘制文字，忽略默认的Ascent/Descent
            canvas.drawText(lineText.toString(), 0f, lineBaseline.toFloat(), textPaint)
        }
    }
}