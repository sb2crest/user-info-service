package com.example.user_info_service.util;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

public class ListUtil {
    private ListUtil(){

    }
    public static <T> Stream<T> collectionAsStream(Collection<T> collection){
        if(isNull(collection)){
            return Stream.empty();
        }
        return collection.stream();
    }
}
