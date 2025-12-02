package com.xzd.motherboardguider

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.xzd.motherboardguider.api.ApiClient
import com.xzd.motherboardguider.bean.RegisterRequest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.regex.Pattern

class Register : ComponentActivity() {
    private lateinit var nickNameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var pwdEdit: EditText
    private lateinit var registerButton: RelativeLayout
    private lateinit var backToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        nickNameEdit = findViewById(R.id.register_nick_name_edit)
        emailEdit = findViewById(R.id.register_email_edit)
        pwdEdit = findViewById(R.id.register_pwd_edit)
        registerButton = findViewById(R.id.registerButton)
        backToLogin = findViewById(R.id.backToLogin)
        backToLogin.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                startActivity(Intent(this@Register,Login::class.java))
            }
        })
        setupNickNameInputFilter()
        setupEmailInputFilter()
        setupPasswordInputFilter()
        setupRegisterButtonState()
        setupRegisterButtonClick()
    }

    private fun setupNickNameInputFilter() {
        // 昵称允许的字符：汉字、字母、数字
        val nickNamePattern = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9]*$")
        
        nickNameEdit.filters = arrayOf(
            InputFilter { source, start, end, dest, dstart, dend ->
                if (source == null || source.isEmpty()) {
                    return@InputFilter null
                }
                
                val builder = StringBuilder()
                for (i in start until end) {
                    val c = source[i]
                    if (nickNamePattern.matcher(c.toString()).matches()) {
                        builder.append(c)
                    }
                }
                
                if (builder.length == end - start) {
                    null
                } else {
                    builder.toString()
                }
            }
        )

        nickNameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRegisterButtonState()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupEmailInputFilter() {
        // 邮箱允许的字符：字母、数字、@、.、_、-、+
        val emailPattern = Pattern.compile("^[a-zA-Z0-9@._+-]*$")
        
        emailEdit.filters = arrayOf(
            InputFilter { source, start, end, dest, dstart, dend ->
                if (source == null || source.isEmpty()) {
                    return@InputFilter null
                }
                
                val builder = StringBuilder()
                for (i in start until end) {
                    val c = source[i]
                    if (emailPattern.matcher(c.toString()).matches()) {
                        builder.append(c)
                    }
                }
                
                if (builder.length == end - start) {
                    null
                } else {
                    builder.toString()
                }
            }
        )

        emailEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRegisterButtonState()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupPasswordInputFilter() {
        // 密码允许的字符：数字和大小写字母
        val passwordPattern = Pattern.compile("^[a-zA-Z0-9]*$")
        
        pwdEdit.filters = arrayOf(
            InputFilter { source, start, end, dest, dstart, dend ->
                if (source == null || source.isEmpty()) {
                    return@InputFilter null
                }
                
                val builder = StringBuilder()
                for (i in start until end) {
                    val c = source[i]
                    if (passwordPattern.matcher(c.toString()).matches()) {
                        builder.append(c)
                    }
                }
                
                if (builder.length == end - start) {
                    null
                } else {
                    builder.toString()
                }
            }
        )

        pwdEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRegisterButtonState()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupRegisterButtonState() {
        updateRegisterButtonState()
    }

    private fun updateRegisterButtonState() {
        val nickName = nickNameEdit.text.toString().trim()
        val email = emailEdit.text.toString().trim()
        val password = pwdEdit.text.toString()

        // 检查昵称是否不为空且不超过16字，邮箱格式是否完整且有效，密码长度是否超过6位
        val isNickNameValid = nickName.isNotEmpty() && nickName.length <= 16
        val isEmailValid = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.length >= 6 && password.length <= 32

        registerButton.alpha = if (isNickNameValid && isEmailValid && isPasswordValid) 1f else 0.5f
    }

    private fun setupRegisterButtonClick() {
        registerButton.setOnClickListener {
            val nickName = nickNameEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val password = pwdEdit.text.toString()

            // 再次验证输入
            val isNickNameValid = nickName.isNotEmpty() && nickName.length <= 16
            val isEmailValid = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
            val isPasswordValid = password.length >= 6 && password.length <= 32

            if (!isNickNameValid || !isEmailValid || !isPasswordValid) {
                return@setOnClickListener
            }

            // 调用注册接口
            performRegister(nickName, email, password)
        }
    }

    private fun performRegister(nickName: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val request = RegisterRequest(
                    nickName = nickName,
                    contactAd = email,
                    pwd = password,
                    appName = "mg" // 应用名称
                )

                Log.i("API", "开始注册，昵称: $nickName, 邮箱: $email")
                val response = ApiClient.collectionApi.register(request)
                Log.i("API", "收到响应，code: ${response.code}, data: ${response.data}")

                if (response.code == 0) {
                    // 注册成功
                    Toast.makeText(this@Register, response.data, Toast.LENGTH_SHORT).show()
                    // 跳转到登录页面
                    val intent = Intent(this@Register, Login::class.java)
                    startActivity(intent)
                    finish() // 关闭注册页面
                } else {
                    // 注册失败（code=1），有两种错误可能性，显示错误信息
                    Toast.makeText(this@Register, response.data, Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                // 网络连接异常
                Log.e("API", "网络连接异常: ${e.message}", e)
                Toast.makeText(this@Register, "网络连接失败，请检查网络设置", Toast.LENGTH_SHORT).show()
            } catch (e: SocketTimeoutException) {
                // 请求超时
                Log.e("API", "请求超时: ${e.message}", e)
                Toast.makeText(this@Register, "请求超时，请检查网络连接", Toast.LENGTH_SHORT).show()
            } catch (e: UnknownHostException) {
                // 无法解析主机
                Log.e("API", "无法连接服务器: ${e.message}", e)
                Toast.makeText(this@Register, "无法连接服务器，请检查网络", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // 其他异常
                Log.e("API", "注册请求异常: ${e.message}", e)
                Toast.makeText(this@Register, "注册失败，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        }
    }
}