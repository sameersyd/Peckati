package com.haze.sameer.peckati;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.haze.sameer.peckati.RecyclerViewStory.StoryObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfilePageActivity extends AppCompatActivity {

    ImageView logoutMenu,profilePic,avaStoImg;
    FirebaseAuth auth;
    GridView gv;
    DatabaseReference db;
    HomeFirebaseHelper helper;
    HomeCustomAdapter adapter;
    Dialog loadDialog;
    boolean onceFetch = false,avaSto = false;

    RelativeLayout topView;
    Button editProfileBtn,followUnBtn,verifyEmailBtn;
    TextView emailTxt,nameTxt,statusTxt,postsTxt,followingTxt;
    String postsCountTxt,followingCountTxt,userUid,whoseProfile,profile_pic,username,email,email_verified;

    ArrayList<HomeSpacecraft> spacecrafts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            userUid = extras.getString("user_uid");
            whoseProfile = extras.getString("whoseProfile");
        }

        auth = FirebaseAuth.getInstance();
        logoutMenu = (ImageView)findViewById(R.id.profilePage_menu);
        profilePic = (ImageView)findViewById(R.id.profilePage_profileImage);
        emailTxt = (TextView) findViewById(R.id.profilePage_emailTxt);
        nameTxt = (TextView) findViewById(R.id.profilePage_nameTxt);
        statusTxt = (TextView) findViewById(R.id.profilePage_statusTxt);
        postsTxt = (TextView) findViewById(R.id.profilePage_postsTxt);
        followingTxt = (TextView) findViewById(R.id.profilePage_followingTxt);
        editProfileBtn = (Button)findViewById(R.id.profilePage_editProfileBtn);
        followUnBtn = (Button)findViewById(R.id.profilePage_followUnBtn);
        topView = (RelativeLayout)findViewById(R.id.profilePage_topView);
        avaStoImg = (ImageView)findViewById(R.id.avaStoImg);
        verifyEmailBtn = (Button)findViewById(R.id.profilePage_verifyEmailBtn);

        avaStoImg.setVisibility(View.GONE);

        if (whoseProfile.equals("myProfile")){
            logoutMenu.setVisibility(View.VISIBLE);
            editProfileBtn.setVisibility(View.VISIBLE);
            followUnBtn.setVisibility(View.GONE);

            DatabaseReference emailVerifyDB = FirebaseDatabase.getInstance().getReference();
            final DatabaseReference emailV = emailVerifyDB
                    .child("users")
                    .child(userUid)
                    .child("email_verified");
            emailV.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    email_verified = dataSnapshot.getValue(String.class) + "";
                    if (email_verified.equals("true")){
                        verifyEmailBtn.setVisibility(View.GONE);
                    }else if(email_verified.equals("false")){
                        verifyEmailBtn.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });

        }else if (whoseProfile.equals("othersProfile")){
            logoutMenu.setVisibility(View.GONE);
            editProfileBtn.setVisibility(View.GONE);
            followUnBtn.setVisibility(View.VISIBLE);
        }

        if(UserInformation.listFollowing.contains(userUid)){
            followUnBtn.setText("following");
        }else{
            followUnBtn.setText("follow");
        }

        verifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmailVerification();
            }
        });
        verifyEmailBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                verifyEmailBtn.setVisibility(View.GONE);
                return true;
            }
        });

        DatabaseReference followingStoryDb = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userUid);
        followingStoryDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
                        avaSto = true;
                        avaStoImg.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (avaSto){
                    Intent i = new Intent(ProfilePageActivity.this,DisplayImageActivity.class);
                    Bundle b = new Bundle();
                    b.putString("userId", userUid);
                    b.putString("chatOrStory","story");
                    i.putExtras(b);
                    startActivity(i);
                }else {
                    Toast.makeText(ProfilePageActivity.this, "No Stories Found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        followingTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ProfilePageActivity.this,FollowingActivity.class);
                i.putExtra("userUid",userUid);
                startActivity(i);
            }
        });

        followUnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!UserInformation.listFollowing.contains(userUid)){
                    followUnBtn.setText("following");
                    FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("following").child(userUid).setValue(true);
                }else{
                    followUnBtn.setText("follow");
                    FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("following").child(userUid).removeValue();
                }
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfilePageActivity.this,EditProfileActivity.class));
            }
        });

        DatabaseReference refOne = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgOne = refOne
                .child("users")
                .child(userUid)
                .child("profile_pic");
        imgOne.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profile_pic = dataSnapshot.getValue(String.class) + "";
                Glide.with(getApplicationContext()).load(profile_pic).into(profilePic);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference refTwo = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgTwo = refTwo
                .child("users")
                .child(userUid)
                .child("username");
        imgTwo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.getValue(String.class) + "";
                nameTxt.setText(username);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference refThree = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgThree = refThree
                .child("users")
                .child(userUid)
                .child("email");
        imgThree.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                email = dataSnapshot.getValue(String.class) + "";
                emailTxt.setText(email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference refFour = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgFour = refFour
                .child("users")
                .child(userUid)
                .child("status");
        imgFour.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status = dataSnapshot.getValue(String.class) + "";
                statusTxt.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference dba = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userUid)
                .child("posts");
        try {
            dba.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    postsCountTxt = dataSnapshot.getChildrenCount()+"";
                    postsTxt.setText(postsCountTxt);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(ProfilePageActivity.this, e+"", Toast.LENGTH_SHORT).show();
        }

        DatabaseReference dbb = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userUid)
                .child("following");
        try {
            dbb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    followingCountTxt = dataSnapshot.getChildrenCount()+"";
                    followingTxt.setText(followingCountTxt);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(ProfilePageActivity.this, e+"", Toast.LENGTH_SHORT).show();
        }

        loadDialog = new Dialog(ProfilePageActivity.this);
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

        gv = (GridView) findViewById(R.id.profilePage_gridview);
        gv.setEmptyView((RelativeLayout)findViewById(R.id.profilePage_noPostsRela));
        //INITIALIZE FIREBASE DB
        db = FirebaseDatabase.getInstance().getReference().child("users")
                .child(userUid)
                .child("posts");
        helper = new HomeFirebaseHelper(db);
        //ADAPTER
        adapter = new HomeCustomAdapter(this);
        adapter.delegate = this;
        gv.setAdapter(adapter);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0){
                    retrieve();
                    loadDialog.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        logoutMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(ProfilePageActivity.this,logoutMenu);
                popupMenu.getMenuInflater().inflate(R.menu.logout,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.item1:
                                auth.signOut();
                                Intent intent = new Intent(ProfilePageActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                                return true;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

    }

    private void fetchData(DataSnapshot dataSnapshot) {

        HomeSpacecraft sp = new HomeSpacecraft();
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            if(!URLUtil.isValidUrl(ds.getValue().toString())){
                continue;
            }
            sp.setPostImageUrl(ds.getValue().toString());
            spacecrafts.add(sp);
        }
        adapter.spacecrafts.add(sp);
        ((GlobalContext)getApplicationContext()).globaUserList = spacecrafts;
        adapter.notifyDataSetChanged();
        gv.invalidate();
    }

    //READ THEN RETURN ARRAYLIST
    public void retrieve() {

        adapter.spacecrafts.clear();

        ChildEventListener c = db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (onceFetch){
                    return;
                }
                fetchData(dataSnapshot);
                loadDialog.cancel();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (onceFetch){
                    return;
                }
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
    }

    public void displayPicMethod(final String viewPicUrl){

        final Dialog displayPic = new Dialog(ProfilePageActivity.this);
        displayPic.requestWindowFeature(Window.FEATURE_NO_TITLE);
        displayPic.setContentView(R.layout.post_detailed);
        displayPic.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    backDialog.setVisibility(View.GONE);
                    topView.setAlpha(1);
                    displayPic.dismiss();
                }
                return true;
            }
        });
        topView.setAlpha((float) 0.5);

        final String[] post_key = new String[1];
        final String[] likesCountTxt = new String[1];

        final Query userQuery = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(userUid)
                .child("posts")
                .orderByChild("postImageUrl");

        userQuery.equalTo(viewPicUrl).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot foodSnapshot: dataSnapshot.getChildren()) {
                            // result
                            post_key[0] = foodSnapshot.getKey();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

        final ImageView likeBtn, deleteBtn, viewPic;
        final TextView likesTxt, nameTxt;
        likeBtn = (ImageView) displayPic.findViewById(R.id.postDetailed_likeBtnImg);
        deleteBtn = (ImageView) displayPic.findViewById(R.id.postDetailed_deleteBtnImg);
        viewPic = (ImageView) displayPic.findViewById(R.id.postDetailed_viewedPic);
        likesTxt = (TextView) displayPic.findViewById(R.id.postDetailed_numLikesTxt);
        nameTxt = (TextView) displayPic.findViewById(R.id.postDetailed_nameTxt);

        if (whoseProfile.equals("myProfile")) {
            deleteBtn.setVisibility(View.VISIBLE);
        } else if (whoseProfile.equals("othersProfile")) {
            deleteBtn.setVisibility(View.GONE);
        }

        DatabaseReference refChk = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgChk = refChk
                .child("users")
                .child(userUid)
                .child("posts");
        imgChk.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(post_key[0]).child("likes").hasChild(auth.getCurrentUser().getUid())) {
                    likeBtn.setImageResource(R.drawable.likeblue);
                }else {
                    likeBtn.setImageResource(R.drawable.likeempty);
                }

                DatabaseReference likeCount = FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child(userUid)
                        .child("posts")
                        .child(post_key[0])
                        .child("likes");
                try {
                    likeCount.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            likesCountTxt[0] = dataSnapshot.getChildrenCount()+"";
                            likesTxt.setText(likesCountTxt[0]);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }catch (Exception e){
                    Toast.makeText(ProfilePageActivity.this, e+"", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        Glide.with(getApplicationContext()).load(viewPicUrl).into(viewPic);
        nameTxt.setText(username);

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onceFetch = true;
                DatabaseReference refFour = FirebaseDatabase.getInstance().getReference();
                DatabaseReference imgFour = refFour
                        .child("users")
                        .child(userUid)
                        .child("posts");
                imgFour.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(post_key[0]).child("likes").hasChild(auth.getCurrentUser().getUid())) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("users")
                                    .child(userUid)
                                    .child("posts")
                                    .child(post_key[0])
                                    .child("likes")
                                    .child(auth.getCurrentUser().getUid())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ProfilePageActivity.this, "Post Unliked", Toast.LENGTH_SHORT).show();
                                        likeBtn.setImageResource(R.drawable.likeempty);

                                        DatabaseReference likeCount = FirebaseDatabase.getInstance().getReference()
                                                .child("users")
                                                .child(userUid)
                                                .child("posts")
                                                .child(post_key[0])
                                                .child("likes");
                                        try {
                                            likeCount.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    likesCountTxt[0] = dataSnapshot.getChildrenCount()+"";
                                                    likesTxt.setText(likesCountTxt[0]);
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }catch (Exception e){
                                            Toast.makeText(ProfilePageActivity.this, e+"", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }
                            });
                        } else if (!dataSnapshot.child(post_key[0]).child("likes").hasChild(auth.getCurrentUser().getUid())) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("users")
                                    .child(userUid)
                                    .child("posts")
                                    .child(post_key[0])
                                    .child("likes")
                                    .child(auth.getCurrentUser().getUid())
                                    .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ProfilePageActivity.this, "Post Liked", Toast.LENGTH_SHORT).show();
                                        likeBtn.setImageResource(R.drawable.likeblue);

                                        DatabaseReference likeCount = FirebaseDatabase.getInstance().getReference()
                                                .child("users")
                                                .child(userUid)
                                                .child("posts")
                                                .child(post_key[0])
                                                .child("likes");
                                        try {
                                            likeCount.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    likesCountTxt[0] = dataSnapshot.getChildrenCount()+"";
                                                    likesTxt.setText(likesCountTxt[0]);
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }catch (Exception e){
                                            Toast.makeText(ProfilePageActivity.this, e+"", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(ProfilePageActivity.this).setIcon(R.drawable.delete).setTitle("Delete Photo")
                        .setMessage("Are you sure you want to delete the pic?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final Dialog loadDialog = new Dialog(ProfilePageActivity.this);
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

                                try {
                                    FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
                                    StorageReference photoRef = mFirebaseStorage.getReferenceFromUrl(viewPicUrl);
                                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            loadDialog.dismiss();
                                            Toast.makeText(ProfilePageActivity.this, e+"", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                DatabaseReference refFour = FirebaseDatabase.getInstance().getReference();
                                                DatabaseReference imgFour = refFour
                                                        .child("users")
                                                        .child(userUid)
                                                        .child("posts");
                                                imgFour.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("users")
                                                                .child(userUid)
                                                                .child("posts")
                                                                .child(post_key[0])
                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    loadDialog.dismiss();
                                                                    Toast.makeText(ProfilePageActivity.this, "Pic Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                }
                                                            }
                                                        });

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                        loadDialog.dismiss();
                                                        Toast.makeText(ProfilePageActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }
                                        }
                                    });

                                }catch (Exception e){
                                    loadDialog.dismiss();
                                    Toast.makeText(ProfilePageActivity.this, e+"", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).setNegativeButton("No", null).show();
            }
        });

        Window window = displayPic.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        displayPic.show();

    }

    private void sendEmailVerification() {

        new AlertDialog.Builder(ProfilePageActivity.this).setIcon(android.R.drawable.ic_dialog_email).setTitle("Verify Email")
                .setMessage("Send verification link to "+email)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user !=null){
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){

                                    Toast.makeText(ProfilePageActivity.this,"Please check your email for verification link",Toast.LENGTH_SHORT).show();
                                    new AlertDialog.Builder(ProfilePageActivity.this).setIcon(android.R.drawable.ic_dialog_email).setTitle("Email Verified")
                                            .setMessage("Click verified after clicking link in the email.")
                                            .setPositiveButton("Verified", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    startActivity(new Intent(ProfilePageActivity.this,MainActivity.class));
//                                                    checkIfEMailVerified();
                                                }
                                            }).show();
                                }
                            }
                        });
                    }

                            }
                }).setNegativeButton("No", null).show();
    }

    private void checkIfEMailVerified() {

        final Dialog loadDialog = new Dialog(ProfilePageActivity.this);
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

        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        mAuth.getCurrentUser().reload();
        FirebaseUser user = mAuth.getInstance().getCurrentUser();
        if (user.isEmailVerified()){
            try {
                HashMap<String, Object> result = new HashMap<>();
                result.put("email_verified", "true");
                DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                db.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfilePageActivity.this, e+"", Toast.LENGTH_SHORT).show();
                        loadDialog.dismiss();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            verifyEmailBtn.setVisibility(View.GONE);
                            Toast.makeText(ProfilePageActivity.this, "Email Successfully Verified", Toast.LENGTH_SHORT).show();
                        }
                        loadDialog.dismiss();
                    }
                });
            }catch (Exception e){
                Toast.makeText(ProfilePageActivity.this, e+"", Toast.LENGTH_SHORT).show();
                loadDialog.dismiss();
            }
        }else if (!user.isEmailVerified()){
            try {
                HashMap<String, Object> result = new HashMap<>();
                result.put("email_verified", "false");
                DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                db.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfilePageActivity.this, e+"", Toast.LENGTH_SHORT).show();
                        loadDialog.dismiss();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loadDialog.dismiss();
                    }
                });
            }catch (Exception e){
                Toast.makeText(ProfilePageActivity.this, e+"", Toast.LENGTH_SHORT).show();
                loadDialog.dismiss();
            }
            Toast.makeText(ProfilePageActivity.this, "Email Not Verified", Toast.LENGTH_SHORT).show();
        }
    }

}
