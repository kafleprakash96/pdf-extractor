package com.example.demo.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PharmacyBenefit {
    private String tier;
    private String tierDescription;
    private String inNetworkCost;
    private String outOfNetworkCost;
}
