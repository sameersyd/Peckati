package com.haze.sameer.peckati;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
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

public class SignupActivity extends AppCompatActivity {

    private Button login,signup;
    EditText usernameEdit,phoneEdit,emailEdit,passwordEdit;
    FirebaseAuth auth;
    DatabaseReference db;
    ImageView profileImg;
    String email,password;

    public static final String FB_STORAGE_PATH = "image/";
    public static final int REQUEST_CODE = 1234;
    private Uri imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        auth = FirebaseAuth.getInstance();

        login = (Button)findViewById(R.id.signup_login);
        signup = (Button)findViewById(R.id.signup_signupBtn);
        usernameEdit = (EditText)findViewById(R.id.signup_nameSignup);
        phoneEdit = (EditText)findViewById(R.id.signup_phoneSignup);
        emailEdit = (EditText)findViewById(R.id.signup_emailSignup);
        passwordEdit = (EditText)findViewById(R.id.signup_passwordSignup);
        profileImg = (ImageView)findViewById(R.id.signup_profileImage);
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnBrowse_Click();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (usernameEdit.getText().toString().isEmpty()){
                    Toast.makeText(SignupActivity.this, "Enter Your Name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phoneEdit.getText().toString().isEmpty()){
                    Toast.makeText(SignupActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (emailEdit.getText().toString().isEmpty()){
                    Toast.makeText(SignupActivity.this, "Enter Email Address", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (passwordEdit.getText().toString().isEmpty()){
                    Toast.makeText(SignupActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (imgUri==null){
                    Toast.makeText(SignupActivity.this, "Select a profile pic", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnUpload_click();
            }
        });

    }

    public void btnUpload_click(){

        final Dialog loadDialog = new Dialog(SignupActivity.this);
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

        email = emailEdit.getText().toString();
        password = passwordEdit.getText().toString();

        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {

                        if (!task.isSuccessful()){

                            try{
                                throw task.getException();
                            }catch (FirebaseAuthWeakPasswordException weak){
                                Toast.makeText(SignupActivity.this, "Password too weak", Toast.LENGTH_SHORT).show();
                                return;
                            }catch (FirebaseAuthInvalidCredentialsException malw){
                                Toast.makeText(SignupActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                                return;
                            }catch (FirebaseAuthUserCollisionException col){
                                Toast.makeText(SignupActivity.this, "Email already registered!", Toast.LENGTH_SHORT).show();
                                Toast.makeText(SignupActivity.this, "Try Signing In", Toast.LENGTH_SHORT).show();
                                return;
                            }catch (Exception e){
                                Toast.makeText(SignupActivity.this, e+"", Toast.LENGTH_SHORT).show();
                                return;
                            }

                        }else {
                            StorageReference mStorageRef;
                            mStorageRef = FirebaseStorage.getInstance().getReference();
                            final DatabaseReference mDataBaseRef = FirebaseDatabase.getInstance().getReference();
                            StorageReference ref = mStorageRef.child(FB_STORAGE_PATH + System.currentTimeMillis() + "." + getImageExt(imgUri));
                            ref.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SignupActivity.this, "Authentication failed",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        //GET DATA
                                        String name = usernameEdit.getText().toString().trim();
                                        String email = emailEdit.getText().toString().trim();
                                        String phn = phoneEdit.getText().toString().trim();
                                        //SET DATA
                                        HomeSpacecraft s = new HomeSpacecraft();
                                        s.setPhn(phn);
                                        s.setEmail(email);
                                        s.setStatus("Hey there am using Peckati!");
                                        s.setEmail_verified("false");
                                        s.setProfile_pic(taskSnapshot.getDownloadUrl().toString());
                                        s.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid() + "");
                                        s.setUsername(name);
                                        Boolean saved;
                                        try {
                                            db = FirebaseDatabase.getInstance().getReference();
                                            db.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(s);
                                            saved = true;
                                        } catch (Exception e) {
                                            saved = false;
                                        }
                                        if (saved) {
                                            Toast.makeText(SignupActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                            finish();
                                        } else if (!saved) {
                                            Toast.makeText(SignupActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), e.getMessage() + "", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadDialog.dismiss();
                Toast.makeText(SignupActivity.this, e+"", Toast.LENGTH_SHORT).show();
            }
        });


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
                profileImg.setImageBitmap(bmp);
            } catch (Exception e){
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
