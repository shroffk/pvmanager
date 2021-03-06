/**
 * Copyright (C) 2010-14 pvmanager developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.pvmanager.test;

import org.epics.pvmanager.PVReaderEvent;
import org.epics.pvmanager.PVReaderListener;

/**
 *
 * @author carcassi
 */
public class CounterTestListener implements PVReaderListener<Integer> {

    private volatile int nextExpected = 0;
    private volatile boolean failed;

    @Override
    public void pvChanged(PVReaderEvent<Integer> event) {
        if (event.getPvReader().getValue() == null) {
            System.out.println("Fail: expected " + nextExpected + " was null");
            failed = true;
        } else if (event.getPvReader().getValue() != nextExpected) {
            System.out.println("Fail: expected " + nextExpected + " was " + event.getPvReader().getValue());
            failed = true;
        }
        nextExpected++;
    }

    public boolean isFailed() {
        return failed;
    }

    public int getNextExpected() {
        return nextExpected;
    }
    
    
}
