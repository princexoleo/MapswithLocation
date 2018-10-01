package com.maps.leo.drawpolygon;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.Listener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import me.zhanghai.android.effortlesspermissions.EffortlessPermissions;
import me.zhanghai.android.effortlesspermissions.OpenAppDetailsDialogFragment;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Listener {

    private static final String TAG = "MapsActivity";

    private static final  int REQUEST_CODE_PERMISSION=1;
    private static final String[] PERMISSION={Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int LOCATION_SETTING_REQUEST_CODE = 11;
    private boolean permissionResult;
    Button openSettingsbtn;




    EasyWayLocation easyWayLocation;
    private Double lat=-23.0,lon=73.0;
    Context context = this;
    boolean requireFineGranularity = false;
    boolean passiveMode = false;
    long updateIntervalInMilliseconds = 6*1000;
    boolean requireNewLocation = false;


    private GoogleMap mMap;
    private float default_zoom_level=15f;


    @Override
    protected void onStart() {
        super.onStart();
        permissionGantResult();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);



            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        //easy way location track
        easyWayLocation = new EasyWayLocation(context,requireFineGranularity,passiveMode,updateIntervalInMilliseconds,
                requireNewLocation);
        easyWayLocation.setListener(this);





    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

   mMap=googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EffortlessPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    ///
    private void permissionGantResult()
    {
        Log.d(TAG, "permissionGantResult: called ");
        if (EffortlessPermissions.hasPermissions(this,PERMISSION))
        {
            //permission Granted now go on...
            Log.i(TAG, "permissionGantResult: granted !!");
            permissionResult=true;
        }
        else if (EffortlessPermissions.somePermissionPermanentlyDenied(this,PERMISSION)) {
            //some permission Deined
            Log.i(TAG, "permissionGantResult: Deined !!!!");

        }
        else {

            //request the permission
            Log.i(TAG, "permissionGantResult: requestFor permission");
            EffortlessPermissions.requestPermissions(this,
                    "Request for Permission",
                    REQUEST_CODE_PERMISSION,
                    PERMISSION);

        }
    }


    @Override
    public void locationOn() {
        Log.d(TAG, "locationOn: ");
        Toast.makeText(this, "Location On", Toast.LENGTH_SHORT).show();
        easyWayLocation.beginUpdates();
        lat=easyWayLocation.getLatitude();
        lon=easyWayLocation.getLongitude();

        LatLng newPos= new LatLng(lat,lon);
        updateMapsLocation(newPos);

    }

    @Override
    public void onPositionChanged() {
        Toast.makeText(this, "location Changed", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onPositionChanged: lati: "+easyWayLocation.getLatitude()+" Longi: "+easyWayLocation.getLongitude());
        lat=easyWayLocation.getLatitude();
        lon=easyWayLocation.getLongitude();
        LatLng newPos= new LatLng(lat,lon);
        updateMapsLocation(newPos);



    }

    @Override
    public void locationCancelled() {
        Log.d(TAG, "locationCancelled: ");
        easyWayLocation.showAlertDialog("Location Cancel","location is cancel",null);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOCATION_SETTING_REQUEST_CODE:
                easyWayLocation.onActivityResult(resultCode);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        easyWayLocation.beginUpdates();
    }

    @Override
    protected void onPause() {

        easyWayLocation.endUpdates();
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        easyWayLocation.endUpdates();
        super.onDestroy();
    }

    private void  updateMapsLocation(LatLng newLatlng)
    {
        try{
            boolean success=mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getApplicationContext(),R.raw.mapstyle
                    )
            );

            if (!success)
            {
                Log.d(TAG, "updateMaps:  Style parsing failed");
            }
        }catch (Resources.NotFoundException e)
        {
            Log.d(TAG, "updateMaps::can't find Style Error");
        }

        mMap.addMarker(new MarkerOptions().position(newLatlng).title(easyWayLocation.getPosition().toString()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatlng,default_zoom_level));
    }
}
