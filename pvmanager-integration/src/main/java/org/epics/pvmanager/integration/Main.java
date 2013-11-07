/**
 * Copyright (C) 2010-12 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager.integration;

import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReader;
import org.epics.pvmanager.jca.JCADataSource;
import static org.epics.pvmanager.ExpressionLanguage.*;
import org.epics.pvmanager.PVReaderEvent;
import org.epics.pvmanager.PVReaderListener;
import static org.epics.pvmanager.vtype.ExpressionLanguage.*;
import org.epics.util.time.TimeDuration;
import org.epics.util.time.Timestamp;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.Time;
import static org.epics.vtype.ValueFactory.*;

/**
 *
 * @author carcassi
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        PVManager.setDefaultDataSource(new JCADataSource());
        
        PVReaderTestListener<Object> listener = PVReaderTestListener.matchConnections(true);
        PVReaderTestListener<Object> valueListener = PVReaderTestListener.matchValues(newVDouble(0.13, newAlarm(AlarmSeverity.INVALID, "UDF_ALARM"), newTime(Timestamp.of(631152000, 0), null, false), displayNone()));
        
        PVReader<Object> pvReader = PVManager.read(channel("passive_double"))
                .readListener(new PVReaderListener<Object>() {

            @Override
            public void pvChanged(PVReaderEvent<Object> event) {
                System.out.println(event.getPvReader().getValue());
            }
        }).readListener(listener).readListener(valueListener).maxRate(TimeDuration.ofHertz(50));
        
        Thread.sleep(1000);
       
        pvReader.close();
        listener.close();
        valueListener.close();
        
        if (!valueListener.isSuccess()) {
            System.out.println(valueListener.getErrorMessage());
        }
        
        if (!listener.isSuccess()) {
            System.out.println(listener.getErrorMessage());
        }
        
        Thread.sleep(100);
        
        PVManager.getDefaultDataSource().close();
        
        
    }
}
