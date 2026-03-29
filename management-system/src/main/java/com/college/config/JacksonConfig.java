package com.college.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 创建JavaTimeModule并添加自定义日期时间序列化和反序列化器
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        javaTimeModule.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        
        // 添加LocalDate序列化和反序列化器
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        mapper.registerModule(javaTimeModule);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        return mapper;
    }
    
    // 自定义LocalDateTime反序列化器，支持多种格式
    public static class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_INSTANT
        };

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws java.io.IOException {
            String dateString = parser.getText();
            if (dateString == null || dateString.trim().isEmpty()) {
                return null;
            }
            
            dateString = dateString.trim();
            
            for (DateTimeFormatter formatter : FORMATTERS) {
                try {
                    if (formatter == DateTimeFormatter.ISO_OFFSET_DATE_TIME) {
                        // 处理带时区信息的日期时间，转换为系统默认时区
                        return java.time.OffsetDateTime.parse(dateString, formatter)
                                .withOffsetSameInstant(ZoneOffset.ofHours(8)) // 转换为GMT+8
                                .toLocalDateTime();
                    } else if (formatter == DateTimeFormatter.ISO_INSTANT) {
                        // 处理UTC时间戳，转换为系统默认时区
                        return java.time.Instant.parse(dateString)
                                .atOffset(ZoneOffset.ofHours(8)) // 转换为GMT+8
                                .toLocalDateTime();
                    } else {
                        return LocalDateTime.parse(dateString, formatter);
                    }
                } catch (DateTimeParseException ignored) {
                    // 尝试下一个格式
                }
            }
            
            throw new java.io.IOException("Unable to parse date: " + dateString + 
                ". Supported formats: yyyy-MM-dd HH:mm:ss, ISO_LOCAL_DATE_TIME, ISO_OFFSET_DATE_TIME, ISO_INSTANT");
        }
    }
}