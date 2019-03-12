package com.example.hades.garbage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;

public class HistorySingle extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {
    private String idHistory, currentUserId, customerId, driverId, cekStatus="kosong";
    private TextView rideLocation;
    private TextView rideDistance;
    private TextView rideDate;
    private TextView userName;
    private TextView userPhone;
    private TextView cost;
    @BindView(R.id.toolbar)Toolbar mtoolbar;
    private ImageView userImage;
    private RatingBar mRatingBar;
    private double mDistance;
    private DatabaseReference mDatabase;
    private LatLng  destinationLatLng, pickupLatLng;
    private String  distance;
    private String mIdDriver;
    private int saldo;

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

    public String getmIdCustomer() {
        return mIdCustomer;
    }

    public void setmIdCustomer(String mIdCustomer) {
        this.mIdCustomer = mIdCustomer;
    }

    private String mIdCustomer;
    private Boolean customerPaid = true;
    private double mSaldoSekarang,mCekSaldoCustomer;
    private double mCostDriver,mCostSampah,mTotal,mTransferDriver;
    private android.app.AlertDialog mDialog;
    private  int pointCostumerNow = 0,pointDriverNow=0;
    private FloatingActionButton button_show_info;
    private LinearLayout mCustomerInfo;
    private Button btn_top_up;
    private android.app.AlertDialog  Dialog;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        mtoolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(R.string.history);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        button_show_info=(FloatingActionButton) findViewById(R.id.show_info);
        btn_top_up=(Button) findViewById(R.id.btn_top_up);
        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);

        mtoolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                setContentView(R.layout.activity_history);
                finish();

            }
        });

        button_show_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomerInfo.setVisibility(mCustomerInfo.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

            }
        });

        btn_top_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSaldo();


            }
        });
        polylines=new ArrayList<>();
        idHistory=getIntent().getExtras().getString("rideId");
        mMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mMapFragment.getMapAsync(this);

        rideLocation=(TextView) findViewById(R.id.rideLocation);
        rideDistance = (TextView) findViewById(R.id.rideDistance);
        rideDate = (TextView) findViewById(R.id.rideDate);
        userName = (TextView) findViewById(R.id.userName);
        userPhone = (TextView) findViewById(R.id.userPhone);
        cost=(TextView) findViewById(R.id.rideCost);

        userImage = (ImageView) findViewById(R.id.userImage);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(idHistory);
        getRideInformation();


    }

    private List<Polyline> polylines;

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {

    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    private void addSaldo(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText mAddSaldo = new EditText(this);
        mAddSaldo.setInputType(1234567890);
        layout.addView(mAddSaldo);
        builder.setTitle("Top Up Saldo");
        builder.setMessage("Masukan jumlah saldo:");
        builder.setView(mAddSaldo);
        builder.setPositiveButton("Tambahkan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(getmIdCustomer());
                if(mAddSaldo.getText().equals(null)){
                    Toast.makeText(getApplicationContext(), "field saldo tidak boleh kosong", Toast.LENGTH_SHORT).show();
                }
                else{
                    int saldo2=Integer.parseInt(mAddSaldo.getText().toString());
                    int total=saldo2+getSaldo();
                    Map data=new HashMap();
                    data.put("saldo",total);
                    mOtherUserDB.updateChildren(data);
                    messageDialog("saldo berhasil ditambahkan","Sukses");
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        Dialog = builder.create();

        if (!HistorySingle.this.isFinishing()){
            Dialog.setView(layout);
            Dialog.show();


        }

    }



    private void getRideInformation() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&& dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("customer")!=null) {
                        mIdCustomer=map.get("customer").toString();
                        getUserInformation("Customers",mIdCustomer);
                        setmIdCustomer(mIdCustomer);
                    }
                    if (map.get("transaksi")!=null) {
                        cekStatus=map.get("transaksi").toString();
                    }

                    if(map.get("distance")!=null) {
                        mDistance=Double.parseDouble(map.get("distance").toString());
                        DecimalFormat df = new DecimalFormat("#.##");
                        rideDistance.setText(""+df.format(mDistance/1000)+" KM");

                    }

                    if (map.get("timestamp")!=null){
                        rideDate.setText(getDate(Long.valueOf(map.get("timestamp").toString())));
                    }
                    if(map.get("cost_driver")!=null){
                        mCostDriver=Double.parseDouble(map.get("cost_driver").toString());
                        if(map.get("cost_sampah")!=null){
                            mCostSampah=Double.parseDouble(map.get("cost_sampah").toString());
                            mTotal=mCostDriver+mCostSampah;
                            cost.setText("Rp."+(mTotal));

                        }
                    }


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
        return date;
    }


    private void cek_saldo(){
        DatabaseReference dbcekSaldo=FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(mIdCustomer);
        dbcekSaldo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("saldo")!=null){
                        mCekSaldoCustomer=Double.parseDouble(map.get("saldo").toString());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void update_saldo_point(final double saldo, final int point){

        final DatabaseReference dbSaldo=FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(mIdCustomer);
        dbSaldo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("point")!=null){
                        pointCostumerNow =Integer.parseInt(map.get("point").toString());


                    }
                }
                int totalPoint=0;
                totalPoint=pointCostumerNow+point;
                Map userinfo=new HashMap();
                userinfo.put("saldo",saldo);
                userinfo.put("point",totalPoint);
                dbSaldo.updateChildren(userinfo);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void update_status_history(String data){
        Map update=new HashMap();
        update.put("transaksi",data);
        mDatabase.updateChildren(update);
    }


    private void getUserInformation(String user, String id) {
        DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(user).child(id);
        mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        userName.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){
                        userPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("saldo") != null){
                        setSaldo(Integer.parseInt(map.get("saldo").toString()));
                    }
                    if(map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(userImage);
                    }

                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void messageDialog(String kata,String title){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(kata);
        mDialog = builder.create();
        mDialog.show();
    }



}
