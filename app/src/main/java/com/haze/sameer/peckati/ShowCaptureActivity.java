package com.haze.sameer.peckati;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class ShowCaptureActivity extends AppCompatActivity {

    String Uid,bitmapUrl;
    Bitmap bitmap,rotateBitmap;
    String dishName = null,recipeName,recipeImage,recipeUrl,direct,which_direct;
    Boolean tempBoo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_capture);

        AndroidNetworking.initialize(getApplicationContext());
        try {
            bitmap = BitmapFactory.decodeStream(getApplication().openFileInput("imageToSend"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            finish();
            return;
        }

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            direct = extras.getString("direct");
            which_direct = extras.getString("which_direct");
        }

        ImageView mImage = (ImageView) findViewById(R.id.imageCaptured);
        if (direct.equals("camera")){
            if (which_direct.equals("facing_back")){
                rotateBitmap = rotate(bitmap,90);
                SaveImageToStorage(rotateBitmap,"imageToSend");
            }else if (which_direct.equals("facing_front")){
                rotateBitmap = rotate(bitmap,270);
                SaveImageToStorage(rotateBitmap,"imageToSend");
            }
        }else if (direct.equals("add_post")){
            rotateBitmap = rotate(bitmap,360);
            SaveImageToStorage(rotateBitmap,"imageToSend");
        }

        mImage.setImageBitmap(rotateBitmap);
        onImage(rotateBitmap);

        Uid = FirebaseAuth.getInstance().getUid();

        Button mSend = (Button) findViewById(R.id.send);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tempBoo){
                    storeFoodPic();
                }else if(!tempBoo) {
                    Toast.makeText(ShowCaptureActivity.this, "Wait for the suggestion", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private Bitmap rotate(Bitmap decodedBitmap, int deg) {
        int w = decodedBitmap.getWidth();
        int h = decodedBitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.setRotate(deg);

        return Bitmap.createBitmap(decodedBitmap, 0, 0, w, h, matrix, true);

    }

    public void storeFoodPic(){

        final Dialog loadDialog = new Dialog(ShowCaptureActivity.this);
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

        Bitmap bitmap;

        MyAsync obj = new MyAsync(bitmapUrl){

            @Override
            protected void onPostExecute(Bitmap bmp) {
                super.onPostExecute(bmp);

                Bitmap bm = bmp;
                LinearLayout layout = new LinearLayout(getApplicationContext());
                layout.setLayoutParams(new AbsListView.LayoutParams(250, 250));
                layout.setGravity(Gravity.CENTER);

                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setLayoutParams(new AbsListView.LayoutParams(220, 220));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageBitmap(bm);

                layout.addView(imageView);
            }
        };
        obj.execute();

        try{
            bitmap = obj.get();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();

            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            String fileLocation = SaveImageToStorage(decodedBitmap,"foodImageToSend");
            if (fileLocation != null)
                startActivity(new Intent(ShowCaptureActivity.this,AddPostActivity.class));
            loadDialog.dismiss();
        } catch (Exception e){
            Toast.makeText(this, e+"", Toast.LENGTH_SHORT).show();
            loadDialog.dismiss();
        }
    }

    public String SaveImageToStorage(Bitmap bitmap,String passedFilename){
        String fileName = passedFilename;
        try{
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = ShowCaptureActivity.this.openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        }catch(Exception e){
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    public void onImage(Bitmap bitmap) {

        final Dialog loadDialog = new Dialog(ShowCaptureActivity.this);
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

//        backDialog.setVisibility(View.VISIBLE);
        loadDialog.show();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,70,out);
        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

        String bs64 = convertToBase64(decoded);
        Log.v("CHECK bitmap:",bs64);

        AndroidNetworking.post("https://api-us.faceplusplus.com/facepp/v3/detect")
                .addBodyParameter("api_key","qA8c7F8B3ftAPLTfGXAPGl1oher-MEic")
                .addBodyParameter("api_secret","0Sx814fUV0muWBZEKU0eGmIFMQeXZfvs")
                .addBodyParameter("image_base64",bs64)
                .addBodyParameter("return_attributes","emotion").build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("CHECK response:",response);
                        try {
                            JSONObject rootObject = new JSONObject(response);
                            JSONArray faceArray = rootObject.getJSONArray("faces");
                            if (faceArray.length() > 1){
//                                backDialog.setVisibility(View.GONE);
                                loadDialog.dismiss();
                                new AlertDialog.Builder(ShowCaptureActivity.this).setIcon(R.drawable.face_smileee).setTitle("Peckati")
                                        .setMessage("Multiple faces detected!")
                                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                                return;
                                            }
                                        }).show();
                                return;
                            }
                            JSONObject faceObject = new JSONObject(faceArray.get(0).toString());
                            JSONObject attributes = faceObject.getJSONObject("attributes");
                            JSONObject emotion = attributes.getJSONObject("emotion");
                            final Map<String,Double> map = new HashMap<>();
                            map.put("sadness",emotion.getDouble("sadness"));
                            map.put("neutral",emotion.getDouble("neutral"));
                            map.put("disgust",emotion.getDouble("disgust"));
                            map.put("anger",emotion.getDouble("anger"));
                            map.put("surprise",emotion.getDouble("surprise"));
                            map.put("fear",emotion.getDouble("fear"));
                            map.put("happiness",emotion.getDouble("happiness"));
                            Map.Entry<String, Double> detectedFacialEmotion = null;
//                            for (Map.Entry<String, Double> entry : map.entrySet())
//                            {
//                                if (detectedFacialEmotion == null || entry.getValue().compareTo(detectedFacialEmotion.getValue()) > 0)
//                                {
//                                    detectedFacialEmotion = entry;
//                                }
//                            }
                            Log.v("happ%:",emotion.getDouble("happiness")+"");
                            displayDish(emotion.getDouble("happiness"),loadDialog);

                        } catch (JSONException e) {
//                            backDialog.setVisibility(View.GONE);
                            loadDialog.dismiss();
                            Toast.makeText(ShowCaptureActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                            new AlertDialog.Builder(ShowCaptureActivity.this).setIcon(R.drawable.face_smileee).setTitle("Peckati")
                                    .setMessage("No face detected. Try once again!")
                                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                            return;
                                        }
                                    }).show();
                        }catch (Exception e){
//                            backDialog.setVisibility(View.GONE);
                            loadDialog.dismiss();
                            Toast.makeText(ShowCaptureActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
//                        backDialog.setVisibility(View.GONE);
                        loadDialog.dismiss();
                        Toast.makeText(ShowCaptureActivity.this, anError+" 216", Toast.LENGTH_SHORT).show();
                        Log.v("Post Method Error:",anError+"");
                    }
                });

    }

    public void displayDish(double happiPer,Dialog loadDialog){

        final Dialog emoDish = new Dialog(this);
        emoDish.requestWindowFeature(Window.FEATURE_NO_TITLE);
        emoDish.setContentView(R.layout.captured_layout);
        emoDish.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    backDialog.setVisibility(View.GONE);
                    emoDish.dismiss();
                }
                return true;
            }
        });

        ImageView buyBtn,cookBtn;
        final ImageView dishImage;
        final TextView recipeTxt;
        buyBtn = (ImageView)emoDish.findViewById(R.id.captured_buyImg);
        cookBtn = (ImageView)emoDish.findViewById(R.id.captured_cookImg);
        recipeTxt = (TextView)emoDish.findViewById(R.id.captured_food_txt);
        dishImage = (ImageView)emoDish.findViewById(R.id.captured_food_pic);

        getRecipe(emoDish,loadDialog,happiPer);

        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=restaurants");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        cookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recipeTxt.setText(recipeName);
                Glide.with(getApplicationContext()).load(recipeImage).into(dishImage);
                Intent i = new Intent(ShowCaptureActivity.this,CookActivity.class);
                i.putExtra("recipeUrl",recipeUrl);
                startActivity(i);
            }
        });

        Window window = emoDish.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

//        backDialog.setVisibility(View.VISIBLE);
        emoDish.show();
    }

    public void getRecipe(final Dialog dialogBox,final Dialog loadDialog, double happiPer){

        String[] zero_to_fifty =
                {"vanilla cupcakes","meat atetranean","Olympian Burgers","stuffed greek burgers","cannoli filling","crispy baked chicken breasts","trail mix chocolate smoothie","choclate yogurt","chicken soup","chicken picatta","chicken sausage omelete","pudding fruity mix","smoothie bowl",
                "pizza casserole","creamy polenta","mint choclate chip ice cream","greek bifteki","basic crepes","flour butter cream","banana cheese cake",
                "french fried","cheese burger","double cheese pizza","china noodles","hot dog","fried chicken","pasta","penne white sauce","steak lemon sauce",
                "pancakes","doner kebab","falafel"};
        String[] fiftyOne_to_eighty =
                {"Tortilla pizza","Classic southern","fried chicken Summer burgers","Crispy dill Chicken sandwich","Black bottom banana cream pie","Banana chocolate chunk ice cream", "Golden crips chicken nuggets","Beef and spinach breakfast sandwich",
                "Beef pramesan chicken","Chocolate buckwheat cake","Chocolate dip","Fried chicken","Beef rid roast and Yorkshire pudding from the grill",
                "Bacon wrapped stuffed chicken breast","Tropical fruit salad","Zakusky for Easter luncheon","Hot dog","Tuna pizza","Nuggets","Shawarma","Burrito","Onion rings","Noodles","kebab","BBQ pizza","Nigiri sushi","tuna sandwich","spaghetti meatballs"};
        String[] eightyOne_to_ninety = {
                "fajitas","taco","fried chicken","chicken fillet","chicken fillet bites","muffins","saffron chicken wings","black out choco cakes","caesar salad","chicken salad sandwich"};
        String[] ninetyOne_to_hundred = {
                "lasagna burger","lasagna","baked salmon fish chips","carrot cake","negresco pasta","chicken florentine","meatballs with rice"};

        if (happiPer>=0 && happiPer<=50)
            dishName = zero_to_fifty[(int)(Math.random() * zero_to_fifty.length)];
        if (happiPer>=51 && happiPer<=80)
            dishName = fiftyOne_to_eighty[(int)(Math.random() * fiftyOne_to_eighty.length)];
        if (happiPer>=81 && happiPer<=90)
            dishName = eightyOne_to_ninety[(int)(Math.random() * eightyOne_to_ninety.length)];
        if (happiPer>=91 && happiPer<=100)
            dishName = ninetyOne_to_hundred[(int)(Math.random() * ninetyOne_to_hundred.length)];
        Log.v("DishName43:",dishName+"");
        AndroidNetworking.get("https://api.edamam.com/search")
                .addQueryParameter("q",dishName)
                .addQueryParameter("app_id","677cc834")
                .addQueryParameter("app_key","de33f03f1ac79382dcff706c6063082c")
                .addQueryParameter("from","0")
                .addQueryParameter("to","1").build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("CHECK FOOD responce:",response);
                        try {
                            JSONObject rawRootJson = new JSONObject(response);
                            JSONArray hitArray = rawRootJson.getJSONArray("hits");
                            JSONObject recipe = new JSONObject(hitArray.get(0).toString());
                            JSONObject recipeDetails = recipe.getJSONObject("recipe");
                            recipeName = recipeDetails.getString("label");
                            recipeImage = recipeDetails.getString("image");
                            recipeUrl = recipeDetails.getString("url");
                            loadDialog.dismiss();
//                            backDialog.setVisibility(View.VISIBLE);
                            dialogBox.findViewById(R.id.userView).setVisibility(View.VISIBLE);
                            ((TextView)dialogBox.findViewById(R.id.captured_food_txt)).setText(recipeName);
                            ImageView foodPic = dialogBox.findViewById(R.id.captured_food_pic);
                            Glide.with(getApplicationContext()).load(recipeImage).into(foodPic);
                            bitmapUrl = recipeImage;
                            tempBoo=true;
//                            Glide.with(getApplicationContext()).load(recipeImage).into(homeBack);
                        } catch (JSONException e) {
//                            backDialog.setVisibility(View.GONE);
                            loadDialog.dismiss();
                            Toast.makeText(ShowCaptureActivity.this, e+"", Toast.LENGTH_SHORT).show();
                            Log.v("ERROR MSG TWO",e+"");
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
//                        backDialog.setVisibility(View.GONE);
                        loadDialog.dismiss();
                        Toast.makeText(ShowCaptureActivity.this, anError+"", Toast.LENGTH_SHORT).show();
                        Log.v("ERROR MSG three",anError+"");
                    }
                });

    }

    private String convertToBase64(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] byteArrayImage = baos.toByteArray();
        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        return encodedImage;
    }

}

class MyAsync extends AsyncTask<Void, Void, Bitmap>{

    String BitmapUrl;

    public MyAsync (String bitmapUrl){
        this.BitmapUrl = bitmapUrl;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {

        try {
            URL url = new URL(BitmapUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}