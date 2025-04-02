package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedCost {
    private Integer dollarValue;
    private Integer percentageValue;
    private Integer maximumValue;
    private String type;  // COPAYMENT, COINSURANCE, UNKNOWN
    private String originalText;  // Keep original text for reference
}
