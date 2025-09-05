package de.leancoders.sharepoint.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

/**
 * @author Ralf
 */
public final class ObjectMapperFactory {

    private static final Set<Module> ENABLED_MODULES =
        ImmutableSet.of(
            new JavaTimeModule(),
            new GuavaModule()
        );

    private ObjectMapperFactory() {
    }

    @Nonnull
    public static ObjectMapperBuilder builder() {
        return new ObjectMapperBuilder();
    }

    @Nonnull
    public static ObjectMapper createDefaultObjectMapper() {
        return builder().withDefaultConfiguration().build();
    }

    public static class ObjectMapperBuilder {

        private final Map<DeserializationFeature, Boolean> deserializationFeatures = Maps.newHashMap();
        private final Map<SerializationFeature, Boolean> serializationFeatures = Maps.newHashMap();
        private JsonInclude.Include serializationInclusion;

        private ObjectMapperBuilder() {
        }

        public ObjectMapperBuilder withDefaultConfiguration() {
            deserializationFeatures.put(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            deserializationFeatures.put(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
            deserializationFeatures.put(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
            deserializationFeatures.put(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true);
            deserializationFeatures.put(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

            serializationFeatures.put(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
            serializationFeatures.put(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            serializationInclusion = JsonInclude.Include.NON_NULL;

            return this;
        }

        public ObjectMapper build() {
            final ObjectMapper mapper = new ObjectMapper();

            for (final Map.Entry<DeserializationFeature, Boolean> deserializationEntry : deserializationFeatures.entrySet()) {
                mapper.configure(deserializationEntry.getKey(), deserializationEntry.getValue());
            }

            for (final Map.Entry<SerializationFeature, Boolean> serializationEntry : serializationFeatures.entrySet()) {
                mapper.configure(serializationEntry.getKey(), serializationEntry.getValue());
            }

            if (serializationInclusion != null) {
                mapper.setSerializationInclusion(serializationInclusion);
            }

            mapper.registerModules(ENABLED_MODULES);
            mapper.deactivateDefaultTyping();

            return mapper;
        }

    }

}
