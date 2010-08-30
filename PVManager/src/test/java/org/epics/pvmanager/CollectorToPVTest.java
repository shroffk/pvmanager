/*
 * Copyright 2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */

package org.epics.pvmanager;

import java.util.Collections;
import org.epics.pvmanager.sim.SimulationDataSource;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import org.epics.pvmanager.data.VDouble;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author carcassi
 */
public class CollectorToPVTest {

    public CollectorToPVTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Force type support loading
        SourceRateExpression<VDouble> exp = org.epics.pvmanager.data.ExpressionLanguage.vDouble("test");
        if (exp.hashCode() == 0)
            System.out.println("Loaded");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        pv = null;
    }

    @After
    public void tearDown() {
        if (pv == null)
            pv.close();
    }

    private volatile PV<VDouble> pv;
    private AtomicInteger counter;

    @Test
    public void testFasterRate() throws Exception {
        long testTimeMs = 1000;
        long scanPeriodMs = 40;
        double notificationPeriodMs = 0.1;
        final int nNotifications = (int) (testTimeMs / notificationPeriodMs);
        int maxNotifications = (int) (testTimeMs / scanPeriodMs);
        int targetNotifications = Math.min(nNotifications, maxNotifications);

        final ValueCache<VDouble> cache = new ValueCache<VDouble>(VDouble.class);
        final Collector<VDouble> collector = new QueueCollector<VDouble>(cache);
        counter = new AtomicInteger();
        LastValueAggregator<VDouble> aggregator = new LastValueAggregator<VDouble>(VDouble.class, collector);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                pv = PV.createPv("My pv", VDouble.class);
                pv.addPVValueChangeListener(new PVValueChangeListener() {

                    @Override
                    public void pvValueChanged() {
                        counter.incrementAndGet();
                    }
                });
            }
        });
        Notifier<VDouble> notifier = new Notifier<VDouble>(pv, aggregator, ThreadSwitch.onSwingEDT());
        Scanner.scan(notifier, scanPeriodMs);
        DataRecipe connRecipe = new DataRecipe();
        double secBetweenSamples = ((double) notificationPeriodMs / 1000.0);
        connRecipe = connRecipe.includeCollector(collector, Collections.<String,ValueCache>singletonMap("gaussian(0.0, 1.0, " + secBetweenSamples + ")", cache));
        SimulationDataSource.simulatedData().connect(connRecipe);
        Thread.sleep(testTimeMs);
        pv.close();
        int actualNotification = counter.get();
        if (Math.abs(actualNotification - targetNotifications) > 2) {
            fail("Expected " + targetNotifications + " but got " + actualNotification);
        }
    }

    @Test
    public void testSlowerRate() throws Exception {
        long testTimeMs = 1000;
        long scanPeriodMs = 40;
        double notificationPeriodMs = 100;
        final int nNotifications = (int) (testTimeMs / notificationPeriodMs);
        int maxNotifications = (int) (testTimeMs / scanPeriodMs);
        int targetNotifications = Math.min(nNotifications, maxNotifications);

        final ValueCache<VDouble> cache = new ValueCache<VDouble>(VDouble.class);
        final Collector<VDouble> collector = new QueueCollector<VDouble>(cache);
        counter = new AtomicInteger();
        LastValueAggregator<VDouble> aggregator = new LastValueAggregator<VDouble>(VDouble.class, collector);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                pv = PV.createPv("My pv", VDouble.class);
                pv.addPVValueChangeListener(new PVValueChangeListener() {

                    @Override
                    public void pvValueChanged() {
                        counter.incrementAndGet();
                    }
                });
            }
        });
        Notifier<VDouble> notifier = new Notifier<VDouble>(pv, aggregator, ThreadSwitch.onSwingEDT());
        Scanner.scan(notifier, scanPeriodMs);
        DataRecipe connRecipe = new DataRecipe();
        double secBetweenSamples = ((double) notificationPeriodMs / 1000.0);
        connRecipe = connRecipe.includeCollector(collector, Collections.<String,ValueCache>singletonMap("gaussian(0.0, 1.0, " + secBetweenSamples + ")", cache));
        SimulationDataSource.simulatedData().connect(connRecipe);
        Thread.sleep(testTimeMs);
        int actualNotification = counter.get();
        if (Math.abs(actualNotification - targetNotifications) > 2) {
            fail("Expected " + targetNotifications + " but got " + actualNotification);
        }
    }


//    @Test
//    public void testStatistics() throws Exception {
//        long testTimeMs = 5000;
//        long scanPeriodMs = 40;
//        long notificationPeriodMs = 1;
//        int samplesPerNotification = 5;
//        final int nNotifications = (int) (testTimeMs / notificationPeriodMs);
//        int maxNotifications = (int) (testTimeMs / scanPeriodMs);
//        int targetNotifications = Math.min(nNotifications, maxNotifications);
//
//        final ValueCache<Double> cache = new ValueCache<Double>(Double.class);
//        final Collector<Double> collector = new QueueCollector<Double>(cache);
//        counter = new AtomicInteger();
//        StatisticsAggregator aggregator = new StatisticsAggregator(collector);
//        SwingUtilities.invokeAndWait(new Runnable() {
//            @Override
//            public void run() {
//                pvStat = PV.createPv("My pv", DoubleStatistics.class);
//                pvStat.addPVValueChangeListener(new PVValueChangeListener() {
//
//                    @Override
//                    public void pvValueChanged() {
//                        counter.incrementAndGet();
//                    }
//                });
//            }
//        });
//        Notifier<DoubleStatistics> notifier = new Notifier<DoubleStatistics>(pvStat, aggregator, ExpressionLanguage.onSwingEDT());
//        Scanner.scan(notifier, scanPeriodMs);
//        PVRecipe connRecipe = new PVRecipe();
//        connRecipe.cache = cache;
//        connRecipe.collector = collector;
//        connRecipe.pvName = SimulationDataSource.mockPVName(samplesPerNotification, notificationPeriodMs, nNotifications);
//        SimulationDataSource.instance.connect(connRecipe);
//        Thread.sleep(testTimeMs + 100);
//        int actualNotification = counter.get();
//        if (Math.abs(actualNotification - targetNotifications) > 1) {
//            fail("Expected " + targetNotifications + " but got " + actualNotification);
//        }
//    }

}