package com.example.admin.myapplication.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.R;

public class LockManageAdapter extends PagerAdapter{
    private int[] images;
    private LayoutInflater inflater;
    private Context context;

    public LockManageAdapter(Context context, int count){
        this.context = context;
        //test

        getImages(count);
    }


    public void getImages(int count){


        //test
        images = new int[count + 1];
        for(int i = 0; i < count; ++i){
            images[i] = R.drawable.locked;

        }
        images[count] = R.drawable.unlocked;
    }
    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((LinearLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position){
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.lock_slider, container, false);
        ImageView imageView = (ImageView)v.findViewById(R.id.imageView);
        TextView textView = (TextView)v.findViewById(R.id.tv_num);
        imageView.setImageResource(images[position]);
        textView.setText(String.valueOf(position + 1));

        container.addView(v);
        return v;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        container.invalidate();

    }
}
