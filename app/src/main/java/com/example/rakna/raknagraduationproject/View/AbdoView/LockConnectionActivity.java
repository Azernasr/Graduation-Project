package com.example.rakna.raknagraduationproject.View.AbdoView;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.rakna.raknagraduationproject.R;
import com.example.rakna.raknagraduationproject.View.azerCode.Login;
import com.example.rakna.raknagraduationproject.View.azerCode.SendDataLogin;
import com.example.rakna.raknagraduationproject.View.hassan.MapsActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


import javax.net.ssl.HttpsURLConnection;


public class LockConnectionActivity extends AppCompatActivity {

    LottieAnimationView lottieAnimationView;
    private Button btnQRcode;
    private Button btnNfc;

    private ProgressDialog progressDialog ;

    private String ownerId ;
    private String garageId = "";
    private boolean isReserved = false;

//    private static final String URL_RESERVE = "https://rakna-app.000webhostapp.com/Recerve_Car_In_Garage.php";
    private String JSON_URL = "https://rakna-app.000webhostapp.com/Calculate_Price.php?";
    private String JSON_NOTIFI = "https://rakna-app.000webhostapp.com/notification_services/index.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_connection);

        savedInstanceState = getIntent().getExtras();
        ownerId = savedInstanceState.getString("ownerId");
        garageId = savedInstanceState.getString("garageId");
        isReserved = savedInstanceState.getBoolean("isReserved");

        initWidgets();
        actionWidgets();

        showAnimateConnection();
    }

    public void initWidgets(){

        lottieAnimationView = findViewById(R.id.animation_view);
        btnQRcode = findViewById(R.id.btn_qrCode);
        btnNfc = findViewById(R.id.btn_nfc);

        progressDialog = new ProgressDialog(this);
        initDialog();
    }

    public void actionWidgets(){

        btnQRcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(LockConnectionActivity.this).setCaptureActivity(QRcodeActivity.class).initiateScan();
            }
        });

        btnNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LockConnectionActivity.this, NfcActivity.class);
                startActivityForResult(intent,2);
            }
        });

    }


    public void showAnimateConnection(){
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                lottieAnimationView.cancelAnimation();

                lottieAnimationView.setAnimation("scan_nfc.json");
                lottieAnimationView.loop(false);
                lottieAnimationView.setProgress(0);
                lottieAnimationView.playAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }


    public void sendNotiReservedForGarage(String processCode, String message){
        // Saeed eldeeb rented your garage hahahahahahhaahahhaahhahhahhaha

        StringRequest stringRequest = new StringRequest(Request.Method.POST, JSON_NOTIFI, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LockConnectionActivity.this, "Register error! 16 \n"+ error.toString(),Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("noti_code", processCode);
                params.put("noti_message", message);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    @Override
    protected void onStop() {
        lottieAnimationView.cancelAnimation();
        super.onStop();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode != 2) {
                //We will get scan results here
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                //check for null
                if (result != null) {
                    if (result.getContents() == null) {
                        Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
                    } else {

                        if ("147852".equals(result.getContents())) {

                            if (isReserved) {
                                JSON_URL += "driver_licence=" + ownerId + "&national_id_number=" + garageId + "&flag=0";
                                new JsonUtils().execute();
                                sendNotiReservedForGarage("1","Saeed eldeeb rented your garage");
                            } else {
                                JSON_URL += "driver_licence=" + ownerId + "&national_id_number=" + garageId + "&flag=1";
                                new JsonUtils().execute();
                                sendNotiReservedForGarage("2","Saeed eldeeb Leaved your garage");
                            }
                        } else {
                            Toast.makeText(LockConnectionActivity.this, "This garage has't been reserved", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                } else {
                    // This is important, otherwise the result will not be passed to the fragment
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }else {

                // NFC
                String nfcScanResult = data.getStringExtra("nfc_scaned");

                if ("147852".equals(nfcScanResult)) {

                    if (isReserved) {
//                        reserveCarInGarage("552", "6358");
                        JSON_URL += "driver_licence=" + ownerId + "&national_id_number=" + garageId + "&flag=0";
                        new JsonUtils().execute();
                    } else {
                        JSON_URL += "driver_licence=" + ownerId + "&national_id_number=" + garageId + "&flag=1";
                        new JsonUtils().execute();
                    }

                }else {
                    Toast.makeText(this, "This garage has't been reserved", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initDialog(){
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please, Wait to Reserve Car");
        progressDialog.setMessage("Checking...");
    }

    //    public void animate(View v) {
//        if (lottieAnimationView.isAnimating()) {
//            lottieAnimationView.cancelAnimation();
//            button.setText("play");
//        } else {
//            lottieAnimationView.playAnimation();
//            button.setText("pause");
//        }
//    }
    class JsonUtils extends AsyncTask<Void, Void, String> {

        String JsonUrl = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            JsonUrl = JSON_URL;
            Log.d("url", "onPreExecute: "+JsonUrl);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(JsonUrl);
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

                InputStream is = new BufferedInputStream(con.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {

                    sb.append(line + "\n");

                }
                is.close();
                bufferedReader.close();
                con.disconnect();
                return sb.toString().trim();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!isReserved) {
                try {
                    JSONObject object = new JSONObject(s);
                    JSONArray jsonArray = object.getJSONArray("Rakna_Payment");

                    JSONObject obj = jsonArray.getJSONObject(0);

                    getSharedPreferences("reservedData", MODE_PRIVATE)
                            .edit().
                            putBoolean("isReserved", false).
                            commit();

                    Intent intent = new Intent(LockConnectionActivity.this, PayActivity.class);
                    intent.putExtra("rakna_time", obj.getString("rakna_time"));
                    intent.putExtra("price", obj.getString("price"));
                    intent.putExtra("rakna_in_hour", obj.getString("rakna_in_hour"));
                    startActivity(intent);
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else {

                try {
                    JSONObject object = new JSONObject(s);
                    JSONArray jsonArray = object.getJSONArray("Garage_Reserve_Info");

                    JSONObject obj = jsonArray.getJSONObject(0);

                    reservedDataSharedpref(ownerId, garageId,
                            obj.getString("name"),
                            obj.getString("phone"),
                            obj.getString("rate"),
                            obj.getString("garage_width"),
                            obj.getString("garage_length"),
                            obj.getString("garage_image"),
                            obj.getString("wash"),
                            obj.getString("lubrication"),
                            obj.getString("maintenance"),
                            obj.getString("oil_change"),
                            true);

                    Intent intent = new Intent(LockConnectionActivity.this, HomeServiceActivity.class);
                    startActivity(intent);
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void reservedDataSharedpref(String ownerId, String garageId, String garageName,
                                           String garagePhone, String garageRate, String garageWidth,
                                           String garageLength, String garageImage, String wash, String lubrication,
                                           String maintenance, String oil_change, boolean isReserved) {
            getSharedPreferences("reservedData", MODE_PRIVATE)
                    .edit().
                    putString("ownerId", ownerId).
                    putString("garageId", garageId).
                    putString("name", garageName).
                    putString("phone", garagePhone).
                    putString("rate", garageRate).
                    putString("garage_width", garageWidth).
                    putString("garage_length", garageLength).
                    putString("garage_image", garageImage).
                    putString("wash", wash).
                    putString("lubrication", lubrication).
                    putString("maintenance", maintenance).
                    putString("oil_change", oil_change).
                    putBoolean("isReserved", isReserved).
                    commit();
        }
    }
}
