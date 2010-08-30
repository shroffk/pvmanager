/*
 * Copyright 2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */

package org.epics.pvmanager.data;

/**
 * Scalar enum with alarm and timestamp.
 *
 * @author carcassi
 */
public interface VEnum extends Scalar<String>, Enum, Alarm, Time {
}