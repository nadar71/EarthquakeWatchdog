package com.indiewalk.watchdog.earthquake.util;


import android.net.Uri;

import com.indiewalk.watchdog.earthquake.util.MyUtil;

import junit.framework.Assert;

import org.junit.Test;

import org.junit.Assert.*;

// TODO
public class MyUtilTest {

    @Test
    public void composeQueryUrl_test() {
        String url = MyUtil.composeQueryUrl("0");
        System.out.println(url);
    }

    @Test
    public void fromDegreeToRadiant_test() {
        double res = MyUtil.fromDegreeToRadiant(180);
        Assert.assertEquals(3.141592653589793,res,0);

        res = MyUtil.fromDegreeToRadiant(0);
        Assert.assertEquals(0.0,res,0);

        res = MyUtil.fromDegreeToRadiant(360);
        Assert.assertEquals(6.283185307179586,res,0);

    }

    @Test
    public void haversineDistanceCalc_test() {
        double res = MyUtil.haversineDistanceCalc(0.0,0.0,0.0,0.0);
        Assert.assertEquals(0.0,res,0);

        res = MyUtil.haversineDistanceCalc(0.0,0.0,1.0,1.0);
        Assert.assertEquals(0.0,res,0);

        res = MyUtil.haversineDistanceCalc(10.0,100.123,112.0,14.55);
        Assert.assertEquals(8777.477194552577,res,0);

    }

    @Test
    public void fromKmToMiles_test() {
        double res = MyUtil.fromKmToMiles(100);
        Assert.assertEquals(62.1371192,res,0);

        res = MyUtil.fromKmToMiles(1);
        Assert.assertEquals(0.621371192,res,0);

        res = MyUtil.fromKmToMiles(1221);
        Assert.assertEquals(758.694225432,res,0);
    }

    @Test
    public void formatDateFromMsec_test() {
        String date = MyUtil.formatDateFromMsec(54597600000L);
        Assert.assertEquals("set 25, 1971",date);

        date = MyUtil.formatDateFromMsec(-119494800000L);
        Assert.assertEquals("mar 20, 1966",date);

        date = MyUtil.formatDateFromMsec(1243980000000L);
        Assert.assertEquals("giu 03, 2009",date);
    }

    @Test
    public void formatTimeFromMsec_test() {
        String time = MyUtil.formatTimeFromMsec(54597600000L);
        Assert.assertEquals("12:00 AM",time);

        time = MyUtil.formatTimeFromMsec(1237525200000L);
        Assert.assertEquals("12:00 AM",time);

        time = MyUtil.formatTimeFromMsec(1244025240000L);
        Assert.assertEquals("giu 03, 2009",time);
    }



    @Test
    public void returnDigit_test() {
        String dig = MyUtil.returnDigit("depth 123.33");
        Assert.assertEquals("123.33",dig);

        dig = MyUtil.returnDigit("distance 234.654");
        Assert.assertEquals("234.654",dig);
    }

    @Test
    public void returnChar_test() {
        String dig = MyUtil.returnChar("depth 123.33");
        Assert.assertEquals("depth .",dig);

        dig = MyUtil.returnChar("distance 234.654");
        Assert.assertEquals("distance .",dig);
    }

    @Test
    public void oldDate_test() {
        String date = MyUtil.oldDate(2);
        Assert.assertEquals("2019-11-06 ",date);

    }




}