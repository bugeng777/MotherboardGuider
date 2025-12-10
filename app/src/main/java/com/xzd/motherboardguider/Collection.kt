package com.xzd.motherboardguider

import SpacingItemDecoration
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.FileProvider
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
import android.content.Context
import com.xzd.motherboardguider.api.ApiClient
import com.xzd.motherboardguider.bean.CollectionItem
import com.xzd.motherboardguider.bean.DeleteCollectionRequest
import com.xzd.motherboardguider.bean.LoadCollectionListRequest
import com.xzd.motherboardguider.utils.PrefsManager
import com.xzd.motherboardguider.utils.LocaleHelper
import com.xzd.motherboardguider.utils.ImageGenerator
import com.xzd.motherboardguider.utils.ImageSaver
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Collection : ComponentActivity(){
    private lateinit var hardwareListView:RecyclerView
    private lateinit var collectionBackButton:ImageView
    private lateinit var collectionAdapter: CollectionAdapter
    
    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = PrefsManager.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)
        hardwareListView=findViewById(R.id.hardwareListView)
        collectionBackButton=findViewById(R.id.collectionBackButton)
        // 在 Collection.kt 的 onCreate 方法中，初始化 RecyclerView 后添加：
        val spacing = (20 * resources.displayMetrics.density).toInt()
        hardwareListView.addItemDecoration(SpacingItemDecoration(spacing))
        // 初始化 RecyclerView
        collectionAdapter = CollectionAdapter(
            emptyList(),
            onDeleteClick = { item ->
                showDeleteConfirmDialog(item)
            },
            onShareClick = { item ->
                shareCollectionItem(item)
            }
        )
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
                    Toast.makeText(baseContext, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(baseContext, getString(R.string.load_collection_failed), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(baseContext, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(baseContext, getString(R.string.delete_success), Toast.LENGTH_SHORT).show()
                    // 重新加载收藏列表
                    loadCollectionList()
                } else {
                    Log.e("API", "删除收藏失败，code: ${response.code}")
                    Toast.makeText(baseContext, response.data ?: getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API", "请求异常: ${e.message}", e)
                Toast.makeText(baseContext, "请求异常: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun shareCollectionItem(item: CollectionItem) {
        lifecycleScope.launch {
            try {
                // 在后台线程生成图片
                val bitmap = withContext(Dispatchers.IO) {
                    val screenWidth = resources.displayMetrics.widthPixels
                    ImageGenerator.generateImage(item, width = screenWidth)
                }
                
                // 保存图片到相册
                ImageSaver.saveImageToGallery(this@Collection, bitmap, item.collect_name)
                
                // 保存图片到临时文件用于分享
                val shareUri = ImageSaver.saveImageForShare(this@Collection, bitmap, item.collect_name)
                
                if (shareUri != null) {
                    // 显示分享选择弹窗
                    showShareDialog(shareUri)
                } else {
                    Toast.makeText(this@Collection, getString(R.string.share_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Share", "分享失败: ${e.message}", e)
                Toast.makeText(this@Collection, getString(R.string.share_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 显示分享选择弹窗
     */
    private fun showShareDialog(shareUri: android.net.Uri) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_share)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // 设置对话框宽度和位置（底部弹窗效果）
        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val layoutParams = window?.attributes
        layoutParams?.gravity = android.view.Gravity.BOTTOM
        layoutParams?.verticalMargin = 0.05f // 距离底部5%
        window?.attributes = layoutParams
        
        val wechatLayout = dialog.findViewById<View>(R.id.wechatLayout)
        val shareToOtherLayout = dialog.findViewById<View>(R.id.shareToOtherLayout)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        
        // 微信选项点击
        wechatLayout.setOnClickListener {
            dialog.dismiss()
            shareToWeChat(shareUri)
        }
        
        // 发送到其他应用选项点击
        shareToOtherLayout.setOnClickListener {
            dialog.dismiss()
            shareToOtherApps(shareUri)
        }
        
        // 取消按钮
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    /**
     * 分享到其他应用（通用分享选择器）
     */
    private fun shareToOtherApps(imageUri: android.net.Uri) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // 使用 ClipData 确保权限正确传递
                clipData = android.content.ClipData.newUri(contentResolver, "Image", imageUri)
            }
            
            // 为所有可能接收 Intent 的应用授予权限（在创建 Chooser 之前）
            val resInfoList = packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                try {
                    grantUriPermission(
                        packageName,
                        imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    Log.w("Share", "无法授予权限给 $packageName: ${e.message}")
                }
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, getString(R.string.share_to_other))
            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooserIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            
            startActivity(chooserIntent)
        } catch (e: Exception) {
            Log.e("Share", "分享到其他应用失败: ${e.message}", e)
            Toast.makeText(this, getString(R.string.share_failed), Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 检查微信是否已安装
     */
    private fun isWeChatInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo("com.tencent.mm", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * 分享图片到微信好友
     */
    private fun shareToWeChat(imageUri: android.net.Uri) {
        try {
            // 检查微信是否安装
            if (!isWeChatInstalled()) {
                Toast.makeText(this, getString(R.string.share_to_wechat) + " " + getString(R.string.share_failed), Toast.LENGTH_SHORT).show()
                return
            }
            
            // 直接启动微信分享
            val weChatIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                setPackage("com.tencent.mm")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // 授予微信读取文件的权限
            grantUriPermission(
                "com.tencent.mm",
                imageUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            
            startActivity(weChatIntent)
        } catch (e: Exception) {
            Log.e("Share", "分享到微信失败: ${e.message}", e)
            Toast.makeText(this, getString(R.string.share_failed), Toast.LENGTH_SHORT).show()
        }
    }
}