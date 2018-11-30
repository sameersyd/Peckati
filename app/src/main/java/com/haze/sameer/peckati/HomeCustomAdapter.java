package com.haze.sameer.peckati;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HomeCustomAdapter extends BaseAdapter {
    Context c;
    ProfilePageActivity delegate;
    ArrayList<HomeSpacecraft> spacecrafts = new ArrayList<>();
    public HomeCustomAdapter(Context c) {
        this.c = c;
    }

    @Override
    public int getCount() {
        return spacecrafts.size();
    }
    @Override
    public Object getItem(int position) {
        return spacecrafts.get(spacecrafts.size() - position - 1);      // Existing Code Modified To Display Recent Top
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            convertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.home_display,parent,false);
        }
        ImageView imgProfile = (ImageView)convertView.findViewById(R.id.home_profile_pic);
        TextView profilePicTxt = (TextView)convertView.findViewById(R.id.home_profilePicTxt);
        final TextView profilePicKey = (TextView)convertView.findViewById(R.id.home_profilePicKey);
        final HomeSpacecraft s = spacecrafts.get(position);

        profilePicTxt.setText(s.getPostImageUrl());
        profilePicKey.setText(s.getPost_key());
        Glide.with(parent.getContext()).load(s.getPostImageUrl()).into(imgProfile);

        //ONITECLICK
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delegate.displayPicMethod(s.getPostImageUrl());
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delegate.displayPicMethod(s.getPostImageUrl());
            }
        });
        return convertView;
    }

}
