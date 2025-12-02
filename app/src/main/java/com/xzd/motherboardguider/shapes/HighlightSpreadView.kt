package com.xzd.motherboardguider.shapes

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class HighlightSpreadView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f

        // 中间的高光线颜色（可以调）
        val highlightColor = Color.parseColor("#66FFFFFF") // 中间最亮
        val fadeColor = Color.parseColor("#00FFFFFF")      // 透明

        // 渐变：从透明 → 亮 → 透明
        val shader = LinearGradient(
            0f, centerY(), width.toFloat(), centerY(),
            intArrayOf(fadeColor, highlightColor, fadeColor),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )

        paint.shader = shader

        // 模糊，让高光自然扩散（模糊半径建议≈高度的一半）
        paint.maskFilter = BlurMaskFilter(height / 2f, BlurMaskFilter.Blur.NORMAL)

        // 绘制长条
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun centerY() = height / 2f
}
