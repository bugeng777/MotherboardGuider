package com.xzd.motherboardguider

import SpacingItemDecoration
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xzd.motherboardguider.adapter.CollectionAdapter
import com.xzd.motherboardguider.api.ApiClient
import com.xzd.motherboardguider.bean.CollectionItem
import com.xzd.motherboardguider.bean.DeleteCollectionRequest
import com.xzd.motherboardguider.bean.LoadCollectionListRequest
import com.xzd.motherboardguider.utils.PrefsManager
import kotlinx.coroutines.launch

class Collection : ComponentActivity(){
    private lateinit var hardwareListView:RecyclerView
    private lateinit var collectionBackButton:ImageView
    private lateinit var collectionAdapter: CollectionAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)
        hardwareListView=findViewById(R.id.hardwareListView)
        collectionBackButton=findViewById(R.id.collectionBackButton)
        // 在 Collection.kt 的 onCreate 方法中，初始化 RecyclerView 后添加：
        val spacing = (20 * resources.displayMetrics.density).toInt()
        hardwareListView.addItemDecoration(SpacingItemDecoration(spacing))
        // 初始化 RecyclerView
        collectionAdapter = CollectionAdapter(emptyList()) { item ->
            showDeleteConfirmDialog(item)
        }
        hardwareListView.layoutManager = LinearLayoutManager(this)
        hardwareListView.adapter = collectionAdapter
        
        collectionBackButton.setOnClickListener(object:OnClickListener{
            override fun onClick(v: View?) {
                val intent= Intent(this@Collection,MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        })

        loadCollectionList() //请求看看有没有结果
    }
    private fun loadCollectionList() {
        lifecycleScope.launch {
            try {
                val token = PrefsManager.getToken(this@Collection)
                if (token == null) {
                    Toast.makeText(baseContext, "请先登录", Toast.LENGTH_SHORT).show()
                    // 跳转到登录页面
                    val intent = Intent(this@Collection, Login::class.java)
                    startActivity(intent)
                    finish()
                    return@launch
                }

                val request = LoadCollectionListRequest(token = token)
                Log.i("API", "开始加载收藏列表，token: $token")

                val response = ApiClient.collectionApi.loadCollectionList(request)
                Log.i("API", "收到响应，code: ${response.code}")

                if (response.code == 0) {
                    val collectionList = response.data
                    Log.i("API", "加载收藏列表成功，共 ${collectionList.size} 条")

                    // 更新 RecyclerView 数据
                    collectionAdapter.updateList(collectionList)

                    if (collectionList.isEmpty()) {
                        Log.i("API", "收藏列表为空")
                    } else {
                        for ((index, item) in collectionList.withIndex()) {
                            Log.i("API", "========== Collection ${index + 1} ==========")
                            Log.i("API", "ID: ${item.id}")
                            Log.i("API", "用户ID: ${item.user_id}")
                            Log.i("API", "配置名称: ${item.collect_name}")
                            Log.i("API", "CPU ID: ${item.cpu_id}, CPU 名称: ${item.cpu_name}")
                            Log.i("API", "GPU ID: ${item.gpu_id}, GPU 名称: ${item.gpu_name}")
                            Log.i("API", "硬盘数量: ${item.disk_count}")
                            Log.i("API", "总功耗: ${item.total_powerConsumption}W")
                            Log.i("API", "支持的主板: ${item.supportedMotherboard}")
                            Log.i("API", "推荐的主板: ${item.suggestMotherboard}")
                            Log.i("API", "创建时间: ${item.create_time}")
                            Log.i("API", "更新时间: ${item.update_time}")
                            Log.i("API", "=======================================")
                        }
                    }
                } else {
                    Log.e("API", "加载收藏列表失败，code: ${response.code}")
                    Toast.makeText(baseContext, "加载收藏列表失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API", "请求异常: ${e.message}", e)
                Toast.makeText(baseContext, "请求异常: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showDeleteConfirmDialog(item: CollectionItem) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_confirm)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 设置对话框宽度
        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)

        // 取消按钮
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // 确定按钮
        confirmButton.setOnClickListener {
            dialog.dismiss()
            deleteCollection(item)
        }
        
        dialog.show()
    }
    
    private fun deleteCollection(item: CollectionItem) {
        lifecycleScope.launch {
            try {
                val token = PrefsManager.getToken(this@Collection)
                if (token == null) {
                    Toast.makeText(baseContext, "请先登录", Toast.LENGTH_SHORT).show()
                    // 跳转到登录页面
                    val intent = Intent(this@Collection, Login::class.java)
                    startActivity(intent)
                    finish()
                    return@launch
                }

                val request = DeleteCollectionRequest(
                    token = token,
                    collection_id = item.id
                )
                Log.i("API", "开始删除收藏，token: $token, collection_id: ${item.id}")

                val response = ApiClient.collectionApi.deleteCollection(request)
                Log.i("API", "收到响应，code: ${response.code}, data: ${response.data}")

                if (response.code == 0) {
                    Toast.makeText(baseContext, "删除成功", Toast.LENGTH_SHORT).show()
                    // 重新加载收藏列表
                    loadCollectionList()
                } else {
                    Log.e("API", "删除收藏失败，code: ${response.code}")
                    Toast.makeText(baseContext, response.data ?: "删除失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API", "请求异常: ${e.message}", e)
                Toast.makeText(baseContext, "请求异常: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}