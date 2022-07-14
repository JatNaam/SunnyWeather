package com.sunnyweather.android.ui.place

import androidx.lifecycle.*
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

/*ViewModel可以保证手机发生旋转时屏幕显示数据也不会消失*/
class PlaceViewModel : ViewModel() {
    /*用于对界面上显示的城市数据进行缓存*/
    val placeList = ArrayList<Place>()

    /*使用LiveData包装的值，然后在Activity中观察它，就可以主动将数据变化通知给Activity，解决线程问题*/
    private val searchLiveData = MutableLiveData<String>()
    val placeLiveData = Transformations.switchMap(searchLiveData) { query ->
        Repository.searchPlaces(query)
        /*如果使用JAVA的话，调用KT的单例类的写法是：Repository.INSTANCE.searchPlaces(query)*/
    }
    /*每当这个函数被调用时，searchLiveData发生变化，map方法会监听到变化并执行转换函数
    * 转换函数中调用仓库层Repository中的searchPlaces()方法发起网络请求并返回一个可供Activity观察的LiveData对象*/
    fun searchPlaces(query: String) {
        searchLiveData.value = query
    }

//    fun savePlace(place: Place) = Repository.savePlace(place)
//
//    fun getSavedPlace() = Repository.getSavedPlace()
//
//    fun isPlaceSaved() = Repository.isPlaceSaved()

}