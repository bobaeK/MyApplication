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

import com.example.admin.myapplication.R;

public class LockManageAdapter extends PagerAdapter{
    private int[] images;
    private LayoutInflater inflater;
    private Context context;

    public LockManageAdapter(Context context, int id){
        this.context = context;
        //test
        id = 0;

        getImages(id);
    }


    public void getImages(int id){

        int count = 0;
        //test
        count = 2;
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

        textView.setText(String.valueOf(position + 1));
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        container.invalidate();

    }
}
