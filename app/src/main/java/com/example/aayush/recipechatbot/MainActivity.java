package com.example.aayush.recipechatbot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ArrayList<String> dataset;
    private VerticalAdapter verticalAdapter;
    private RecyclerView.SmoothScroller smoothScroller;
    private LinearLayoutManager verticalLayoutManagaer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check internet connectivity
        if(!isInternetAvailable()){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("INTERNET IS NOT AVAILABLE")
                    .setCancelable(false)
                    .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            Intent i = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(i);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        editText = (EditText) findViewById(R.id.editText);
        imageView = (ImageView) findViewById(R.id.imageView);
        recyclerView = (RecyclerView) findViewById(R.id.recyler_view);

        dataset = new ArrayList<String>();


        verticalLayoutManagaer
                = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        verticalAdapter = new VerticalAdapter(dataset);

        recyclerView.setLayoutManager(verticalLayoutManagaer);
        recyclerView.setAdapter(verticalAdapter);

        // for smooth scrolling
        smoothScroller = new LinearSmoothScroller(getApplicationContext()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };



        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.length()==0) {
                    Toast.makeText(MainActivity.this, "Please enter someting", Toast.LENGTH_SHORT).show();
                }
                else{
                    // adding %20 in place of spaces
                    String str = editText.getText().toString(), str1="";
                    for(int i=0;i<str.length();i++){
                        if(str.charAt(i)==' '){
                            str1 = str1 + "%20";
                        }
                        else{
                            str1 = str1 + str.charAt(i);
                        }

                    }
                    dataset.add(str+"/.@./user_msg");
                    verticalAdapter.notifyDataSetChanged();
                    smoothScroller.setTargetPosition(dataset.size()-1);
                    verticalLayoutManagaer.startSmoothScroll(smoothScroller);
                    editText.setText("");
                    getData(str1);
                }
            }
        });
    }

    // Get data from backend
    public void getData(String str){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.hutoma.ai/v1/ai/71c73455-463a-4fcc-ad1b-befb91aeacee/chat?q="+str;

        queue.add( new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject status = response.getJSONObject("status");
                            if(status.getString("info").equalsIgnoreCase("OK")
                                    && status.getString("code").equalsIgnoreCase("200")){
                                JSONObject result = response.getJSONObject("result");
                                String answer = result.getString("answer");
                                //Toast.makeText(MainActivity.this, answer, Toast.LENGTH_SHORT).show();
                                dataset.add(answer+"/.@./backend_msg");
                                verticalAdapter.notifyDataSetChanged();
                                smoothScroller.setTargetPosition(dataset.size()-1);
                                verticalLayoutManagaer.startSmoothScroll(smoothScroller);
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Something went w" +
                                        "rong. Please try again after some time", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Something went w" +
                                    "rong. Please try again after some time", Toast.LENGTH_SHORT).show();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();

                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //Setting Authentication header token
                Map<String, String> headers = new HashMap<>();
                String token = "eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNocizEOwjAMAP_iGUt1GtcNG4IOkaJWQixMKHadD1RMiL8TMd1wdx-4b2WB8x-va8" +
                        "nL-tjW8oQTXHK-dSFkMkZmjNNYMTYzrDspqjdNVL2a" +
                        "e6-Pt_bYNVhrvmNg8X4EwtlFcGaaEmsahpjg-wMAAP__.9q0aC-DMDj5RyT5rUN8XaO4dULDcDR-aDlhgQHJ1Jw4";
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer "+token);
                return headers;
            }
        });



    }

    // Adapter for recyler view
    public class VerticalAdapter extends RecyclerView.Adapter<VerticalAdapter.MyViewHolder> {

        private List<String> horizontalList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView user_msg, backend_msg;
            public CardView user_cd, backend_cd;

            public MyViewHolder(View view) {
                super(view);
                user_msg = (TextView) view.findViewById(R.id.user_msg);
                backend_msg = (TextView) view.findViewById(R.id.backend_msg);
                user_cd = (CardView) view.findViewById(R.id.user_msg_cd);
                backend_cd = (CardView) view.findViewById(R.id.backend_msg_cd);

            }
        }


        public VerticalAdapter(List<String> horizontalList) {
            this.horizontalList = horizontalList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyler_view, parent, false);

            return new MyViewHolder(itemView);
        }



        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            final String[] data = horizontalList.get(position).split( "/.@./" );
            Log.e("data",data[0]+" "+data[0]);
            if(data[1].equalsIgnoreCase("user_msg")){
                holder.user_cd.setVisibility(View.VISIBLE);
                holder.user_msg.setVisibility(View.VISIBLE);
                holder.user_msg.setText(data[0]);
                holder.backend_msg.setVisibility(View.GONE);
                holder.backend_cd.setVisibility(View.GONE);
            }
            else if(data[1].equalsIgnoreCase("backend_msg")){
                holder.backend_msg.setVisibility(View.VISIBLE);
                holder.backend_cd.setVisibility(View.VISIBLE);
                holder.backend_msg.setText(data[0]);
                holder.user_msg.setVisibility(View.GONE);
                holder.user_cd.setVisibility(View.GONE);
            }


        }

        @Override
        public int getItemCount() {
            return horizontalList.size();
        }
    }

    // Internet connectivity checking method
    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }


}
