package com.xzd.motherboardguider

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.xzd.motherboardguider.bean.LoginRequest
import com.xzd.motherboardguider.utils.PrefsManager
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.regex.Pattern

class Login : ComponentActivity() {
    private lateinit var emailEdit: EditText
    private lateinit var pwdEdit: EditText
    private lateinit var loginButton: RelativeLayout
    private lateinit var loginToRegister: TextView
    private lateinit var goToChangePwd: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEdit = findViewById(R.id.login_email_edit)
        pwdEdit = findViewById(R.id.login_pwd_edit)
        loginButton = findViewById(R.id.loginButton)
        loginToRegister = findViewById(R.id.login_to_register)
        goToChangePwd = findViewById(R.id.goToChangePwd)
        goToChangePwd.setOnClickListener(object :OnClickListener{
            override fun onClick(v: View?) {
                val intent=Intent(this@Login,ForgetPwd::class.java);
                startActivity(intent)
            }
        })
        loginToRegister.setOnClickListener(object :OnClickListener{
            override fun onClick(v: View?) {
                val intent=Intent(this@Login,Register::class.java);
                startActivity(intent)
            }
        })
        setupEmailInputFilter()
        setupPasswordInputFilter()
        setupLoginButtonState()
        setupLoginButtonClick()
    }

    private fun setupEmailInputFilter() {
        // 邮箱允许的字符：字母、数字、@、.、_、-、+
        val emailPattern = Pattern.compile("^[a-zA-Z0-9@._+-]*$")
        
        emailEdit.filters = arrayOf(
            InputFilter { source, start, end, dest, dstart, dend ->
                // 如果source为空（删除操作），允许
                if (source == null || source.isEmpty()) {
                    return@InputFilter null
                }
                
                // 检查每个要输入的字符
                val builder = StringBuilder()
                for (i in start until end) {
                    val c = source[i]
                    if (emailPattern.matcher(c.toString()).matches()) {
                        builder.append(c)
                    }
                }
                
                // 如果所有字符都合法，返回null（允许输入）
                // 如果有字符被过滤，返回过滤后的字符串
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
                updateLoginButtonState()
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupPasswordInputFilter() {
        // 密码允许的字符：数字和大小写字母
        val passwordPattern = Pattern.compile("^[a-zA-Z0-9]*$")
        
        pwdEdit.filters = arrayOf(
            InputFilter { source, start, end, dest, dstart, dend ->
                // 如果source为空（删除操作），允许
                if (source == null || source.isEmpty()) {
                    return@InputFilter null
                }
                
                // 检查每个要输入的字符
                val builder = StringBuilder()
                for (i in start until end) {
                    val c = source[i]
                    if (passwordPattern.matcher(c.toString()).matches()) {
                        builder.append(c)
                    }
                }
                
                // 如果所有字符都合法，返回null（允许输入）
                // 如果有字符被过滤，返回过滤后的字符串
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
                updateLoginButtonState()
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupLoginButtonState() {
        updateLoginButtonState()
    }

    private fun updateLoginButtonState() {
        val email = emailEdit.text.toString().trim()
        val password = pwdEdit.text.toString()

        // 检查邮箱格式是否完整且有效，密码长度是否超过6位
        val isEmailValid = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.length >= 6

        loginButton.alpha = if (isEmailValid && isPasswordValid) 1f else 0.5f
    }

    private fun setupLoginButtonClick() {
        loginButton.setOnClickListener {
            val email = emailEdit.text.toString().trim()
            val password = pwdEdit.text.toString()

            // 再次验证输入
            val isEmailValid = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
            val isPasswordValid = password.length >= 6

            if (!isEmailValid || !isPasswordValid) {
                return@setOnClickListener
            }

            // 调用登录接口
            performLogin(email, password)
        }
    }

    private fun performLogin(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val request = LoginRequest(
                    contactAd = email,
                    pwd = password
                )

                Log.i("API", "开始登录，邮箱: $email")
                val response = ApiClient.collectionApi.login(request)
                Log.i("API", "收到响应，code: ${response.code}, data: ${response.data}, token: ${response.token}")

                if (response.code == 0) {
                    // 登录成功，保存 token
                    response.token?.let { token ->
                        PrefsManager.saveToken(this@Login, token)
                        Log.i("API", "Token 已保存到 SharedPreferences")
                    }
                    // 登录成功
                    Toast.makeText(this@Login, response.data, Toast.LENGTH_SHORT).show()
                    // 跳转到 MainActivity
                    val intent = Intent(this@Login, MainActivity::class.java)
                    startActivity(intent)
                    finish() // 关闭登录页面
                } else {
                    // 账号或密码错误（API正常返回但code=1）
                    Toast.makeText(this@Login, response.data, Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                // 网络连接异常
                Log.e("API", "网络连接异常: ${e.message}", e)
                Toast.makeText(this@Login, "网络连接失败，请检查网络设置", Toast.LENGTH_SHORT).show()
            } catch (e: SocketTimeoutException) {
                // 请求超时
                Log.e("API", "请求超时: ${e.message}", e)
                Toast.makeText(this@Login, "请求超时，请检查网络连接", Toast.LENGTH_SHORT).show()
            } catch (e: UnknownHostException) {
                // 无法解析主机
                Log.e("API", "无法连接服务器: ${e.message}", e)
                Toast.makeText(this@Login, "无法连接服务器，请检查网络", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // 其他异常
                Log.e("API", "登录请求异常: ${e.message}", e)
                Toast.makeText(this@Login, "登录失败，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        }
    }
}