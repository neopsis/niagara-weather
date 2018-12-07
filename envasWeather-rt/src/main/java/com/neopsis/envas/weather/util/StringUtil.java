/*
 * @(#)StringUtil.java   10.04.12
 *
 * Copyright (c) 2007 Neopsis GmbH
 *
 *
 */



package com.neopsis.envas.weather.util;

/**
 * String Support Class
 *
 *
 */
public class StringUtil {

    public static boolean isNumeric(String value) {

        try {

            Double.parseDouble(value);

            return true;

        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean isNumeric(char value) {
        return Character.isDigit(value);
    }

    public static String fillString(int len, String ch) {
        return fillString(len, ch.charAt(0));
    }

    public static String fillString(int len, char ch) {

        StringBuffer buf = new StringBuffer(len);

        for (int i = 0; i < len; i++) {
            buf.append(ch);
        }

        return buf.toString();
    }
}
