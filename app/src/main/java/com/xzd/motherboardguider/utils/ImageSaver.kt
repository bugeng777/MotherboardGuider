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
     * 这个方法会将图片保存到应用缓存目录，并返回一个可以通过 FileProvider 分享的 Uri
     */
    fun saveImageForShare(context: Context, bitmap: Bitmap, fileName: String = "配置分享"): Uri? {
        return try {
            // 保存到应用缓存目录
            val cacheDir = context.cacheDir
            val imageFile = File(cacheDir, "${fileName}_${getCurrentTimeString()}.jpg")
            
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            
            // 使用 FileProvider 获取 Uri
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
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



