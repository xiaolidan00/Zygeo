package com.xiao.zygeo.map;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.xiao.zygeo.R;
import com.xiao.zygeo.tool.WebData;
import com.xiao.zygeo.util.AMapUtil;
import com.xiao.zygeo.util.DialogUtil;
import com.xiao.zygeo.util.LogUtil;
import com.xiao.zygeo.util.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MyLocation implements LocationSource, AMapLocationListener,
        AMap.OnMarkerDragListener, AMap.OnMapLongClickListener,
        GeocodeSearch.OnGeocodeSearchListener {
    private Context context;
    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private GeocodeSearch geocodeSearch;
    public AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    public AMapLocation myLoc;
    private AMapLocation nowLoc;
    public boolean firstFix = false, reFix = false;
    public Marker mLocMarker, selectmarker;

    private View reLoc;
    public int limit = 1000;
    private ProgressDialog pd;

    public MyLocation(Context context, final AMap aMap, MapView mapView, View reLoc) {
        this.context = context;
        this.aMap = aMap;
        this.mapView = mapView;
        this.reLoc = reLoc;
        selectmarker=mLocMarker;
        pd = DialogUtil.showWait(context, "正在加载地图信息");

    }

    //初始化地图
    public void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }

    }

    //地图设置
    private void setUpMap() {
        pd.show();
        aMap.setMyLocationEnabled(true);
//        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setOnMarkerDragListener(this);
        aMap.setOnMapLongClickListener(this);
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                selectmarker = marker;
                return true;
            }
        });
        reLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng pos=new LatLng(nowLoc.getLatitude(),nowLoc.getLongitude());
                mLocMarker.setPosition(pos);
                moveCamera(pos);
                reFix = true;
            }
        });
        geocodeSearch = new GeocodeSearch(context);
        geocodeSearch.setOnGeocodeSearchListener(this);
    }

    //重新加载
    public void reload() {
        aMap.clear();
        firstFix = false;

    }

    //添加获取数据库中marker信息
    public void initMapData() throws JSONException {

        String url = WebData.MAP + "?limit=" + String.valueOf(limit);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        mapMarkers(s);
//                        LogUtil.e(s);
                        pd.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                ToastUtil.show(context, error.toString());

            }
        });
        request.setTag("zygeoGET");
        RequestQueue mQueue = Volley.newRequestQueue(context);
        mQueue.add(request);
        mQueue.start();
    }

    //添加数据库中的marker数据解析
    private void mapMarkers(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.getInt("code") == 0) {
                final JSONArray jsonArray = jsonObject.getJSONArray("result");
//                LogUtil.e(String.valueOf(jsonArray.length()));
                for (int i = 0; i < jsonArray.length(); i++) {

                    double lat = Double.parseDouble(jsonArray.getJSONObject(i).getString("latitude"));
                    double lng = Double.parseDouble(jsonArray.getJSONObject(i).getString("longitude"));
                    String cmname = jsonArray.getJSONObject(i).getString("cmname");
                    String username = jsonArray.getJSONObject(i).getString("username");
                    String rid = jsonArray.getJSONObject(i).getString("mainId");
                    LatLng pos = new LatLng(lat, lng);
//                    LogUtil.e(cmname+"--"+username+"---");
//                    LogUtil.e(pos.latitude+"--"+pos.longitude);
                    addMarkers(pos, cmname, username, rid);
                }
            } else {
                ToastUtil.show(context, jsonObject.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //添加数据库中的marker
    private void addMarkers(LatLng pos, String cmname, String user, String rid) {
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        options.anchor(0.5f, 0.5f);
        options.title("中药：" + cmname + "\n记录者：" + user);
        options.draggable(false);
        options.position(pos);
        options.snippet(rid);
        Marker marker = aMap.addMarker(options);
    }

    //添加当前的marker
    private void addMarker(LatLng pos) {
        if (mLocMarker != null) {
            return;
        }
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        options.anchor(0.5f, 0.5f);
        options.position(pos);
        options.title("你的位置");
        mLocMarker = aMap.addMarker(options);
        mLocMarker.showInfoWindow();
        mLocMarker.setDraggable(true);
//        LogUtil.e(pos.latitude+"--"+pos.longitude);
        try {
            initMapData();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                nowLoc=amapLocation;
                LatLng location = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                if (!firstFix) {
                    myLoc = amapLocation;

                    firstFix = true;
                    addMarker(location);//添加定位图标
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
                } else {
                    if (reFix) {
                        myLoc = amapLocation;
                        mLocMarker.setPosition(location);
                        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
                        reFix = false;
                    }
                }

            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    /**
     * 拖拉坐标动作响应
     */
    @Override
    public void onMarkerDragStart(Marker marker) {
        mLocMarker.hideInfoWindow();

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng pos = marker.getPosition();
        mLocMarker.showInfoWindow();
        mLocMarker.setDraggable(true);
        mLocMarker.setPosition(pos);
        getAddressByLatlng(pos);
    }

    //移动自己位置坐标，改变地理信息
    private void newLocAddress(String address) {
        LatLng pos = mLocMarker.getPosition();
        myLoc.setLongitude(pos.longitude);
        myLoc.setLatitude(pos.latitude);
        myLoc.setAddress(address);
    }

    //通过经纬度获取地址
    public void getAddressByLatlng(LatLng latLng) {
        //逆地理编码查询条件：逆地理编码查询的地理坐标点、查询范围、坐标类型。
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 500f, GeocodeSearch.AMAP);
        //异步查询
        geocodeSearch.getFromLocationAsyn(query);
    }

    public void moveCamera(LatLng pos) {
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 18));
    }

    //通过地址获取经纬度
    public void getLatlngByAddress(String name, String code) {
        GeocodeQuery query = new GeocodeQuery(name, code);// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
        geocodeSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
    }

    //长按地图动作响应
    @Override
    public void onMapLongClick(LatLng latLng) {
        LatLng pos = latLng;
        mLocMarker.setPosition(pos);
        mLocMarker.showInfoWindow();
        mLocMarker.setDraggable(true);
        getAddressByLatlng(pos);
    }

    //通过经纬度获取地址的，地址逆编码
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
        String formatAddress = regeocodeAddress.getFormatAddress();
        newLocAddress(formatAddress);

    }

    //通过地址获取经纬度，地址编码
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        AMapUtil.convertToLatLng(address.getLatLonPoint()), 18));
                mLocMarker.setPosition(AMapUtil.convertToLatLng(address
                        .getLatLonPoint()));
            } else {
                ToastUtil.show(context, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(context, rCode);
        }
    }


    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(context);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
}