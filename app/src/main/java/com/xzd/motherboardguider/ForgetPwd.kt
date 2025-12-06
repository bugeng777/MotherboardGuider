package com.xzd.motherboardguider

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import android.content.Context
import com.xzd.motherboardguider.api.ApiClient
import com.xzd.motherboardguider.bean.ForgetPwdRequest
import com.xzd.motherboardguider.bean.SendVerificationCodeRequest
import com.xzd.motherboardguider.utils.PrefsManager
import com.xzd.motherboardguider.utils.LocaleHelper
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.regex.Pattern

class ForgetPwd : ComponentActivity() {
    private lateinit var emailEdit: EditText
    private lateinit var verfiWordEdit: EditText
    private lateinit var newPwdEdit: EditText
    private lateinit var confirmPwdEdit: EditText
    private lateinit var changePwdButton: RelativeLayout
    private lateinit var backToHome: TextView
    private lateinit var getVerificationCodeButton: TextView
    private var countDownTimer: CountDownTimer? = null

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = PrefsManager.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, savedLanguage)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_pwd)

        emailEdit = findViewById(R.id.forget_email_edit)
        verfiWordEdit = findViewById(R.id.forget_pwd_edit)
        newPwdEdit = findViewById(R.id.forget_pwd_new_edit)
        confirmPwdEdit = findViewById(R.id.forget_pwd_new_confirm_edit)
        changePwdButton = findViewById(R.id.changePwdButton)
        backToHome = findViewById(R.id.backToHome)
        getVerificationCodeButton = findViewById(R.id.getVerificationCodeButton)

        backToHome.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(this@ForgetPwd, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })

        getVerificationCodeButton.setOnClickListener {
            val email = emailEdit.text.toString().trim()
            // 先判断邮箱地址是否正确
            if (email.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, getString(R.string.please_enter_correct_email), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 发送验证码
            sendVerificationCode(email)
        }

        setupEmailInputFilter()
        setupVerfiWordInputFilter()
        setupPasswordInputFilter()
        setupChangePwdButtonState()
        setupChangePwdButtonClick()
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
                updateChangePwdButtonState()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupVerfiWordInputFilter() {
        // 验证码允许的字符：数字和大小写字母
        val verfiWordPattern = Pattern.compile("^[a-zA-Z0-9]*$")
        
        verfiWordEdit.filters = arrayOf(
            InputFilter { source, start, end, dest, dstart, dend ->
                if (source == null || source.isEmpty()) {
                    return@InputFilter null
                }
                
                val builder = StringBuilder()
                for (i in start until end) {
                    val c = source[i]
                    if (verfiWordPattern.matcher(c.toString()).matches()) {
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

        verfiWordEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateChangePwdButtonState()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupPasswordInputFilter() {
        // 密码允许的字符：数字和大小写字母
        val passwordPattern = Pattern.compile("^[a-zA-Z0-9]*$")
        
        val passwordFilter = InputFilter { source, start, end, dest, dstart, dend ->
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

        newPwdEdit.filters = arrayOf(passwordFilter)
        confirmPwdEdit.filters = arrayOf(passwordFilter)

        val passwordWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateChangePwdButtonState()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        newPwdEdit.addTextChangedListener(passwordWatcher)
        confirmPwdEdit.addTextChangedListener(passwordWatcher)
    }

    private fun setupChangePwdButtonState() {
        updateChangePwdButtonState()
    }

    private fun updateChangePwdButtonState() {
        val email = emailEdit.text.toString().trim()
        val verfiWord = verfiWordEdit.text.toString()
        val newPwd = newPwdEdit.text.toString()
        val confirmPwd = confirmPwdEdit.text.toString()

        // 检查邮箱格式是否完整且有效，验证码为6位，密码长度是否超过6位且不超过32位，两次密码是否一致
        val isEmailValid = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isVerfiWordValid = verfiWord.length == 6
        val isNewPwdValid = newPwd.length >= 6 && newPwd.length <= 32
        val isConfirmPwdValid = confirmPwd.length >= 6 && confirmPwd.length <= 32
        val isPwdMatch = newPwd == confirmPwd && newPwd.isNotEmpty()

        changePwdButton.alpha = if (isEmailValid && isVerfiWordValid && isNewPwdValid && isConfirmPwdValid && isPwdMatch) 1f else 0.5f
    }

    private fun setupChangePwdButtonClick() {
        changePwdButton.setOnClickListener {
            val email = emailEdit.text.toString().trim()
            val verfiWord = verfiWordEdit.text.toString()
            val newPwd = newPwdEdit.text.toString()
            val confirmPwd = confirmPwdEdit.text.toString()

            // 再次验证输入
            val isEmailValid = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
            val isVerfiWordValid = verfiWord.isNotEmpty()
            val isNewPwdValid = newPwd.length >= 6 && newPwd.length <= 32
            val isConfirmPwdValid = confirmPwd.length >= 6 && confirmPwd.length <= 32
            val isPwdMatch = newPwd == confirmPwd

            if (!isEmailValid) {
                Toast.makeText(this, getString(R.string.please_enter_correct_email_short), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isVerfiWordValid) {
                Toast.makeText(this, getString(R.string.please_enter_6_digit_code), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isNewPwdValid) {
                Toast.makeText(this, getString(R.string.password_length_6_32), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isPwdMatch) {
                Toast.makeText(this, getString(R.string.passwords_not_match), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 调用修改密码接口
            performChangePassword(email, verfiWord, newPwd, confirmPwd)
        }
    }

    private fun performChangePassword(email: String, verfiWord: String, newPwd: String, confirmPwd: String) {
        lifecycleScope.launch {
            try {
                val request = ForgetPwdRequest(
                    contactAd = email,
                    verfiWord = verfiWord,
                    pwd = newPwd,
                    reinputPwd = confirmPwd
                )

                Log.i("API", "开始修改密码，邮箱: $email")
                val response = ApiClient.collectionApi.changePassword(request)
                Log.i("API", "收到响应，code: ${response.code}, data: ${response.data}")

                if (response.code == 0) {
                    // 修改成功，跳转到登录页面
                    Toast.makeText(this@ForgetPwd, response.data, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ForgetPwd, Login::class.java)
                    startActivity(intent)
                    finish() // 关闭忘记密码页面
                } else {
                    // 修改失败（code=1），显示错误信息
                    Toast.makeText(this@ForgetPwd, response.data, Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                // 网络连接异常
                Log.e("API", "网络连接异常: ${e.message}", e)
                Toast.makeText(this@ForgetPwd, getString(R.string.network_error), Toast.LENGTH_SHORT).show()
            } catch (e: SocketTimeoutException) {
                // 请求超时
                Log.e("API", "请求超时: ${e.message}", e)
                Toast.makeText(this@ForgetPwd, getString(R.string.request_timeout), Toast.LENGTH_SHORT).show()
            } catch (e: UnknownHostException) {
                // 无法解析主机
                Log.e("API", "无法连接服务器: ${e.message}", e)
                Toast.makeText(this@ForgetPwd, getString(R.string.cannot_connect_server), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // 其他异常
                Log.e("API", "修改密码请求异常: ${e.message}", e)
                Toast.makeText(this@ForgetPwd, getString(R.string.change_password_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendVerificationCode(email: String) {
        lifecycleScope.launch {
            try {
                val request = SendVerificationCodeRequest(
                    contactAd = email,
                    appName = "mg" // 应用名称
                )

                Log.i("API", "开始发送验证码，邮箱: $email")
                val response = ApiClient.collectionApi.sendVerificationCode(request)
                Log.i("API", "收到响应，code: ${response.code}, data: ${response.data}")

                if (response.code == 0) {
                    // 发送成功，开始倒计时
                    startCountDown()
                    Toast.makeText(this@ForgetPwd, response.data, Toast.LENGTH_SHORT).show()
                } else {
                    // 发送失败，显示错误信息
                    Toast.makeText(this@ForgetPwd, response.data, Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Log.e("API", "网络连接异常: ${e.message}", e)
                Toast.makeText(this@ForgetPwd, getString(R.string.network_error), Toast.LENGTH_SHORT).show()
            } catch (e: SocketTimeoutException) {
                Log.e("API", "请求超时: ${e.message}", e)
                Toast.makeText(this@ForgetPwd, getString(R.string.request_timeout), Toast.LENGTH_SHORT).show()
            } catch (e: UnknownHostException) {
                Log.e("API", "无法连接服务器: ${e.message}", e)
                Toast.makeText(this@ForgetPwd, getString(R.string.cannot_connect_server), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("API", "发送验证码请求异常: ${e.message}", e)
                Toast.makeText(this@ForgetPwd, getString(R.string.send_verification_code_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCountDown() {
        // 取消之前的倒计时
        countDownTimer?.cancel()
        
        // 禁用按钮
        getVerificationCodeButton.isEnabled = false
        getVerificationCodeButton.isClickable = false
        getVerificationCodeButton.alpha = 0.5f

        // 创建60秒倒计时
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                getVerificationCodeButton.text = getString(R.string.get_verification_code_countdown, seconds.toInt())
            }

            override fun onFinish() {
                // 倒计时结束，恢复按钮
                getVerificationCodeButton.text = getString(R.string.get_verification_code)
                getVerificationCodeButton.isEnabled = true
                getVerificationCodeButton.isClickable = true
                getVerificationCodeButton.alpha = 1f
                countDownTimer = null
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 页面销毁时取消倒计时
        countDownTimer?.cancel()
    }
}