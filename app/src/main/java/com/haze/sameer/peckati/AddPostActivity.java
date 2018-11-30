package com.haze.sameer.peckati;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private ImageView closeImg,chooseImg,foodImg,capturedImg,addStoryImg,mainImg;
    LinearLayout sendLin;
    public static final int REQUEST_CODE = 1234;
    private Uri imgUri;
    Bitmap bitmap;
    String fromWhere = "nope";
    boolean forward = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        closeImg = (ImageView)findViewById(R.id.addPost_closeImg);
        foodImg = (ImageView)findViewById(R.id.addPost_foodImg);
        capturedImg = (ImageView)findViewById(R.id.addPost_capturedImg);
        chooseImg = (ImageView)findViewById(R.id.addPost_chooseImg);
        addStoryImg = (ImageView)findViewById(R.id.addPost_addStoryImg);
        sendLin = (LinearLayout)findViewById(R.id.addPost_sendLinear);
        mainImg = (ImageView)findViewById(R.id.addPost_mainImg);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            fromWhere = extras.getString("fromWhere");
        }

        if (fromWhere.equals("camera")){
            forward=false;
            mainImg.setBackgroundResource(R.drawable.def_black);
            btnBrowse_Click();
        }else{
            try {
                bitmap = BitmapFactory.decodeStream(getApplication().openFileInput("imageToSend"));
                mainImg.setImageBitmap(bitmap);
                forward=true;
            }catch (FileNotFoundException e) {
                e.printStackTrace();
                finish();
                forward=false;
                return;
            }
        }

        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddPostActivity.this,MainActivity.class));
                finish();
            }
        });
        chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnBrowse_Click();
            }
        });
        capturedImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromWhere.equals("camera")){
                    Toast.makeText(AddPostActivity.this, "No Captured Image!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    bitmap = BitmapFactory.decodeStream(getApplication().openFileInput("imageToSend"));
                    mainImg.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        foodImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromWhere.equals("camera")){
                    Toast.makeText(AddPostActivity.this, "No Food Image!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    bitmap = BitmapFactory.decodeStream(getApplication().openFileInput("foodImageToSend"));
                    mainImg.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        addStoryImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!forward){
                    Toast.makeText(AddPostActivity.this, "Select an image!", Toast.LENGTH_SHORT).show();
                    btnBrowse_Click();
                    return;
                }
                addStoryMethod();
            }
        });
        sendLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!forward){
                    Toast.makeText(AddPostActivity.this, "Select an image!", Toast.LENGTH_SHORT).show();
                    btnBrowse_Click();
                    return;
                }
                String filename = SaveImageToStorage();
                if (filename!=null)
                    startActivity(new Intent(AddPostActivity.this,ChooseReceiverActivity.class));
            }
        });

    }

    private void addStoryMethod() {

        final Dialog loadDialog = new Dialog(AddPostActivity.this);
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

        final DatabaseReference userStoryDb = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("story");
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

                Map<String, Object> mapToUpload = new HashMap<>();
                mapToUpload.put("imageUrl", imageUrl.toString());
                mapToUpload.put("timestampBeg", currentTimestamp);
                mapToUpload.put("timestampEnd", endTimestamp);
                mapToUpload.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                userStoryDb.child(key).setValue(mapToUpload);

                loadDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "Story Updated", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AddPostActivity.this, e+"", Toast.LENGTH_SHORT).show();
//                finish();
                return;
            }
        });
    }

    public String SaveImageToStorage(){
        Bitmap bitmap = ((BitmapDrawable)mainImg.getDrawable()).getBitmap();
        String fileName = "imageToSend";
        try{
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = AddPostActivity.this.openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        }catch(Exception e){
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    public void btnBrowse_Click(){
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            imgUri = data.getData();
            try {
                Bitmap bmp = ImagePicker.getImageFromResult(this, resultCode, data);//your compressed bitmap here
                bitmap = bmp;
                mainImg.setImageBitmap(bmp);
                forward=true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}