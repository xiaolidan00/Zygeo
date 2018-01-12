package com.xiao.zygeo.record;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.xiao.zygeo.R;
import com.xiao.zygeo.set.SearchZYActivity;
import com.xiao.zygeo.tool.WebData;
import com.xiao.zygeo.user.LoginActivity;
import com.xiao.zygeo.util.DialogUtil;
import com.xiao.zygeo.util.ToastUtil;
import com.xiao.zygeo.view.MyListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordDetailActivity extends AppCompatActivity {
    private String rid, uid;
    private TextView tv, tvtime, textView;
    private EditText ed;
    private Button bt;
    private ViewFlipper vf;
    private boolean isCollect = false;
//    private ProgressDialog pd;
    private MyListView lv;
    private List<Map<String, Object>> list;
    private int limit = 10;
    private FloatingActionButton fab;
    private SimpleAdapter adapter;
    private Map<String, String> mymap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        rid = intent.getStringExtra("rid");
        SharedPreferences sp = getSharedPreferences("zygeo", Context.MODE_PRIVATE);
        uid = sp.getString("uid", null);
        lv = (MyListView) findViewById(R.id.lv_comment);
        list = new ArrayList<>();
        ed = (EditText) findViewById(R.id.ed_comment);
        bt = (Button) findViewById(R.id.comment_bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addComment();
            }
        });
        fab = (FloatingActionButton) findViewById(R.id.fab_collect);
        textView = (TextView) findViewById(R.id.detail_info);

        initRecord();
        if (uid == null) {
            fab.setImageResource(R.drawable.ic_collect);
        } else {
            initCollect();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isCollect) {

                    collect_on();
                } else {

                    collect_off();

                }
            }
        });
        initComment();


    }

    //添加评论
    private void addComment() {
        if (uid == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        String content = ed.getText().toString();
        mymap = new HashMap<>();
        mymap.put("uid", uid);
        mymap.put("rid", rid);
        mymap.put("content", content);
        String url = WebData.REMARK_ADD;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                resultComment(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtil.show(RecordDetailActivity.this, error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return mymap;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
        queue.start();
    }

    //添加评论返回数据分析
    private void resultComment(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("code") == WebData.RESULT_OK) {
                ed.setText("");
                initComment();
                ToastUtil.show(this, jsonObject.getString("message"));
            } else {
                ToastUtil.show(this, jsonObject.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //初始化评论列表
    private void initComment() {
        if (uid == null) {
            limit = 10;
        }
        String url = WebData.REMARK_LIST + "?rid=" + rid + "&&limit=" + limit;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                initList(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtil.show(RecordDetailActivity.this, error.getMessage());
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
        queue.start();
    }

    //评论列表数据加载到listview
    private void initList(String response) {
        try {

            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("code") == WebData.RESULT_OK) {


                if (list.size() == 0) {
                    int len = getListData(jsonObject);
                    if (len == 0) {
                        textView.setText("暂无评论");
                        textView.setOnClickListener(null);
//                        ToastUtil.show(RecordDetailActivity.this, "暂无评论");
                    } else {
                        adapter = new SimpleAdapter(this, list, R.layout.comment_item,
                                new String[]{"user", "content", "uptime"}, new int[]{R.id.comment_name, R.id.comment_content, R.id.comment_date});
                        lv.setAdapter(adapter);

                        isMore(len);
                    }
                    } else{
                        list.removeAll(list);
                        int len = getListData(jsonObject);
                        adapter.notifyDataSetChanged();
                        isMore(len);
                    }


            } else {
                textView.setText("暂无评论");
                textView.setOnClickListener(null);
//                ToastUtil.show(RecordDetailActivity.this, "暂无评论");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void isMore(int len) {
        if (len % 10 == 0&&len>0&&len==limit) {
//            textView.setVisibility(View.VISIBLE);
            textView.setText("更多评论");
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    limit = limit + 10;
                    initComment();

                }
            });
        }else if (len==0){
//            textView.setVisibility(View.VISIBLE);
            textView.setText("暂无评论");
            textView.setOnClickListener(null);
//            ToastUtil.show(RecordDetailActivity.this, "暂无评论");
        }else {
            textView.setText("没有更多评论");
            textView.setOnClickListener(null);
//            ToastUtil.show(RecordDetailActivity.this, "没有更多评论");
        }

    }

    //评论列表数据转为list
    private int getListData(JSONObject jsonObject) throws JSONException {
        final JSONArray jsonArray = jsonObject.getJSONArray("result");
//        LogUtil.e(String.valueOf(jsonArray.length()));
        if(jsonArray.length()>0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                String username = jsonArray.getJSONObject(i).getString("username");
                String content = jsonArray.getJSONObject(i).getString("content");
                String uptime = jsonArray.getJSONObject(i).getString("uptime");
                Map<String, Object> map = new HashMap<>();
                map.put("user", username);
                map.put("content", content);
                map.put("uptime", uptime);
                list.add(map);
            }
        }
        return jsonArray.length();
    }

    //初始化是否收藏
    private void initCollect() {
        if (uid == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        String url = WebData.COLLECT_IS + "?rid=" + rid + "&&uid=" + uid;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("code") == WebData.RESULT_OK) {
                        isCollect = true;
                        fab.setImageResource(R.drawable.ic_collect_on);
                    } else {
                        isCollect = false;
                        fab.setImageResource(R.drawable.ic_collect);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isCollect = false;
                fab.setImageResource(R.drawable.ic_collect);
                ToastUtil.show(RecordDetailActivity.this, error.getMessage());
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
        queue.start();
    }

    //取消收藏
    private void collect_off() {
        if (uid == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        String url = WebData.COLLECT_OFF + "?rid=" + rid + "&&uid=" + uid;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("code") == WebData.RESULT_OK) {
                        ToastUtil.show(RecordDetailActivity.this, jsonObject.getString("message"));
                        fab.setImageResource(R.drawable.ic_collect);
                        isCollect = false;
                    } else {
                        ToastUtil.show(RecordDetailActivity.this, jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                ToastUtil.show(RecordDetailActivity.this, error.getMessage());
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
        queue.start();
    }

    //添加收藏
    private void collect_on() {
        if (uid == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        String url = WebData.COLLECT_ON + "?rid=" + rid + "&&uid=" + uid;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("code") == WebData.RESULT_OK) {
                        ToastUtil.show(RecordDetailActivity.this, jsonObject.getString("message"));
                        fab.setImageResource(R.drawable.ic_collect_on);
                        isCollect = true;
                    } else {
                        ToastUtil.show(RecordDetailActivity.this, jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                ToastUtil.show(RecordDetailActivity.this, error.getMessage());
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
        queue.start();
    }

    //初始化记录数据
    private void initRecord() {
        tv = (TextView) findViewById(R.id.tv_record);
        tvtime = (TextView) findViewById(R.id.tv_uptime);
        vf = (ViewFlipper) findViewById(R.id.vf);
        String url = WebData.RECORD_DETAIL + "?rid=" + rid;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                tvData(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                ToastUtil.show(RecordDetailActivity.this, error.getMessage());
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
        queue.start();
    }

    //记录数据添加到tv
    private void tvData(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("code") == WebData.RESULT_OK) {
                JSONObject object = jsonObject.getJSONObject("result");
                String username = object.getString("username");
                String location = object.getString("location");
                String lng = object.getString("longitude");
                String lat = object.getString("latitude");
                String content = object.getString("record");
                String uptime = object.getString("uptime");
                String cmname = object.getString("cmname");
                String pic1 = object.getString("pic1");
                String pic2 = object.getString("pic2");
                String pic3 = object.getString("pic3");
                String pic4 = object.getString("pic4");
                setTitle(cmname);
                tv.append("记录者：" + username + "\n");
                tv.append("经度：" + lng + "\t纬度：" + lat + "\n");
                tv.append("地址：" + location + "\n");
                tvtime.setText(uptime);
                tv.append(content + "\n");
                if (pic1 != "") {
                    loadImage(pic1);
                }
                if (pic2 != "") {
                    loadImage(pic2);
                }
                if (pic3 != "") {
                    loadImage(pic3);
                }
                if (pic4 != "") {
                    loadImage(pic4);
                }
                vf.setFlipInterval(5000);
                vf.setInAnimation(this, android.R.anim.slide_in_left);
                vf.setOutAnimation(this, android.R.anim.slide_out_right);
                vf.startFlipping();
            } else {
                ToastUtil.show(RecordDetailActivity.this, jsonObject.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //加载图片
    private void loadImage(String pic1) {
        ImageView img1 = new ImageView(this);
        img1.setScaleType(ImageView.ScaleType.FIT_XY);
        img1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Picasso.with(this)
                .load(pic1)
                .into(img1);
        vf.addView(img1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
