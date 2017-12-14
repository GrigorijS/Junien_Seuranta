package com.example.grigorijsemykin.junien_seuranta;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Grigorij Semykin on 11.12.2017.
 */

@Entity
public class Route {

    @PrimaryKey
    public int rId;

    @ColumnInfo(name = "departureStation")
    public String depStation;

    @ColumnInfo(name = "arrivalStation")
    public String arrStation;

    public int getrId() {
        return rId;
    }

    public void setrId(int rId) {
        this.rId = rId;
    }

    public String getDepStation() {
        return depStation;
    }

    public void setDepStation(String depStation) {
        this.depStation = depStation;
    }

    public String getArrStation() {
        return arrStation;
    }

    public void setArrStation(String arrStation) {
        this.arrStation = arrStation;
    }
}

