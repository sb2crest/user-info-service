package com.example.user_info_service.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
class CommonFunctionTest {

    @InjectMocks
    private CommonFunction commonFunction;

    @Test
    void getFilterDetails() {
        Assertions.assertEquals(6, CommonFunction.getFilterDetails("ALL/ALL").size());
    }

    @Test
    void collectionAsStreamTest(){
        Assertions.assertEquals(0, ListUtil.collectionAsStream(null).toList().size());
    }
}