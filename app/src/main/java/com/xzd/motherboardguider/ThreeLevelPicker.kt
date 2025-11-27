package com.xzd.motherboardguider

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * 三级联动选择器
 * 使用方式：
 * ThreeLevelPicker.show(context, 
 *     listOf("AMD", "Intel"),  // 第一级数据
 *     { brand -> listOf("9000系", "7000系") },  // 第二级数据（根据第一级返回）
 *     { brand, series -> listOf("9950X3D", "9950X") },  // 第三级数据（根据第一、二级返回）
 *     { brand, series, model -> /* 选择完成回调 */ }
 * )
 */
class ThreeLevelPicker {
    companion object {
        @SuppressLint("NotifyDataSetChanged")
        fun show(
            context: Context,
            level1Data: List<String>,
            level2DataProvider: (String) -> List<String>,
            level3DataProvider: (String, String) -> List<String>,
            onSelected: (String, String, String) -> Unit
        ) {
            val dialog = BottomSheetDialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_three_level_picker, null)
            dialog.setContentView(view)
            
            val recyclerView1 = view.findViewById<RecyclerView>(R.id.recyclerView1)
            val recyclerView2 = view.findViewById<RecyclerView>(R.id.recyclerView2)
            val recyclerView3 = view.findViewById<RecyclerView>(R.id.recyclerView3)
            val confirmButton = view.findViewById<TextView>(R.id.confirmButton)
            val cancelButton = view.findViewById<TextView>(R.id.cancelButton)
            
            val layoutManager1 = LinearLayoutManager(context)
            val layoutManager2 = LinearLayoutManager(context)
            val layoutManager3 = LinearLayoutManager(context)
            
            recyclerView1.layoutManager = layoutManager1
            recyclerView2.layoutManager = layoutManager2
            recyclerView3.layoutManager = layoutManager3
            
            // 确保所有 RecyclerView 都可以滚动和接收触摸事件
            recyclerView1.isNestedScrollingEnabled = true
            recyclerView1.isEnabled = true
            recyclerView1.setHasFixedSize(false)
            recyclerView2.isNestedScrollingEnabled = true
            recyclerView2.isEnabled = true
            recyclerView2.setHasFixedSize(false)
            recyclerView3.isNestedScrollingEnabled = true
            recyclerView3.isEnabled = true
            recyclerView3.setHasFixedSize(false)
            
            LinearSnapHelper().attachToRecyclerView(recyclerView1)
            LinearSnapHelper().attachToRecyclerView(recyclerView2)
            LinearSnapHelper().attachToRecyclerView(recyclerView3)
            
            var currentLevel1: String? = null
            var currentLevel2: String? = null
            var currentLevel3: String? = null
            
            // 第二级适配器（初始为空）
            var adapter2: SimpleAdapter? = null
            var adapter3: SimpleAdapter? = null
            
            // 先定义函数，再使用
            val updateLevel3: (RecyclerView, LinearLayoutManager, String, String, (String, String) -> List<String>, (String) -> Unit) -> Unit = { recyclerView, layoutManager, level1, level2, level3Provider, onLevel3Selected ->
                val level3Data = level3Provider(level1, level2)
                adapter3 = SimpleAdapter(level3Data) { item ->
                    currentLevel3 = item
                    onLevel3Selected(item)
                }
                // 先设置适配器
                recyclerView.adapter = adapter3
                // 确保可以滚动和接收触摸事件
                recyclerView.isNestedScrollingEnabled = true
                recyclerView.isEnabled = true
                recyclerView.setHasFixedSize(false)
                // 通知适配器数据已更改
                adapter3?.notifyDataSetChanged()
                // 等待布局完成后再滚动
                recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        updateAlpha(recyclerView, layoutManager)
                        if (level3Data.isNotEmpty()) {
                            recyclerView.scrollToPosition(0)
                            onLevel3Selected(level3Data[0])
                        }
                    }
                })
            }
            
            val updateLevel2: (RecyclerView, LinearLayoutManager, String, (String) -> List<String>, (String, String) -> List<String>, (String) -> Unit) -> Unit = { recyclerView, layoutManager, level1, level2Provider, level3Provider, onLevel2Selected ->
                val level2Data = level2Provider(level1)
                adapter2 = SimpleAdapter(level2Data) { item ->
                    currentLevel2 = item
                    onLevel2Selected(item)
                    updateLevel3(recyclerView3, layoutManager3, level1, item, level3Provider) { level3 ->
                        currentLevel3 = level3
                    }
                }
                // 先设置适配器
                recyclerView.adapter = adapter2
                // 确保可以滚动和接收触摸事件
                recyclerView.isNestedScrollingEnabled = true
                recyclerView.isEnabled = true
                recyclerView.setHasFixedSize(false)
                // 通知适配器数据已更改
                adapter2?.notifyDataSetChanged()
                // 等待布局完成后再滚动
                recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        updateAlpha(recyclerView, layoutManager)
                        if (level2Data.isNotEmpty()) {
                            recyclerView.scrollToPosition(0)
                            onLevel2Selected(level2Data[0])
                        }
                    }
                })
            }
            
            // 第一级适配器
            val adapter1 = SimpleAdapter(level1Data) { item ->
                currentLevel1 = item
                updateLevel2(recyclerView2, layoutManager2, item, level2DataProvider, level3DataProvider) { level2 ->
                    currentLevel2 = level2
                    updateLevel3(recyclerView3, layoutManager3, item, level2, level3DataProvider) { level3 ->
                        currentLevel3 = level3
                    }
                }
            }
            recyclerView1.adapter = adapter1
            
            
            // 监听滚动
            recyclerView1.addOnScrollListener(createScrollListener(recyclerView1, layoutManager1) {
                val pos = getSelectedPosition(recyclerView1, layoutManager1)
                if (pos >= 0 && pos < level1Data.size) {
                    val level1 = level1Data[pos]
                    currentLevel1 = level1
                    updateLevel2(recyclerView2, layoutManager2, level1, level2DataProvider, level3DataProvider) { level2 ->
                        currentLevel2 = level2
                        updateLevel3(recyclerView3, layoutManager3, level1, level2, level3DataProvider) { level3 ->
                            currentLevel3 = level3
                        }
                    }
                }
            })
            
            recyclerView2.addOnScrollListener(createScrollListener(recyclerView2, layoutManager2) {
                val pos = getSelectedPosition(recyclerView2, layoutManager2)
                if (pos >= 0 && currentLevel1 != null) {
                    val level2Data = level2DataProvider(currentLevel1!!)
                    if (pos < level2Data.size) {
                        val level2 = level2Data[pos]
                        currentLevel2 = level2
                        updateLevel3(recyclerView3, layoutManager3, currentLevel1!!, level2, level3DataProvider) { level3 ->
                            currentLevel3 = level3
                        }
                    }
                }
            })
            
            recyclerView3.addOnScrollListener(createScrollListener(recyclerView3, layoutManager3) {
                val pos = getSelectedPosition(recyclerView3, layoutManager3)
                if (pos >= 0 && currentLevel1 != null && currentLevel2 != null) {
                    val level3Data = level3DataProvider(currentLevel1!!, currentLevel2!!)
                    if (pos < level3Data.size) {
                        currentLevel3 = level3Data[pos]
                    }
                }
            })
            
            confirmButton.setOnClickListener {
                if (currentLevel1 != null && currentLevel2 != null && currentLevel3 != null) {
                    onSelected(currentLevel1!!, currentLevel2!!, currentLevel3!!)
                }
                dialog.dismiss()
            }
            
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
            
            dialog.show()
            
            // 在对话框显示后初始化数据，确保所有 RecyclerView 都已测量完成
            view.post {
                view.post {
                    if (level1Data.isNotEmpty()) {
                        recyclerView1.scrollToPosition(0)
                        val level1 = level1Data[0]
                        currentLevel1 = level1
                        updateLevel2(recyclerView2, layoutManager2, level1, level2DataProvider, level3DataProvider) { level2 ->
                            currentLevel2 = level2
                            updateLevel3(recyclerView3, layoutManager3, level1, level2, level3DataProvider) { level3 ->
                                currentLevel3 = level3
                            }
                        }
                    }
                }
            }
        }
        
        private fun createScrollListener(
            recyclerView: RecyclerView,
            layoutManager: LinearLayoutManager,
            onIdle: () -> Unit
        ) = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                updateAlpha(recyclerView, layoutManager)
            }
            
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onIdle()
                }
            }
        }
        
        private fun getSelectedPosition(recyclerView: RecyclerView, layoutManager: LinearLayoutManager): Int {
            val centerY = recyclerView.height / 2
            var closestView: View? = null
            var minDistance = Int.MAX_VALUE
            
            for (i in 0 until layoutManager.childCount) {
                val child = layoutManager.getChildAt(i) ?: continue
                val childCenterY = child.top + child.height / 2
                val distance = Math.abs(childCenterY - centerY)
                if (distance < minDistance) {
                    minDistance = distance
                    closestView = child
                }
            }
            return closestView?.let { layoutManager.getPosition(it) } ?: -1
        }
        
        private fun updateAlpha(recyclerView: RecyclerView, layoutManager: LinearLayoutManager) {
            val centerY = recyclerView.height / 2
            for (i in 0 until layoutManager.childCount) {
                val child = layoutManager.getChildAt(i) ?: continue
                val childCenterY = child.top + child.height / 2
                val distance = Math.abs(childCenterY - centerY)
                val maxDistance = recyclerView.height / 2
                val alpha = 1.0f - (distance.toFloat() / maxDistance) * 0.7f
                child.alpha = alpha.coerceIn(0.3f, 1.0f)
            }
        }
    }
    
    private class SimpleAdapter(
        private val items: List<String>,
        private val onItemSelected: (String) -> Unit
    ) : RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {
        
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.itemText)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_picker, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = items[position]
        }
        
        override fun getItemCount(): Int = items.size
    }
}

