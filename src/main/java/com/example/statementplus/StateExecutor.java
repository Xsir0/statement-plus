package com.example.statementplus;

import com.example.statementplus.enums.AfterSaleStateChangeEvent;
import com.example.statementplus.enums.AfterSaleStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Component;

/**
 * @ClassName StateExecutor
 * @Description
 * @Author xsir
 * @Date 2021/11/28 11:30
 * @Version V1.0
 */
@Component
public class StateExecutor {

    @Autowired
    private StateMachine<AfterSaleStateEnum, AfterSaleStateChangeEvent> orderStateMachine;

    @Autowired
    private StateMachinePersister<AfterSaleStateEnum, AfterSaleStateChangeEvent, AfterOrder> persister;


    /**
     * 发送订单状态转换事件
     *
     * @param message
     * @param order
     * @return
     */
    public synchronized boolean sendEvent(Message<AfterSaleStateChangeEvent> message, AfterOrder order) {
        boolean result = false;
        try {
            orderStateMachine.start();
            //尝试恢复状态机状态
            persister.restore(orderStateMachine, order);
            //添加延迟用于线程安全测试
            // Thread.sleep(100);
            result = orderStateMachine.sendEvent(message);
            //持久化状态机状态
            persister.persist(orderStateMachine, order);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            orderStateMachine.stop();
        }
        return result;
    }

}
