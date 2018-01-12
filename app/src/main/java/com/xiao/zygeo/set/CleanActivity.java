package com.xiao.zygeo.set;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xiao.zygeo.R;
import com.xiao.zygeo.tool.DataCleanHelper;
import com.xiao.zygeo.util.DialogUtil;
import com.xiao.zygeo.util.ToastUtil;

public class CleanActivity extends AppCompatActivity {
    private TextView tv;
    private Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("清除缓存");
        tv = (TextView) findViewById(R.id.clean_cache);
        bt = (Button) findViewById(R.id.clean_bt);
        try {
            String cache = DataCleanHelper.getTotalCacheSize(this);
            tv.setText(cache);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clean();
            }
        });
    }

    private void clean() {
        ProgressDialog pd = DialogUtil.showWait(this, "正在清理");
        pd.show();
        DataCleanHelper.clearAllCache(this);
        pd.dismiss();
        ToastUtil.show(this, "清理成功");
        try {
            String cache = DataCleanHelper.getTotalCacheSize(this);
            tv.setText(cache);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
