/*
 * Copyright 2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */

package gov.bnl.pvmanager.epics;

import gov.bnl.pvmanager.TimeStamp;

/**
 * Time information.
 *
 * @author carcassi
 */
public interface Time {
    TimeStamp getTimeStamp();
}