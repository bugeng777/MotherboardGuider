package com.xzd.motherboardguider.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xzd.motherboardguider.R
import com.xzd.motherboardguider.bean.CollectionItem

class CollectionAdapter(
    private var collectionList: List<CollectionItem>,
    private val onDeleteClick: (CollectionItem) -> Unit,
    private val onShareClick: (CollectionItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_EMPTY_HINT = 1
        private const val TYPE_TO_END = 2
    }

    // 数据项的 ViewHolder
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.collectionItemName)
        val itemDate: TextView = itemView.findViewById(R.id.collectionItemDate)
        val itemCpu: TextView = itemView.findViewById(R.id.collectionItemCpu)
        val itemGpu: TextView = itemView.findViewById(R.id.collectionItemGpu)
        val itemDiskCount: TextView = itemView.findViewById(R.id.collectionItemDiskCount)
        val itemPower: TextView = itemView.findViewById(R.id.collectionItemPower)
        val expectSuggestMotherboard: TextView = itemView.findViewById(R.id.expectSuggestMotherboard)
        val expectSupportMotherboard: TextView = itemView.findViewById(R.id.expectSupportMotherboard)
        val deleteButton: RelativeLayout = itemView.findViewById(R.id.deleteButton)
        val shareButton: RelativeLayout = itemView.findViewById(R.id.shareButton)
    }

    // 空提示和底部提示的 ViewHolder（它们布局相同，可以共用）
    class HintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return when {
            collectionList.isEmpty() -> TYPE_EMPTY_HINT
            position == collectionList.size -> TYPE_TO_END
            else -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_collection, parent, false)
                ItemViewHolder(view)
            }
            TYPE_EMPTY_HINT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_collection_empty_hint, parent, false)
                HintViewHolder(view)
            }
            TYPE_TO_END -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_collection_to_end, parent, false)
                HintViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = collectionList[position]
                
                // 绑定配置名称
                holder.itemName.text = item.collect_name
                
                // 绑定日期（使用创建时间，格式化为日期）
                holder.itemDate.text = formatDate(item.create_time)
                
                // 绑定 CPU 信息
                holder.itemCpu.text = item.cpu_name
                
                // 绑定 GPU 信息
                holder.itemGpu.text = item.gpu_name
                
                // 绑定硬盘个数
                holder.itemDiskCount.text = "${item.disk_count} 个"
                
                // 绑定预计功耗
                holder.itemPower.text = "${item.total_powerConsumption} W"
                
                // 绑定推荐主板
                holder.expectSuggestMotherboard.text = if (item.suggestMotherboard.isNotEmpty()) {
                    item.suggestMotherboard
                } else {
                    "待测算"
                }
                
                // 绑定支持主板
                holder.expectSupportMotherboard.text = if (item.supportedMotherboard.isNotEmpty()) {
                    item.supportedMotherboard
                } else {
                    "待测算"
                }
                
                // 设置删除按钮点击事件
                holder.deleteButton.setOnClickListener {
                    onDeleteClick(item)
                }
                
                // 设置分享按钮点击事件
                holder.shareButton.setOnClickListener {
                    onShareClick(item)
                }
            }
            is HintViewHolder -> {
                // 空提示和底部提示的布局已经在 XML 中定义好了，不需要额外绑定数据
            }
        }
    }

    override fun getItemCount(): Int {
        return when {
            collectionList.isEmpty() -> 1 // 只显示空提示
            else -> collectionList.size + 1 // 数据项 + 底部提示
        }
    }

    fun updateList(newList: List<CollectionItem>) {
        collectionList = newList
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        // 如果日期字符串格式为 "2025-01-20 12:00:00" 或类似格式，提取日期部分
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
