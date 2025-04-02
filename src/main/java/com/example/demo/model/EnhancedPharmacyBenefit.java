package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedPharmacyBenefit {
    private String tier;
    private String tierDescription;
    private EnhancedCost inNetworkCost;
    private String outOfNetworkCost;
}
