package com.example.hades.garbage;

import android.Manifest;
import android.net.Uri;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import android.support.v4.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.example.hades.garbage.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.maps.android.SphericalUtil;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


public class HomeMember extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    private Toolbar toolbar;
    private Switch mWorkingSwitch;
    private NavigationView navigasiView;
    private DrawerLayout drawerLayout;
    private GoogleMap mMap;
    private String userId;
    private String customerId = "", destination;
    private float rideDistance;
    private LatLng destinationLatLng, pickupLatLng, meLatLng;
    private int status = 0;
    private List<Polyline> polylines;
    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment mapFragment;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private TextView mCustomerDestination;
    private LinearLayout mCustomerInfo;
    private TextView mCustomerName;
    private TextView mCustomerPhone;
    private TextView mCostDriver;
    private TextView mJenisSampah;
    private TextView mCostSampah;
    private TextView mJarak;
    private double distance;
    private double biaya_driver;
    static double km = 1000;
    private String cost_driver, jenis_sampah, cost_sampah;

    private ImageView mCustomerProfileImage;
    private Button mRideStatus, mHistory;
    private LocationManager service;
    private static final int[] COLORS = new int[]{R.color.colorPrimary};
    private String call;
    private Button mCall;
    private FloatingActionButton button_show_info;
    private boolean connect_drivers=false;
    private String status_driver;


    boolean network_enabled = false;
    private AlertDialog mInternetDialog;

    public boolean isConnect_drivers() {
        return connect_drivers;
    }

    public void setConnect_drivers(boolean connect_drivers) {
        this.connect_drivers = connect_drivers;
    }


    private BroadcastReceiver mNetworkDetectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkInternetConnection();

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_member);


        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        registerReceiver(mNetworkDetectReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        ButterKnife.bind(this);
        polylines = new ArrayList<>();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);
        mCustomerProfileImage = (ImageView) findViewById(R.id.customerProfileImage);

        toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        navigasiView = (NavigationView) findViewById(R.id.navigasi_view);

        button_show_info=(FloatingActionButton) findViewById(R.id.show_info);
        mCustomerName = (TextView) findViewById(R.id.customerName);
        mCustomerPhone = (TextView) findViewById(R.id.customerPhone);
        mCustomerDestination = (TextView) findViewById(R.id.customerDestination);
        mCostSampah = (TextView) findViewById(R.id.costSampah);
        mCostDriver = (TextView) findViewById(R.id.costDriver);
        mJenisSampah = (TextView) findViewById(R.id.jenisSampah);
        mJarak=(TextView) findViewById(R.id.jarak);








        mWorkingSwitch = (Switch) findViewById(R.id.workingSwitch);
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (network_enabled == false) {
                        showNoInternetDialog();
                    }
                    else {
                        try {
                            connectDriver();
                            connect_drivers = true;

                        } catch (Exception e) {
                            Log.d("", "" + e);
                        }
                    }

                }
                else{
                    disconnectDriver();
                    connect_drivers=false;
                }
            }
        });
        navigasiView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.navigation1:
                        Intent intent1 = new Intent(HomeMember.this, Profil.class);
                        startActivity(intent1);
                        return true;
                    case R.id.navigation2:
                        Intent intent2 = new Intent(HomeMember.this, wallet.class);
                        startActivity(intent2);
                        return true;
                    case R.id.navigation3:
                        Intent intent3 = new Intent(HomeMember.this, History.class);
                        startActivity(intent3);
                        return true;
                    case R.id.navigation4:
                        Toast.makeText(getApplicationContext(), "About", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.navigation5:
                        disconnectDriver();
                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        firebaseAuth.signOut();
                        Intent intent = new Intent(HomeMember.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(), "Kesalahan", Toast.LENGTH_SHORT).show();
                        return true;
                }

            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        button_show_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomerInfo.setVisibility(mCustomerInfo.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

            }
        });


        mCall = (Button) findViewById(R.id.call);
        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:" + call));
                if (ActivityCompat.checkSelfPermission(HomeMember.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(i);
            }
        });


        mRideStatus = (Button) findViewById(R.id.rideStatus);
        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(status){
                    case 1:
                        status=2;
                        erasePolylines();
                        if(destinationLatLng.latitude!=0.0 && destinationLatLng.longitude!=0.0){
                            getRouteToMarker(destinationLatLng);
                        }
                        mRideStatus.setText("drive completed");

                        break;
                    case 2:
                        recordRide();
                        endRide();
                        break;
                }
            }
        });



        //Mensetting actionbarToggle untuk drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        //memanggil synstate
        actionBarDrawerToggle.syncState();
        cekAktivasiDriver();
        getAssignedCustomer();

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mNetworkDetectReceiver);
        super.onDestroy();
    }


    private void getAssignedCustomer() {
        String driverId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverId).child("customerRequest").child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    status = 1;
                    customerId = dataSnapshot.getValue().toString();

                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeMember.this);
                    builder.setTitle("Jobs");
                    builder.setMessage("Terdapat Customer Masuk");
                    builder.setPositiveButton("Tolak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            disconnectDriver();
                            endRide();
                            mWorkingSwitch.setChecked(false);

                        }
                    }).setNegativeButton("Terima", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            getAssignedCustomerPickupLocation();//1
                            getAssignedCustomerDestination();//2
                            getAssignedCustomerInfo();//3
                        }
                    });
                    mInternetDialog = builder.create();
                    if (!HomeMember.this.isFinishing()){

                        mInternetDialog.show();
                    }



                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;
    private void getAssignedCustomerPickupLocation() {
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener=assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    double locationLat1=0;
                    double locationLng1=0;
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Cost").child("Driver");

                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                        //Toast.makeText(HomeMember.this, "NILAIIII B"+locationLat, Toast.LENGTH_SHORT).show();
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());

                    }

                    pickupLatLng = new LatLng(locationLat,locationLng);

                    distance = SphericalUtil.computeDistanceBetween(meLatLng,pickupLatLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    getRouteToMarker(pickupLatLng);
                    driverRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()&& dataSnapshot.getChildrenCount()>0){
                                Map <String,Object> map = (Map <String,Object>) dataSnapshot.getValue();
                                if(map.get("jarak")!=null){
                                    if(distance!=0){
                                        biaya_driver= Double.valueOf(map.get("jarak").toString()).doubleValue();
                                        DecimalFormat df = new DecimalFormat("#");
                                        mCostDriver.setText(""+String.valueOf(df.format((distance/km)*biaya_driver)));
                                        mJarak.setText(""+df.format(distance/km)+" KM");
                                        cost_driver=String.valueOf(df.format((distance/km)*biaya_driver));


                                    }
                                }

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        if (pickupLatLng != null && mLastLocation != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                    .key("AIzaSyAZpvxhoqh6oSDYlupHMni8N6WSozDQq6M")
                    .build();
            routing.execute();
        }
    }

    private void getAssignedCustomerDestination() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverId).child("customerRequest");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("destination")!=null){
                        destination = map.get("destination").toString();
                         mCustomerDestination.setText("Destination: " + destination);
                    }
                    else{
                        mCustomerDestination.setText("");
                    }

                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;
                    if(map.get("destinationLat") != null){
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if(map.get("destinationLng") != null){
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void getAssignedCustomerInfo() {
        mCustomerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        final DatabaseReference mCostCustomer = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userId).child("customerRequest");
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mCustomerName.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!=null){
                        mCustomerPhone.setText(map.get("phone").toString());
                        call=map.get("phone").toString();

                    }
                    if(map.get("profileImageUrl")!=null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mCustomerProfileImage);
                    }
                }
                mCostCustomer.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            if(map.get("cost_sampah")!=null){mCostSampah.setText(""+map.get("cost_sampah").toString());
                                cost_sampah=String.valueOf(map.get("cost_sampah"));}
                            if(map.get("sampah")!=null){mJenisSampah.setText("Sampah "+map.get("sampah").toString());
                                jenis_sampah=String.valueOf(map.get("sampah"));}
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void endRide() {
        mRideStatus.setText("Selesai");
        erasePolylines();


        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userId).child("customerRequest");
        driverRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);

        geoFire.removeLocation(customerId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
        customerId="";
        rideDistance = 0;

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (assignedCustomerPickupLocationRefListener != null){
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }
        mCustomerInfo.setVisibility(View.GONE);
        mCustomerName.setText("");
        mCustomerPhone.setText("");
        mCustomerDestination.setText("");
        mJenisSampah.setText("");
        mCostSampah.setText("");
        mCostDriver.setText("");
        mJarak.setText("");
        mCustomerProfileImage.setImageResource(R.drawable.ic_male_user);
    }


    private void recordRide(){

        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userId).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requestId = historyRef.push().getKey();
        driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("driver", userId);
        map.put("customer", customerId);
        map.put("rating", 0);
        map.put("timestamp", getCurrentTimestamp());
        map.put("destination", destination);
        map.put("location/from/lat", pickupLatLng.latitude);
        map.put("location/from/lng", pickupLatLng.longitude);
        map.put("location/to/lat", destinationLatLng.latitude);
        map.put("location/to/lng", destinationLatLng.longitude);
        map.put("distance", distance);
        map.put("cost_sampah", cost_sampah);
        map.put("sampah",jenis_sampah);
        map.put("cost_driver",cost_driver);
        map.put("transaksi","0");
        historyRef.child(requestId).updateChildren(map);
    }

    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult (LocationResult locationResult)  {
            for(Location location : locationResult.getLocations()) {

                if(getApplicationContext()!=null) {

                    if (!customerId.equals("") && mLastLocation != null && location != null) {
                        rideDistance += mLastLocation.distanceTo(location) / 1000;
                    }
                    mLastLocation = location;
                    meLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(meLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                    if(connect_drivers==true){
                        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriverAvailable");
                        DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriverWorking");
                        GeoFire geoFireAvailable = new GeoFire(refAvailable);
                        GeoFire geoFireWorking = new GeoFire(refWorking);

                        switch (customerId) {
                            case "":
                                geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        // Toast.makeText(HomeMember.this, key+"1", Toast.LENGTH_SHORT).show();


                                    }
                                });
                                geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        // Toast.makeText(HomeMember.this, key+"2", Toast.LENGTH_SHORT).show();

                                    }
                                });
                                break;

                            default:
                                geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        //Toast.makeText(HomeMember.this, key+"3", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        //Toast.makeText(HomeMember.this, key+"4", Toast.LENGTH_SHORT).show();

                                    }
                                });
                                break;

                        }

                    }

                }


            }
        }
    };


    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(HomeMember.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(HomeMember.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        meLatLng =  new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void connectDriver(){

        if(status_driver.equals("tidak aktif")){
            connect_drivers=false;
            showDialog("Activation","hubungi admin sistem untuk aktivasi akun driver anda dapat digunakan","Ok");
            mWorkingSwitch.setChecked(false);
        }
        if(status_driver.equals("aktif")){

            checkLocationPermission();
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            //LocationServices.FusedLocationApi.requestLocationUpdates(mGmapClientApi, mLocationRequest, (LocationListener) this);
            mMap.setMyLocationEnabled(true);

        }


    }

    private void disconnectDriver(){
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        setConnect_drivers(false);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriverAvailable");
        DatabaseReference working=FirebaseDatabase.getInstance().getReference("DriverWorking");
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userId).child("customerRequest");
        driverRef.removeValue();

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
        GeoFire geoFire1= new GeoFire(working);
        geoFire1.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }

    private void cekAktivasiDriver(){
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userId);
        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("status_driver")!=null){
                        status_driver=(map.get("status_driver").toString());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onRoutingStart() {

    }
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }


    private void checkInternetConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();

        if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
            network_enabled=true;
        } else {
            showNoInternetDialog();
        }
    }

    private void showNoInternetDialog() {

        if (mInternetDialog != null && mInternetDialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Internet Disabled!");
        builder.setMessage("No active Internet connection found.");
        builder.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(gpsOptionsIntent, WifiManager.WIFI_STATE_ENABLED);
            }
        }).setNegativeButton("No, Just Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        mInternetDialog = builder.create();
        mInternetDialog.show();
    }



    private void showDialog(String title,String contain,String messageButton) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(contain);
        mInternetDialog = builder.create();
        builder.setPositiveButton(messageButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        mInternetDialog.show();
    }




}


