package com.haze.sameer.peckati;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.haze.sameer.peckati.RecyclerViewFollow.FollowAdapater;
import com.haze.sameer.peckati.RecyclerViewFollow.FollowObject;
import com.haze.sameer.peckati.RecyclerViewStory.StoryAdapter;
import com.haze.sameer.peckati.RecyclerViewStory.StoryObject;

import java.util.ArrayList;

public class FollowingActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    String userUid;

    public static ArrayList<String> newlistFollowing = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            userUid = extras.getString("userUid");
        }

        mRecyclerView = findViewById(R.id.following_recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getApplication());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FollowAdapater(getDataSet(),getApplication());
        mRecyclerView.setAdapter(mAdapter);

        clear();
        startFetching(userUid);

    }

    private void clear() {
        int size = this.results.size();
        this.results.clear();
        mAdapter.notifyItemRangeChanged(0, size);
    }

    private ArrayList<FollowObject> results = new ArrayList<>();
    private ArrayList<FollowObject> getDataSet() {
        startFetching(userUid);
        return results;
    }

    public void startFetching(String userUid){
        newlistFollowing.clear();
        getUserFollowing(userUid);
    }

    private void getUserFollowing(String userUid) {
        DatabaseReference userFollowingDB = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userUid)
                .child("following");
        userFollowingDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(int i = 0; i < newlistFollowing.size(); i++){
                    DatabaseReference followingStoryDb = FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(newlistFollowing.get(i));
                    followingStoryDb.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String email = dataSnapshot.child("email").getValue().toString();
                            String profilePic = dataSnapshot.child("profile_pic").getValue().toString();
                            String uid = dataSnapshot.getRef().getKey();

                            if (!uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                FollowObject obj = new FollowObject(email, uid,profilePic);
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        userFollowingDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()){
                    String uid = dataSnapshot.getRef().getKey();
                    if (uid != null && !newlistFollowing.contains(uid)){
                        newlistFollowing.add(uid);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String uid = dataSnapshot.getRef().getKey();
                    if (uid != null){
                        newlistFollowing.remove(uid);
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
