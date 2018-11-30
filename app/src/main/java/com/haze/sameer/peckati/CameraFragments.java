package com.haze.sameer.peckati;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraFragments extends Fragment implements SurfaceHolder.Callback{

    Camera camera;
    Camera.PictureCallback jpegCallback;

    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    ImageView profilePic,searchIcon,chooseGallery;
    FirebaseAuth auth;
    int currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

    final int CAMERA_REQUEST_CODE = 1;
    public static CameraFragments newInstance(){
        CameraFragments fragment = new CameraFragments();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cameras, container, false);

        mSurfaceView = view.findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }else{
            mSurfaceHolder.addCallback(this);
        }

        auth = FirebaseAuth.getInstance();
        profilePic = (ImageView)view.findViewById(R.id.homeone_profilepic);
        searchIcon = (ImageView)view.findViewById(R.id.homeone_searchFriends);
        chooseGallery = (ImageView)view.findViewById(R.id.homeone_chooseGallery);
        chooseGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),AddPostActivity.class);
                i.putExtra("fromWhere","camera");
                getActivity().startActivity(i);
            }
        });
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),FindUsersActivity.class));
            }
        });
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),ProfilePageActivity.class);
                i.putExtra("whoseProfile","myProfile");
                i.putExtra("user_uid",FirebaseAuth.getInstance().getCurrentUser().getUid()+"");
                getActivity().startActivity(i);
            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference img = ref
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("profile_pic");
        img.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profile_pic = dataSnapshot.getValue(String.class) + "";
                Glide.with(getActivity().getApplicationContext()).load(profile_pic).into(profilePic);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ImageView mToggle = view.findViewById(R.id.homeone_rotatecamera);
        ImageView mCapture = view.findViewById(R.id.homeone_captureimage);
        mToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    camera.stopPreview();
                    camera.release();
                    if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        ((GlobalContext) getActivity().getApplicationContext()).cameraFacingBack = false;
                    } else {
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                        ((GlobalContext) getActivity().getApplicationContext()).cameraFacingBack = true;
                    }

                    camera = Camera.open(currentCameraId);
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
                    try {
                        camera.setPreviewDisplay(mSurfaceView.getHolder());
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), e + "", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), e + "", Toast.LENGTH_SHORT).show();
                    }
                    try {
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.setJpegQuality(100);
//                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        parameters.set("orientation", "portrait");
//                        parameters.setRotation(90);
                        camera.setDisplayOrientation(90);
                        camera.setParameters(parameters);
                    }catch (Exception e){Toast.makeText(getActivity(), e+"", Toast.LENGTH_SHORT).show();}

                    camera.startPreview();

                }catch (Exception e){Toast.makeText(getActivity(), e+"", Toast.LENGTH_SHORT).show();}
            }
        });
        mCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });

        jpegCallback = new Camera.PictureCallback(){
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {

                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                String fileLocation = SaveImageToStorage(decodedBitmap);
                if(fileLocation!= null){
                    Intent intent = new Intent(getActivity(), ShowCaptureActivity.class);
                    intent.putExtra("direct","camera");
                    if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                        intent.putExtra("which_direct","facing_back");
                    } else {
                        intent.putExtra("which_direct","facing_front");
                    }
                    startActivity(intent);
                    return;
                }
            }
        };

        return view;
    }

    public String SaveImageToStorage(Bitmap bitmap){
        String fileName = "imageToSend";
        try{
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), e+"", Toast.LENGTH_SHORT).show();
            fileName = null;
        }
        return fileName;
    }

    private void captureImage() {
        camera.takePicture(null, null, jpegCallback);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        camera = Camera.open(currentCameraId);
        ((GlobalContext) getActivity().getApplicationContext()).cameraFacingBack = false;

        try {
            Camera.Parameters parameters;
            parameters = camera.getParameters();
            parameters.set("orientation", "portrait");
//            parameters.setRotation(90);
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            parameters.setJpegQuality(100);
            camera.setParameters(parameters);
        }catch (Exception e){
            Toast.makeText(getActivity(), e+"", Toast.LENGTH_SHORT).show();
        }
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            Toast.makeText(getActivity(), e+"", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Toast.makeText(getActivity(), e+"", Toast.LENGTH_SHORT).show();
        }

        camera.setDisplayOrientation(90);
        camera.startPreview();

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mSurfaceHolder.addCallback(this);
                    mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                } else {
                    Toast.makeText(getContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

}
