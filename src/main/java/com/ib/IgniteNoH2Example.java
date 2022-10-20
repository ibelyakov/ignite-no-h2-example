package com.ib;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.calcite.CalciteQueryEngineConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.SqlConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.util.Collections;
import java.util.List;

public class IgniteNoH2Example {
    public static void main(String[] args) {
        IgniteConfiguration cfg = new IgniteConfiguration()
                .setSqlConfiguration(
                        new SqlConfiguration().setQueryEnginesConfiguration(new CalciteQueryEngineConfiguration())
                )
                .setDiscoverySpi(
                        new TcpDiscoverySpi().setIpFinder(
                                new TcpDiscoveryVmIpFinder().setAddresses(Collections.singleton("localhost:47500..47501"))
                        )
                );

        Ignite ignite = Ignition.start(cfg);

        IgniteCache<Object, Object> cache = ignite.getOrCreateCache("test");

        cache.query(new SqlFieldsQuery("CREATE TABLE PERSON (ID INT PRIMARY KEY, NAME VARCHAR)")).getAll();

        for (int i = 0; i < 10; i++)
            cache.query(new SqlFieldsQuery("INSERT INTO PERSON (ID, NAME) VALUES (?, ?)")
                            .setArgs(i, "person" + i))
                    .getAll();

        List<List<?>> res = cache.query(new SqlFieldsQuery("SELECT ID, NAME FROM PERSON")).getAll();

        for (List<?> row : res)
            System.out.println("id=" + row.get(0) + ", name=" + row.get(1));

        ignite.close();
    }
}
