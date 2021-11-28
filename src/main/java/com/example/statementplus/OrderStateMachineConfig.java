package com.example.statementplus;

import com.example.statementplus.enums.AfterSaleStateChangeEvent;
import com.example.statementplus.enums.AfterSaleStateEnum;
import com.example.statementplus.enums.AfterSaleTypeEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.util.EnumSet;

/**
* 订单状态机配置
*/
@Configuration
@EnableStateMachine(name = "orderStateMachine")
public class OrderStateMachineConfig extends StateMachineConfigurerAdapter<AfterSaleStateEnum, AfterSaleStateChangeEvent> {

    /**
     * 配置状态
     *
     * @param states
     * @throws Exception
     */
    public void configure(StateMachineStateConfigurer<AfterSaleStateEnum, AfterSaleStateChangeEvent> states) throws Exception {
        states
                .withStates()
                .initial(AfterSaleStateEnum.WAIT_AUDIT)
                .states(EnumSet.allOf(AfterSaleStateEnum.class));
    }

    /**
     * 配置状态转换事件关系
     *
     * @param transitions
     * @throws Exception
     */
    public void configure(StateMachineTransitionConfigurer<AfterSaleStateEnum, AfterSaleStateChangeEvent> transitions) throws Exception {
        transitions
                // 审核失败
                .withExternal().source(AfterSaleStateEnum.WAIT_AUDIT).target(AfterSaleStateEnum.CLOSED).event(AfterSaleStateChangeEvent.AUDIT_FAIL)
                // 仅退款
                .and().withExternal().source(AfterSaleStateEnum.WAIT_AUDIT).target(AfterSaleStateEnum.WAIT_REFUND).event(AfterSaleStateChangeEvent.AUDIT_SUCCESS)
                .guard(refundOnlyGuard())
                // 退换货
                .and().withExternal().source(AfterSaleStateEnum.WAIT_AUDIT).target(AfterSaleStateEnum.BUYER_TO_BE_DELIVERY).event(AfterSaleStateChangeEvent.AUDIT_SUCCESS)
                .guard(returnRefundOrExchangeGuard())
                // 买家发货
                .and().withExternal().source(AfterSaleStateEnum.BUYER_TO_BE_DELIVERY).target(AfterSaleStateEnum.SELLER_TO_BE_RECEIVED).event(AfterSaleStateChangeEvent.BUYER_DELIVERY)
                .guard(returnRefundOrExchangeGuard())
                // 卖家收货，待退款
                .and().withExternal().source(AfterSaleStateEnum.SELLER_TO_BE_RECEIVED).target(AfterSaleStateEnum.WAIT_REFUND).event(AfterSaleStateChangeEvent.SELLER_RECEIVING)
                .guard(returnRefundGuard())
                // 卖家待发货
                .and().withExternal().source(AfterSaleStateEnum.SELLER_TO_BE_RECEIVED).target(AfterSaleStateEnum.SELLER__TO_BE_DELIVERY).event(AfterSaleStateChangeEvent.SELLER_RECEIVING)
                .guard(exchangeGoodsGuard())
                // 买家待收货
                .and().withExternal().source(AfterSaleStateEnum.SELLER__TO_BE_DELIVERY).target(AfterSaleStateEnum.BUYER_TO_BE_RECEIVED).event(AfterSaleStateChangeEvent.SELLER_DELIVERY)
                .guard(exchangeGoodsGuard())
                // 换货完成
                .and().withExternal().source(AfterSaleStateEnum.BUYER_TO_BE_RECEIVED).target(AfterSaleStateEnum.FINISHED).event(AfterSaleStateChangeEvent.BUYER_RECEIVING)
                .guard(exchangeGoodsGuard())
                // 退款完成
                .and().withExternal().source(AfterSaleStateEnum.WAIT_REFUND).target(AfterSaleStateEnum.FINISHED).event(AfterSaleStateChangeEvent.REFUND);
    }

    @Bean
    public Guard<AfterSaleStateEnum,AfterSaleStateChangeEvent> refundOnlyGuard(){
        return new Guard<AfterSaleStateEnum, AfterSaleStateChangeEvent>() {
            @Override
            public boolean evaluate(StateContext<AfterSaleStateEnum, AfterSaleStateChangeEvent> stateContext) {

                AfterOrder order = stateContext.getMessageHeaders().get("order", AfterOrder.class);
                if (order.getType().equals(AfterSaleTypeEnum.REFUND_ONLY)){
                    return true;
                }
                return false;
            }
        };
    }


    @Bean
    public Guard<AfterSaleStateEnum,AfterSaleStateChangeEvent> returnRefundGuard(){
        return new Guard<AfterSaleStateEnum, AfterSaleStateChangeEvent>() {
            @Override
            public boolean evaluate(StateContext<AfterSaleStateEnum, AfterSaleStateChangeEvent> stateContext) {

                AfterOrder order = stateContext.getMessageHeaders().get("order", AfterOrder.class);
                if (order.getType().equals(AfterSaleTypeEnum.RETURN_REFUND)){
                    return true;
                }
                return false;
            }
        };
    }

    @Bean
    public Guard<AfterSaleStateEnum,AfterSaleStateChangeEvent> returnRefundOrExchangeGuard(){
        return new Guard<AfterSaleStateEnum, AfterSaleStateChangeEvent>() {
            @Override
            public boolean evaluate(StateContext<AfterSaleStateEnum, AfterSaleStateChangeEvent> stateContext) {

                AfterOrder order = stateContext.getMessageHeaders().get("order", AfterOrder.class);
                if (order.getType().equals(AfterSaleTypeEnum.RETURN_REFUND) || order.getType().equals(AfterSaleTypeEnum.EXCHANGE_GOODS)){
                    return true;
                }
                return false;
            }
        };
    }


    @Bean
    public Guard<AfterSaleStateEnum,AfterSaleStateChangeEvent> exchangeGoodsGuard(){
        return new Guard<AfterSaleStateEnum, AfterSaleStateChangeEvent>() {
            @Override
            public boolean evaluate(StateContext<AfterSaleStateEnum, AfterSaleStateChangeEvent> stateContext) {
                AfterOrder order = stateContext.getMessageHeaders().get("order", AfterOrder.class);
                if (order.getType().equals(AfterSaleTypeEnum.EXCHANGE_GOODS)){
                    return true;
                }
                return false;
            }
        };
    }


    /**
     * 持久化配置
     * 实际使用中，可以配合redis等，进行持久化操作
     *
     * @return
     */
    @Bean
    public DefaultStateMachinePersister persister() {
        return new DefaultStateMachinePersister<>(new StateMachinePersist<AfterSaleStateEnum, AfterSaleStateChangeEvent, AfterOrder>() {
            @Override
            public void write(StateMachineContext<AfterSaleStateEnum, AfterSaleStateChangeEvent> context, AfterOrder order) throws Exception {
                //此处并没有进行持久化操作
                System.out.println(order);
                order.setState(context.getState());
            }

            @Override
            public StateMachineContext<AfterSaleStateEnum, AfterSaleStateChangeEvent> read(AfterOrder order) throws Exception {
                //此处直接获取order中的状态，其实并没有进行持久化读取操作
                return new DefaultStateMachineContext(order.getState(), null, null, null);
            }
        });
    }
}