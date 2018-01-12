package com.xiao.zygeo.record;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.xiao.zygeo.R;
import com.xiao.zygeo.adapter.MyListAdapter;
import com.xiao.zygeo.tool.WebData;
import com.xiao.zygeo.user.LoginActivity;
import com.xiao.zygeo.util.ToastUtil;
import com.xiao.zygeo.view.MyListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//收藏列表
public class CollectActivity extends AppCompatActivity {
    private MyListView lv;
    private List<Map<String, Object>> list;
    private String uid;
    private MyListAdapter adapter;
    private TextView tv;
    private int limit = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("我的收藏");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lv = (MyListView) findViewById(R.id.collect_lv);
        list = new ArrayList<>();
        tv = (TextView) findViewById(R.id.collect_info);

        SharedPreferences sp = getSharedPreferences("zygeo", Context.MODE_PRIVATE);
        uid = sp.getString("uid", null);
        if (uid == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        }
        initListView();
    }

    //初始化收藏列表数据
    private void initListView() {
        String url = WebData.COLLECT_LIST + "?uid=" + uid + "&&limit=" + limit;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                resultData(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtil.show(CollectActivity.this, error.getMessage());
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
        queue.start();


    }

    //返回数据分析
    private void resultData(String response) {
        try {

            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("code") == WebData.RESULT_OK) {


                if (list.size() == 0) {
                    int len = getListData(jsonObject);
                    if(len==0){
                        tv.setText("暂无收藏");
                        tv.setOnClickListener(null);
//                        ToastUtil.show(CollectActivity.this, "暂无收藏");
                    }else {
                        adapter = new MyListAdapter(this, list);
                        lv.setAdapter(adapter);
                        isMore(len);

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                                            Intent intent = new Intent(CollectActivity.this, RecordDetailActivity.class);
                                            intent.putExtra("rid", String.valueOf(list.get(position).get("rid")));
                                            startActivity(intent);


                            }
                        });
                    }
                } else {
                    list.removeAll(list);
                    int len = getListData(jsonObject);
                    adapter.notifyDataSetChanged();
                    isMore(len);

                }

            } else {
                tv.setText("暂无收藏");
                tv.setOnClickListener(null);
//                ToastUtil.show(CollectActivity.this, "暂无收藏");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void isMore(int len) {
        if (len % 10 == 0&&len>0&&len==limit) {
            tv.setText("更多收藏");
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    limit = limit + 10;
                    initListView();

                }
            });
        }else if(len==0){
            tv.setText("暂无收藏");
            tv.setOnClickListener(null);
//            ToastUtil.show(CollectActivity.this, "暂无收藏");
        }else {
            tv.setText("没有更多收藏");
            tv.setOnClickListener(null);
//            ToastUtil.show(CollectActivity.this, "没有更多收藏");
        }
    }

    //返回数据转化为list
    private int getListData(JSONObject jsonObject) throws JSONException {
        final JSONArray jsonArray = jsonObject.getJSONArray("result");
//        LogUtil.e(String.valueOf(jsonArray.length()));
        if(jsonArray.length()>0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                String cmname = jsonArray.getJSONObject(i).getString("cmname");
                String username = jsonArray.getJSONObject(i).getString("username");
                String pic1 = jsonArray.getJSONObject(i).getString("pic1");
                String uptime = jsonArray.getJSONObject(i).getString("uptime");
                String rid = jsonArray.getJSONObject(i).getString("mainId");
                Map<String, Object> map = new HashMap<>();
                map.put("rid", rid);
                map.put("title", cmname);
                map.put("img", pic1);
                String str = "By " + username + "   " + uptime;
                map.put("date", str);
                list.add(map);
            }
        }
        return jsonArray.length();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
