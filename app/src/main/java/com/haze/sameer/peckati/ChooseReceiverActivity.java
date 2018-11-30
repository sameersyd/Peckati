package com.haze.sameer.peckati;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.haze.sameer.peckati.RecyclerViewReceiver.ReceiverAdapater;
import com.haze.sameer.peckati.RecyclerViewReceiver.ReceiverObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChooseReceiverActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    FirebaseAuth auth;
    String Uid;
    Bitmap bitmap;
    CheckBox postChckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_receiver);

        try {
            bitmap = BitmapFactory.decodeStream(getApplication().openFileInput("imageToSend"));
        }catch (FileNotFoundException e) {
            e.printStackTrace();
            finish();
            return;
        }

        auth = FirebaseAuth.getInstance();
        Uid = FirebaseAuth.getInstance().getUid();

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getApplication());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ReceiverAdapater(getDataSet(),getApplication());
        mRecyclerView.setAdapter(mAdapter);

        postChckBox = (CheckBox)findViewById(R.id.receiver_post_Chckbox);
        FloatingActionButton mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog loadDialog = new Dialog(ChooseReceiverActivity.this);
                loadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                loadDialog.setContentView(R.layout.loading_one);
                loadDialog.setOnKeyListener(new Dialog.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            loadDialog.dismiss();
                        }
                        return true;
                    }
                });
                LottieAnimationView animSelect;
                animSelect = (LottieAnimationView)loadDialog.findViewById(R.id.loading_one);
                animSelect.setAnimation("blueline.json");
                animSelect.playAnimation();
                animSelect.loop(true);

                Window window = loadDialog.getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                loadDialog.show();

                if (postChckBox.isChecked())
                    postToProfile(loadDialog);
                saveToStories(loadDialog);
            }
        });
    }


    private ArrayList<ReceiverObject> results = new ArrayList<>();
    private ArrayList<ReceiverObject> getDataSet() {
        listenForData();
        return results;
    }

    private void listenForData() {
        for(int i = 0; i < UserInformation.listFollowing.size();i++){
            DatabaseReference usersDb = FirebaseDatabase.getInstance().getReference().child("users").child(UserInformation.listFollowing.get(i));
            usersDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String email = "";
                    String uid = dataSnapshot.getRef().getKey();
                    if(dataSnapshot.child("email").getValue() != null){
                        email = dataSnapshot.child("email").getValue().toString();
                    }
                    ReceiverObject obj = new ReceiverObject(email, uid, false);
                    if(!results.contains(obj)){
                        results.add(obj);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ChooseReceiverActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    Toast.makeText(ChooseReceiverActivity.this, "OnCancelled Process", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void postToProfile(final Dialog loadDialog){

        final DatabaseReference userStoryDb = FirebaseDatabase.getInstance().getReference().child("users").child(Uid).child("posts");
        final String key = userStoryDb.push().getKey();

        StorageReference filePath = FirebaseStorage.getInstance().getReference().child("captures").child(key);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] dataToUpload = baos.toByteArray();
        UploadTask uploadTask = filePath.putBytes(dataToUpload);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri imageUrl = taskSnapshot.getDownloadUrl();

                Map<String, Object> mapToUpload = new HashMap<>();
                mapToUpload.put("postImageUrl", imageUrl.toString());
                mapToUpload.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                mapToUpload.put("post_key", key);
                userStoryDb.child(key).setValue(mapToUpload);

            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadDialog.dismiss();
                Toast.makeText(ChooseReceiverActivity.this, e+"", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveToStories(final Dialog loadDialog) {

        final DatabaseReference userStoryDb = FirebaseDatabase.getInstance().getReference().child("users").child(Uid).child("story");
        final String key = userStoryDb.push().getKey();

        StorageReference filePath = FirebaseStorage.getInstance().getReference().child("captures").child(key);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] dataToUpload = baos.toByteArray();
        UploadTask uploadTask = filePath.putBytes(dataToUpload);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri imageUrl = taskSnapshot.getDownloadUrl();

                Long currentTimestamp = System.currentTimeMillis();
                Long endTimestamp = currentTimestamp + (24*60*60*1000);

                CheckBox mStory = findViewById(R.id.story);
                if(mStory.isChecked()){
                    Map<String, Object> mapToUpload = new HashMap<>();
                    mapToUpload.put("imageUrl", imageUrl.toString());
                    mapToUpload.put("timestampBeg", currentTimestamp);
                    mapToUpload.put("timestampEnd", endTimestamp);
                    mapToUpload.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    userStoryDb.child(key).setValue(mapToUpload);
                }
                for(int i = 0; i< results.size(); i++){
                    if(results.get(i).getReceive()){
                        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("users").child(results.get(i).getUid()).child("received").child(Uid);
                        Map<String, Object> mapToUpload = new HashMap<>();
                        mapToUpload.put("imageUrl", imageUrl.toString());
                        mapToUpload.put("timestampBeg", currentTimestamp);
                        mapToUpload.put("timestampEnd", endTimestamp);
                        mapToUpload.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        userDb.child(key).setValue(mapToUpload);
                    }
                }

                loadDialog.dismiss();
                Toast.makeText(ChooseReceiverActivity.this, "Picture Sent", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return;
            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadDialog.dismiss();
                Toast.makeText(ChooseReceiverActivity.this, e+"", Toast.LENGTH_SHORT).show();
//                finish();
                return;
            }
        });
    }
}
