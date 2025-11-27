package com.xzd.motherboardguider

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.transition.Visibility
import java.util.regex.Pattern
import com.github.gzuliyujiang.wheelpicker.AddressPicker
import com.github.gzuliyujiang.wheelpicker.OptionPicker
import com.github.gzuliyujiang.wheelpicker.annotation.AddressMode
import com.github.gzuliyujiang.wheelpicker.contract.OnAddressPickedListener
import com.github.gzuliyujiang.wheelpicker.contract.OnOptionPickedListener
import com.github.gzuliyujiang.wheelpicker.entity.CityEntity
import com.github.gzuliyujiang.wheelpicker.entity.CountyEntity
import com.github.gzuliyujiang.wheelpicker.entity.ProvinceEntity
import com.github.gzuliyujiang.wheelpicker.utility.AddressJsonParser
import com.github.gzuliyujiang.wheelpicker.widget.LinkageWheelLayout
import com.github.gzuliyujiang.wheelview.annotation.CurtainCorner
import com.google.android.flexbox.FlexboxLayout
import com.xzd.motherboardguider.bean.CPUBean
import com.xzd.motherboardguider.bean.CpuModelBean
import com.xzd.motherboardguider.bean.DiskBean
import com.xzd.motherboardguider.bean.GPUBean
import com.xzd.motherboardguider.bean.GpuModelBean
import org.json.JSONArray

class MainActivity : ComponentActivity() {

    private var cpuSelectorText: TextView? = null
    private var gpuSelectorText: TextView? = null
    private var diskSelectorText: TextView? = null
    private var cpuValue:CPUBean?=null
    private var gpuValue:GPUBean?=null
    private var diskVale:DiskBean?=null
    private lateinit var startCalButton: RelativeLayout
    private lateinit var expectPower:TextView
    private lateinit var expectSuggestMotherboard:TextView
    private lateinit var expectSupportMotherboard:TextView
    private lateinit var expectSupportMotherboardList: FlexboxLayout
    private lateinit var startCalText:TextView
    private var calCount=0;// 如果是0，就显示开始测算，如果是0以外的数字，就显示重新测算
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cpuSelectorText = findViewById(R.id.cpuSelectorText)
        gpuSelectorText = findViewById(R.id.gpuSelectorText)
        diskSelectorText = findViewById(R.id.diskSelectorText)
        val cpuSelectorButton = findViewById<LinearLayout>(R.id.cpuSelectorButton)
        cpuSelectorButton.setOnClickListener {
            showCpuPicker()
        }
        val gpuSelectorButton = findViewById<LinearLayout>(R.id.gpuSelectorButton)
        gpuSelectorButton.setOnClickListener {
            showGpuPicker()
        }
        val diskSelectorButton = findViewById<LinearLayout>(R.id.diskSelectorButton)
        diskSelectorButton.setOnClickListener{
            showDiskPicker()
        }
        startCalButton= findViewById(R.id.startCalButton)
        expectPower=findViewById(R.id.expectPower)
        expectSuggestMotherboard=findViewById(R.id.expectSuggestMotherboard)
        expectSupportMotherboard=findViewById(R.id.expectSupportMotherboard)
        expectSupportMotherboardList=findViewById(R.id.expectSupportMotherboardList)

        startCalText=findViewById(R.id.startCalText)
        
        val saveConfigButton = findViewById<RelativeLayout>(R.id.saveConfigButton)
        saveConfigButton.setOnClickListener {
            showConfigNameDialog()
        }
    }
    private fun resetExpectText(){
        setTextStatus(expectPower,"待测算",1) //重制下面预计功耗和主板系列
        setTextStatus(expectSupportMotherboard,"待测算",1) //重制下面预计功耗和主板系列
        setTextStatus(expectSuggestMotherboard,"待测算",1) //重制下面预计功耗和主板系列
        expectSupportMotherboardList.removeAllViews() //删掉所有TextView
        expectSupportMotherboard.visibility=View.VISIBLE
        expectSupportMotherboardList.visibility=View.GONE
    }
    private fun showDiskPicker() {
        val data: MutableList<DiskBean?> = ArrayList<DiskBean?>()
        for(item:Int in  1..99){
            data.add(DiskBean(item, "$item"))
        }
        val diskPicker = OptionPicker(this)
        diskPicker.setTitle("硬盘数量")
        diskPicker.setBodyWidth(140)
        diskPicker.setData(data)
        diskPicker.setDefaultPosition(2)
        diskPicker.setOnOptionPickedListener(object:OnOptionPickedListener{
            override fun onOptionPicked(position: Int, item: Any?) {
                resetExpectText() //重置输出结果
                diskVale = item as DiskBean
                Log.i("看看硬盘数量","${item.name} 个")
                setTextStatus(diskSelectorText!!, "${item.name} 个", 0)
                checkStartCalStatus() //检查一下开始计算的按钮状态
            }
        })
        val wheelLayout = diskPicker.wheelLayout
        wheelLayout.background = getDrawable(R.drawable.pop_background)
        wheelLayout.setTextColor(getColor(R.color.white))
        wheelLayout.setSelectedTextColor(getColor(R.color.white))
        val textSize14sp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
        wheelLayout.setTextSize(textSize14sp)
        wheelLayout.setSelectedTextSize(textSize14sp)
        wheelLayout.setIndicatorColor(getColor(R.color.pop_indicator_line))
        wheelLayout.setSelectedTextBold(false)
        wheelLayout.setCurtainEnabled(false)
        wheelLayout.setCurtainColor(-0x252A30)

        diskPicker.cancelView.setTextColor(getColor(R.color.white))
        diskPicker.cancelView.setTextSize(16F)
        diskPicker.okView.setTextColor(getColor(R.color.white))
        diskPicker.okView.setTextSize(16F)
        diskPicker.titleView.setTextColor(getColor(R.color.white))
        diskPicker.titleView.setTextSize(16F)

        diskPicker.contentView.setBackgroundColor(getColor(R.color.pop_background))

        diskPicker.headerView.setBackgroundColor(getColor(R.color.pop_background))
        diskPicker.topLineView.setBackgroundColor(getColor(R.color.pop_divider))
        diskPicker.setAnimationStyle(R.style.PopupAnimation)

//        wheelLayout.setCurtainRadius(8 * view.getResources().getDisplayMetrics().density)
//        val padding = (10 * view.getResources().getDisplayMetrics().density) as Int
        wheelLayout.setPadding(0, 0, 0, 0)
        wheelLayout.setCurtainCorner(CurtainCorner.ALL)
//        wheelLayout.setCurtainRadius(5 * view.getResources().getDisplayMetrics().density)
        wheelLayout.setOnOptionSelectedListener { position, item ->
            diskPicker.titleView.text = diskPicker.wheelView.formatItem(position)
        }
        diskPicker.show()
    }
    private fun showCpuPicker() {
        val cpuPicker = AddressPicker(this)
        cpuPicker.setAddressMode(
            "cpu_data.json", AddressMode.PROVINCE_CITY_COUNTY,
            AddressJsonParser.Builder()
                .provinceCodeField("id")
                .provinceNameField("name")
                .provinceChildField("series")
                .cityCodeField("id")
                .cityNameField("name")
                .cityChildField("model")
                .countyCodeField("id")
                .countyNameField("name")
                .build()
        )
        cpuPicker.setTitle("CPU");
        cpuPicker.setDefaultValue("Intel", "200系列", "Ultra 285K");
        cpuPicker.setOnAddressPickedListener(object : OnAddressPickedListener {
            override fun onAddressPicked(
                province: ProvinceEntity?,
                city: CityEntity?,
                county: CountyEntity?
            ) {
                resetExpectText() //重置输出结果
                if (province != null && city != null && county != null) {
                    Log.i("CPU选择", "${province}  ${city} ${county}")
                    setTextStatus(cpuSelectorText!!, "${province.name} ｜ ${city.name} ｜ ${county.name}", 0)
                    val cb = CPUBean()
                    cb.brandId=province.code
                    cb.seriesId=city.code
                    cb.modelId=county.code
                    // 读取主板信息并保存到 CPUBean
                    val motherboardInfo = getCpuMotherboardInfo(province.code, city.code, county.code)
                    cb.supportedMotherboards = motherboardInfo.first
                    cb.recommendedMotherboards = motherboardInfo.second
                    cpuValue=cb
                } else {
                    cpuValue=null
                    Toast.makeText(baseContext, "选项内容有错误请重启app", Toast.LENGTH_SHORT).show()
                    setTextStatus(cpuSelectorText!!, "请选择", 1)
                }
                checkStartCalStatus() //检查一下开始计算的按钮状态
            }
        })
        val wheelLayout: LinkageWheelLayout = cpuPicker.wheelLayout
//        wheelLayout.setTextSize(15 * view.getResources().getDisplayMetrics().scaledDensity)
//        wheelLayout.setSelectedTextSize(17 * view.getResources().getDisplayMetrics().scaledDensity)
        wheelLayout.background = getDrawable(R.drawable.pop_background)
        wheelLayout.setTextColor(getColor(R.color.white))
        wheelLayout.setSelectedTextColor(getColor(R.color.white))
        wheelLayout.setIndicatorColor(getColor(R.color.pop_indicator_line))
        val textSize14sp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
        wheelLayout.setTextSize(textSize14sp)
        wheelLayout.setSelectedTextSize(textSize14sp)
        wheelLayout.setSelectedTextBold(false)
        wheelLayout.setCurtainEnabled(false)
        wheelLayout.setCurtainColor(-0x252A30)

//        wheelLayout.setCurtainRadius(8 * view.getResources().getDisplayMetrics().density)
//        val padding = (10 * view.getResources().getDisplayMetrics().density) as Int
        wheelLayout.setPadding(0, 0, 0, 0)
        wheelLayout.setOnLinkageSelectedListener { first, second, third ->
            cpuPicker.titleView.text = java.lang.String.format(
                "%s%s%s",
                cpuPicker.provinceWheelView.formatItem(first),
                cpuPicker.cityWheelView.formatItem(second),
                cpuPicker.countyWheelView.formatItem(third)
            )
        }

        cpuPicker.cancelView.setTextColor(getColor(R.color.white))
        cpuPicker.cancelView.setTextSize(16F)
        cpuPicker.okView.setTextColor(getColor(R.color.white))
        cpuPicker.okView.setTextSize(16F)
        cpuPicker.titleView.setTextColor(getColor(R.color.white))
        cpuPicker.titleView.setTextSize(16F)

        cpuPicker.headerView.setBackgroundColor(getColor(R.color.pop_background))
        cpuPicker.topLineView.setBackgroundColor(getColor(R.color.pop_divider))
        cpuPicker.provinceWheelView.setCurtainCorner(CurtainCorner.LEFT)
        cpuPicker.cityWheelView.setCurtainCorner(CurtainCorner.RIGHT)
        cpuPicker.setAnimationStyle(R.style.PopupAnimation)

        cpuPicker.show()
    }
    private fun showGpuPicker(){
        val gpuPicker = AddressPicker(this)
        gpuPicker.setAddressMode(
            "gpu_data.json", AddressMode.PROVINCE_CITY_COUNTY,
            AddressJsonParser.Builder()
                .provinceCodeField("id")
                .provinceNameField("name")
                .provinceChildField("series")
                .cityCodeField("id")
                .cityNameField("name")
                .cityChildField("model")
                .countyCodeField("id")
                .countyNameField("name")
                .build()
        )
        gpuPicker.setTitle("GPU");
        gpuPicker.setDefaultValue("Intel", "200系列", "Ultra 285K");
        gpuPicker.setOnAddressPickedListener(object : OnAddressPickedListener {
            override fun onAddressPicked(
                province: ProvinceEntity?,
                city: CityEntity?,
                county: CountyEntity?
            ) {
                resetExpectText() //重置输出结果
                if (province != null && city != null && county != null) {
                    Log.i("GPU选择", "${province.name}  ${city.name} ${county.name}")
                    setTextStatus(gpuSelectorText!!, "${city.name} ｜ ${county.name}", 0)
                    val gb = GPUBean()
                    gb.brandId=province.code
                    gb.seriesId=city.code
                    gb.modelId=county.code
                    gpuValue=gb
                } else {
                    gpuValue=null
                    Toast.makeText(baseContext, "选项内容有错误请重启app", Toast.LENGTH_SHORT).show()
                    setTextStatus(gpuSelectorText!!, "请选择", 1)
                }
                checkStartCalStatus() //检查一下开始计算的按钮状态
            }
        })
        val wheelLayout: LinkageWheelLayout = gpuPicker.wheelLayout
//        wheelLayout.setTextSize(15 * view.getResources().getDisplayMetrics().scaledDensity)
//        wheelLayout.setSelectedTextSize(17 * view.getResources().getDisplayMetrics().scaledDensity)
        wheelLayout.background = getDrawable(R.drawable.pop_background)
        wheelLayout.setTextColor(getColor(R.color.white))
        wheelLayout.setSelectedTextColor(getColor(R.color.white))
        wheelLayout.setIndicatorColor(getColor(R.color.pop_indicator_line))
        val textSize14sp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
        wheelLayout.setTextSize(textSize14sp)
        wheelLayout.setSelectedTextSize(textSize14sp)
        wheelLayout.setSelectedTextBold(false)
        wheelLayout.setCurtainEnabled(false)
        wheelLayout.setCurtainColor(-0x252A30)
//        wheelLayout.setCurtainRadius(8 * view.getResources().getDisplayMetrics().density)
//        val padding = (10 * view.getResources().getDisplayMetrics().density) as Int
        wheelLayout.setPadding(0, 0, 0, 0)
        wheelLayout.setOnLinkageSelectedListener { first, second, third ->
            gpuPicker.titleView.text = java.lang.String.format(
                "%s%s%s",
                gpuPicker.provinceWheelView.formatItem(first),
                gpuPicker.cityWheelView.formatItem(second),
                gpuPicker.countyWheelView.formatItem(third)
            )
        }
        gpuPicker.cancelView.setTextColor(getColor(R.color.white))
        gpuPicker.cancelView.setTextSize(16F)
        gpuPicker.okView.setTextColor(getColor(R.color.white))
        gpuPicker.okView.setTextSize(16F)
        gpuPicker.titleView.setTextColor(getColor(R.color.white))
        gpuPicker.titleView.setTextSize(16F)
        gpuPicker.headerView.setBackgroundColor(getColor(R.color.pop_background))
        gpuPicker.topLineView.setBackgroundColor(getColor(R.color.pop_divider))
        gpuPicker.provinceWheelView.setCurtainCorner(CurtainCorner.LEFT)
        gpuPicker.cityWheelView.setCurtainCorner(CurtainCorner.RIGHT)
        gpuPicker.setAnimationStyle(R.style.PopupAnimation)
        gpuPicker.show()
    }
    private fun startCal(){
        val cpuModel = findCpuModel(cpuValue)
        val gpuModel = findGpuModel(gpuValue)
        if(cpuModel==null || gpuModel==null){
            Toast.makeText(this,"无法读取所选配置，请重试",Toast.LENGTH_SHORT).show()
            return
        }
        Log.i("StartCal","CPU: ${cpuModel.name} ｜ ${cpuModel.powerConsumption}W ｜ ${cpuModel.releaseYear} ｜" +
                " ${cpuValue?.recommendedMotherboards} |${cpuValue?.supportedMotherboards}")
        Log.i("StartCal","GPU: ${gpuModel.name} ｜ ${gpuModel.powerConsumption}W ｜ ${gpuModel.releaseYear}")
        val diskCount = diskVale?.name?.toIntOrNull() ?: 0
        val cpuPower = cpuModel.powerConsumption?.toFloatOrNull() ?: 0f
        val gpuPower = gpuModel.powerConsumption?.toFloatOrNull() ?: 0f
        val totalPowerConsumption = cpuPower + gpuPower + diskCount * 15f
        expectPower.text = "${totalPowerConsumption}W"
        Log.i("StartCal","预计功耗:${totalPowerConsumption}W")
        val recommendedMotherboardsText = cpuValue?.recommendedMotherboards?.joinToString(" | ") ?: "无"
        val supportedMotherboardsText = cpuValue?.supportedMotherboards?.joinToString(" | ") ?: "无"
        setTextStatus(expectSuggestMotherboard, recommendedMotherboardsText, 0)

        val height=expectSupportMotherboard.height
        expectSupportMotherboard.visibility= View.GONE
        expectSupportMotherboardList.visibility=View.VISIBLE
        expectSupportMotherboardList.removeAllViews() // 先要清理原先所有的view
        val supportedMotherboards = cpuValue!!.supportedMotherboards
        for ((index, item) in supportedMotherboards.withIndex()) {
            val tv = TextView(this)
            val isLast = index == supportedMotherboards.size - 1 //最后一个不加 | 分隔符
            tv.text = if (isLast) item else "$item | "
            tv.textSize = 14f
            tv.height=height
            tv.setTextColor(getColor(R.color.home_name_light))
            expectSupportMotherboardList.addView(tv)
        }
        setTextStatus(expectPower,totalPowerConsumption.toString(),0)
        calCount=1;
        startCalText.text="重新测算"
        // TODO: 后续在这里继续完成测算逻辑
    }

    private fun getCpuMotherboardInfo(brandId: String, seriesId: String, modelId: String): Pair<List<String>, List<String>> {
        val brands = readJsonArrayFromAssets("cpu_data.json") ?: return Pair(emptyList(), emptyList())
        for(i in 0 until brands.length()){
            val brandObj = brands.getJSONObject(i)
            if(brandId == brandObj.optString("id")){
                val seriesArray = brandObj.optJSONArray("series") ?: continue
                for(j in 0 until seriesArray.length()){
                    val seriesObj = seriesArray.getJSONObject(j)
                    if(seriesId == seriesObj.optString("id")){
                        val supportedMotherboards = seriesObj.optJSONArray("supportedMotherboards")?.toStringList()
                            ?: emptyList()
                        val modelArray = seriesObj.optJSONArray("model") ?: continue
                        for(k in 0 until modelArray.length()){
                            val modelObj = modelArray.getJSONObject(k)
                            if(modelId == modelObj.optString("id")){
                                val recommendedMotherboards = modelObj.optJSONArray("recommendedMotherboards")?.toStringList()
                                    ?: emptyList()
                                return Pair(supportedMotherboards, recommendedMotherboards)
                            }
                        }
                    }
                }
            }
        }
        return Pair(emptyList(), emptyList())
    }

    private fun findCpuModel(bean: CPUBean?): CpuModelBean? {
        if(bean==null){
            return null
        }
        val brands = readJsonArrayFromAssets("cpu_data.json") ?: return null
        for(i in 0 until brands.length()){
            val brandObj = brands.getJSONObject(i)
            if(bean.brandId == brandObj.optString("id")){
                val seriesArray = brandObj.optJSONArray("series") ?: continue
                for(j in 0 until seriesArray.length()){
                    val seriesObj = seriesArray.getJSONObject(j)
                    if(bean.seriesId == seriesObj.optString("id")){
                        val modelArray = seriesObj.optJSONArray("model") ?: continue
                        for(k in 0 until modelArray.length()){
                            val modelObj = modelArray.getJSONObject(k)
                            if(bean.modelId == modelObj.optString("id")){
                                return CpuModelBean(
                                    id = modelObj.optString("id"),
                                    code = modelObj.optString("code"),
                                    name = modelObj.optString("name"),
                                    powerConsumption = modelObj.optString("powerConsumption"),
                                    releaseYear = modelObj.optString("releaseYear"),
                                    supportedMotherboards = bean.supportedMotherboards ?: emptyList(),
                                    recommendedMotherboards = bean.recommendedMotherboards ?: emptyList()
                                )
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun findGpuModel(bean: GPUBean?): GpuModelBean? {
        if(bean==null){
            return null
        }
        val brands = readJsonArrayFromAssets("gpu_data.json") ?: return null
        for(i in 0 until brands.length()){
            val brandObj = brands.getJSONObject(i)
            if(bean.brandId == brandObj.optString("id")){
                val seriesArray = brandObj.optJSONArray("series") ?: continue
                for(j in 0 until seriesArray.length()){
                    val seriesObj = seriesArray.getJSONObject(j)
                    if(bean.seriesId == seriesObj.optString("id")){
                        val modelArray = seriesObj.optJSONArray("model") ?: continue
                        for(k in 0 until modelArray.length()){
                            val modelObj = modelArray.getJSONObject(k)
                            if(bean.modelId == modelObj.optString("id")){
                                return GpuModelBean(
                                    id = modelObj.optString("id"),
                                    code = modelObj.optString("code"),
                                    name = modelObj.optString("name"),
                                    powerConsumption = modelObj.optString("powerConsumption"),
                                    releaseYear = modelObj.optString("releaseYear")
                                )
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun readJsonArrayFromAssets(fileName:String): JSONArray?{
        return try{
            val jsonString = assets.open(fileName).bufferedReader().use { it.readText() }
            JSONArray(jsonString)
        }catch (e:Exception){
            Log.e("StartCal","读取 $fileName 失败",e)
            null
        }
    }

    private fun JSONArray.toStringList(): List<String> {
        val result = mutableListOf<String>()
        for (i in 0 until length()) {
            result.add(optString(i))
        }
        return result
    }
    private fun setTextStatus(textView: TextView, context: String, mode: Int) {
        // mode 1是灰色字，0是白色字
        textView.text = context
        val colorRes = when (mode) {
            1 -> R.color.home_name_dark
            0 -> R.color.home_name_light
            else -> R.color.home_name_light
        }
        textView.setTextColor(ContextCompat.getColor(this, colorRes))
    }

    private fun checkStartCalStatus(){
        if(cpuValue!=null&& gpuValue!=null && diskVale!=null){
            startCalButton.alpha=1f
            startCalButton.setOnClickListener(object : OnClickListener {
                override fun onClick(v: View?) {
                    startCal()
                }
            })
        }else{
            startCalButton.alpha=0.5f
            startCalButton.setOnClickListener(object : OnClickListener {
                override fun onClick(v: View?) {
                    //去掉了点击时间
                }
            })
        }

    }
    
    private fun showConfigNameDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_config_name)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // 设置对话框宽度
        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        val editText = dialog.findViewById<EditText>(R.id.configNameEditText)
        val charCountText = dialog.findViewById<TextView>(R.id.charCountText)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)
        
        // 字符计数监听
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                // 验证输入：仅支持汉字/字母/数字
                val pattern = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9]*$")
                if (!pattern.matcher(text).matches()) {
                    // 如果包含非法字符，移除最后一个字符
                    val validText = text.dropLast(1)
                    editText.setText(validText)
                    editText.setSelection(validText.length)
                    return
                }
                if(text.length>0){
                    confirmButton.alpha=1f
                }else{
                    confirmButton.alpha=0.5f
                }
                charCountText.text = "${text.length}/10"
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // 取消按钮
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        // 确定按钮
        confirmButton.setOnClickListener {
            val configName = editText.text.toString().trim()
            if (configName.isEmpty()) {
                Toast.makeText(this, "请输入配置名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // TODO: 保存配置逻辑
            Log.i("保存配置", "配置名称: $configName")
            Toast.makeText(this, "配置已保存: $configName", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        
        dialog.show()
    }
}