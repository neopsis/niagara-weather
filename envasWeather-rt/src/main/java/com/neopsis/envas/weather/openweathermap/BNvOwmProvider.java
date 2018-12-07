/*
 * @(#)BNvOwmProvider.java   26.11.2018
 *
 * Copyright (c) 2007 Neopsis GmbH
 *
 *
 */



package com.neopsis.envas.weather.openweathermap;

import com.neopsis.envas.weather.util.NvLog;

import javax.baja.status.BStatus;
import javax.baja.status.BStatusValue;
import javax.baja.sys.*;
import javax.baja.weather.*;
import java.util.ArrayList;

/**
 * Envas Open Weather Maps Provider
 *
 */
public class BNvOwmProvider extends BWeatherProvider {

    /*-
    class BNvOwmProvider
    {
           properties
           {
                location: String
                     default {[ "" ]}

                apiKey: String
                     default {[ "" ]}

                faultCause: String
                     default {[ "" ]}

           }
    }
    -*/

    /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
    /*@ $com.neopsis.envas.weather.openweathermap.BNvOwmProvider(3353605412)1.0$ @*/
    /* Generated Fri Dec 07 00:57:28 CET 2018 by Slot-o-Matic 2000 (c) Tridium, Inc. 2000 */

    ////////////////////////////////////////////////////////////////
    // Property "location"
    ////////////////////////////////////////////////////////////////

    /**
     * Slot for the <code>location</code> property.
     * @see BNvOwmProvider#getLocation
     * @see BNvOwmProvider#setLocation
     */
    public static final Property location = newProperty(0, "", null);

    /**
     * Get the <code>location</code> property.
     * @see BNvOwmProvider#location
     */
    public String getLocation() {
        return getString(location);
    }

    /**
     * Set the <code>location</code> property.
     * @see BNvOwmProvider#location
     */
    public void setLocation(String v) {
        setString(location, v, null);
    }

    ////////////////////////////////////////////////////////////////
    // Property "apiKey"
    ////////////////////////////////////////////////////////////////

    /**
     * Slot for the <code>apiKey</code> property.
     * @see BNvOwmProvider#getApiKey
     * @see BNvOwmProvider#setApiKey
     */
    public static final Property apiKey = newProperty(0, "", null);

    /**
     * Get the <code>apiKey</code> property.
     * @see BNvOwmProvider#apiKey
     */
    public String getApiKey() {
        return getString(apiKey);
    }

    /**
     * Set the <code>apiKey</code> property.
     * @see BNvOwmProvider#apiKey
     */
    public void setApiKey(String v) {
        setString(apiKey, v, null);
    }

    ////////////////////////////////////////////////////////////////
    // Property "faultCause"
    ////////////////////////////////////////////////////////////////

    /**
     * Slot for the <code>faultCause</code> property.
     * @see BNvOwmProvider#getFaultCause
     * @see BNvOwmProvider#setFaultCause
     */
    public static final Property faultCause = newProperty(0, "", null);

    /**
     * Get the <code>faultCause</code> property.
     * @see BNvOwmProvider#faultCause
     */
    public String getFaultCause() {
        return getString(faultCause);
    }

    /**
     * Set the <code>faultCause</code> property.
     * @see BNvOwmProvider#faultCause
     */
    public void setFaultCause(String v) {
        setString(faultCause, v, null);
    }

    ////////////////////////////////////////////////////////////////
    // Type
    ////////////////////////////////////////////////////////////////
    public Type getType() {
        return TYPE;
    }

    public static final Type TYPE = Sys.loadType(BNvOwmProvider.class);

    /*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/

    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Updates report components. Called periodically from the timer.
     */
    public void updateReport() {

        BWeatherReport     weatherReport = (BWeatherReport) getParent();
        NvOwmReader        weatherReader = new NvOwmReader();
        ArrayList          forecasts     = new ArrayList();
        BCurrentConditions cond          = weatherReport.getCurrent();
        BSunPosition       sunPosition   = new BSunPosition();
        BForecast[]        forecastArr   = new BForecast[0];

        try {

            weatherReader.getCurrentConditions(cond, sunPosition, getLocation(), getApiKey());
            weatherReader.getForecast(forecasts, getLocation(), getApiKey());
            forecastArr = (BForecast[]) forecasts.toArray(new BForecast[forecasts.size()]);

            for (int j = 0; j < forecastArr.length; j++) {

                BForecast forecast = forecastArr[j];

                sunPosition.doRecalculate(forecast.getDate());
                forecast.setSunrise(BTime.make(sunPosition.getSunrise()));
                forecast.setSunset(BTime.make(sunPosition.getSunset()));
            }

            weatherReport.setStatus(BStatus.ok);
            this.setFaultCause("");

        } catch (NvOwmException ex) {

            // ooops, anything was wrong
            NvLog.error("Read forecast failed: " + ex.getMessage());
            weatherReport.setStatus(ex.getStatus());
            this.setFaultCause(ex.getMessage());
        }

        weatherReport.setForecast(forecastArr);
    }

    /**
     * Sets the overall component status, traverses down over all component children
     *
     * @param comp     report component (Current, Today, Tomorrow, ....)
     * @param isDown   true if service is down
     * @param isFault  status to propagate
     */
    static void setStatus(BComponent comp, boolean isDown, boolean isFault) {

        BStatus status = (BStatus) comp.get("status");

        if (status != null) {

            int i = status.getBits();
            int j = i;

            if (isDown) {
                j |= 4;
            } else {
                j &= -5;
            }

            if (isFault) {
                j |= 2;
            } else {
                j &= -3;
            }

            if (i != j) {
                comp.set("status", BStatus.make(j));
            }
        }

        for (SlotCursor slotcursor = comp.getSlots(); slotcursor.next(); ) {

            if (slotcursor.get() instanceof BStatusValue) {

                BStatusValue statusValue = (BStatusValue) slotcursor.get();
                int          k           = statusValue.getStatus().getBits();
                int          l           = k;

                if (isDown) {
                    l |= 4;
                } else {
                    l &= -5;
                }

                if (isFault) {
                    l |= 2;
                } else {
                    l &= -3;
                }

                if (k != l) {
                    statusValue.setStatus(BStatus.make(l));
                }
            }
        }
    }

    /**
     * Reports the service provider and the location
     */
    public String toString(Context context) {

        StringBuffer stringbuffer = new StringBuffer();

        stringbuffer.append("Open Weather Map Service: ").append(getLocation());

        return stringbuffer.toString();
    }
}
