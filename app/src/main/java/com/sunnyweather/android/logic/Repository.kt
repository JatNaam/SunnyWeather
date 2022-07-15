package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.Exception

object Repository {
    /*此处的liveData()函数是lifecycle-livedata-ktx库提供的一个功能：
    * 它可以自动构建并返回一个LiveData对象，然后在它的代码块中提供一个挂起函数的上下文
    * 所以可以在它的代码块调用任意的挂起函数，这里调用了SunnyWeatherNetwork.searchPlaces()*/
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            /*发起网络请求*/
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            /*服务器响应为OK则用KT内置的Result.success()方法包装获取到的城市数据
            * 否则用Result.failure()包装一个异常
            * 最后用emit将包装结果发射出去*/
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }

    fun refreshWeather(lng: String, lat: String) = liveData(Dispatchers.IO) {
        val result = try {
            /*用coroutineScope函数创建一个协程作用域，因为async函数必须再协程作用域内才能调用*/
            coroutineScope {
                /*使用async函数发起网络请求再调用await()可以使两个网络请求都成功响应后再近一半执行程序*/
                val deferredRealtime = async {
                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
                }
                val deferredDaily = async {
                    SunnyWeatherNetwork.getDailyWeather(lng, lat)
                }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                    val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                    Result.success(weather)
                } else {
                    Result.failure(
                        RuntimeException(
                            "realtime response status is ${realtimeResponse.status}" +
                                    "daily response status is ${dailyResponse.status}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure<Weather>(e)
        }
        emit(result)
    }

    /*这里只是进行接口封装
    * 这里的写法不标准，数据读写不建议在主线程进行，这里为了简单直接进行操作
    * 标准写法是开启子线程再通过LiveData对象进行数据返回*/
    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()
}