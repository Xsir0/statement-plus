package com.example.statementplus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum AfterSaleStateChangeEvent {

    AUDIT_SUCCESS,

    AUDIT_FAIL,

    REFUND,

    BUYER_DELIVERY,

    SELLER_RECEIVING,

    SELLER_DELIVERY,

    BUYER_RECEIVING;



}
