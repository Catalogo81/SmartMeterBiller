package com.example.smartmeterbiller.classes;

import android.media.Image;

import com.google.firebase.database.Exclude;

import java.util.Date;

public class CapturedReadings
{
     String capturedReadingDownloadLink;
        String capturedReadingId;
     String capturedReadingUrl;
     String capturedReading;
     String capturedDate;
     String totalCost;
     String validationMessage;
     String mKey;

    public CapturedReadings(){}

    public CapturedReadings(String capturedReadingId, String capturedReadingDownloadLink, String capturedReadingUrl, String capturedReading, String capturedDate, String totalCost, String validationMessage) {
        this.capturedReadingId = capturedReadingId;
        this.capturedReadingDownloadLink = capturedReadingDownloadLink;
        this.capturedReadingUrl = capturedReadingUrl;
        this.capturedReading = capturedReading;
        this.capturedDate = capturedDate;
        this.totalCost = totalCost;
        this.validationMessage = validationMessage;
    }


    public String getCapturedReadingId() {
        return capturedReadingId;
    }

    public void setCapturedReadingId(String capturedReadingId) {
        this.capturedReadingId = capturedReadingId;
    }

    public String getCapturedReadingDownloadLink() {
        return capturedReadingDownloadLink;
    }

    public void setCapturedReadingDownloadLink(String capturedReadingDownloadLink) {
        this.capturedReadingDownloadLink = capturedReadingDownloadLink;
    }

    public String getCapturedReadingUrl() {
        return capturedReadingUrl;
    }

    public void setCapturedReadingUrl(String capturedReadingUrl) {
        this.capturedReadingUrl = capturedReadingUrl;
    }

    public String getCapturedReading() {
        return capturedReading;
    }

    public void setCapturedReading(String capturedReading) {
        this.capturedReading = capturedReading;
    }

    public String getCapturedDate() {
        return capturedDate;
    }

    public void setCapturedDate(String capturedDate) {
        this.capturedDate = capturedDate;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String mKey) {
        this.mKey = mKey;
    }
}
