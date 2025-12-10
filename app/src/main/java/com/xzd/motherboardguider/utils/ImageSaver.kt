package com.xzd.motherboardguider.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageSaver {
    
    /**
     * 保存图片到相册
     */
    fun saveImageToGallery(context: Context, bitmap: Bitmap, fileName: String = "配置分享"): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用 MediaStore
            saveImageToGalleryQ(context, bitmap, fileName)
        } else {
            // Android 10 以下使用传统方式
            saveImageToGalleryLegacy(context, bitmap, fileName)
        }
    }
    
    /**
     * Android 10+ 保存图片
     */
    private fun saveImageToGalleryQ(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${fileName}_${getCurrentTimeString()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MotherboardGuider")
        }
        
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        uri?.let {
            try {
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }
                return it
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        
        return null
    }
    
    /**
     * Android 10 以下保存图片
     */
    private fun saveImageToGalleryLegacy(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val appDir = File(imagesDir, "MotherboardGuider")
        
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        
        val imageFile = File(appDir, "${fileName}_${getCurrentTimeString()}.jpg")
        
        try {
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            
            // 通知媒体库更新
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            
            return Uri.fromFile(imageFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 保存图片到临时文件并返回用于分享的 Uri
     * 优先保存到外部存储的公共 Pictures 目录，微信等应用可以直接访问
     */
    fun saveImageForShare(context: Context, bitmap: Bitmap, fileName: String = "配置分享"): Uri? {
        return try {
            // 优先尝试保存到外部存储公共目录（所有 Android 版本都支持）
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(imagesDir, "MotherboardGuider")
            
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            
            val imageFile = File(appDir, "${fileName}_${getCurrentTimeString()}.jpg")
            
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            
            // 通知媒体库更新
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            
            // 对于 Android 7.0+，需要使用 FileProvider 转换 file:// URI
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android 7.0+ 使用 FileProvider
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
            } else {
                // Android 7.0 以下直接使用 file:// URI
                Uri.fromFile(imageFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果外部存储失败，回退到使用缓存目录 + FileProvider
            return try {
                val cacheDir = context.cacheDir
                val imageFile = File(cacheDir, "${fileName}_${getCurrentTimeString()}.jpg")
                
                val fos = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
                
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
            } catch (e2: Exception) {
                e2.printStackTrace()
                null
            }
        }
    }
    
    /**
     * 获取当前时间字符串
     */
    private fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }
}



