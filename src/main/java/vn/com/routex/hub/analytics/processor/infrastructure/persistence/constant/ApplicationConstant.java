package vn.com.routex.hub.analytics.processor.infrastructure.persistence.constant;

import java.math.BigDecimal;
import java.time.ZoneId;

public class ApplicationConstant {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 1;
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final BigDecimal DEFAULT_MERCHANT_COMMISSION_RATE = new BigDecimal("15.00");

}
