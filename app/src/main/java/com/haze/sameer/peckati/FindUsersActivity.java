package com.haze.sameer.peckati;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.haze.sameer.peckati.RecyclerViewFollow.FollowAdapater;
import com.haze.sameer.peckati.RecyclerViewFollow.FollowObject;

import java.util.ArrayList;

public class FindUsersActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    EditText mInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);

        mInput = findViewById(R.id.input);
        Button mSearch = findViewById(R.id.search);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getApplication());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FollowAdapater(getDataSet(),getApplication());
        mRecyclerView.setAdapter(mAdapter);

        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
                listenForData();
            }
        });

    }

    private void listenForData() {
        try {
            DatabaseReference usersDb = FirebaseDatabase.getInstance().getReference().child("users");
            Query query = usersDb.orderByChild("email").startAt(mInput.getText().toString()).endAt(mInput.getText().toString() + "\uf8ff");
            query.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String email = "";
                    String uid = dataSnapshot.getRef().getKey();
                    String profile_pic = "";
                    if (dataSnapshot.child("email").getValue() != null) {
                        email = dataSnapshot.child("email").getValue().toString();
                    }
                    if (dataSnapshot.child("profile_pic").getValue() != null) {
                        profile_pic = dataSnapshot.child("profile_pic").getValue().toString();
                    }
                    if (!email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        FollowObject obj = new FollowObject(email, uid, profile_pic);
                        results.add(obj);
                        mAdapter.notifyDataSetChanged();
                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }catch (Exception r){
            Toast.makeText(this,r+":6598", Toast.LENGTH_SHORT).show();
        }
    }
    private void clear() {
        int size = this.results.size();
        this.results.clear();
//        mAdapter.notifyItemRangeChanged(0, size);
        mAdapter.notifyDataSetChanged();    //replaced above code with this one
    }

    private ArrayList<FollowObject> results = new ArrayList<>();
    private ArrayList<FollowObject> getDataSet() {
        listenForData();
        return results;
    }

}
