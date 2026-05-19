package vn.com.routex.hub.analytics.processor.infrastructure.persistence.utils;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.DefaultAccessorNamingStrategy;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import vn.com.routex.hub.analytics.processor.infrastructure.persistence.exception.BusinessException;

import java.io.IOException;

import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ErrorConstant.TIMEOUT_ERROR;
import static vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant.ErrorConstant.TIMEOUT_ERROR_MESSAGE;

@UtilityClass
public class JsonUtils {

    private final ObjectMapper objectMapper = getObjectMapper();


    public String parseToJsonStr(Object message) throws JsonProcessingException {
        try {
            return objectMapper.writeValueAsString(message);

        } catch (Exception e) {
            throw new BusinessException(ExceptionUtils.buildResultResponse(TIMEOUT_ERROR, TIMEOUT_ERROR_MESSAGE));
        }
    }

    public <T> T parseToObject(String message, Class<T> clazz) throws JsonProcessingException {
        try {
            return objectMapper.readValue(message, clazz);
        } catch (Exception e) {
            throw new BusinessException(ExceptionUtils.buildResultResponse(TIMEOUT_ERROR, TIMEOUT_ERROR_MESSAGE));
        }
    }

    public <T> T parseToKafkaObject(String message, TypeReference<T> type) {
        try {
            return objectMapper.readValue(message, type);
        } catch (Exception e) {
            throw new BusinessException(ExceptionUtils.buildResultResponse(TIMEOUT_ERROR, TIMEOUT_ERROR_MESSAGE));
        }
    }

    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = JsonMapper.builder()
                .accessorNaming(new DefaultAccessorNamingStrategy.Provider().withBuilderPrefix(""))
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .build();

        SimpleModule stringNulltoEmptyModule = new SimpleModule();
        mapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<>() {

            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
                if (value == null) {
                    gen.writeString("");
                    return;
                }
                gen.writeObject(value);
            }
        });

        mapper.registerModule(stringNulltoEmptyModule);
        mapper.registerModules(new JavaTimeModule());
        return mapper;
    }

}
