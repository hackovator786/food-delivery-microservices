package com.hackovation.cartservice.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class GenericUtils {

    public static BigDecimal bigDecimal(BigDecimal val){
        return val.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal bigDecimal(Double val){
        return new BigDecimal(val.toString()).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal bigDecimal(String val){
        return new BigDecimal(val).setScale(2, RoundingMode.HALF_UP);
    }
}
