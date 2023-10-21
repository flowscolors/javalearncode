package com.flowscolors.javaspi;

import com.flowscolors.javaspi.exception.ObjectSerializerException;
import com.flowscolors.javaspi.serializer.ObjectSerializer;
import com.flowscolors.javaspi.service.SerializerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;

@SpringBootTest
class JavaSpiApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private SerializerService serializerService;

    @Test
    public void serializerTest() throws ObjectSerializerException {
        ObjectSerializer objectSerializer = serializerService.getObjectSerializer();
        System.out.println(objectSerializer.getSchemeName());
        byte[] arrays = objectSerializer.serialize(Arrays.asList("1", "2", "3"));
        ArrayList list = objectSerializer.deSerialize(arrays, ArrayList.class);
        Assert.assertArrayEquals(Arrays.asList("1", "2", "3").toArray(), list.toArray());
    }

}
