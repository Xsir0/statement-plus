package com.example.statementplus;

import com.example.statementplus.enums.AfterSaleStateEnum;
import com.example.statementplus.enums.AfterSaleTypeEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName AfterOrder
 * @Description
 * @Author xsir
 * @Date 2021/11/28 11:08
 * @Version V1.0
 */
@Data
@Builder
public class AfterOrder {

    private Long id;

    private AfterSaleStateEnum state;

    private AfterSaleTypeEnum type;

}
