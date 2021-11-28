package com.example.statementplus;

import com.example.statementplus.enums.AfterSaleStateChangeEvent;
import com.example.statementplus.enums.AfterSaleStateEnum;
import com.example.statementplus.enums.AfterSaleTypeEnum;
import lombok.Builder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName Test
 * @Description
 * @Author xsir
 * @Date 2021/11/28 11:33
 * @Version V1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class Test {

    @Autowired
    private StateExecutor stateExecutor;

    @org.junit.Test
    public void test(){

        AfterOrder build = AfterOrder.builder().id(10000L).state(AfterSaleStateEnum.SELLER_TO_BE_RECEIVED).type(AfterSaleTypeEnum.RETURN_REFUND).build();
        boolean result = stateExecutor.sendEvent(MessageBuilder.withPayload(AfterSaleStateChangeEvent.SELLER_RECEIVING).setHeader("order", build).build(), build);
        System.out.println(build);
        assert result;
        // assert build.getState().equals(AfterSaleStateEnum.CLOSED);

    }


}
