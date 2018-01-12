package com.xiao.zygeo.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.xiao.zygeo.R;
import com.xiao.zygeo.tool.WebData;
import com.xiao.zygeo.util.DialogUtil;
import com.xiao.zygeo.util.LogUtil;
import com.xiao.zygeo.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private  EditText edemail,edname,edpwd,edrepwd;
    private Map<String,String> mymap;
    private ProgressDialog pd;
    private Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("注册");
        edemail = (EditText) findViewById(R.id.register_email);
        edname= (EditText) findViewById(R.id.register_user);
        edpwd= (EditText) findViewById(R.id.register_pwd);
        edrepwd= (EditText) findViewById(R.id.register_repwd);
        bt= (Button) findViewById(R.id.register_bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

    }

    private void register() {
        pd= DialogUtil.showWait(this,"正在注册");
        pd.show();
        String email=edemail.getText().toString();
        String name=edname.getText().toString();
        String pwd=edpwd.getText().toString();
        String repwd=edrepwd.getText().toString();
        if(!WebData.isEmail(email)) {
            ToastUtil.show(this,"邮箱格式错误");
           return;
        }
        if(repwd.equals(pwd)) {
            mymap=new HashMap<>();
            mymap.put("email",email);
            mymap.put("username",name);
            mymap.put("pwd", WebData.getSha1(pwd));
            StringRequest request = new StringRequest(Request.Method.POST, WebData.REDISTER, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    result(response);
                    pd.dismiss();
//                    LogUtil.e(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ToastUtil.show(RegisterActivity.this,error.toString());
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
        }else {
            pd.dismiss();
            ToastUtil.show(this,"密码不一致");

        }

    }

    private void result(String response) {
        try {
            JSONObject jsonObject=new JSONObject(response);
            if(jsonObject.getInt("code")==WebData.RESULT_OK){
                ToastUtil.show(this,jsonObject.getString("message"));
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                        RegisterActivity.this.finish();
                    }
                }).start();

            }else {
                ToastUtil.show(this,jsonObject.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pd.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.register,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_login:
                Intent intent=new Intent(this,LoginActivity.class);
                startActivity(intent);
                this.finish();
                break;
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
