/*
 * @(#)NvOwmException.java   07.12.2018
 *
 * Copyright (c) 2007 Neopsis GmbH
 *
 *
 */



package com.neopsis.envas.weather.openweathermap;

import javax.baja.status.BStatus;

/**
 * Exception reports service status (fault, down)
 *
 *
 * @version        1.0.0, 06.12.2018
 * @author         Robert Carnecky
 */
public class NvOwmException extends Exception {

    private BStatus status;

    public NvOwmException(String message, BStatus stat) {

        super(message);
        status = stat;
    }

    public BStatus getStatus() {
        return status;
    }
}
