package com.example.dell.appbanhang.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dell.appbanhang.R;
import com.example.dell.appbanhang.adapter.DienThoaiAdapter;
import com.example.dell.appbanhang.adapter.LaptopAdapter;
import com.example.dell.appbanhang.model.Sanpham;
import com.example.dell.appbanhang.util.CheckInternet;
import com.example.dell.appbanhang.util.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LaptopActivity extends AppCompatActivity {
    Toolbar toolbarlaptop;
    ListView listViewlaptop;
    LaptopAdapter laptopAdapter;
    ArrayList<Sanpham> manglaptop;
    int idlaptop = 0;
    int page = 1;
    View footerview;
    boolean isloading = false;
    boolean limitdata = false;
    myHandler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laptop);
        Anhxa();
        if(CheckInternet.haveNetworkConnection(getApplicationContext())){
            getIDLoaiSP();
            ActionToolbar();
            getData(page);
            LoadmoreData();
        }
        else{
            CheckInternet.showToast_short(getApplicationContext(),"Bạn hãy kiểm tra lại kết nối!");
            finish();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menugiohang:
                Intent intent = new Intent(getApplicationContext(), com.example.dell.appbanhang.activity.Giohang.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    private void LoadmoreData() {
        listViewlaptop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),ChiTietSanPham.class);
                intent.putExtra("thongtinsanpham",manglaptop.get(position));
                startActivity(intent);
            }
        });
        listViewlaptop.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0 && isloading == false && limitdata == false){
                    isloading=true;
                    ThreadData threadData = new ThreadData();
                    threadData.start();
                }
            }
        });
    }
    public class myHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    listViewlaptop.addFooterView(footerview);
                    break;
                case 1:
                    getData(++page);
                    isloading = false;
                    break;
            }
            super.handleMessage(msg);
        }
    }
    public class ThreadData extends Thread{
        @Override
        public void run() {
            mHandler.sendEmptyMessage(0);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message = mHandler.obtainMessage(1);
            mHandler.sendMessage(message);
            super.run();
        }
    }
    private void getData(int p) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String duongdan = Server.Duongdandienthoai + String.valueOf(p);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, duongdan, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                int id=0;
                String tenlaptop="";
                int gialaptop=0;
                String hinhanhlaptop = "";
                String motalaptop ="";
                int idsplaptop=0;
                if(response!=null && response.length()!=2){
                    listViewlaptop.removeFooterView(footerview);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            id =  jsonObject.getInt("id");
                            tenlaptop = jsonObject.getString("tensp");
                            gialaptop = jsonObject.getInt("giasp");
                            hinhanhlaptop = jsonObject.getString("hinhanhsp");
                            motalaptop = jsonObject.getString("motasp");
                            idsplaptop = jsonObject.getInt("idsanpham");
                            manglaptop.add(new Sanpham(id,tenlaptop,gialaptop,hinhanhlaptop,motalaptop,idsplaptop));
                            //cap nhat Adapter
                            laptopAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    limitdata=true;
                    listViewlaptop.removeFooterView(footerview);
                    CheckInternet.showToast_short(getApplicationContext(),"Đã hết dữ liệu!");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> param = new HashMap<String,String>();
                param.put("idsanpham",String.valueOf(idlaptop));
                return param;
            }
        };
        requestQueue.add(stringRequest);
    }
    private void ActionToolbar() {
        setSupportActionBar(toolbarlaptop);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarlaptop.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void getIDLoaiSP() {
        idlaptop = getIntent().getIntExtra("idloaisp",-1);
    }

    private void Anhxa() {
        toolbarlaptop = (Toolbar) findViewById(R.id.toolbarlaptop);
        listViewlaptop = (ListView) findViewById(R.id.listviewlaptop);
        manglaptop = new ArrayList<>();
        laptopAdapter = new LaptopAdapter(getApplicationContext(),manglaptop);
        listViewlaptop.setAdapter(laptopAdapter);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        footerview = inflater.inflate(R.layout.progressbar,null);
        mHandler = new myHandler();
    }
}

