package com.enes2.burdayimogrenci;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class Main extends AppCompatActivity {
    Student student;
    String qrCode;
    TextView textView;
    ImageButton imageButton;
    DatabaseReference ref;
    String studentNumber;
    String lessonName;
    SecretKeyGenerator secretKeyGenerator;
    String tempTakenLesson;
    String tempcheckContinuity;
    int tempLessonControl;
    String oldQrCode;
    private Location tempLocation;


    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    TextView textView2,textView3;

    Context context;
    LocationManager locationManager ;
    boolean GpsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageButton = findViewById(R.id.imageButton);
        textView = findViewById(R.id.textView);
        ref = FirebaseDatabase.getInstance().getReference();

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        textView2=findViewById(R.id.textView2);
        textView3=findViewById(R.id.textView3);


        Intent i = getIntent();
        studentNumber = i.getStringExtra("studentNumber");
        oldQrCode = " ";
        secretKeyGenerator = new SecretKeyGenerator();

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        ref.child("ogrenci").child(studentNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                student = dataSnapshot.getValue(Student.class);
                student.setNumber(dataSnapshot.getKey().toString());

                if (student.getCheckContinuity() == 2 && student.isTakenLesson()) {
                    ref.child("ogrenci").child(student.getNumber()).child("checkContinuity").setValue(0);
                    ref.child("ogrenci").child(student.getNumber()).child("takenLesson").setValue(false);
                    ref.child("ogrenci").child(student.getNumber()).child("justOneScan").setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void onClick(View view) {
        if(isMockSettingsON(getApplicationContext()) && areThereMockPermissionApps(getApplicationContext())){
            Toast.makeText(getApplicationContext(), "TURN OFF FAKE GPS", Toast.LENGTH_SHORT).show();
        }else{
            if(CheckGpsStatus()) {
                checkLocation();

               //takeQrCode();    //qr kod taramaya başlıyacak yorum satırından cıkar
            }else {
                buildAlertMessageNoGps();
            }
        }

    }

    private void checkLock() {
        ref.child("Classes").child(qrCode).child("control").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    textView.setText("Kilid acilmamıştır.Null");
                } else if (dataSnapshot.getValue().toString().equals("0")) {
                    textView.setText("Kilid acilmamıştır. 0");
                } else {
                    textView.setText("Kilid OPEENN");
                    checkContinuity();
                    tempLessonControl = Integer.parseInt(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addToClass() {
        if (student.isJustOneScan()) {
            String key = ref.child("Classes").child(qrCode).child("Students").push().getKey();
            ref.child("Classes").child(qrCode).child("Students").child(key).child("number").setValue(student.getNumber());
            ref.child("Classes").child(qrCode).child("Students").child(key).child("name").setValue(student.getName());
            ref.child("Classes").child(qrCode).child("Students").child(key).child("level").setValue(student.getLevel());
            ref.child("ogrenci").child(student.getNumber()).child("justOneScan").setValue(false);
            textView.setText(lessonName + " dersine başarıyla giriş yaptınız.");
        } else {
            Toast.makeText(getApplicationContext(), "Tekrar Scan Yapmanıza gerek yoktur.", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkContinuity() {
        ref.child("Classes").child(qrCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String tempControl = null;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals("control")) {
                        tempControl = ds.getValue().toString();
                    } else if (ds.getKey().equals("lessonName")) {
                        lessonName = ds.getValue().toString();
                    }
                }
                if (tempControl.equals("1")) {
                    if (student.getCheckContinuity() == 0 && !student.isTakenLesson()) {
                        ref.child("ogrenci").child(student.getNumber()).child("checkContinuity").setValue(1);
                        addToClass();
                        textView.setText("ilk yarıya giriş yaptınız.");
                    } else if (student.getCheckContinuity() == 1 && student.isTakenLesson()) {
                        ref.child("ogrenci").child(student.getNumber()).child("checkContinuity").setValue(1);
                        ref.child("ogrenci").child(student.getNumber()).child("takenLesson").setValue(false);
                        addToClass();
                    }
                } else {
                    if (student.getCheckContinuity() == 1 && student.isTakenLesson()) {
                        ref.child("ogrenci").child(student.getNumber()).child("checkContinuity").setValue(2);
                        addToClass();
                        textView.setText("ikinci yatıya giriş yaptınız.");
                    } else {
                        lessonName = null;
                        Toast.makeText(getApplicationContext(), "ilk qr koduna katılmadığınız için derse giriş yapılmamıştır", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void takeQrCode() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(Main.this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setPrompt("Scan");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setBarcodeImageEnabled(false);
        intentIntegrator.initiateScan();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "You called the scan", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    qrCode = secretKeyGenerator.decrypt(result.getContents());
                    checkLock();
                 /*   if (tempLessonControl == 1) {
                        oldQrCode = qrCode;
                    }
                    if (oldQrCode.equals(qrCode)) {
                        checkLock();
                    }
                    */
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void checkLocation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            buildLocationRequest();
            buildLocationCallBack();

            fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

            if (ActivityCompat.checkSelfPermission(Main.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Main.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Main.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public boolean CheckGpsStatus(){

        locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return GpsStatus;
    }

    public static boolean isMockSettingsON(Context context) {
            return false;
    }

    public static boolean areThereMockPermissionApps(Context context) {
        int count = 0;

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i]
                                .equals("android.permission.ACCESS_MOCK_LOCATION")
                                && !applicationInfo.packageName.equals(context.getPackageName())) {
                            count++;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Got exception " , e.getMessage());
            }
        }

        if (count > 0)
            return true;
        return false;
    }

    private void buildAlertMessageNoGps() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(
                "Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false).setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        startActivity(new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildLocationCallBack() {
        locationCallback= new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location : locationResult.getLocations()){
                    tempLocation=location;
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    System.out.println("location sooonnnn" + tempLocation.getLatitude() +" "+tempLocation.getLongitude());
                    System.out.println("location sooonnnn" + tempLocation.getLatitude() +" "+tempLocation.getLongitude());
                    System.out.println("location sooonnnn" + tempLocation.getLatitude() +" "+tempLocation.getLongitude());
                    textView2.setText(String.valueOf(location.getLongitude()));
                    textView3.setText(String.valueOf(location.getLatitude()));
                }
            }
        };

    }

    private void buildLocationRequest() {
        locationRequest=LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
    }
}
