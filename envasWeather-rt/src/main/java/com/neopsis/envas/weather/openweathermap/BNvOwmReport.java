/*
 * @(#)BAccuWeatherReport.java   10.04.12
 *
 * Copyright (c) 2007 Neopsis GmbH
 *
 *
 */



package com.neopsis.envas.weather.openweathermap;

import javax.baja.sys.Sys;
import javax.baja.sys.Type;
import javax.baja.weather.BWeatherReport;

/**
 * Weather report container for Open Weather Map stream
 *
 */
public class BNvOwmReport extends BWeatherReport {

    /*-
       class BNvOwmReport
       {
       }
    -*/

    /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
    /*@ $com.neopsis.envas.weather.openweathermap.BNvOwmReport(2672460077)1.0$ @*/
    /* Generated Fri Dec 07 02:56:40 CET 2018 by Slot-o-Matic (c) Tridium, Inc. 2012 */

    ////////////////////////////////////////////////////////////////
    // Type
    ////////////////////////////////////////////////////////////////
    @Override
    public Type getType() {
        return TYPE;
    }

    public static final Type TYPE = Sys.loadType(BNvOwmReport.class);

    /*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

    /**
     * Constructor. Set up the weather report provider
     */
    public BNvOwmReport() {
        setProvider(new BNvOwmProvider());
    }
}
