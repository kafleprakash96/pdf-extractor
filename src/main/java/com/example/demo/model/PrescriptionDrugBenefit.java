package com.example.demo.model;


import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class PrescriptionDrugBenefit {
    private List<PharmacyBenefit> retailPharmacyBenefits;
    private List<PharmacyBenefit> homeDeliveryBenefits;
    private List<PharmacyBenefit> specialtyPharmacyBenefits;
    private Map<String, String> daySupplyLimitations;

}

