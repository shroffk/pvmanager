/*
 * Copyright 2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */

package gov.bnl.pvmanager.epics;

import gov.bnl.pvmanager.TimeStamp;
import java.text.NumberFormat;
import java.util.List;
import java.util.Set;

/**
 * Factory class for all concrete implementation of the types.
 * <p>
 * The factory methods do not do anything in terms of defensive copy and
 * immutability to the objects, which they are passed as they are. It's the
 * client responsibility to prepare them appropriately, which is automatically
 * done anyway for all objects except collections.
 *
 * @author carcassi
 */
public class TypeFactory {

    /**
     * Creates new immutable EInt.
     */
    public static EInt newEInt(final Integer value, final AlarmSeverity alarmSeverity,
            final Set<String> alarmStatus, final List<String> possibleAlarms, final TimeStamp timeStamp,
            final Integer timeUserTag,
            final Integer lowerDisplayLimit, final Integer lowerAlarmLimit, final Integer lowerWarningLimit,
            final String units, final NumberFormat numberFormat, final Integer upperWarningLimit,
            final Integer upperAlarmLimit, final Integer upperDisplayLimit,
            final Integer lowerCtrlLimit, final Integer upperCtrlLimit) {
        return new EInt() {

            @Override
            public Integer getLowerCtrlLimit() {
                return lowerCtrlLimit;
            }

            @Override
            public Integer getUpperCtrlLimit() {
                return upperCtrlLimit;
            }

            @Override
            public Integer getLowerDisplayLimit() {
                return lowerDisplayLimit;
            }

            @Override
            public Integer getLowerAlarmLimit() {
                return lowerAlarmLimit;
            }

            @Override
            public Integer getLowerWarningLimit() {
                return lowerWarningLimit;
            }

            @Override
            public String getUnits() {
                return units;
            }

            @Override
            public NumberFormat getFormat() {
                return numberFormat;
            }

            @Override
            public Integer getUpperWarningLimit() {
                return upperWarningLimit;
            }

            @Override
            public Integer getUpperAlarmLimit() {
                return upperAlarmLimit;
            }

            @Override
            public Integer getUpperDisplayLimit() {
                return upperDisplayLimit;
            }

            @Override
            public Integer getTimeUserTag() {
                return timeUserTag;
            }

            @Override
            public TimeStamp getTimeStamp() {
                return timeStamp;
            }

            @Override
            public AlarmSeverity getAlarmSeverity() {
                return alarmSeverity;
            }

            @Override
            public Set<String> getAlarmStatus() {
                return alarmStatus;
            }

            @Override
            public List<String> getPossibleAlarms() {
                return possibleAlarms;
            }

            @Override
            public Integer getValue() {
                return value;
            }
        };
    }

    /**
     * Creates new immutable newDbrCtrlInt by using the metadata from the old value.
     */
    public static EInt newEInt(final Integer value, final AlarmSeverity alarmSeverity,
            final Set<String> alarmStatus, final Integer timeUserTag, final TimeStamp timeStamp,
            EInt oldValue) {
        return newEInt(value, alarmSeverity, alarmStatus, oldValue.getPossibleAlarms(),
                timeStamp,
                timeUserTag,
                oldValue.getLowerDisplayLimit(), oldValue.getLowerAlarmLimit(),
                oldValue.getLowerWarningLimit(), oldValue.getUnits(),
                oldValue.getFormat(), oldValue.getUpperWarningLimit(),
                oldValue.getUpperAlarmLimit(), oldValue.getUpperDisplayLimit(),
                oldValue.getLowerCtrlLimit(), oldValue.getUpperCtrlLimit());
    }

}