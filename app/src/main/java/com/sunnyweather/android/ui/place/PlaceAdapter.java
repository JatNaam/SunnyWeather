package com.sunnyweather.android.ui.place;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.sunnyweather.android.R;
import com.sunnyweather.android.logic.model.Place;
import com.sunnyweather.android.ui.weather.WeatherActivity;


import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {
    private PlaceFragment fragment;
    private List<Place> placeList;//List只是一个接口，所以要ArrayList来实现

//    public PlaceAdapter(Fragment fragment, ArrayList<Place> placeList) {
//        this.fragment = fragment;
//        this.placeList = placeList;
//    }

    /*将Fragment的构造函数变成PlaceFragment，这样调用PlaceFragment对应的PlaceViewModel中的方法*/
    public PlaceAdapter(PlaceFragment fragment, List<Place> placeList) {
        this.fragment = fragment;
        this.placeList = placeList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView placeName;
        TextView placeAddress;

        public ViewHolder(@NonNull View view) {
            super(view);
            placeName = view.findViewById(R.id.placeName);
            placeAddress = view.findViewById(R.id.placeAddress);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getBindingAdapterPosition();
                //因为布局中可能存在多个数据源，可分为 3类 A B (AB)，
                // 当我们点击过去item的位置时候，就会出现一个位置有多个position，
                // 比如说 B 的三位置 可能是（AB）的6位置，所以官方选择放弃这个getAdapterPosition()方法，
                // 改为用getBindingAdapterPosition()和getAbsoluteAdapterPosition()这两个方法，当
                // 你在使用listView或者recycleView的监听事件时，其中getBindingAdapterPosition()表示A或者B的数据源的单独位置，
                // getAbsoluteAdapterPosition()负责是（AB）中的位置。
                Place place = placeList.get(position);
                Intent intent = new Intent(parent.getContext(), WeatherActivity.class);
                intent.putExtra("location_lng", place.getLocation().getLng());
                intent.putExtra("location_lat", place.getLocation().getLat());
                intent.putExtra("place_name", place.getName());
                /*在跳转前线保存这个城市*/
                fragment.getViewModel().savePlace(place);
                fragment.startActivity(intent);
                /*结束当前fragment*/
//                if (fragment.getActivity() != null)
//                    fragment.getActivity().finish();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.placeName.setText(place.getName());
        holder.placeAddress.setText(place.getAddress());
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

}
