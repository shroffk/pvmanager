/**
 * Copyright (C) 2010-14 pvmanager developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.pvmanager.jdbc;

import java.util.concurrent.Executors;
import org.epics.vtype.VNumber;
import org.epics.vtype.VString;

/**
 *
 * @author carcassi
 */
public class JDBCSampleService extends JDBCService {

    public JDBCSampleService() {
        super(new JDBCServiceDescription("jdbcSample", "A test service")
                .dataSource(new SimpleDataSource("jdbc:mysql://localhost/test?user=root&password=root"))
                .executorService(Executors.newSingleThreadExecutor(org.epics.pvmanager.util.Executors.namedPool("jdbcSample")))
                .addServiceMethod(new JDBCServiceMethodDescription("query", "A test query")
                    .query("SELECT * FROM Data")
                    .queryResult("result", "The query result")
                )
                .addServiceMethod(new JDBCServiceMethodDescription("insert", "A test insertquery")
                    .query("INSERT INTO `test`.`Data` (`Name`, `Index`, `Value`, `Time`) VALUES (?, ?, ?, now())")
                    .addArgument("name", "The name", VString.class)
                    .addArgument("index", "The index", VNumber.class)
                    .addArgument("value", "The value", VNumber.class)
                ));
    }
    
}
