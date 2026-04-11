package com.medihelp.app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "consultations")
public class Consultation {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String symptoms;
    private String advice;
    private int userAge;
    private long timestamp;
    private String photoPath;

    public Consultation(String symptoms, String advice, int userAge, String photoPath) {
        this.symptoms = symptoms;
        this.advice = advice;
        this.userAge = userAge;
        this.timestamp = System.currentTimeMillis();
        this.photoPath = photoPath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }
    public int getUserAge() { return userAge; }
    public void setUserAge(int userAge) { this.userAge = userAge; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}
