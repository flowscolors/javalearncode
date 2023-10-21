package com.flowscolors.javaspi.service;

import com.flowscolors.javaspi.serializer.JavaSerializer;
import com.flowscolors.javaspi.serializer.ObjectSerializer;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * @author flowscolors
 * @date 2021-12-13 11:24
 */
@Service
public class SerializerService {


    public ObjectSerializer getObjectSerializer() {
        ServiceLoader<ObjectSerializer> serializers = ServiceLoader.load(ObjectSerializer.class);

        final Optional<ObjectSerializer> serializer = StreamSupport.stream(serializers.spliterator(), false)
                .findFirst();

        return serializer.orElse(new JavaSerializer());
    }
}