package com.enes2.burdayimogrenci;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Student {
    private String name;
    private String number;
    private String password;
    private String macId;
    private String level;
    private String department;
    private int checkContinuity;
    private boolean justOneScan;
    private boolean takenLesson;

    public Student() {
    }
    public Student(String name, String number, String password, String macId, String level, String department, int checkContinuity,boolean justOneScan) {
        this.name = name;
        this.number = number;
        this.password = password;
        this.macId = macId;
        this.level = level;
        this.department = department;
        this.checkContinuity = checkContinuity;
        this.justOneScan=justOneScan;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getCheckContinuity() {
        return checkContinuity;
    }

    public void setCheckContinuity(int checkContinuity) {
        this.checkContinuity = checkContinuity;
    }

    public boolean isJustOneScan() {
        return justOneScan;
    }
    public void setJustOneScan(boolean justOneScan) {
        this.justOneScan = justOneScan;
    }

    public boolean isTakenLesson() {
        return takenLesson;
    }

    public void setTakenLesson(boolean takenLesson) {
        this.takenLesson = takenLesson;
    }
}
