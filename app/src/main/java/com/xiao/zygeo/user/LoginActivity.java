package com.xiao.zygeo.user;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.xiao.zygeo.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText edemail, edpwd;
    private ImageView img;
    private CheckBox checkBox;
    private TextView tvforget;
    private Button bt;
    private ProgressDialog pd;
    private Map<String, String> mymap;
private String email,pwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("登录");
        edemail = (EditText) findViewById(R.id.login_user);
        edpwd = (EditText) findViewById(R.id.login_pwd);
        bt = (Button) findViewById(R.id.login_bt);
        checkBox = (CheckBox) findViewById(R.id.login_rem);
        img = (ImageView) findViewById(R.id.login_img);
        tvforget = (TextView) findViewById(R.id.login_forget);
        tvforget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
            }
        });
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = DialogUtil.showWait(LoginActivity.this, "正在登录");
                pd.show();
                login();

            }
        });
    }

    //登陆操作
    private void login() {
         email = edemail.getText().toString();
         pwd = edpwd.getText().toString();
        mymap = new HashMap<>();
        mymap.put("email", email);
        mymap.put("pwd", WebData.getSha1(pwd));

        StringRequest request = new StringRequest(Request.Method.POST, WebData.LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            saveData(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        pd.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                ToastUtil.show(LoginActivity.this, error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return mymap;
            }
        };
        request.setTag("zygeoPOST");
        RequestQueue mQueue = Volley.newRequestQueue(LoginActivity.this);
        mQueue.add(request);
        mQueue.start();
    }

    //成功后保存数据
    private void saveData(String result) throws JSONException {
        JSONObject jsonObject = new JSONObject(result);
        if(jsonObject.getInt("code")==WebData.RESULT_OK) {
            JSONObject object = jsonObject.getJSONObject("result");
            boolean ischeck = checkBox.isChecked();
            SharedPreferences sp = getSharedPreferences("zygeo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            String uid = object.getString("mainId");
            editor.putString("uid", uid);
            editor.putString("username", object.getString("username"));
//        LogUtil.e(object.getString("mainId"));
            if (ischeck) {

                editor.putString("email", email);
                editor.putString("pwd", pwd);
                editor.putBoolean("isRem", true);

            } else {
                editor.putBoolean("isRem", false);
            }
            editor.commit();

//        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//        startActivity(intent);
            LoginActivity.this.finish();
        }else {
            ToastUtil.show(LoginActivity.this, "无此用户");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("zygeo", Context.MODE_PRIVATE);
        if (sp.getBoolean("isRem", false)) {
            edemail.setText(sp.getString("email", null));
            edpwd.setText(sp.getString("password", null));
            checkBox.setChecked(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.action_register:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

