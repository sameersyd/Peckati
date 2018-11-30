package com.haze.sameer.peckati.RecyclerViewReceiver;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.haze.sameer.peckati.R;

public class ReceiverViewHolder extends RecyclerView.ViewHolder{
    public TextView mEmail;
    public CheckBox mReceive;

    public ReceiverViewHolder(View itemView){
        super(itemView);
        mEmail = itemView.findViewById(R.id.email);
        mReceive = itemView.findViewById(R.id.receive);
    }

}
