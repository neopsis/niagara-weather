/*
 * @(#)AccuWeatherBaseReader.java   23.11.2018
 *
 * Copyright (c) 2007 Neopsis GmbH
 *
 *
 */



package com.neopsis.envas.weather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.baja.nre.util.TextUtil;

/**
 * Basic abstract reader
 *
 */
public abstract class NvBaseReader {

    public final String read(String link) {

        InputStream inputStream;

        try {

            link = TextUtil.replace(link, " ", "%20");

            HttpURLConnection conn = (HttpURLConnection) (new URL(link)).openConnection();

            // We need to check for the http status using getResponseCode() to decide
            // if we should use getInputStream() or getErrorStream().
            int rspCode = conn.getResponseCode();

            if (rspCode == 200) {
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
            }

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

            NvLog.error("Error opening link " + link, exception);

            return null;
        }
    }
}
