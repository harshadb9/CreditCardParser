package com.credit.parser.creditcardparser;

import lombok.Data;

import java.util.List;

@Data
public class StatementInfo {

    private String cardLast4;
    private String billingPeriod;
    private String paymentDueDate;
    private String totalAmountDue;
    private List<String> transactions;
}
