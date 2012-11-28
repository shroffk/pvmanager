/**
 * Copyright (C) 2010-12 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager.expression;

import org.epics.pvmanager.extra.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.epics.pvmanager.ReadRecipe;
import org.epics.pvmanager.DataSource;
import org.epics.pvmanager.expression.DesiredRateExpression;
import org.epics.pvmanager.expression.DesiredRateExpressionImpl;
import org.epics.pvmanager.ExceptionHandler;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReaderDirector;
import org.epics.pvmanager.QueueCollector;
import org.epics.pvmanager.ReadRecipeBuilder;
import org.epics.pvmanager.expression.DesiredRateExpressionListImpl;

/**
 * A expression that returns the result of a dynamically managed group.
 * Once the group is created, any {@link DesiredRateExpression} can be
 * added dynamically. The exceptions eventually generated by those
 * expressions can be obtained through {@link #lastExceptions() }.
 *
 * @author carcassi
 */
public class ReadMap<T> extends DesiredRateExpressionImpl<Map<String, T>> {

    private final Object lock = new Object();
    private final Map<String, DesiredRateExpression<T>> expressions = new HashMap<>();
    private PVReaderDirector<?> director;

    /**
     * Creates a new group.
     */
    public ReadMap() {
        super(new DesiredRateExpressionListImpl<Object>(), new MapOfReadFunction<T>(new QueueCollector<MapUpdate<T>>(1000)), "map");
    }

    MapOfReadFunction<T> getMapOfFunction() {
        return (MapOfReadFunction<T>) getFunction();
    }

    /**
     * Removes all the expressions currently in the group.
     * 
     * @return this
     */
    public ReadMap<T> clear() {
        synchronized(lock) {
            getMapOfFunction().getMapUpdateCollector().writeValue(MapUpdate.<T>clear());
            if (director != null) {
                for (DesiredRateExpression<T> desiredRateExpression : expressions.values()) {
                    director.disconnectExpression(desiredRateExpression);
                }
            }
            expressions.clear();
            return this;
        }
    }

    /**
     * Returns the number of expressions in the group.
     * 
     * @return number of expressions in the group
     */
    public int size() {
        synchronized(lock) {
            return expressions.size();
        }
    }

    /**
     * Adds the expression at the end.
     * 
     * @param expression the expression to be added
     * @return this
     */
    public ReadMap<T> add(DesiredRateExpression<T> expression) {
        synchronized(lock) {
            if (expression.getName() == null) {
                throw new NullPointerException("Expression has a null name");
            }
            if (expressions.containsKey(expression.getName())) {
                throw new IllegalArgumentException("MapExpression already contain an expression named '" + expression.getName() + "'");
            }
            
            getMapOfFunction().getMapUpdateCollector().writeValue(MapUpdate.addReadFunction(expression.getName(), expression.getFunction()));
            expressions.put(expression.getName(), expression);
            if (director != null) {
                director.connectExpression(expression);
            }
            return this;
        }
    }
    
    public ReadMap<T> add(DesiredRateExpressionList<T> expressions) {
        synchronized(lock) {
            for (DesiredRateExpression<T> desiredRateExpression : expressions.getDesiredRateExpressions()) {
                add(desiredRateExpression);
            }
            return this;
        }
    }

    /**
     * Removes the expression at the given location.
     * 
     * @param index the position to remove
     * @return this
     */
    public ReadMap<T> remove(String name) {
        synchronized(lock) {
            if (!expressions.containsKey(name)) {
                throw new IllegalArgumentException("MapExpression does not contain an expression named '" + name + "'");
            }
            
            getMapOfFunction().getMapUpdateCollector().writeValue(MapUpdate.<T>removeFunction(name));
            DesiredRateExpression<T> expression = expressions.remove(name);
            if (director != null) {
                director.disconnectExpression(expression);
            }
            return this;
        }
    }
    
    public ReadMap<T> remove(List<String> names) {
        synchronized(lock) {
            for (String name : names) {
                remove(name);
            }
            return this;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fillReadRecipe(PVReaderDirector director, ReadRecipeBuilder builder) {
        synchronized(lock) {
            this.director = director;
            for (Map.Entry<String, DesiredRateExpression<T>> entry : expressions.entrySet()) {
                DesiredRateExpression<T> readExpression = entry.getValue();
                director.connectExpression(readExpression);
            }
        }
    }
    
}
