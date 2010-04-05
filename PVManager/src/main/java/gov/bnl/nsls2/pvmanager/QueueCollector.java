/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.nsls2.pvmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Collects the data at the CA rate and allows a client to get all values
 * since last check. The class needs to be thread safe, and it function
 * as a queue where the CA threads post data, and a timer based thread
 * collects the data.
 * <p>
 * There are two locks used: one (the object itself) must be used whenever
 * one is either calculating the function or preparing the inputs for the function.
 * The other (the buffer) is used whenever the buffer is modified. The idea
 * is that the new calculation may not block the scanning and reading of the
 * values in the buffer.
 *
 * @author carcassi
 */
class QueueCollector<T> extends Collector<T> {

    // @GuardedBy(buffer)
    private final List<T> buffer = new ArrayList<T>();
    private final PVFunction<T> function;
    
    QueueCollector(PVFunction<T> function) {
        this.function = function;
    }

    /**
     * Calculates the next value and puts it in the queue.
     */
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
    List<T> getData() {
        synchronized(buffer) {
            List<T> data = new ArrayList<T>(buffer);
            buffer.clear();
            return data;
        }
    }
}