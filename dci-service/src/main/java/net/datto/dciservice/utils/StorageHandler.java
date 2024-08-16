package net.datto.dciservice.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.datto.dci.api.dto.OffsiteStorageUsedDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StorageHandler extends StdDeserializer<OffsiteStorageUsedDto> {

    public StorageHandler() {
        this(null);
    }

    public StorageHandler(Class<?> clazz) {
        super(clazz);
    }

    public OffsiteStorageUsedDto deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        ObjectCodec objectCodec = jsonParser.getCodec();

        if (jsonParser.getCurrentToken().equals(JsonToken.START_ARRAY)) {
            List<OffsiteStorageUsedDto> storages = objectCodec.readValue(jsonParser, new TypeReference<ArrayList<OffsiteStorageUsedDto>>() { });
            if (storages != null && storages.size() > 0) {
                return  storages.get(0);
            } else {
                return null;
            }
        } else {
            OffsiteStorageUsedDto storage =objectCodec.readValue(jsonParser, OffsiteStorageUsedDto.class);
            return storage;
        }

    }
}
