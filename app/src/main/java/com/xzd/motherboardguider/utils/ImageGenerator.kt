package com.xzd.motherboardguider.utils

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.xzd.motherboardguider.bean.CollectionItem

object ImageGenerator {
    
    // 颜色定义（深色主题）
    private const val BACKGROUND_COLOR = 0xFF1A1A1A.toInt() // 深灰色背景
    private const val CARD_BACKGROUND_COLOR = 0xFF252A30.toInt() // 卡片背景
    private const val TEXT_COLOR_WHITE = 0xFFFFFFFF.toInt() // 白色文字
    private const val TEXT_COLOR_GRAY = 0xFFD0D2D3.toInt() // 浅灰色文字
    private const val TEXT_COLOR_DATE = 0xFF666666.toInt() // 日期灰色
    private const val DIVIDER_COLOR = 0xFF3B3B3B.toInt() // 分隔线颜色
    private const val RECOMMEND_BG_COLOR = 0xFF4A90E2.toInt() // 推荐标签蓝色
    private const val SUPPORT_BG_COLOR = 0xFF3B3B3B.toInt() // 支持标签深灰色
    private const val MOTHERBOARD_BG_COLOR = 0xFF1E2329.toInt() // 主板区域背景
    
    // 尺寸定义（基于1080p屏幕，会按比例缩放）
    private const val BASE_WIDTH = 1080
    private const val BASE_HEIGHT = 1920
    private const val CARD_MARGIN = 40
    private const val CARD_PADDING = 32
    private const val ITEM_PADDING = 24
    private const val TEXT_SIZE_TITLE = 64f
    private const val TEXT_SIZE_LABEL = 64f
    private const val TEXT_SIZE_VALUE = 56f
    private const val TEXT_SIZE_DATE = 48f
    private const val TEXT_SIZE_TAG = 56f
    
    /**
     * 生成配置信息图片
     */
    fun generateImage(item: CollectionItem, width: Int = BASE_WIDTH, height: Int = BASE_HEIGHT): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // 绘制背景
        canvas.drawColor(BACKGROUND_COLOR)
        
        val scale = width.toFloat() / BASE_WIDTH
        
        // 准备画笔
        val titlePaint = TextPaint().apply {
            color = TEXT_COLOR_WHITE
            textSize = TEXT_SIZE_TITLE * scale
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
        
        val datePaint = TextPaint().apply {
            color = TEXT_COLOR_DATE
            textSize = TEXT_SIZE_DATE * scale
            isAntiAlias = true
        }
        
        val labelPaint = TextPaint().apply {
            color = TEXT_COLOR_WHITE
            textSize = TEXT_SIZE_LABEL * scale
            isAntiAlias = true
        }
        
        val valuePaint = TextPaint().apply {
            color = TEXT_COLOR_GRAY
            textSize = TEXT_SIZE_VALUE * scale
            isAntiAlias = true
        }
        
        val dividerPaint = Paint().apply {
            color = DIVIDER_COLOR
            strokeWidth = 1f * scale
        }
        
        val cardPaint = Paint().apply {
            color = CARD_BACKGROUND_COLOR
            isAntiAlias = true
        }
        
        val motherboardAreaPaint = Paint().apply {
            color = MOTHERBOARD_BG_COLOR
            isAntiAlias = true
        }
        
        val recommendTagPaint = Paint().apply {
            color = RECOMMEND_BG_COLOR
            isAntiAlias = true
        }
        
        val supportTagPaint = Paint().apply {
            color = SUPPORT_BG_COLOR
            isAntiAlias = true
        }
        
        val tagTextPaint = TextPaint().apply {
            color = TEXT_COLOR_WHITE
            textSize = TEXT_SIZE_TAG * scale
            isAntiAlias = true
        }
        
        // 绘制顶部信息（配置名称和日期）
        val topMargin = (CARD_MARGIN * scale).toInt()
        val titleY = topMargin + (TEXT_SIZE_TITLE * scale).toInt()
        canvas.drawText(item.collect_name, (CARD_MARGIN * scale).toInt().toFloat(),
            titleY.toFloat(), titlePaint)
        
        val dateText = formatDate(item.create_time)
        val dateWidth = datePaint.measureText(dateText)
        canvas.drawText(dateText, (width - CARD_MARGIN * scale - dateWidth).toFloat(),
            titleY.toFloat(), datePaint)
        
        // 计算卡片位置
        val cardTop = topMargin + (TEXT_SIZE_TITLE * scale * 1.5f).toInt()
        val cardLeft = (CARD_MARGIN * scale).toInt()
        val cardRight = (width - CARD_MARGIN * scale).toInt()
        val cardPadding = (CARD_PADDING * scale).toInt()
        val itemPadding = (ITEM_PADDING * scale).toInt()
        val cornerRadius = 16f * scale
        
        // 先计算所有内容的高度
        var currentY = cardTop + cardPadding
        
        // CPU
        currentY = calculateItemHeight(currentY, itemPadding, scale)
        
        // 显卡
        currentY = calculateItemHeight(currentY, itemPadding, scale)
        
        // 硬盘个数
        currentY = calculateItemHeight(currentY, itemPadding, scale)
        
        // 预计功耗
        currentY = calculateItemHeight(currentY, itemPadding, scale)
        
        // 主板系列标题
        val motherboardTitleY = currentY + itemPadding
        currentY = motherboardTitleY + (TEXT_SIZE_LABEL * scale * 1.2f).toInt()
        
        // 主板详情区域
        val motherboardAreaTop = currentY
        val motherboardAreaLeft = cardLeft + cardPadding
        val motherboardAreaRight = cardRight - cardPadding
        
        currentY = motherboardAreaTop + cardPadding
        
        // 推荐主板高度
        val recommendText = if (item.suggestMotherboard.isNotEmpty()) item.suggestMotherboard else "待测算"
        currentY = calculateMotherboardItemHeight(currentY, recommendText, tagTextPaint, valuePaint, 
            motherboardAreaLeft + cardPadding, motherboardAreaRight - cardPadding, itemPadding, scale)
        
        // 分隔线
        val dividerY = currentY + itemPadding
        currentY = dividerY + itemPadding * 2
        
        // 支持主板高度
        currentY = calculateMotherboardItemHeight(currentY, item.supportedMotherboard, tagTextPaint, valuePaint,
            motherboardAreaLeft + cardPadding, motherboardAreaRight - cardPadding, itemPadding, scale)
        
        // 计算主板区域底部
        val motherboardAreaBottom = currentY + cardPadding
        
        // 绘制主板区域背景
        val motherboardRect = RectF(
            motherboardAreaLeft.toFloat(), 
            motherboardAreaTop.toFloat(), 
            motherboardAreaRight.toFloat(), 
            motherboardAreaBottom.toFloat()
        )
        canvas.drawRoundRect(motherboardRect, cornerRadius * 0.5f, cornerRadius * 0.5f, motherboardAreaPaint)
        
        // 计算卡片底部
        val cardBottom = motherboardAreaBottom + cardPadding
        
        // 绘制卡片背景
        val cardRect = RectF(cardLeft.toFloat(), cardTop.toFloat(), cardRight.toFloat(), cardBottom.toFloat())
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardPaint)
        
        // 绘制卡片内容（在背景之上）
        currentY = cardTop + cardPadding
        
        // CPU
        currentY = drawItem(canvas, "CPU", item.cpu_name, cardLeft + cardPadding, currentY, 
            cardRight - cardPadding, labelPaint, valuePaint, dividerPaint, scale)
        
        // 显卡
        currentY = drawItem(canvas, "显卡", item.gpu_name, cardLeft + cardPadding, currentY, 
            cardRight - cardPadding, labelPaint, valuePaint, dividerPaint, scale)
        
        // 硬盘个数
        currentY = drawItem(canvas, "硬盘个数", "${item.disk_count} 个", cardLeft + cardPadding, currentY, 
            cardRight - cardPadding, labelPaint, valuePaint, dividerPaint, scale)
        
        // 预计功耗
        currentY = drawItem(canvas, "预计功耗", "${item.total_powerConsumption} W", cardLeft + cardPadding, currentY, 
            cardRight - cardPadding, labelPaint, valuePaint, dividerPaint, scale)
        
        // 主板系列标题（往下移动一点）
        val motherboardTitleY2 = currentY + itemPadding + (ITEM_PADDING * scale * 0.5f).toInt()
        canvas.drawText("主板系列", (cardLeft + cardPadding).toFloat(),
            motherboardTitleY2.toFloat(), labelPaint)
        
        // 主板详情区域内容（在主板背景之上）
        val motherboardAreaTop2 = motherboardTitleY2 + (TEXT_SIZE_LABEL * scale * 1.2f).toInt()
        var motherboardY = motherboardAreaTop2 + cardPadding
        
        // 推荐主板（传入 dividerPaint 会在内部绘制分隔线）
        motherboardY = drawMotherboardItem(canvas, "推荐", recommendText, 
            motherboardAreaLeft + cardPadding, motherboardY, motherboardAreaRight - cardPadding,
            recommendTagPaint, tagTextPaint, valuePaint, dividerPaint, scale)
        
        // 支持主板（不传入 dividerPaint，不绘制分隔线）
        val supportY = motherboardY + itemPadding * 2
        drawMotherboardItem(canvas, "支持", item.supportedMotherboard, 
            motherboardAreaLeft + cardPadding, supportY, motherboardAreaRight - cardPadding,
            supportTagPaint, tagTextPaint, valuePaint, null, scale)
        
        return bitmap
    }
    
    /**
     * 计算配置项高度
     */
    private fun calculateItemHeight(top: Int, itemPadding: Int, scale: Float): Int {
        val textHeight = (TEXT_SIZE_LABEL * scale).toInt()
        return top + textHeight + itemPadding + (1 * scale).toInt() + itemPadding
    }
    
    /**
     * 计算主板项高度
     */
    private fun calculateMotherboardItemHeight(
        top: Int,
        value: String,
        tagTextPaint: TextPaint,
        valuePaint: TextPaint,
        left: Int,
        right: Int,
        itemPadding: Int,
        scale: Float
    ): Int {
        // 标签背景框的 padding：左右 4dp，上下 2dp（与绘制时保持一致）
        val tagPaddingH = (4 * scale).toInt() // 左右 padding
        val tagPaddingV = (2 * scale).toInt() // 上下 padding
        val tagTextWidth = tagTextPaint.measureText("推荐")
        val tagTextHeight = (TEXT_SIZE_TAG * scale).toInt()
        val tagHeight = tagTextHeight + tagPaddingV * 2 // 文字高度 + 上下 padding
        
        val tagSpacing = (8 * scale).toInt() // 标签和值之间的间距
        val maxWidth = right - left - (tagTextWidth.toInt() + tagPaddingH * 2 + tagSpacing).toInt()
        
        val valueHeight = if (valuePaint.measureText(value) <= maxWidth) {
            (TEXT_SIZE_VALUE * scale).toInt()
        } else {
            val staticLayout = StaticLayout.Builder
                .obtain(value, 0, value.length, valuePaint, maxWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL) // 与绘制时保持一致
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()
            staticLayout.height
        }
        
        val itemHeight = maxOf(tagHeight, valueHeight)
        return top + itemHeight + itemPadding
    }
    
    /**
     * 绘制配置项（CPU、显卡等）
     */
    private fun drawItem(
        canvas: Canvas,
        label: String,
        value: String,
        left: Int,
        top: Int,
        right: Int,
        labelPaint: TextPaint,
        valuePaint: TextPaint,
        dividerPaint: Paint,
        scale: Float
    ): Int {
        val itemPadding = (ITEM_PADDING * scale).toInt()
        val y = top + (TEXT_SIZE_LABEL * scale).toInt()
        
        // 绘制标签
        canvas.drawText(label, left.toFloat(), y.toFloat(), labelPaint)
        
        // 绘制值（右对齐）
        val valueWidth = valuePaint.measureText(value)
        canvas.drawText(value, (right - valueWidth).toFloat(), y.toFloat(), valuePaint)
        
        // 绘制分隔线
        val dividerY = y + itemPadding
        canvas.drawLine(left.toFloat(), dividerY.toFloat(), right.toFloat(), dividerY.toFloat(), dividerPaint)
        
        return dividerY + itemPadding
    }
    
    /**
     * 绘制主板项（推荐/支持）
     */
    private fun drawMotherboardItem(
        canvas: Canvas,
        tagLabel: String,
        value: String,
        left: Int,
        top: Int,
        right: Int,
        tagPaint: Paint,
        tagTextPaint: TextPaint,
        valuePaint: TextPaint,
        dividerPaint: Paint?,
        scale: Float
    ): Int {
        val itemPadding = (ITEM_PADDING * scale).toInt()
        // 标签背景框的 padding：左右 4dp，上下 2dp
        val tagPaddingH = (4 * scale).toInt() // 左右 padding
        val tagPaddingV = (2 * scale).toInt() // 上下 padding
        
        // 计算标签尺寸（包含 padding）
        val tagTextWidth = tagTextPaint.measureText(tagLabel)
        val tagTextHeight = (TEXT_SIZE_TAG * scale).toInt()
        val tagHeight = tagTextHeight + tagPaddingV * 2 // 文字高度 + 上下 padding
        val tagLeft = left
        val tagRight = left + tagTextWidth.toInt() + tagPaddingH * 2
        
        // 计算值的可用宽度和位置
        val tagSpacing = (8 * scale).toInt() // 标签和值之间的间距
        val maxWidth = right - tagRight - tagSpacing
        val valueLeft = tagRight + tagSpacing
        
        // 计算文本高度（单行或多行）
        val actualValueHeight: Int
        val staticLayout: StaticLayout?
        
        if (valuePaint.measureText(value) <= maxWidth) {
            // 单行
            actualValueHeight = (TEXT_SIZE_VALUE * scale).toInt()
            staticLayout = null
        } else {
            // 多行 - 使用左对齐，但整体右对齐显示
            staticLayout = StaticLayout.Builder
                .obtain(value, 0, value.length, valuePaint, maxWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL) // 左对齐，保持多行文本统一
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()
            actualValueHeight = staticLayout.height
        }
        
        // 计算整体高度，确保标签和值垂直居中对齐
        val itemHeight = maxOf(tagHeight, actualValueHeight)
        val centerY = top + itemHeight / 2
        
        // 绘制标签背景（垂直居中，包含 padding）
        val tagTop = centerY - tagHeight / 2
        val tagRect = RectF(
            tagLeft.toFloat(),
            tagTop.toFloat(),
            tagRight.toFloat(),
            (tagTop + tagHeight).toFloat()
        )
        canvas.drawRoundRect(tagRect, tagHeight * 0.3f, tagHeight * 0.3f, tagPaint)
        
        // 绘制标签文字（垂直居中，考虑 padding）
        val tagTextY = centerY + (TEXT_SIZE_TAG * scale * 0.3f)
        canvas.drawText(tagLabel, (tagLeft + tagPaddingH).toFloat(), tagTextY, tagTextPaint)
        
        // 绘制值（垂直居中，右对齐）
        if (staticLayout == null) {
            // 单行 - 右对齐
            val valueWidth = valuePaint.measureText(value)
            val valueY = centerY + (TEXT_SIZE_VALUE * scale * 0.3f)
            canvas.drawText(value, (right - valueWidth).toFloat(), valueY, valuePaint)
        } else {
            // 多行 - 整体右对齐，但每行左对齐
            val valueY = centerY - actualValueHeight / 2
            canvas.save()
            canvas.translate((right - maxWidth).toFloat(), valueY.toFloat())
            staticLayout.draw(canvas)
            canvas.restore()
        }
        
        val bottom = top + itemHeight + itemPadding
        
        dividerPaint?.let {
            val dividerY = bottom
            canvas.drawLine(left.toFloat(), dividerY.toFloat(), right.toFloat(),
                dividerY.toFloat(), it)
            return dividerY + itemPadding
        }
        
        return bottom
    }
    
    /**
     * 格式化日期
     */
    private fun formatDate(dateString: String): String {
        return try {
            if (dateString.contains(" ")) {
                dateString.split(" ")[0].replace("-", "/")
            } else {
                dateString.replace("-", "/")
            }
        } catch (e: Exception) {
            dateString
        }
    }
}
