/*
 * @(#)WeatherLog.java   10.04.12
 *
 * Copyright (c) 2007 Neopsis GmbH
 *
 *
 */



package com.neopsis.envas.weather.util;

import javax.baja.log.Log;

/**
 * Logging Utility
 *
 *
 * @version    1.0.0
 * @author     Robert Carnecky
 */
public class NvLog {

    public static final Log log = Log.getLog("neoWeather");

    public static void trace(String message) {

        if (log.isTraceOn()) {
            log.trace(message);
        }
    }

    public static void message(String message) {
        log.message(message);
    }

    public static void warning(String message) {
        log.warning(message);
    }

    public static void error(String message) {
        log.error(message);
    }

    public static void error(String message, Exception ex) {
        log.error(message, ex);
    }
}
