/**
 * Copyright (C) 2010-14 pvmanager developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.pvmanager.test;

import java.util.Objects;
import org.epics.pvmanager.DataSource;
import static org.epics.pvmanager.ExpressionLanguage.*;
import org.epics.pvmanager.PV;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReader;
import org.epics.pvmanager.PVReaderEvent;
import org.epics.pvmanager.PVReaderListener;
import org.epics.pvmanager.PVWriter;
import org.epics.pvmanager.TimeoutException;
import org.epics.util.time.TimeDuration;
import static org.epics.util.time.TimeDuration.*;
import org.epics.util.time.TimeInterval;
import org.epics.util.time.Timestamp;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author carcassi
 */
public class TestDataSourceTest {
    
    public TestDataSourceTest() {
    }
    
    public static void waitForChannelToClose(DataSource source, String channelName) {
        TimeDuration timeout = ofMillis(5000);
        TimeInterval timeoutInterval = timeout.after(Timestamp.now());
        while (timeoutInterval.contains(Timestamp.now())) {
            if (source.getChannels().get(channelName) == null || !source.getChannels().get(channelName).isConnected()) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch(Exception ex) {
                
            }
        }
        fail("Channel " + channelName + " didn't close after 5 seconds");
    }
    
    private DataSource dataSource;
    
    PV<Object, Object> pv;
    PVReader<Object> pvReader;
    PVReader<Object> pvReader2;
    PVWriter<Object> pvWriter;

    @Before
    public void setupDataSource() throws Exception {
        dataSource = new TestDataSource();
    }
    
    @After
    public void closePVsAndDataSource() {
        if (pv != null) {
            pv.close();
            pv = null;
        }
        if (pvReader != null) {
            pvReader.close();
            pvReader = null;
        }
        if (pvReader2 != null) {
            pvReader2.close();
            pvReader2 = null;
        }
        if (pvWriter != null) {
            pvWriter.close();
            pvWriter = null;
        }

        waitForChannelToClose(dataSource, "delayedWrite");
        waitForChannelToClose(dataSource, "delayedConnection");
        dataSource.close();
        dataSource = null;
    }
    
    @Test
    public void channelDoesNotExist1() throws Exception {
        // Requesting a channel that does not exist
        // Making sure that the exception is properly notified
        CountDownPVReaderListener listener = new CountDownPVReaderListener(1);
        pvReader = PVManager.read(channel("nothing"))
                .readListener(listener)
                .from(dataSource).maxRate(ofMillis(10));
        
        listener.await(TimeDuration.ofMillis(100));
        
        RuntimeException ex = (RuntimeException) pvReader.lastException();
        assertThat(ex, not(nullValue()));
    }
    
    @Test
    public void channelDoesNotExist2() throws Exception {
        // Requesting a channel that does not exist
        // Making sure that the exception is properly notified
        CountDownPVWriterListener<Object> listener = new CountDownPVWriterListener<>(1);
        pvWriter = PVManager.write(channel("nothing"))
                .writeListener(listener)
                .from(dataSource).async();

        listener.await(TimeDuration.ofMillis(100));
        
        RuntimeException ex = (RuntimeException) pvWriter.lastWriteException();
        assertThat(ex, not(nullValue()));
    }
    
    @Test
    public void channelDoesNotExist3() throws Exception {
        // Requesting a channel that does not exist
        // Making sure that the exception is routed
        CountDownWriteFunction exceptionHandler = new CountDownWriteFunction(1);
        pvReader = PVManager.read(channel("nothing"))
                .routeExceptionsTo(exceptionHandler)
                .from(dataSource).maxRate(ofMillis(10));

        exceptionHandler.await(TimeDuration.ofMillis(100));
        
        RuntimeException ex = (RuntimeException) exceptionHandler.getException();
        assertThat(ex, not(nullValue()));
    }
    
    @Test
    public void channelDoesNotExist4() throws Exception {
        // Requesting a channel that does not exist
        // Making sure that the exception is properly notified
        CountDownWriteFunction exceptionHandler = new CountDownWriteFunction(1);
        pvWriter = PVManager.write(channel("nothing"))
                .routeExceptionsTo(exceptionHandler)
                .from(dataSource).async();

        exceptionHandler.await(TimeDuration.ofMillis(100));
        
        RuntimeException ex = (RuntimeException) exceptionHandler.getException();
        assertThat(ex, not(nullValue()));
    }
    
    @Test
    public void delayedWrite() throws Exception {
        CountDownPVWriterListener<Object> listener = new CountDownPVWriterListener<>(1);
        pvWriter = PVManager.write(channel("delayedWrite"))
                .writeListener(listener)
                .from(dataSource).async();
        pvWriter.write("test");

        listener.await(TimeDuration.ofMillis(15));
        assertThat(listener.getCount(), equalTo(1));
        
        RuntimeException ex = (RuntimeException) pvWriter.lastWriteException();
        assertThat(ex, nullValue());
        
        listener.await(TimeDuration.ofMillis(1100));
        assertThat(listener.getCount(), equalTo(0));
        listener.resetCount(1);
        
        ex = (RuntimeException) pvWriter.lastWriteException();
        assertThat(ex, nullValue());
    }
    
    @Test
    public void delayedWriteWithTimeout() throws Exception {
        CountDownPVWriterListener<Object> writerListener = new CountDownPVWriterListener<>(1);
        pvWriter = PVManager.write(channel("delayedWrite"))
                .writeListener(writerListener)
                .timeout(ofMillis(500)).from(dataSource).async();
        pvWriter.write("test");

        writerListener.await(TimeDuration.ofMillis(750));
        assertThat(writerListener.getCount(), equalTo(0));
        writerListener.resetCount(1);
        Exception ex = pvWriter.lastWriteException(); 
        assertThat(ex, not(nullValue()));
        assertThat(ex, instanceOf(TimeoutException.class));
        
        writerListener.await(TimeDuration.ofMillis(2000));
        assertThat(writerListener.getCount(), equalTo(0));
        ex = pvWriter.lastWriteException(); 
        assertThat(ex, nullValue());
    }
    
    @Test
    public void delayedWriteWithTimeout2() throws Exception {
        // Test a write that happens 2 seconds late
        // Checks whether we get a timeout beforehand
        CountDownPVWriterListener<Object> writerListener = new CountDownPVWriterListener<>(1);
        pvWriter = PVManager.write(channel("delayedWrite")).timeout(ofMillis(500))
                .writeListener(writerListener)
                .from(dataSource).async();
        pvWriter.write("test");

        // Wait for the first notification, should be the timeout
        writerListener.await(TimeDuration.ofMillis(600));
        assertThat(writerListener.getCount(), equalTo(0));
        writerListener.resetCount(1);
        
        Exception ex = pvWriter.lastWriteException(); 
        assertThat(ex, not(nullValue()));
        assertThat(ex, instanceOf(TimeoutException.class));

        // Wait for the second notification, should be
        // the success notification
        writerListener.await(TimeDuration.ofMillis(2000));
        assertThat(writerListener.getCount(), equalTo(0));
        writerListener.resetCount(1);
        
        ex = pvWriter.lastWriteException();
        assertThat(ex, nullValue());
        
        // Write again
        pvWriter.write("test2");
        
        // Wait for a notification: should not come
        writerListener.await(TimeDuration.ofMillis(400));
        assertThat(writerListener.getCount(), equalTo(1));
        ex = pvWriter.lastWriteException();
        assertThat(ex, nullValue());
        
        writerListener.await(TimeDuration.ofMillis(250));
        assertThat(writerListener.getCount(), equalTo(0));
        writerListener.resetCount(1);
        ex = pvWriter.lastWriteException();
        assertThat(ex, not(nullValue()));
        assertThat(ex, instanceOf(TimeoutException.class));
        
        writerListener.await(TimeDuration.ofMillis(2000));
        assertThat(writerListener.getCount(), equalTo(0));
        ex = pvWriter.lastWriteException();
        assertThat(ex, nullValue());
    }
    
    @Test
    public void delayedReadConnectionWithTimeout() throws Exception {
        CountDownPVReaderListener readListener = new CountDownPVReaderListener(1, PVReaderEvent.VALUE_MASK | PVReaderEvent.EXCEPTION_MASK);
        pvReader = PVManager.read(channel("delayedConnection")).timeout(ofMillis(500))
                .readListener(readListener)
                .from(dataSource).maxRate(ofMillis(50));
        
        readListener.await(TimeDuration.ofMillis(50));
        assertThat(readListener.getCount(), equalTo(1));
        
        TimeoutException ex = (TimeoutException) pvReader.lastException();
        assertThat(ex, nullValue());
        
        readListener.await(TimeDuration.ofMillis(600));
        assertThat(readListener.getCount(), equalTo(0));
        readListener.resetCount(1);
        
        ex = (TimeoutException) pvReader.lastException();
        assertThat(ex, not(nullValue()));
        
        readListener.await(TimeDuration.ofMillis(1000));
        assertThat(readListener.getCount(), equalTo(0));
        
        ex = (TimeoutException) pvReader.lastException();
        assertThat(ex, nullValue());
        assertThat((String) pvReader.getValue(), equalTo("Initial value"));
    }
    
    @Test
    public void delayedReadOnPVWithTimeout() throws Exception {
        CountDownPVReaderListener readListener = new CountDownPVReaderListener(1, PVReaderEvent.VALUE_MASK | PVReaderEvent.EXCEPTION_MASK);
        pv = PVManager.readAndWrite(channel("delayedConnection"))
                .timeout(ofMillis(500))
                .readListener(readListener)
                .from(dataSource)
                .asynchWriteAndMaxReadRate(ofMillis(50));

        readListener.await(TimeDuration.ofMillis(50));
        assertThat(readListener.getCount(), equalTo(1));
        
        TimeoutException ex = (TimeoutException) pv.lastException();
        assertThat(ex, nullValue());
        
        readListener.await(TimeDuration.ofMillis(600));
        assertThat(readListener.getCount(), equalTo(0));
        readListener.resetCount(1);
        
        ex = (TimeoutException) pv.lastException();
        assertThat(ex, not(nullValue()));
        
        readListener.await(TimeDuration.ofMillis(600));
        assertThat(readListener.getCount(), equalTo(0));
        
        ex = (TimeoutException) pv.lastException();
        assertThat(ex, nullValue());
        assertThat((String) pv.getValue(), equalTo("Initial value"));
    }
    
    @Test
    public void delayedReadOnPVWithTimeoutAndCustomMessage() throws Exception {
        String message = "Ouch! Timeout!";
        CountDownPVReaderListener readListener = new CountDownPVReaderListener(1);
        pv = PVManager.readAndWrite(channel("delayedConnection"))
                .readListener(readListener)
                .timeout(ofMillis(500), message)
                .from(dataSource)
                .asynchWriteAndMaxReadRate(ofMillis(50));
        
        readListener.await(TimeDuration.ofMillis(50));
        assertThat(readListener.getCount(), equalTo(1));
        
        TimeoutException ex = (TimeoutException) pv.lastException();
        assertThat(ex, nullValue());
        
        readListener.await(TimeDuration.ofMillis(600));
        assertThat(readListener.getCount(), equalTo(0));
        readListener.resetCount(1);
        
        ex = (TimeoutException) pv.lastException();
        assertThat(ex, not(nullValue()));
        assertThat(ex.getMessage(), equalTo(message));
        
        readListener.await(TimeDuration.ofMillis(1000));
        assertThat(readListener.getCount(), equalTo(0));
        
        ex = (TimeoutException) pv.lastException();
        assertThat(ex, nullValue());
        assertThat((String) pv.getValue(), equalTo("Initial value"));
    }
    
    @Test
    public void delayedMultipleReadWithConnectionError() throws Exception {
        CountDownPVReaderListener readListener1 = new CountDownPVReaderListener(1);
        CountDownPVReaderListener readListener2 = new CountDownPVReaderListener(1);
        pvReader = PVManager.read(channel("delayedConnectionError"))
                .readListener(readListener1)
                .from(dataSource).maxRate(ofMillis(50));
        pvReader2 = PVManager.read(channel("delayedConnectionError"))
                .readListener(readListener2)
                .from(dataSource).maxRate(ofMillis(50));
        
        Thread.sleep(50);
        
        RuntimeException ex = (RuntimeException) pvReader.lastException();
        assertThat(ex, nullValue());
        assertThat(readListener1.getCount(), equalTo(1));
        ex = (RuntimeException) pvReader2.lastException();
        assertThat(ex, nullValue());
        assertThat(readListener2.getCount(), equalTo(1));
        
        readListener1.await(TimeDuration.ofMillis(1500));
        readListener1.resetCount(1);
        readListener2.await(TimeDuration.ofMillis(1500));
        readListener2.resetCount(1);
        
        ex = (RuntimeException) pvReader.lastException();
        assertThat(ex, instanceOf(RuntimeException.class));
        assertThat(ex.getMessage(), equalTo("Connection error"));
        ex = (RuntimeException) pvReader2.lastException();
        assertThat(ex, instanceOf(RuntimeException.class));
        assertThat(ex.getMessage(), equalTo("Connection error"));
    }
}
