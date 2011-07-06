/*
 * Copyright 2008-2011 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager.loc;

import java.util.HashMap;
import java.util.Map;
import org.epics.pvmanager.Collector;
import org.epics.pvmanager.DataRecipe;
import org.epics.pvmanager.ExceptionHandler;
import org.epics.pvmanager.PV;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVValueWriteListener;
import org.epics.pvmanager.PVWriter;
import org.epics.pvmanager.ValueCache;
import org.epics.pvmanager.WriteBuffer;
import org.epics.pvmanager.WriteCache;
import org.epics.pvmanager.data.VDouble;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.epics.pvmanager.ExpressionLanguage.*;

/**
 *
 * @author carcassi
 */
public class LocalDataSourceTest {

    public LocalDataSourceTest() {
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Mock WriteBuffer writeBuffer;
    @Mock DataRecipe dataRecipe;
    @Mock WriteCache<?> writeCache1;
    @Mock WriteCache<?> writeCache2;
    @Mock Runnable callback;
    @Mock ExceptionHandler exceptionHandler;
    @Mock ValueCache<VDouble> valueCache1;
    @Mock ValueCache<VDouble> valueCache2;
    @Mock Collector collector;
    @Mock PVValueWriteListener listener;
    String channelName1 = "test1";
    String channelName2 = "test2";

    @Test
    public void writeToLocalDataSource() throws Exception {
        // Prepare mock write buffer
        {
            Map<String, WriteCache> caches = new HashMap<String, WriteCache>();
            caches.put(channelName1, writeCache1);
            when(writeCache1.getValue()).thenReturn(6.28);
            caches.put(channelName2, writeCache2);
            when(writeCache2.getValue()).thenReturn(16.28);
            when(writeBuffer.getWriteCaches()).thenReturn(caches);
        }
        
        // Prepare mock read recipe
        {
            Map<String, ValueCache> caches = new HashMap<String, ValueCache>();
            caches.put(channelName1, valueCache1);
            caches.put(channelName2, valueCache2);
            Map<Collector<?>, Map<String, ValueCache>> collectorMap = new HashMap<Collector<?>, Map<String, ValueCache>>();
            collectorMap.put(collector, caches);
            when(dataRecipe.getExceptionHandler()).thenReturn(exceptionHandler);
            when(dataRecipe.getChannelsPerCollectors()).thenReturn(collectorMap);
        }
        
        // TEST: connect, write, disconnect
        LocalDataSource dataSource = new LocalDataSource();
        dataSource.connect(dataRecipe);
        dataSource.prepareWrite(writeBuffer, exceptionHandler);
        dataSource.write(writeBuffer, callback, exceptionHandler);
        Thread.sleep(200);
        dataSource.concludeWrite(writeBuffer, exceptionHandler);
        dataSource.disconnect(dataRecipe);
       
        // Check that the correct value was written and that the write notification was sent
        ArgumentCaptor<VDouble> newValue = ArgumentCaptor.forClass(VDouble.class); 
        verify(valueCache1).setValue(newValue.capture());
        assertThat(newValue.getValue().getValue(), equalTo(6.28));
        verify(valueCache2).setValue(newValue.capture());
        assertThat(newValue.getValue().getValue(), equalTo(16.28));
        verify(collector, times(2)).collect();
        verify(callback).run();
    }
    
    @Test
    public void fullSyncPipeline() throws Exception {
        LocalDataSource dataSource = new LocalDataSource();
        PV<Object> pv = PVManager.read(channel(channelName1)).from(dataSource).atHz(100);
        PVWriter<Object> writer = PVManager.write(toChannel(channelName1)).from(dataSource).sync();
        writer.addPVValueWriteListener(listener);
        writer.write(10);
        
        verify(listener).pvValueWritten();
        Thread.sleep(10);
        pv.close();
        writer.close();
        
        assertThat(((VDouble) pv.getValue()).getValue(), equalTo(10.0));
    }
    
    @Test
    public void fullAsyncPipeline() throws Exception {
        LocalDataSource dataSource = new LocalDataSource();
        PV<Object> pv = PVManager.read(channel(channelName1)).from(dataSource).atHz(100);
        PVWriter<Object> writer = PVManager.write(toChannel(channelName1)).from(dataSource).async();
        writer.addPVValueWriteListener(listener);
        writer.write(10);
        verify(listener, never()).pvValueWritten();
        
        Thread.sleep(10);
        pv.close();
        writer.close();
        
        verify(listener).pvValueWritten();
        assertThat(((VDouble) pv.getValue()).getValue(), equalTo(10.0));
    }

}