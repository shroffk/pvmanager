/*
 * Copyright 2011 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager.expression;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of desired rate expression, to have functions that work on multiple
 * expressions at the same time.
 *
 * @author carcassi
 */
public interface SourceRateExpressionList<T> {
    
    public SourceRateExpressionList<T> and(SourceRateExpressionList<T> expressions);

    List<SourceRateExpression<T>> getSourceRateExpressions();
    
}