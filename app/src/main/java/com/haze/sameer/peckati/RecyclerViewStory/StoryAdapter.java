package com.haze.sameer.peckati.RecyclerViewStory;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.haze.sameer.peckati.R;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryViewHolders>{

    private List<StoryObject> usersList;
    private Context context;

    public StoryAdapter(List<StoryObject> usersList, Context context){
        this.usersList = usersList;
        this.context = context;
    }

    @Override
    public StoryViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_story_item, null);
        StoryViewHolders rcv = new StoryViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final StoryViewHolders holder, int position) {
        holder.mName.setText(usersList.get(position).getUsername());
        holder.mProfilePicTxt.setText(usersList.get(position).getProfilePic());
        holder.mUid.setText(usersList.get(position).getUid());
        holder.mName.setTag(usersList.get(position).getUid());
        Glide.with(context).load(usersList.get(position).getProfilePic()).into(holder.profilePic);
        holder.mLayout.setTag(usersList.get(position).getCharOrStory());

    }

    @Override
    public int getItemCount() {
        return this.usersList.size();
    }
}