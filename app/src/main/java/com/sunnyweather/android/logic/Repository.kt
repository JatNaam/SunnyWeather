package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import java.lang.Exception

object Repository {
    /*此处的liveData()函数是lifecycle-livedata-ktx库提供的一个功能：
    * 它可以自动构建并返回一个LiveData对象，然后在它的代码块中提供一个挂起函数的上下文
    * 所以可以在它的代码块调用任意的挂起函数，这里调用了SunnyWeatherNetwork.searchPlaces()*/
    fun searchPlaces(query: String) = liveData(Dispatchers.IO){
        val result=try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            /*服务器响应为OK则用KT内置的Result.success()方法包装获取到的城市数据
            * 否则用Result.failure()包装一个异常
            * 最后用emit将包装结果发射出去*/
            if (placeResponse.status=="ok"){
                val places =placeResponse.places
                Result.success(places)
            }else{
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        }catch (e :Exception){
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }
}