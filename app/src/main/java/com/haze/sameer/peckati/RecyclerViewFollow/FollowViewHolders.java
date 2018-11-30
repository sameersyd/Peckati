package com.haze.sameer.peckati.RecyclerViewFollow;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.haze.sameer.peckati.R;

public class FollowViewHolders extends RecyclerView.ViewHolder{
    public TextView mEmail,mUid;
    public Button mFollow;
    public ImageView userPic;

    public FollowViewHolders(View itemView){
        super(itemView);
        mEmail = itemView.findViewById(R.id.email);
        mFollow = itemView.findViewById(R.id.follow);
        mUid = itemView.findViewById(R.id.followers_uid);
        userPic = itemView.findViewById(R.id.followers_userProfilePic);

    }

}
