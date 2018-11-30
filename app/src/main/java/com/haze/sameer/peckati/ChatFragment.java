package com.haze.sameer.peckati;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.haze.sameer.peckati.RecyclerViewStory.StoryAdapter;
import com.haze.sameer.peckati.RecyclerViewStory.StoryObject;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    String uid;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ImageView addPost;
    RelativeLayout noStoryRela;

    public static ChatFragment newInstance(){
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat , container, false);

        uid = FirebaseAuth.getInstance().getUid();

        noStoryRela = (RelativeLayout) view.findViewById(R.id.chatFrag_noChatsRela);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new StoryAdapter(getDataSet(), getContext());
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragmentChat_swipeRefreshLayout);
        addPost = (ImageView) view.findViewById(R.id.chat_addPost);
        final ViewPager viewPagerTemp = (ViewPager) MainActivity.mInstance.findViewById(R.id.viewPager);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPagerTemp.setCurrentItem(1,true);
            }
        });
        noStoryRela.setVisibility(View.VISIBLE);
        noStoryRela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPagerTemp.setCurrentItem(1,true);
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                noStoryRela.setVisibility(View.VISIBLE);
                clear();
                listenForData();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1500);
            }
        });

        clear();
        listenForData();

        return view;
    }

    private void clear() {
        int size = this.results.size();
        this.results.clear();
        mAdapter.notifyItemRangeChanged(0, size);
    }

    private ArrayList<StoryObject> results = new ArrayList<>();
    private ArrayList<StoryObject> getDataSet() {
        listenForData();
        return results;
    }

    private void listenForData(){
        DatabaseReference receivedDb = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("received");
        receivedDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    try{
                        noStoryRela.setVisibility(View.GONE);
                    }catch (Exception e){
                        Toast.makeText(getActivity(), e+"", Toast.LENGTH_SHORT).show();
                    }
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        getUserInfo(snapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getUserInfo(String chatUid) {
        DatabaseReference chatUserDb = FirebaseDatabase.getInstance().getReference().child("users").child(chatUid);
        chatUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String email = dataSnapshot.child("email").getValue().toString();
                    String profilePic = dataSnapshot.child("profile_pic").getValue().toString();
                    String uid = dataSnapshot.getRef().getKey();

                    StoryObject obj = new StoryObject(email, uid, "chat",profilePic);
                    if(!results.contains(obj)){
                        results.add(obj);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
