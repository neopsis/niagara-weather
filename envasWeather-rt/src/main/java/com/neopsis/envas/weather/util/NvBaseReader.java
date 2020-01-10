/*
 * @(#)AccuWeatherBaseReader.java   23.11.2018
 *
 * Copyright (c) 2007 Neopsis GmbH
 *
 *
 */



package com.neopsis.envas.weather.util;

import javax.baja.naming.BIpHost;
import javax.baja.net.HttpConnection;
import javax.baja.nre.util.TextUtil;
import javax.baja.sys.BRelTime;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Basic abstract reader
 *
 */
public abstract class NvBaseReader {

    public static final String HOSTNAME = "api.openweathermap.org";
    public static final int    PORT     = 80;

    public final String read(String link) {

        InputStream inputStream;

        try {

            link = TextUtil.replace(link, " ", "%20");

            HttpConnection conn = (HttpConnection) new HttpConnection(new BIpHost(HOSTNAME), PORT, link);

            conn.setTimeout((int) BRelTime.makeSeconds(30).getMillis());
            conn.connect();
            inputStream = conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuffer   sb = new StringBuffer();

            String         line;

            while ((line = br.readLine()) != null) {

                sb.append(line);

                // System.out.println(line);
            }

            br.close();

            return sb.toString();

        } catch (Exception exception) {

            NvLog.error("Error opening link " + HOSTNAME + link, exception);

            return null;
        }
    }
}
