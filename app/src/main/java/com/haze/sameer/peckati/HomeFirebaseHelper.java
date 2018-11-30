package com.haze.sameer.peckati;

import android.content.Context;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class HomeFirebaseHelper {

    HomeCustomAdapter adapter;
    DatabaseReference db;
    Context c;
    ArrayList<HomeSpacecraft> spacecrafts = new ArrayList<>();

    //PASS DATABASE REFRENCE

    public HomeFirebaseHelper(DatabaseReference db) {
        this.db = db;
    }

    //IMPLEMENT FETCH DATA AND FILL ARRAYLIST
    private void fetchData(DataSnapshot dataSnapshot) {
        spacecrafts.clear();
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            HomeSpacecraft spacecraft = ds.getValue(HomeSpacecraft.class);
            spacecrafts.add(spacecraft);
        }
        adapter.spacecrafts = this.spacecrafts;
        adapter.notifyDataSetChanged();
    }

    //READ THEN RETURN ARRAYLIST
    public ArrayList<HomeSpacecraft> retrieve() {
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
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
        return spacecrafts;
    }

}