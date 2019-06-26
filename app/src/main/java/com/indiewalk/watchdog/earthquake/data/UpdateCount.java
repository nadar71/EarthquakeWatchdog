package com.indiewalk.watchdog.earthquake.data;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "QUERY_REQUEST_COUNT")
public class UpdateCount {
    @PrimaryKey(autoGenerate = true)
    private int id;

    // update
    private int num_queries;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNum_queries() {
        return num_queries;
    }

    public void setNum_queries(int num_queries) {
        this.num_queries = num_queries;
    }

    public void reset_num_queries() {
        this.num_queries = 0;
    }

    public void increment_num_queries() {
        this.num_queries += 1;
    }
}
