package com.example.grigorijsemykin.junien_seuranta;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by Grigorij Semykin on 11.12.2017.
 */

@Dao
public interface RouteDao {

    @Query("SELECT * FROM route")
    List<Route> getAll();

    @Insert
    void insertAll(Route... route);

    @Delete
    void delete(Route route);
}
