package com.pathways;

import java.util.List;

/**
 * Created by amandeepsingh on 22/02/18.
 */

public class LatLongObject {

    private double lat;
    private double log;
    private boolean passed;
    private String label;
    private List<String> speechList;


    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLog() {
        return log;
    }

    public void setLog(double log) {
        this.log = log;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getSpeechList() {
        return speechList;
    }

    public void setSpeechList(List<String> speechList) {
        this.speechList = speechList;
    }
}
