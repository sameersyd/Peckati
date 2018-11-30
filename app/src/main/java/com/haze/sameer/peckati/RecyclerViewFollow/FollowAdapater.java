package com.haze.sameer.peckati.RecyclerViewFollow;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.haze.sameer.peckati.ProfilePageActivity;
import com.haze.sameer.peckati.R;
import com.haze.sameer.peckati.UserInformation;

import java.util.List;

public class FollowAdapater extends RecyclerView.Adapter<FollowViewHolders>{

    private List<FollowObject> usersList;
    private Context context;

    public FollowAdapater(List<FollowObject> usersList, Context context){
        this.usersList = usersList;
        this.context = context;
    }
    @Override
    public FollowViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recylerview_followers_item, null);
        FollowViewHolders rcv = new FollowViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final FollowViewHolders holder, int position) {

        Log.v("pos:",position+"");
        Log.v("holder:",holder+"");

        holder.mEmail.setText(usersList.get(position).getEmail());
        holder.mUid.setText(usersList.get(position).getUid());
        Glide.with(holder.userPic.getContext()).load(usersList.get(position).getProfile_pic()).into(holder.userPic);
        if(UserInformation.listFollowing.contains(usersList.get(holder.getLayoutPosition()).getUid())){
            holder.mFollow.setText("following");
        }else{
            holder.mFollow.setText("follow");
        }

        holder.mEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), ProfilePageActivity.class);
                i.putExtra("whoseProfile","othersProfile");
                i.putExtra("user_uid",holder.mUid.getText().toString()+"");
                view.getContext().startActivity(i);
            }
        });

        holder.mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if(!UserInformation.listFollowing.contains(usersList.get(holder.getLayoutPosition()).getUid())){
                    holder.mFollow.setText("following");
                    FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("following").child(usersList.get(holder.getLayoutPosition()).getUid()).setValue(true);
                }else{
                    holder.mFollow.setText("follow");
                    FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("following").child(usersList.get(holder.getLayoutPosition()).getUid()).removeValue();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.usersList.size();
    }
}
