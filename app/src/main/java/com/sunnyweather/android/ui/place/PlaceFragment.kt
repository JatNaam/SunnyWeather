package com.sunnyweather.android.ui.place

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunnyweather.android.MainActivity
import com.sunnyweather.android.databinding.FragmentPlaceBinding
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceFragment : Fragment() {

    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }

    private lateinit var adapter: PlaceAdapter

    private var _binding: FragmentPlaceBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceBinding.inflate(inflater, container, false)
        /*在这里进行判断当前是否已经有存储的城市数据，有就直接跳转
       * 加多一层判断：只有当PlaceFragment嵌入MainActivity时才会直接跳转，
       * 解决了在WeatherActivity更换地点无限跳转的问题*/
        if (activity is MainActivity && viewModel.isPlaceSaved()) {
            val place = viewModel.getSavedPlace()
            val intent = Intent(context, WeatherActivity::class.java).apply {
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
                putExtra("place_name", place.name)
            }
            startActivity(intent)
            activity?.finish()
            return binding.root
        }

        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager
        adapter = PlaceAdapter(this, viewModel.placeList)
        binding.recyclerView.adapter = adapter

        /*给搜索栏添加文本变化监听器*/
        binding.searchPlaceEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()) {
                viewModel.searchPlaces(content)
            } else {
                binding.recyclerView.visibility = View.GONE
                binding.bgImageView.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }

        viewModel.placeLiveData.observe(viewLifecycleOwner, Observer { result ->
            val places = result.getOrNull()
            if (places != null) {
                binding.recyclerView.visibility = View.VISIBLE
                binding.bgImageView.visibility = View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(activity, "未能查询到任何地点", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    @Deprecated("Deprecated in Java")
//    @SuppressLint("NotifyDataSetChanged")
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        /*在这里进行判断当前是否已经有存储的城市数据，有就直接跳转
//        * 加多一层判断：只有当PlaceFragment嵌入MainActivity时才会直接跳转，
//        * 解决了在WeatherActivity更换地点无限跳转的问题*/
//        if (activity is MainActivity && viewModel.isPlaceSaved()) {
//            val place = viewModel.getSavedPlace()
//            val intent = Intent(context, WeatherActivity::class.java).apply {
//                putExtra("location_lng", place.location.lng)
//                putExtra("location_lat", place.location.lat)
//                putExtra("place_name", place.name)
//            }
//            startActivity(intent)
//            activity?.finish()
//            return
//        }
//
//        val layoutManager = LinearLayoutManager(activity)
//        binding.recyclerView.layoutManager = layoutManager
//        adapter = PlaceAdapter(this, viewModel.placeList)
//        binding.recyclerView.adapter = adapter
//
//        /*给搜索栏添加文本变化监听器*/
//        binding.searchPlaceEdit.addTextChangedListener { editable ->
//            val content = editable.toString()
//            if (content.isNotEmpty()) {
//                viewModel.searchPlaces(content)
//            } else {
//                binding.recyclerView.visibility = View.GONE
//                binding.bgImageView.visibility = View.VISIBLE
//                viewModel.placeList.clear()
//                adapter.notifyDataSetChanged()
//            }
//        }
//
//        viewModel.placeLiveData.observe(viewLifecycleOwner, Observer { result ->
//            val places = result.getOrNull()
//            if (places != null) {
//                binding.recyclerView.visibility = View.VISIBLE
//                binding.bgImageView.visibility = View.GONE
//                viewModel.placeList.clear()
//                viewModel.placeList.addAll(places)
//                adapter.notifyDataSetChanged()
//            } else {
//                Toast.makeText(activity, "未能查询到任何地点", Toast.LENGTH_SHORT).show()
//                result.exceptionOrNull()?.printStackTrace()
//            }
//        })
//    }

}