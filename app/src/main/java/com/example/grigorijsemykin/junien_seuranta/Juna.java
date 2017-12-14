package com.example.grigorijsemykin.junien_seuranta;

/**
 * Created by Grigorij Semykin on 6.12.2017.
 */

public class Juna {
    public Juna(){}

    private String depTime, track, trainLetter, destination;

    public Juna(String depTime, String track, String trainLetter, String destination) {
        this.depTime = depTime;
        this.track = track;
        this.trainLetter = trainLetter;
        this.destination = destination;
    }

    public String getDepTime() {
        return depTime;
    }

    public void setDepTime(String depTime) {
        this.depTime = depTime;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getTrainLetter() {
        return trainLetter;
    }

    public void setTrainLetter(String trainLetter) {
        this.trainLetter = trainLetter;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
