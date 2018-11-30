package com.haze.sameer.peckati.RecyclerViewStory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haze.sameer.peckati.DisplayImageActivity;
import com.haze.sameer.peckati.ProfilePageActivity;
import com.haze.sameer.peckati.R;

public class StoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView mName,mProfilePicTxt,mUid;
    public LinearLayout mLayout;
    public ImageView profilePic;

    public StoryViewHolders(View itemView){
        super(itemView);
        itemView.setOnClickListener(this);
        mName = itemView.findViewById(R.id.story_profileName);
        mProfilePicTxt = itemView.findViewById(R.id.story_profileNameTxt);
        mUid = itemView.findViewById(R.id.story_profileUidTxt);
        profilePic =  itemView.findViewById(R.id.story_profilePic);
        mLayout = itemView.findViewById(R.id.layout);
        mName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), ProfilePageActivity.class);
                i.putExtra("whoseProfile","othersProfile");
                i.putExtra("user_uid",mUid.getText().toString()+"");
                view.getContext().startActivity(i);
            }
        });

    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), DisplayImageActivity.class);
        Bundle b = new Bundle();
        b.putString("userId", mName.getTag().toString());
        b.putString("chatOrStory", mLayout.getTag().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);
    }
}