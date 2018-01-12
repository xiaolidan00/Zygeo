package com.xiao.zygeo.set;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.xiao.zygeo.R;
import com.xiao.zygeo.tool.WebData;
import com.xiao.zygeo.util.ToastUtil;
import com.xiao.zygeo.view.MyListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//搜索中药地址
public class SearchZYActivity extends AppCompatActivity {
    private SearchView sv;
    private MyListView lv;
    private List<Map<String, Object>> list;
    private String key;
    private SimpleAdapter adapter;
    private TextView tv;
    private int limit = 10, len = 0;
    private Intent myIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_zy);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myIntent = getIntent();
        lv = (MyListView) findViewById(R.id.lv_zy_ad);
        sv = (SearchView) findViewById(R.id.search_zy);
        list = new ArrayList<>();
        tv = (TextView) findViewById(R.id.zy_info);

        initSearch();

    }

    //初始化搜索栏
    private void initSearch() {
        sv.setSubmitButtonEnabled(true);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                key = query;
                initListView();
//                LogUtil.e(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    //获取数据加载到列表中
    private void initListView() {
        String url = WebData.SEARCH_ZY + "?cmname=" + key + "&&limit=" + limit;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                LogUtil.e(response);
                resultData(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtil.show(SearchZYActivity.this, error.getMessage());
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
                    len = getListData(jsonObject);
                    if (len == 0) {
                        tv.setText("无此中药地址记录");
                        tv.setOnClickListener(null);
//                        ToastUtil.show(SearchZYActivity.this, "无此中药地址记录");
                    } else {
                        adapter = new SimpleAdapter(this, list, R.layout.searchzy_item,
                                new String[]{"cmname", "location"}, new int[]{R.id.zyitem_name, R.id.zy_item_address});
                        lv.setAdapter(adapter);
                        isMore(len);
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                myIntent.putExtra("lat", String.valueOf(list.get(position).get("lat")));
                                myIntent.putExtra("lng", String.valueOf(list.get(position).get("lng")));
                                SearchZYActivity.this.setResult(WebData.SEARCH_ZY_CODE, myIntent);
                                SearchZYActivity.this.finish();
                            }
                        });
                    }
                    } else{
                        list.removeAll(list);
                        len = getListData(jsonObject);
                        adapter.notifyDataSetChanged();
                        isMore(len);

                    }


            } else {
                tv.setText("无此中药地址记录");
                tv.setOnClickListener(null);
//                ToastUtil.show(SearchZYActivity.this,"无此中药地址记录");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void isMore(int len) {
        if (len % 10 == 0 && len > 0 && len == limit) {
            tv.setText("更多中药地址");
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    limit = limit + 10;
                    initListView();

                }
            });
        }else if(len==0){
            tv.setText("无此中药地址记录");
            tv.setOnClickListener(null);
//            ToastUtil.show(SearchZYActivity.this, "无此中药地址记录");
        } else {
            tv.setText("没有更多中药地址");
            tv.setOnClickListener(null);
//            ToastUtil.show(SearchZYActivity.this, "没有更多中药地址");
        }
    }


    //数据转为list
    private int getListData(JSONObject jsonObject) throws JSONException {
        final JSONArray jsonArray = jsonObject.getJSONArray("result");
//        LogUtil.e(String.valueOf(jsonArray.length()));
        if(jsonArray.length()>0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                String cmname = jsonArray.getJSONObject(i).getString("cmname");
                String location = jsonArray.getJSONObject(i).getString("location");
                String lat = jsonArray.getJSONObject(i).getString("latitude");
                String lng = jsonArray.getJSONObject(i).getString("longitude");
                String rid = jsonArray.getJSONObject(i).getString("mainId");
//            LogUtil.e(cmname+location+lat+lng+rid);
                Map<String, Object> map = new HashMap<>();
                map.put("rid", rid);
                map.put("cmname", cmname);
                map.put("location", location);
                map.put("lat", lat);
                map.put("lng", lng);
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
