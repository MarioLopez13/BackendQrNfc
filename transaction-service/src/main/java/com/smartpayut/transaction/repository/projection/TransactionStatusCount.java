package com.smartpayut.transaction.repository.projection;

import com.smartpayut.transaction.domain.enumeration.TransactionStatus;

public interface TransactionStatusCount {

    TransactionStatus getStatus();

    long getTotal();
}
