package com.xzd.motherboardguider

import SpacingItemDecoration
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xzd.motherboardguider.adapter.CollectionAdapter
import com.xzd.motherboardguider.api.ApiClient
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
        collectionAdapter = CollectionAdapter(emptyList())
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
}