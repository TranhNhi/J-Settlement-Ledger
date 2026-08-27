package com.example.J_Settlement.Ledger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Autowired
    private Environment env;

    @Bean
    @Primary
    public DataSource dataSource() {
        String bankListProp = env.getProperty("app.bank.list");
        String[] banks = bankListProp != null ? bankListProp.split(",") : new String[0];

        Map<Object, Object> targetDataSources = new HashMap<>();

        for (String bank : banks) {
            String b = bank.trim().toLowerCase();
            DataSource ds = DataSourceBuilder.create()
                    .url(env.getProperty("spring.datasource." + b + ".url"))
                    .username(env.getProperty("spring.datasource." + b + ".username"))
                    .password(env.getProperty("spring.datasource." + b + ".password"))
                    .driverClassName(env.getProperty("spring.datasource." + b + ".driver-class-name"))
                    .build();

            targetDataSources.put(bank.trim().toUpperCase(), ds);
        }

        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                Object currentDb = DBContextHolder.getCurrentDb();
                System.out.println(">>> [ROUTING] ĐANG CHỌN DB: " + currentDb);
                return currentDb;
            }
        };

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.afterPropertiesSet();

        // BẬT LazyConnectionDataSourceProxy — QUAN TRỌNG!
        // Không có dòng này, determineCurrentLookupKey() chạy TRƯỚC khi
        // setCurrentDb() được gọi → toàn bộ REQUIRES_NEW lấy nhầm DB
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean
    public org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource) {

        org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean em =
                new org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.J_Settlement.Ledger.model");

        org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter vendorAdapter =
                new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");

        // Buộc Hibernate giải phóng connection ngay sau mỗi transaction
        // Để lần gọi tiếp theo (bank khác) lấy connection mới từ Routing
        // — phối hợp với LazyConnectionDataSourceProxy để đảm bảo
        //   determineCurrentLookupKey() luôn chạy SAU setCurrentDb()
        props.put("hibernate.connection.handling_mode",
                "DELAYED_ACQUISITION_AND_RELEASE_AFTER_TRANSACTION");

        em.setJpaPropertyMap(props);
        return em;
    }
}