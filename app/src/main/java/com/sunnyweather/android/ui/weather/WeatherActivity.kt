package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.databinding.ForecastBinding
import com.sunnyweather.android.databinding.LifeIndexBinding
import com.sunnyweather.android.databinding.NowBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    /*kotlin-android-extensions插件被弃用
    * 使用ViewBinding来替代之前的kotlin-android-extensions插件
    * https://guolin.blog.csdn.net/article/details/113089706?spm=1001.2101.3001.6661.1&utm_medium=distribute.pc_relevant_t0.none-task-blog-2~default~CTRLIST~Rate-1.pc_relevant_default&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-2~default~CTRLIST~Rate-1.pc_relevant_default&utm_relevant_index=1*/
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var nowBinding: NowBinding
    private lateinit var forecastBinding: ForecastBinding
    private lateinit var lifeIndexBinding: LifeIndexBinding

    /*为了能让使用JAVA写的PlaceAdapter获取到，所以需要这样定义
    * 若Adapter是KT写的则可以直接通过ID值获取到*/
    lateinit var drawerLayout:DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWeatherBinding.inflate(layoutInflater)
        nowBinding = NowBinding.bind(binding.root)
        forecastBinding = ForecastBinding.bind(binding.root)
        lifeIndexBinding = LifeIndexBinding.bind(binding.root)
        setContentView(binding.root)

        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        drawerLayout=findViewById(R.id.drawerLayout)
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            /*隐藏刷新进度条*/
            binding.swipeRefresh.isRefreshing = false
        })
        /*设置下拉进度条的颜色*/
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        /*打开活动先刷新一下天气信息*/
        refreshWeather()
        /*设置下拉刷新监听器*/
        binding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
        nowBinding.navBtn.setOnClickListener {
            /*打开滑动菜单*/
            drawerLayout.openDrawer(GravityCompat.START)
        }
        /*给滑动菜单添加监听器*/
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                /*隐藏滑动菜单的同时隐藏输入法*/
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })
    }

    fun refreshWeather() {
        /*调用ViewModel的方法刷新天气信息*/
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        /*显示下拉刷新进度条*/
        binding.swipeRefresh.isRefreshing = true
    }


    private fun showWeatherInfo(weather: Weather) {
        nowBinding.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        // 填充now.xml布局中数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        nowBinding.currentTemp.text = currentTempText
        nowBinding.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        nowBinding.currentAQI.text = currentPM25Text
        nowBinding.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充forecast.xml布局中的数据
        forecastBinding.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            /*循环加载forecast_item.xml布局添加数据，并添加到父布局forecastLayout中*/
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            /*加载布局*/
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastBinding.forecastLayout, false)

            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            /*添加到父布局*/
            forecastBinding.forecastLayout.addView(view)
        }

        // 填充life_index.xml布局中的数据
        /*返回了许多天的数据，但我们只需要当天的，所以只取下标为0的*/
        val lifeIndex = daily.lifeIndex
        lifeIndexBinding.coldRiskText.text = lifeIndex.coldRisk[0].desc
        lifeIndexBinding.dressingText.text = lifeIndex.dressing[0].desc
        lifeIndexBinding.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        lifeIndexBinding.carWashingText.text = lifeIndex.carWashing[0].desc
        binding.weatherLayout.visibility = View.VISIBLE
    }

}
