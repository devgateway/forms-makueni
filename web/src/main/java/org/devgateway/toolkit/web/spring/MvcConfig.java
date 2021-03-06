/*******************************************************************************
 * Copyright (c) 2015 Development Gateway, Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/MIT
 *
 * Contributors:
 * Development Gateway - initial API and implementation
 *******************************************************************************/
package org.devgateway.toolkit.web.spring;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.apache.commons.io.FileCleaningTracker;
import org.bson.types.ObjectId;
import org.devgateway.ocds.web.cache.generators.GenericExcelChartKeyGenerator;
import org.devgateway.ocds.web.cache.generators.GenericPagingRequestKeyGenerator;
import org.devgateway.ocds.web.rest.serializers.GeoJsonPointSerializer;
import org.devgateway.toolkit.web.generators.FieldKeyGenerator;
import org.devgateway.toolkit.web.generators.GenericExcelKeyGenerator;
import org.devgateway.toolkit.web.generators.GenericKeyGenerator;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_EMPTY_JSON_ARRAYS;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/portal/**").setViewName("forward:/ui/index.html");
        registry.addViewController("/dashboard").setViewName("redirect:/portal/");
        registry.addViewController("/login").setViewName("login");
    }

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        //builder.featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        builder.serializationInclusion(Include.NON_EMPTY).dateFormat(dateFormatGmt);
        builder.serializerByType(GeoJsonPoint.class, new GeoJsonPointSerializer());
        builder.serializerByType(ObjectId.class, new ToStringSerializer());
        builder.modulesToInstall(new JtsModule());
        builder.defaultViewInclusion(true);
        builder.featuresToDisable(WRITE_EMPTY_JSON_ARRAYS);

        return builder;
    }

    @Bean(name = "genericPagingRequestKeyGenerator")
    public KeyGenerator genericPagingRequestKeyGenerator(final ObjectMapper objectMapper) {
        return new GenericPagingRequestKeyGenerator(objectMapper);
    }

    @Bean(name = "genericExcelChartKeyGenerator")
    public KeyGenerator genericExcelChartKeyGenerator(final ObjectMapper objectMapper) {
        return new GenericExcelChartKeyGenerator(objectMapper);
    }


    @Bean(name = "genericExcelKeyGenerator")
    public KeyGenerator genericExcelKeyGenerator(final ObjectMapper objectMapper) {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return new GenericExcelKeyGenerator(objectMapper);
    }

    @Bean(destroyMethod = "exitWhenFinished")
    public FileCleaningTracker fileCleaningTracker() {
        return new FileCleaningTracker();
    }


    @Bean(name = "genericKeyGenerator")
    public KeyGenerator genericKeyGenerator(final ObjectMapper objectMapper) {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return new GenericKeyGenerator(objectMapper);
    }

    @Bean(name = "fieldKeyGenerator")
    public KeyGenerator fieldKeyGenerator() {
        return new FieldKeyGenerator();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
