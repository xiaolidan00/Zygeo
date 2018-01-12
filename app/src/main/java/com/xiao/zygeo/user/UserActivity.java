package com.xiao.zygeo.user;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.xiao.zygeo.MainActivity;
import com.xiao.zygeo.R;
import com.xiao.zygeo.tool.WebData;
import com.xiao.zygeo.util.DialogUtil;
import com.xiao.zygeo.util.LogUtil;
import com.xiao.zygeo.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity {
    private EditText ed;
    private Button bt;
    private Map<String, String> mymap;
    private ProgressDialog pd;
    private String uid, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("用户中心");
        SharedPreferences sp = getSharedPreferences("zygeo", Context.MODE_PRIVATE);
        username = sp.getString("username", null);
        uid = sp.getString("uid", null);
        if (uid=="") {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            this.finish();
        }
//        uid="123456789";
        ed = (EditText) findViewById(R.id.user_name);
        ed.setText(username);
        bt = (Button) findViewById(R.id.user_bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUser();
            }
        });
    }

    private void saveUser() {
        pd = DialogUtil.showWait(this, "正在修改用户名");
        pd.show();
        username = ed.getText().toString();
        if (username!="") {
            mymap = new HashMap<>();
            mymap.put("uid", uid);
            mymap.put("username", username);
            StringRequest request = new StringRequest(Request.Method.POST, WebData.USER_SAVE, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    result(response);
                    pd.dismiss();
//                    LogUtil.e(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ToastUtil.show(UserActivity.this, error.toString());
                    pd.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return mymap;
                }
            };
            request.setTag("zygeoPOST");
            RequestQueue mQueue = Volley.newRequestQueue(this);
            mQueue.add(request);
            mQueue.start();
        } else {
            pd.dismiss();
            ToastUtil.show(UserActivity.this, "用户名不能为空");

        }

    }

    private void result(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("code") == WebData.RESULT_OK) {
                ToastUtil.show(UserActivity.this, jsonObject.getString("message"));
                SharedPreferences sp = getSharedPreferences("zygeo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("username", username);
                editor.commit();
                Intent intent = new Intent(UserActivity.this, MainActivity.class);
                startActivity(intent);
                this.finish();
            } else {
                ToastUtil.show(UserActivity.this, jsonObject.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.action_out:
                SharedPreferences sp=getSharedPreferences("zygeo",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sp.edit();
                editor.putString("uid",null);
                editor.putString("username",null);
                editor.putString("pwd",null);
                editor.putString("email",null);
                editor.putBoolean("isRem",false);
                editor.commit();
                Intent intent=new Intent(this, LoginActivity.class);
                startActivity(intent);
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
