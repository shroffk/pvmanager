/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.nsls2.pvmanager;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 *
 * @author carcassi
 */
class TimedCacheCollector<T> extends Collector<T> {

    private final Deque<T> buffer = new ArrayDeque<T>();
    private final PVFunction<T> function;
    private final BigDecimal cachedPeriod;
    
    TimedCacheCollector(PVFunction<T> function, int cachedPeriodInMs) {
        this.function = function;
        cachedPeriod = new BigDecimal(cachedPeriodInMs).divide(new BigDecimal(1000));
    }
    /**
     * Calculates the next value and puts it in the queue.
     */
    @Override
    synchronized void collect() {
        // Calculation may take time, and is locked by this
        T newValue = function.getValue();

        // Buffer is locked and updated
        if (newValue != null) {
            synchronized(buffer) {
                buffer.add(newValue);
            }
        }
    }

    /**
     * Returns all values since last check and removes values from the queue.
     * @return a new array with the value; never null
     */
    @Override
    List<T> getData() {
        synchronized(buffer) {
            if (buffer.isEmpty())
                return Collections.emptyList();

            // last allowed time = now - msCache / 1000
            BigDecimal lastAllowedTime = TypeSupport.timestampOfAccordingly(buffer.getLast())
                    .subtract(cachedPeriod);
            while (!buffer.isEmpty() && lastAllowedTime.compareTo(TypeSupport.timestampOfAccordingly(buffer.getFirst()))
                    > 0) {
                // Discard value
                buffer.removeFirst();
            }
            return new ArrayList<T>(buffer);
        }
    }

}