package com.haze.sameer.peckati;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    EditText editName,editStatus,editNum;
    Button save;
    TextView emailTxt;
    private String name,status,profile_pic,email,phn;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    ImageView editProfileImg;
    private Uri imgUri;
    Boolean isPhotoAdded = false;
    Dialog loadDialog;

    public static final String FB_STORAGE_PATH = "image/";
    public static final int REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sharedPreferences = getSharedPreferences("Details", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editName = (EditText) findViewById(R.id.edit_name);
        editStatus = (EditText) findViewById(R.id.edit_status);
        editNum = (EditText) findViewById(R.id.edit_phnNumber);
        emailTxt = (TextView)findViewById(R.id.edit_email);

        save = (Button)findViewById(R.id.saveEdtpro);
        editProfileImg = (ImageView)findViewById(R.id.edit_profileImg);

        DatabaseReference refOne = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgOne = refOne
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("profile_pic");
        imgOne.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profile_pic = dataSnapshot.getValue(String.class) + "";
                Glide.with(getApplicationContext()).load(profile_pic).into(editProfileImg);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference refTwo = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgTwo = refTwo
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("username");
        imgTwo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.getValue(String.class) + "";
                editName.setText(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference refThree = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgThree = refThree
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("email");
        imgThree.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                email = dataSnapshot.getValue(String.class) + "";
                emailTxt.setText(email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference refFour = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgFour = refFour
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("status");
        imgFour.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                status = dataSnapshot.getValue(String.class) + "";
                editStatus.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference refFive = FirebaseDatabase.getInstance().getReference();
        DatabaseReference imgFive = refFive
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("phn");
        imgFive.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                phn = dataSnapshot.getValue(String.class) + "";
                editNum.setText(phn);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

        editProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnBrowse_Click();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(editName.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Enter Name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(editStatus.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Enter Status!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(editNum.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Enter Phone Number!", Toast.LENGTH_SHORT).show();
                    return;
                }

                final Dialog loadDialog = new Dialog(EditProfileActivity.this);
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

                if(isPhotoAdded){
                    final StorageReference mStorageRef;
                    mStorageRef = FirebaseStorage.getInstance().getReference();
                    final DatabaseReference mDataBaseRef = FirebaseDatabase.getInstance().getReference();
                    StorageReference ref = mStorageRef.child(FB_STORAGE_PATH + System.currentTimeMillis() + "."+getImageExt(imgUri));
                    ref.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            updateData(taskSnapshot.getDownloadUrl().toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, e+"", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    updateData("");
                }
            }

        });
    }//************************ONCREATE*************************

    public void updateData(String profileImageUrl){
        try {
            HashMap<String, Object> result = new HashMap<>();
            result.put("username", editName.getText().toString());
            result.put("status", editStatus.getText().toString());
            result.put("phn", editNum.getText().toString());
            if(profileImageUrl != ""){
                result.put("profile_pic",profileImageUrl);
            }

            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
            db.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
                    StorageReference photoRef = mFirebaseStorage.getReferenceFromUrl(profile_pic);
                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, e+"", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e+"", Toast.LENGTH_SHORT).show();
                }
            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        loadDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditProfileActivity.this,MainActivity.class));
                        finish();
                    }else {
                        loadDialog.dismiss();
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.idLayout),"Failed to Update Profile!",Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }
            });
        }catch (Exception e){
            Toast.makeText(EditProfileActivity.this, e+"", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnBrowse_Click(){
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            imgUri = data.getData();
            try {
                Bitmap bmp = ImagePicker.getImageFromResult(this, resultCode, data);//your compressed bitmap here
                editProfileImg.setImageBitmap(bmp);
                isPhotoAdded = true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public String getImageExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}
