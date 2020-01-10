/*
 * @(#)NvOwmReader.java   06.12.2018
 *
 * Copyright (c) 2007 Neopsis GmbH
 *
 *
 */



package com.neopsis.envas.weather.openweathermap;

import com.neopsis.envas.weather.util.NvBaseReader;

import com.tridium.json.JSONArray;
import com.tridium.json.JSONException;
import com.tridium.json.JSONObject;

import javax.baja.status.BStatus;
import javax.baja.status.BStatusEnum;
import javax.baja.status.BStatusNumeric;
import javax.baja.status.BStatusString;
import javax.baja.sys.BAbsTime;
import javax.baja.sys.BDouble;
import javax.baja.sys.BEnum;
import javax.baja.sys.BTime;
import javax.baja.weather.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

/**
 * Weather reader for Open Weather Map service provider.
 *
 */
public class NvOwmReader extends NvBaseReader {

    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

    /**
     * Reading forecast from Open Weather Map.  Read current conditions from Open Weather Map.
     * Please note, we are always reading imperial units. The conversion occurs in the Niagara Workbench
     *
     * @param forecasts  empty list of forecasts passed from the calling routine
     * @param location   location as 'city' or 'city,country_code'
     * @param appid      individual customer application id
     * @throws Exception
     */
    public void getForecast(ArrayList forecasts, String location, String appid) throws NvOwmException {

        BForecast fc   = null;
        String    link = "/data/2.5/forecast?q=" + location + "&mode=json&units=imperial&appid=" + appid;
        String    json = read(link);

        try {

            if (json != null) {

                JSONObject forecast     = new JSONObject(json);
                JSONArray  items        = forecast.optJSONArray("list");
                BDouble    minTemp      = BDouble.make(1000.0d);
                BDouble    maxTemp      = BDouble.make(-1000.0d);
                Date       forecastDate = new Date();
                int        cod          = forecast.getInt("cod");
                boolean    gotIt        = false;

                fc = new BForecast();

                if (cod != 200) {

                    String errMsg = forecast.optString("message");

                    throw new NvOwmException("API error code=" + cod + "  " + errMsg, BStatus.fault);
                }

                if (items == null) {
                    throw new NvOwmException("Open Weather Map returned no forecast", BStatus.down);
                }

                for (int i = 0; i < items.length(); i++) {

                    JSONObject item          = items.getJSONObject(i);
                    Date       itemDate      = new Date(item.optLong("dt") * 1000);
                    long       itemDateJ     = Long.valueOf(dayFormat.format(itemDate)).longValue();
                    long       forecastDateJ = Long.valueOf(dayFormat.format(forecastDate)).longValue();

                    if (itemDateJ > forecastDateJ) {

                        // day break, save hi/lo values, status and initialize next day
                        sn(fc.getHigh(), maxTemp);
                        sn(fc.getLow(), minTemp);
                        fc.setLastUpdate(BAbsTime.now());
                        BNvOwmProvider.setStatus(fc, false, false);
                        forecasts.add(fc);

                        // create new fresh set
                        fc           = new BForecast();
                        forecastDate = itemDate;
                        minTemp      = BDouble.make(1000.0d);
                        maxTemp      = BDouble.make(-1000.0d);
                        gotIt        = false;
                    }

                    // get data from noon, e.g. a measure between 11 ... 15
                    if (((itemDate.getHours() > 10) && (itemDate.getHours() < 14)) ||!gotIt) {

                        fc.setDate(BAbsTime.make(itemDate.getTime()));

                        JSONObject main = item.optJSONObject("main");

                        if (main != null) {
                            sn(fc.getHumidity(), BDouble.make(main.optDouble("humidity")));
                        }

                        JSONObject wind = item.optJSONObject("wind");

                        if (wind != null) {
                            sn(fc.getWindSpeed(), BDouble.make(wind.optDouble("speed")));
                        }

                        JSONArray weatherList = item.optJSONArray("weather");

                        setState(fc.getState(), fc.getWeatherSummary(), weatherList);
                        gotIt = true;
                    }

                    JSONObject mainTemp = item.optJSONObject("main");

                    if (mainTemp != null) {

                        minTemp = BDouble.make(Math.min(minTemp.getDouble(), mainTemp.optDouble("temp")));
                        maxTemp = BDouble.make(Math.max(maxTemp.getDouble(), mainTemp.optDouble("temp")));
                    }
                }

                fc.setLastUpdate(BAbsTime.now());
                sn(fc.getHigh(), maxTemp);
                sn(fc.getLow(), minTemp);
                BNvOwmProvider.setStatus(fc, false, false);
                forecasts.add(fc);

            } else {

                BNvOwmProvider.setStatus(fc, true, false);

                throw new NvOwmException("Open Weather Map returned no forecast data", BStatus.down);
            }

        } catch (JSONException ex) {
            throw new NvOwmException("Cannot parse response " + json + "[" + ex.getMessage() + "]", BStatus.fault);
        } catch (Exception ex) {

            if (fc != null) {
                BNvOwmProvider.setStatus(fc, false, true);
            }

            throw new NvOwmException(ex.getMessage(), BStatus.fault);
        }
    }

    /**
     * Read current conditions from Open Weather Map. Please note, we are always reading
     * imperial units. The conversion occurs in the Niagara Workbench
     *
     * @param cond      current conditions (per reference)
     * @param location  location string (e.g. "London,UK")
     * @param appid     application id
     */
    public void getCurrentConditions(BCurrentConditions cond, BSunPosition sunPos, String location, String appid) throws NvOwmException {

        String link = "/data/2.5/weather?q=" + location + "&mode=json&units=imperial&appid=" + appid;

        try {

            String json = read(link);

            if (json != null) {

                JSONObject current = new JSONObject(json);
                int        cod     = current.getInt("cod");

                if (cod != 200) {

                    String errMsg = current.optString("message");

                    throw new NvOwmException("API error code=" + cod + "  " + errMsg, BStatus.fault);
                }

                JSONObject coord = current.getJSONObject("coord");

                sunPos.setLatitude(coord.getDouble("lat"));
                sunPos.setLongitude(coord.getDouble("lon"));
                cond.setSun(sunPos.getLongitude(), sunPos.getLatitude());

                JSONObject main = current.getJSONObject("main");

                // current weather
                if (main != null) {

                    sn(cond.getPressure(), BDouble.make(main.optDouble("pressure")));
                    sn(cond.getTemp(), BDouble.make(main.optDouble("temp")));
                    sn(cond.getHumidity(), BDouble.make(main.optDouble("humidity")));
                }

                JSONObject wind = current.getJSONObject("wind");

                if (wind != null) {

                    sn(cond.getWindSpeed(), BDouble.make(wind.optDouble("speed")));
                    sn(cond.getWindGust(), BDouble.make(wind.optDouble("gust")));
                }

                sn(cond.getVisibility(), BDouble.make(current.optDouble("visibility")));

                BDouble windDir = BDouble.make(wind.optDouble("deg"));

                if (!windDir.equals(BDouble.NaN)) {
                    se(cond.getWindDirection(), BWindDirection.makeDegrees(windDir.getInt()));
                }

                sn(cond.getWindChill(), BDouble.NaN);
                sn(cond.getHeatIndex(), BDouble.NaN);
                sn(cond.getDewPoint(), BDouble.NaN);

                JSONObject sys = current.optJSONObject("sys");

                if (sys != null) {

                    long sunrise = sys.optLong("sunrise") * 1000;
                    long sunset  = sys.optLong("sunset") * 1000;

                    if ((sunrise != 0) && (sunset != 0)) {

                        BAbsTime sunriseTime = BAbsTime.make(sunrise);
                        BAbsTime sunsetTime  = BAbsTime.make(sunset);

                        cond.setSunrise(BTime.make(sunriseTime));
                        cond.setSunset(BTime.make(sunsetTime));
                    }
                }

                JSONArray weatherList = current.optJSONArray("weather");

                setState(cond.getState(), cond.getWeatherSummary(), weatherList);
                cond.setLastUpdate(BAbsTime.now());
                BNvOwmProvider.setStatus(cond, false, false);

            } else {

                BNvOwmProvider.setStatus(cond, true, false);

                throw new NvOwmException("Open Weather Map returned no current data", BStatus.down);
            }

        } catch (Exception e) {

            BNvOwmProvider.setStatus(cond, false, true);

            throw new NvOwmException(e.getMessage(), BStatus.fault);
        }
    }

    /**
     * Convert the double value to BStatusNumeric and sets the status
     *
     * @param statusNumeric
     * @param val
     * @throws Exception
     */
    private final void sn(BStatusNumeric statusNumeric, BDouble val) throws Exception {

        if (val.equals(BDouble.NaN)) {

            statusNumeric = null;

            return;
        }

        int i = statusNumeric.getStatus().getBits();
        int j = i;

        try {

            statusNumeric.setValue(val.getDouble());
            j &= -65;

        } catch (Exception exception) {
            j |= 64;
        }

        if (i != j) {
            statusNumeric.setStatus(BStatus.make(j));
        }
    }

    /**
     * Convert the string value to BStatusNumeric and sets the status
     *
     * @param statusNumeric
     * @param s
     * @throws Exception
     */
    private final void sn(BStatusNumeric statusNumeric, String s) throws Exception {

        int i = statusNumeric.getStatus().getBits();
        int j = i;

        try {

            statusNumeric.setValue(Double.parseDouble(s));
            j &= -65;

        } catch (Exception exception) {
            j |= 64;
        }

        if (i != j) {
            statusNumeric.setStatus(BStatus.make(j));
        }
    }

    /**
     * Set the weather state (enumeration and plain text summary)
     *
     * @param statusEnum   status as enumeration
     * @param summary      status as plain text
     * @param weatherList  array of weather JSONObjects
     * @throws Exception   should not throw any
     */
    private final void setState(BStatusEnum statusEnum, BStatusString summary, JSONArray weatherList) throws Exception {

        if (weatherList == null) {

            se(statusEnum, BWeatherState.unknown);
            ss(summary, "Unknown");

            return;
        }

        JSONObject weather = weatherList.getJSONObject(0);
        String     state   = weather.optString("description", "");

        if (state != null) {
            ss(summary, state);
        }

        int weatherId = weather.getInt("id");

        switch (weatherId) {

        case 200 :
        case 201 :
        case 202 :
        case 210 :
        case 211 :
        case 212 :
        case 221 :
        case 222 :
        case 223 :
            se(statusEnum, BWeatherState.thunderstorms);

            break;

        case 300 :
        case 301 :
        case 302 :
        case 310 :
        case 311 :
        case 312 :
        case 313 :
        case 314 :
        case 321 :
            se(statusEnum, BWeatherState.lightRain);

            break;

        case 500 :
        case 501 :
            se(statusEnum, BWeatherState.lightRain);

            break;

        case 502 :
        case 503 :
        case 504 :
        case 522 :
            se(statusEnum, BWeatherState.heavyRain);

            break;

        case 520 :
        case 521 :
        case 531 :
        case 611 :
        case 612 :
        case 615 :
        case 616 :
            se(statusEnum, BWeatherState.rain);

            break;

        case 511 :
            se(statusEnum, BWeatherState.freezingRain);

            break;

        case 600 :
        case 601 :
        case 602 :
        case 620 :
        case 621 :
        case 622 :
            se(statusEnum, BWeatherState.snow);

            break;

        case 800 :
            se(statusEnum, BWeatherState.sunny);

            break;

        case 801 :
            se(statusEnum, BWeatherState.partlySunny);

            break;

        case 802 :
            se(statusEnum, BWeatherState.partlyCloudy);

            break;

        case 803 :
            se(statusEnum, BWeatherState.mostlyCloudy);

            break;

        case 804 :
            se(statusEnum, BWeatherState.overcast);

            break;

        case 701 :
            se(statusEnum, BWeatherState.misty);

            break;

        case 741 :
            se(statusEnum, BWeatherState.fog);

            break;

        case 721 :
            se(statusEnum, BWeatherState.haze);

            break;

        case 761 :
            se(statusEnum, BWeatherState.dust);

            break;

        case 762 :
            se(statusEnum, BWeatherState.volcano);

            break;

        case 781 :
            se(statusEnum, BWeatherState.tornado);

            break;

        case 771 :
            se(statusEnum, BWeatherState.thunderstorms);

            break;

        default :
            se(statusEnum, null);
        }
    }

    /**
     * Convert BEnum to BStatusEnum and sets the status
     *
     * @param statusEnum
     * @param anEnum
     * @throws Exception
     */
    private final void se(BStatusEnum statusEnum, BEnum anEnum) throws Exception {

        int i = statusEnum.getStatus().getBits();
        int j = i;

        if (anEnum != null) {

            statusEnum.setValue(anEnum);
            j &= -65;

        } else {
            j |= 64;
        }

        if (i != j) {
            statusEnum.setStatus(BStatus.make(j));
        }
    }

    /**
     * Convert String to BStatusString
     *
     * @param statusString
     * @param str
     * @throws Exception
     */
    private final void ss(BStatusString statusString, String str) throws Exception {

        int i = statusString.getStatus().getBits();
        int j = i;

        if (str != null) {

            statusString.setValue(str);
            j &= -65;

        } else {
            j |= 64;
        }

        if (i != j) {
            statusString.setStatus(BStatus.make(j));
        }
    }
}
