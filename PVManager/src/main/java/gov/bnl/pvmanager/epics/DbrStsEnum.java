/*
 * Copyright 2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */

package gov.bnl.pvmanager.epics;

import java.util.List;

/**
 * Scalar enum with alarm. Given that enumerated values are of very limited use without
 * the labels, and that the current label is the data most likely used, the
 * enum is of type {@link String}. The index is provided as an extra field, and
 * the list of all possible values is always provided.
 *
 * @author carcassi
 */
public interface DbrStsEnum extends Sts, Scalar<String> {
    int getIndex();
    @Metadata
    List<String> getLabels();
}