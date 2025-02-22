package ch.ibkrbot;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Constants {

    public static String ACCOUNT;
    public static final BigDecimal PRCTG_LIMIT_PRICE = new BigDecimal("0.98").setScale(4, RoundingMode.HALF_UP);
    public static final BigDecimal TICK_SIZE = new BigDecimal("0.0001").setScale(4, RoundingMode.HALF_UP);
    public static final String BASE_CURRY = "CHF";

}
