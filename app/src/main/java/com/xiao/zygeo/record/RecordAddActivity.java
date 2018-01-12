package com.xiao.zygeo.record;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.xiao.zygeo.R;
import com.xiao.zygeo.set.SelectPictureActivity;
import com.xiao.zygeo.tool.WebData;
import com.xiao.zygeo.user.LoginActivity;
import com.xiao.zygeo.util.DialogUtil;
import com.xiao.zygeo.util.DpPxUtil;
import com.xiao.zygeo.util.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class RecordAddActivity extends AppCompatActivity implements View.OnClickListener {
    private double lat, lng;
    private ImageView addimg;
    private EditText edtitle, edcontent;
    private TextView tvlat, tvlng, tvaddress;
    private String title, content, address, uid, rid;
    private LinearLayout lin;
    private Map<String, String> mymap;
    private static final int REQUEST_PICK = 0;
    private MediaType MEDIA_TYPE_IMG = MediaType.parse("image/*");
    private ArrayList<String> selectedPicture = new ArrayList<String>();
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_add);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("添加记录");
        SharedPreferences sp = getSharedPreferences("zygeo", Context.MODE_PRIVATE);
        uid = sp.getString("uid", null);
        if (uid == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        edtitle = (EditText) findViewById(R.id.ed_title);
        edcontent = (EditText) findViewById(R.id.ed_content);
        tvlat = (TextView) findViewById(R.id.recordadd_lat);
        tvlng = (TextView) findViewById(R.id.recordadd_lng);
        tvaddress = (TextView) findViewById(R.id.recordadd_address);
        addimg = (ImageView) findViewById(R.id.addimg);
        lin = (LinearLayout) findViewById(R.id.add_photo);
        addimg.setOnClickListener(this);

        Intent intent = getIntent();
        lat = intent.getDoubleExtra("lat", 0);
        lng = intent.getDoubleExtra("lng", 0);
        address = intent.getStringExtra("address");
        if (lat == 90 || lng == 0) {
            Toast.makeText(this, "定位信息错误！请重新选择定位", Toast.LENGTH_SHORT).show();
        }
        tvlng.append(String.valueOf(lng));
        tvlat.append(String.valueOf(lat));
        tvaddress.append(address);
        pd = DialogUtil.showWait(this, "正在添加记录");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addimg:
                if (selectedPicture != null) {
                    for (int i = 0; i < selectedPicture.size(); i++) {
                        selectedPicture.remove(i);
                    }
                }
                startActivityForResult(new Intent(this, SelectPictureActivity.class),
                        REQUEST_PICK);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            lin.removeAllViews();
            lin.addView(addimg);
            selectedPicture = (ArrayList<String>) data
                    .getSerializableExtra(SelectPictureActivity.INTENT_SELECTED_PICTURE);
            String path = "file://" + selectedPicture.get(0);
            Picasso.with(this)
                    .load(path)
                    .placeholder(R.drawable.pic_load)
                    .error(R.drawable.picfail)
                    .into(addimg);
            for (int i = 1; i < selectedPicture.size(); i++) {
                ImageView img = new ImageView(this);
                img.setScaleType(ImageView.ScaleType.FIT_XY);
                int pad = DpPxUtil.dip2px(this, 8);
                img.setPadding(pad, pad, pad, pad);
                int a = DpPxUtil.dip2px(this, 80);
                img.setLayoutParams(new ViewGroup.LayoutParams(a, a));
                String path1 = "file://" + selectedPicture.get(i);
                Picasso.with(this)
                        .load(path1)
                        .placeholder(R.drawable.pic_load)
                        .error(R.drawable.picfail)
                        .into(img);
                lin.addView(img);
            }
        }
    }

    //okhttp上传数据和图片
    private void uploadData() {

        long ran = Math.round(Math.random() * 10000000 + 100000000);
        rid = String.valueOf(ran);
        OkHttpClient client = new OkHttpClient();
        File[] file = new File[selectedPicture.size()];
        for (int i = 0; i < selectedPicture.size(); i++) {
            file[i] = new File("/" + selectedPicture.get(i));
        }
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM)
                .addFormDataPart("mainId", rid)
                .addFormDataPart("uid", uid)
                .addFormDataPart("cmname", title)
                .addFormDataPart("record", content)
                .addFormDataPart("location", address)
                .addFormDataPart("latitude", String.valueOf(lat))
                .addFormDataPart("longitude", String.valueOf(lng));
        for (int i = 0; i < selectedPicture.size(); i++) {
            builder.addFormDataPart("file[]", "pic" + i + "." + WebData.getExtensionName(selectedPicture.get(i)), RequestBody.create(MEDIA_TYPE_IMG, file[i]));
//            LogUtil.e(selectedPicture.get(i));
        }
        pd.show();
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(WebData.RECORD_ADD)
                .post(requestBody)
                .build();
        try {
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, final IOException e) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            pd.dismiss();
                            ToastUtil.show(RecordAddActivity.this, "添加记录失败");
//                            LogUtil.e("onfailure"+e.getMessage());
                        }
                    }).start();

                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            pd.dismiss();
                            if (response.isSuccessful()) {
                                Intent intent = new Intent(RecordAddActivity.this, RecordDetailActivity.class);
                                intent.putExtra("rid", rid);
                                startActivity(intent);
//                                        ToastUtil.show(RecordAddActivity.this, "添加记录成功");
                                RecordAddActivity.this.finish();


                            } else {
                                ToastUtil.show(RecordAddActivity.this, "添加记录失败");
                            }
//
//                            LogUtil.e("onResponse----"+response.isSuccessful());

                        }
                    }).start();

                }
            });
        } catch (Exception e) {
            ToastUtil.show(this, e.getMessage());
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addrecord, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_addrecord) {
            if (selectedPicture.size() < 4) {
                ToastUtil.show(this, "请选择四张图片");
            } else {
                content = edcontent.getText().toString();
                title = edtitle.getText().toString();
                if (title == "" || content == "") {
                    ToastUtil.show(this, "请填写中药名称和记录内容");
                } else {
                    if (title.length() > 200) {
                        ToastUtil.show(this, "标题太长了，不能超过100个");
                    } else {
                        uploadData();
                    }
                }
            }
        } else if (id == android.R.id.home) {
            this.finish();

        }
        return true;
    }


}
