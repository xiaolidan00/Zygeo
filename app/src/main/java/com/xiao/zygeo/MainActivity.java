package com.xiao.zygeo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.xiao.zygeo.map.MyLocation;
import com.xiao.zygeo.record.CollectActivity;
import com.xiao.zygeo.record.RecordActivity;
import com.xiao.zygeo.record.RecordAddActivity;
import com.xiao.zygeo.record.RecordDetailActivity;
import com.xiao.zygeo.set.AboutActivity;
import com.xiao.zygeo.set.CleanActivity;
import com.xiao.zygeo.set.SearchZYActivity;
import com.xiao.zygeo.tool.ConnectionDirector;
import com.xiao.zygeo.tool.WebData;
import com.xiao.zygeo.user.LoginActivity;
import com.xiao.zygeo.user.UserActivity;
import com.xiao.zygeo.util.ToastUtil;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {

    private MapView mapView;
    private AMap aMap;
    private FloatingActionButton toolrecord, tooldetail, toolmyloc, toolzy;
    private SearchView sv;
    private MyLocation myLocation;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ActionBar工具栏设置
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ConnectionDirector director = new ConnectionDirector(this);
        if (!director.isConnectingToInternet()) {
            ToastUtil.show(this, "请链接网络");
        } else {

            initFloatButton();
            initNav(toolbar);
            initMapView(savedInstanceState);
            initSearch();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==WebData.SEARCH_ZY_CODE&&resultCode==WebData.SEARCH_ZY_CODE){
            if(data!=null){
                double lat=Double.parseDouble(data.getStringExtra("lat"));
                double lng=Double.parseDouble(data.getStringExtra("lng"));
                myLocation.moveCamera(new LatLng(lat,lng));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initFloatButton() {
        tooldetail = (FloatingActionButton) findViewById(R.id.detail);
        toolrecord = (FloatingActionButton) findViewById(R.id.record);
        toolmyloc = (FloatingActionButton) findViewById(R.id.mylocation);
        toolzy = (FloatingActionButton) findViewById(R.id.search_zy);
        toolrecord.setOnClickListener(this);
        tooldetail.setOnClickListener(this);
        toolzy.setOnClickListener(this);

    }

    private void initMapView(Bundle savedInstanceState) {
        //地图初始化
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        myLocation = new MyLocation(this, aMap, mapView, toolmyloc);
        myLocation.initMap();
    }

    private void initNav(Toolbar toolbar) {
        //侧滑菜单
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.layout_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void initSearch() {
        sv = (SearchView) findViewById(R.id.search);
        sv.setSubmitButtonEnabled(true);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                myLocation.getLatlngByAddress(query, myLocation.myLoc.getCityCode());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        AMapLocation aMapLocation = myLocation.myLoc;
        switch (v.getId()) {
            case R.id.record://浮动按钮添加记录

                intent = new Intent(MainActivity.this, RecordAddActivity.class);
                intent.putExtra("lat", aMapLocation.getLatitude());
                intent.putExtra("lng", aMapLocation.getLongitude());
                intent.putExtra("address", aMapLocation.getAddress());
                break;
            case R.id.detail://浮动按钮查询记录详情
                if(myLocation.selectmarker==null||myLocation.selectmarker==myLocation.mLocMarker||
                        myLocation.selectmarker.getSnippet()==""||myLocation.selectmarker.getSnippet()==null){
                    ToastUtil.show(this,"请选择已记录坐标");
                }else {

                        intent = new Intent(MainActivity.this, RecordDetailActivity.class);
                        intent.putExtra("rid", myLocation.selectmarker.getSnippet());

                }
                break;
            case R.id.search_zy://浮动按钮搜索中药
                intent = new Intent(MainActivity.this, SearchZYActivity.class);
                startActivityForResult(intent,WebData.SEARCH_ZY_CODE);
                break;

        }
        if(intent!=null) {
            startActivity(intent);
        }
    }


    //amapview重写onResume onPause onSaveInstanceState onDestroy方法
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        myLocation.deactivate();
        myLocation.firstFix = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (null != myLocation.mlocationClient) {
            myLocation.mlocationClient.onDestroy();
        }

    }


    //侧滑菜单
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.layout_main);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = null;
        switch (id) {

            case R.id.nav_record:
                intent = new Intent(this, RecordActivity.class);
                break;
            case R.id.nav_collect:
                intent = new Intent(this, CollectActivity.class);
                break;
//            case R.id.nav_love:
//                break;
            case R.id.nav_clean:
                intent = new Intent(this, CleanActivity.class);
                break;
            case R.id.nav_about:
                intent = new Intent(this, AboutActivity.class);
                break;
            case R.id.nav_user:
                sp = getSharedPreferences("zygeo", Context.MODE_PRIVATE);
                String uid = sp.getString("uid", null);
                if (uid == null) {
                    intent = new Intent(this, LoginActivity.class);
                } else {
                    intent = new Intent(this, UserActivity.class);
                }
                break;
        }
        startActivity(intent);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
