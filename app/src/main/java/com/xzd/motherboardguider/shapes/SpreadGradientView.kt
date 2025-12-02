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

class SpreadGradientView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val shader = LinearGradient(
            0f, height / 2f, width.toFloat(), height / 2f,
            intArrayOf(
                Color.TRANSPARENT,
                Color.parseColor("#44FFFFFF"), // 中间亮
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )

        paint.shader = shader

        // 加模糊扩散（关键）
        paint.maskFilter = BlurMaskFilter(50f, BlurMaskFilter.Blur.NORMAL)

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}