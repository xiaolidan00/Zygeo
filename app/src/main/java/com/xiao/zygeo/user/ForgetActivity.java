package com.xiao.zygeo.user;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.xiao.zygeo.R;
import com.xiao.zygeo.tool.WebData;
import com.xiao.zygeo.util.DialogUtil;
import com.xiao.zygeo.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgetActivity extends AppCompatActivity {
    private EditText edname, edemail, edpwd, edrepwd;
    private Map<String, String> mymap;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("修改密码");
        edname = (EditText) findViewById(R.id.forget_name);
        edemail = (EditText) findViewById(R.id.forget_email);
        edpwd = (EditText) findViewById(R.id.forget_pwd);
        edrepwd = (EditText) findViewById(R.id.forget_repwd);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.forget, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                savePass();
                break;
            case android.R.id.home:
                Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
                startActivity(intent);
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //修改密码操作
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void savePass() {
        pd = DialogUtil.showWait(this, "正在修改密码");
        pd.show();
        String email = edemail.getText().toString();
        String name = edname.getText().toString();
        String pwd = edpwd.getText().toString();
        String repwd = edrepwd.getText().toString();
        if (repwd.equals(pwd)) {
            mymap = new HashMap<>();
            mymap.put("email", email);
            mymap.put("username", name);
            mymap.put("pwd", WebData.getSha1(pwd));
            StringRequest request = new StringRequest(Request.Method.POST, WebData.FORGET, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    result(response);
                    pd.dismiss();
//                    LogUtil.e(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ToastUtil.show(ForgetActivity.this, error.toString());
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
            ToastUtil.show(ForgetActivity.this, "密码不一致");

        }

    }
//结果分析
    private void result(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getInt("code") == WebData.RESULT_OK) {

                ToastUtil.show(ForgetActivity.this, jsonObject.getString("message"));
                Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
                startActivity(intent);
                ForgetActivity.this.finish();

            } else {
                ToastUtil.show(ForgetActivity.this, jsonObject.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
