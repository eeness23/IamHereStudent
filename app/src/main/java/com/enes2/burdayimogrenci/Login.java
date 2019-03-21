package com.enes2.burdayimogrenci;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;


public class Login extends AppCompatActivity {
    private EditText studentNumber;
    private EditText studentPassword;
    private TextView login;
    private DatabaseReference ref;
    private Student student;
    private String sNumber;
    private String sPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        studentNumber = (EditText) findViewById(R.id.studentNumber);
        studentPassword = (EditText) findViewById(R.id.studentPassword);
        login = (TextView) findViewById(R.id.login);
        ref = FirebaseDatabase.getInstance().getReference().child("ogrenci");


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


    }

    public void enter(View view) {
        sNumber = studentNumber.getText().toString();
        sPassword = studentPassword.getText().toString();
        if(TextUtils.isEmpty(sNumber)|| TextUtils.isEmpty(sPassword)){
            Toast.makeText(this, " Tüm boşlukları doldurunuz ", Toast.LENGTH_SHORT).show();
        }else {
            ref.child(sNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    student =dataSnapshot.getValue(Student.class);
                    student.setNumber(dataSnapshot.getKey());
                    if(student ==null){
                        Toast.makeText(Login.this, "KUllANICI BULUNAMADI", Toast.LENGTH_LONG).show();
                    }else {
                        if (sPassword.equals(student.getPassword())) {
                            if(student.getMacId()==null || student.getMacId().equals(" ")){
                                ref.child(sNumber).child("macId").setValue(getMacAddr());
                                Toast.makeText(Login.this, "Login Succeful", Toast.LENGTH_SHORT).show();
                                Intent i2 = new Intent(getApplicationContext(),Main.class);
                                i2.putExtra("studentNumber",sNumber);
                                startActivity(i2);
                            }else{
                                if(student.getMacId().equals(getMacAddr())){
                                    Toast.makeText(Login.this, "Login Succeful", Toast.LENGTH_SHORT).show();
                                    Intent i2 = new Intent(getApplicationContext(),Main.class);
                                    i2.putExtra("studentNumber",sNumber);
                                    startActivity(i2);

                                }else{
                                    Toast.makeText(Login.this, "Lütfen kendi telefonuz ile giriş yapınız", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(Login.this, "Sifre yanlis", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
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


}
