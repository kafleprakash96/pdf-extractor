package com.example.demo.controller;

import com.example.demo.model.EnhancedPrescriptionDrugBenefit;
import com.example.demo.model.PrescriptionDrugBenefit;
import com.example.demo.service.PdfExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/benefits")
@RequiredArgsConstructor
public class PharmacyController {

    private final PdfExtractionService pdfExtractionService;

    @PostMapping("/extract")
    public ResponseEntity<PrescriptionDrugBenefit> extractBenefits(
            @RequestParam("file") MultipartFile file) {

        try {
            PrescriptionDrugBenefit benefits =
                    pdfExtractionService.extractPrescriptionDrugBenefits(file.getInputStream());
            return ResponseEntity.ok(benefits);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Enhanced API that fetches data from the original API and transforms the response
     */
    @PostMapping("/enhanced")
    public ResponseEntity<EnhancedPrescriptionDrugBenefit> getEnhancedBenefits(
            @RequestParam("file") MultipartFile pdfFile) {
        // Forward the PDF file to the original API and get the response
        PrescriptionDrugBenefit originalBenefit = pdfExtractionService.fetchBenefitDataFromOriginalApi(pdfFile);

        // Enhance the data
        EnhancedPrescriptionDrugBenefit enhancedBenefit = pdfExtractionService.enhanceBenefitData(originalBenefit);

        return ResponseEntity.ok(enhancedBenefit);
    }

//    @PostMapping("/extract-enhanced-drug-benefits")
//    public ResponseEntity<?> extractEnhancedPrescriptionDrugBenefits(@RequestParam("file") MultipartFile file) {
//        try {
//            EnhancedPrescriptionDrugBenefit benefit = enhancedPdfExtractionService.extractEnhancedPrescriptionDrugBenefits(file);
//            return ResponseEntity.ok(benefit);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error processing PDF: " + e.getMessage());
//        }
//    }
}
