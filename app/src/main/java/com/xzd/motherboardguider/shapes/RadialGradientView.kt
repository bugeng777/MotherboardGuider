package com.xzd.motherboardguider.shapes

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class RadialGradientView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var radius = 50f // 初始半径
    private val paint = Paint()

    init {
        // 简单动画
        ValueAnimator.ofFloat(50f, 800f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener {
                radius = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val shader = RadialGradient(
            width / 2f,
            height / 2f,
            radius,
            Color.parseColor("#FFFFFF"),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}