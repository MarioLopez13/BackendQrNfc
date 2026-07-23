package com.smartpayut.transaction.repository.projection;

import java.sql.Date;

public interface DailyOperationCount {

    Date getOperationDate();

    long getTotal();
}
