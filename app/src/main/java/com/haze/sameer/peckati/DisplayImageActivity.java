package com.haze.sameer.peckati;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class DisplayImageActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    String userId, currentUid, chatOrStory;

    private boolean started = false;

    private StoriesProgressView storiesProgressView;
    private ImageView image;

    private int counter = 0;

    private final long[] durations = new long[]{
            500L, 1000L, 1500L, 4000L, 5000L, 1000,
    };

    private int imageIterator = 0;

    long pressTime = 0L;
    long limit = 1000L;
    boolean arrBoo = false;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    storiesProgressView.setVisibility(View.INVISIBLE);
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    storiesProgressView.setVisibility(View.VISIBLE);
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    ArrayList<String> imageUrlList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_display_image);

        currentUid = FirebaseAuth.getInstance().getUid();

        Bundle b = getIntent().getExtras();
        userId = b.getString("userId");
        chatOrStory = b.getString("chatOrStory");

        storiesProgressView = (StoriesProgressView) findViewById(R.id.stories);

        image = (ImageView) findViewById(R.id.image);

        switch(chatOrStory){
            case "chat":
                listenForChat();
                break;
            case "story":
                listenForStory();
                break;
        }

        // bind reverse view
        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        // bind skip view
        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

    }

    private void listenForChat() {

        final DatabaseReference chatDb = FirebaseDatabase.getInstance().getReference().child("users").child(currentUid).child("received").child(userId);
        chatDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String imageUrl = "";
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {

                    if (chatSnapshot.child("imageUrl").getValue() != null) {
                        imageUrl = chatSnapshot.child("imageUrl").getValue().toString();
                    }
                    counter++;
                    imageUrlList.add(imageUrl);
                    if (!started) {
                        started = true;
                        Glide.with(getApplication()).load(imageUrlList.get(imageIterator)).into(image);
                    }
//                  chatDb.child(chatSnapshot.getKey()).removeValue();
                }
                arrBoo = true;
                storiesProgressView.setStoriesCount(counter);
                storiesProgressView.setStoryDuration(3000L);
                storiesProgressView.setStoriesListener(DisplayImageActivity.this);
                storiesProgressView.startStories();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void listenForStory() {
        DatabaseReference followingStoryDb = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        followingStoryDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String imageUrl = "";
                long timestampBeg = 0;
                long timestampEnd = 0;
                for(DataSnapshot storySnapshot : dataSnapshot.child("story").getChildren()){
                    if(storySnapshot.child("timestampBeg").getValue() != null){
                        timestampBeg = Long.parseLong(storySnapshot.child("timestampBeg").getValue().toString());
                    }
                    if(storySnapshot.child("timestampEnd").getValue() != null){
                        timestampEnd = Long.parseLong(storySnapshot.child("timestampEnd").getValue().toString());
                    }
                    if(storySnapshot.child("imageUrl").getValue() != null){
                        imageUrl = storySnapshot.child("imageUrl").getValue().toString();
                    }
                    long timestampCurrent = System.currentTimeMillis();
                    if(timestampCurrent >= timestampBeg && timestampCurrent <= timestampEnd){
                        counter++;
                        imageUrlList.add(imageUrl);
                        if (!started){
                            started = true;
                            Glide.with(getApplication()).load(imageUrlList.get(imageIterator)).into(image);
                        }
                    }
                }
                storiesProgressView.setStoriesCount(counter);
                storiesProgressView.setStoryDuration(3000L);
                storiesProgressView.setStoriesListener(DisplayImageActivity.this);
                storiesProgressView.startStories();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNext() {
        if(imageIterator == imageUrlList.size() - 1){
            if(chatOrStory.equals("story")){
                finish();
            }else if(chatOrStory.equals("chat")){
                startActivity(new Intent(DisplayImageActivity.this,MainActivity.class));
                finish();
            }
            return;
        }
        imageIterator++;
        storiesProgressView.pause();
        Glide.with(getApplication())
                .load(imageUrlList.get(imageIterator))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(DisplayImageActivity.this, "Load Failed", Toast.LENGTH_SHORT).show();
                        Toast.makeText(DisplayImageActivity.this, e+"", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        storiesProgressView.resume();
                        return false;
                    }
                })
                .into(image);
    }

    @Override
    public void onPrev() {
        if(imageIterator == 0){
            return;
        }
        imageIterator--;
        storiesProgressView.pause();
        Glide.with(getApplication())
                .load(imageUrlList.get(imageIterator))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(DisplayImageActivity.this, "Load Failed", Toast.LENGTH_SHORT).show();
                        Toast.makeText(DisplayImageActivity.this, e+"", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        storiesProgressView.resume();
                        return false;
                    }
                })
                .into(image);
    }

    @Override
    public void onComplete() {
        try {
            if (arrBoo) {
                final DatabaseReference chatDb = FirebaseDatabase.getInstance().getReference().child("users").child(currentUid).child("received").child(userId);
                chatDb.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                            chatDb.child(chatSnapshot.getKey()).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(DisplayImageActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }catch (Exception e){
            Toast.makeText(this, e+"", Toast.LENGTH_SHORT).show();
        }
        if(chatOrStory.equals("story")){
            finish();
        }else if(chatOrStory.equals("chat")){
            startActivity(new Intent(DisplayImageActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(chatOrStory.equals("story")){
            finish();
        }else if(chatOrStory.equals("chat")){
            startActivity(new Intent(DisplayImageActivity.this,MainActivity.class));
            finish();
        }
    }

}