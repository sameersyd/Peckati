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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.haze.sameer.peckati.RecyclerViewStory.StoryAdapter;
import com.haze.sameer.peckati.RecyclerViewStory.StoryObject;

import java.util.ArrayList;

public class StoryFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    SwipeRefreshLayout mSwipeRefreshLayout;
    ImageView addPost;
    RelativeLayout noStoryRela;

    public static StoryFragment newInstance(){
        StoryFragment fragment = new StoryFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story , container, false);

        noStoryRela = (RelativeLayout)view.findViewById(R.id.storyFrag_noStorysRela);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getContext(),2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new StoryAdapter(getDataSet(), getContext());
        mRecyclerView.setAdapter(mAdapter);

        final ViewPager viewPagerTemp = (ViewPager) MainActivity.mInstance.findViewById(R.id.viewPager);
        noStoryRela.setVisibility(View.VISIBLE);
        noStoryRela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPagerTemp.setCurrentItem(1,true);
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.fragmentstory_swipeRefreshLayout);
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
                },1500);
            }
        });

        addPost = (ImageView)view.findViewById(R.id.stories_addPost);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Food Stories", Toast.LENGTH_SHORT).show();
                viewPagerTemp.setCurrentItem(1,true);
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

    private void listenForData() {
        for(int i = 0; i < UserInformation.listFollowing.size(); i++){
            DatabaseReference followingStoryDb = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(UserInformation.listFollowing.get(i));
            followingStoryDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String username = dataSnapshot.child("username").getValue().toString();
                    String profilePic = dataSnapshot.child("profile_pic").getValue().toString();
                    String uid = dataSnapshot.getRef().getKey();
                    long timestampBeg = 0;
                    long timestampEnd = 0;
                    for(DataSnapshot storySnapshot : dataSnapshot.child("story").getChildren()){
                        if(storySnapshot.child("timestampBeg").getValue() != null){
                            timestampBeg = Long.parseLong(storySnapshot.child("timestampBeg").getValue().toString());
                        }
                        if(storySnapshot.child("timestampEnd").getValue() != null){
                            timestampEnd = Long.parseLong(storySnapshot.child("timestampEnd").getValue().toString());
                        }
                        long timestampCurrent = System.currentTimeMillis();
                        if(timestampCurrent >= timestampBeg && timestampCurrent <= timestampEnd){
                            noStoryRela.setVisibility(View.GONE);
                            StoryObject obj = new StoryObject(username, uid, "story",profilePic);
                            if(!results.contains(obj)){
                                results.add(obj);
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
