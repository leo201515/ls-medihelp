package com.medihelp.app.model;

public class MedicalCondition {
    private String id;
    private String name;
    private String nameDe;
    private String symptoms;
    private String advice;
    private String medicinesAdults;
    private String medicinesKids;

    public MedicalCondition(String id, String name, String nameDe, String symptoms, String advice, String medicinesAdults, String medicinesKids) {
        this.id = id;
        this.name = name;
        this.nameDe = nameDe;
        this.symptoms = symptoms;
        this.advice = advice;
        this.medicinesAdults = medicinesAdults;
        this.medicinesKids = medicinesKids;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getNameDe() { return nameDe; }
    public String getSymptoms() { return symptoms; }
    public String getAdvice() { return advice; }
    public String getMedicinesAdults() { return medicinesAdults; }
    public String getMedicinesKids() { return medicinesKids; }
    public String getMedicinesKids7() { return medicinesKids; }
}
