package model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.TimeZone;

/**
 * Singleton {@link com.fasterxml.jackson.databind.ObjectMapper} factory
 * pre-configured with JSR-310 date/time support and project-standard settings.
 */
public final class ObjectMapperFactory {

    private static final ObjectMapper INSTANCE = new ObjectMapper();

    static {
        INSTANCE.registerModule(new JavaTimeModule());
        INSTANCE.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        INSTANCE.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        INSTANCE.enable(SerializationFeature.INDENT_OUTPUT);
        INSTANCE.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        INSTANCE.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private ObjectMapperFactory() {}

    /**
     * @return the pre-configured singleton {@link ObjectMapper} instance
     */
    public static ObjectMapper get() {
        return INSTANCE;
    }
}
