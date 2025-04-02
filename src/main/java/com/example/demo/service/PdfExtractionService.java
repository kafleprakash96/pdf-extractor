package com.example.demo.service;

import com.example.demo.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfExtractionService {

private final RestTemplate restTemplate;

    private static final Pattern COPAYMENT_PATTERN = Pattern.compile("\\$(\\d+) Copayment per");
    private static final Pattern COINSURANCE_PATTERN = Pattern.compile("(\\d+)% Coinsurance up to a maximum of \\$(\\d+) per");
    private static final Pattern DAY_SUPPLY_PATTERN = Pattern.compile("Up to (\\d+) days");

    public PrescriptionDrugBenefit extractPrescriptionDrugBenefits(InputStream pdfInputStream) {
        try {
            PDDocument document = PDDocument.load(pdfInputStream);
            String text = extractTextFromPdf(document);
            document.close();

            return parsePrescriptionBenefitsFromText(text);
        } catch (IOException e) {
            log.error("Failed to extract data from PDF", e);
            throw new RuntimeException("Failed to process PDF", e);
        }
    }

    private String extractTextFromPdf(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        return stripper.getText(document);
    }

    private PrescriptionDrugBenefit parsePrescriptionBenefitsFromText(String pdfText) {
        // Extract day supply limitations
        Map<String, String> daySupplyLimitations = extractDaySupplyLimitations(pdfText);

        // Extract retail pharmacy benefits
        List<PharmacyBenefit> retailBenefits = extractRetailPharmacyBenefits(pdfText);

        // Extract home delivery pharmacy benefits
        List<PharmacyBenefit> homeDeliveryBenefits = extractHomeDeliveryBenefits(pdfText);

        // Build the full prescription drug benefit object
        return PrescriptionDrugBenefit.builder()
                .retailPharmacyBenefits(retailBenefits)
                .homeDeliveryBenefits(homeDeliveryBenefits)
                .daySupplyLimitations(daySupplyLimitations)
                .build();
    }

    private Map<String, String> extractDaySupplyLimitations(String pdfText) {
        Map<String, String> limitations = new HashMap<>();

        // Use regex to find day supply limitations section
        Pattern pattern = Pattern.compile("Retail Pharmacy \\(In-Network and Out-[\\s\\S]*?Up to (\\d+) days");
        Matcher matcher = pattern.matcher(pdfText);

        if (matcher.find()) {
            limitations.put("RetailPharmacy", "Up to " + matcher.group(1) + " days");
        }

        // Extract home delivery limitation
        pattern = Pattern.compile("Home Delivery \\(Mail Order\\) Pharmacy[\\s\\S]*?Up to (\\d+) days");
        matcher = pattern.matcher(pdfText);

        if (matcher.find()) {
            limitations.put("HomeDeliveryPharmacy", "Up to " + matcher.group(1) + " days");
        }

        // Extract specialty pharmacy limitation
        pattern = Pattern.compile("Specialty Pharmacy[\\s\\S]*?Up to (\\d+) days\\*");
        matcher = pattern.matcher(pdfText);

        if (matcher.find()) {
            limitations.put("SpecialtyPharmacy", "Up to " + matcher.group(1) + " days*");
        }

        return limitations;
    }

    private List<PharmacyBenefit> extractRetailPharmacyBenefits(String pdfText) {
        List<PharmacyBenefit> benefits = new ArrayList<>();

        // Find the retail pharmacy section
        Pattern sectionPattern = Pattern.compile("Retail Pharmacy Copayments /[\\s\\S]*?Home Delivery Pharmacy");
        Matcher sectionMatcher = sectionPattern.matcher(pdfText);

        if (sectionMatcher.find()) {
            String retailSection = sectionMatcher.group(0);

            // Extract individual tier benefits
            extractTierBenefits(retailSection, benefits);
        }

        return benefits;
    }

    private List<PharmacyBenefit> extractHomeDeliveryBenefits(String pdfText) {
        List<PharmacyBenefit> benefits = new ArrayList<>();

        // Find the home delivery pharmacy section
        Pattern sectionPattern = Pattern.compile("Home Delivery Pharmacy[\\s\\S]*?Copayments / Coinsurance[\\s\\S]*?Specialty Drug");
        Matcher sectionMatcher = sectionPattern.matcher(pdfText);

        if (sectionMatcher.find()) {
            String homeDeliverySection = sectionMatcher.group(0);

            // Extract individual tier benefits
            extractTierBenefits(homeDeliverySection, benefits);
        }

        return benefits;
    }

    private void extractTierBenefits(String section, List<PharmacyBenefit> benefits) {
        // Pattern to extract tier information
        Pattern tierPattern = Pattern.compile("• Tier (\\d[a-b]?) Prescription Drugs[\\s\\S]*?\\$(\\d+) Copayment per[\\s\\S]*?Not covered");
        Matcher tierMatcher = tierPattern.matcher(section);

        while (tierMatcher.find()) {
            String tier = tierMatcher.group(1);
            String copayment = "$" + tierMatcher.group(2) + " Copayment per Prescription Drug";

            benefits.add(PharmacyBenefit.builder()
                    .tier("Tier " + tier)
                    .tierDescription("Prescription Drugs")
                    .inNetworkCost(copayment)
                    .outOfNetworkCost("Not covered")
                    .build());
        }

        // Special pattern for Tier 4 which has coinsurance
        Pattern tier4Pattern = Pattern.compile("• Tier 4 Prescription Drugs[\\s\\S]*?(\\d+)% Coinsurance up to a[\\s\\S]*?maximum of \\$(\\d+) per[\\s\\S]*?Not covered");
        Matcher tier4Matcher = tier4Pattern.matcher(section);

        if (tier4Matcher.find()) {
            String coinsurance = tier4Matcher.group(1) + "% Coinsurance up to a maximum of $" +
                    tier4Matcher.group(2) + " per Prescription Drug";

            benefits.add(PharmacyBenefit.builder()
                    .tier("Tier 4")
                    .tierDescription("Prescription Drugs")
                    .inNetworkCost(coinsurance)
                    .outOfNetworkCost("Not covered")
                    .build());
        }
    }


    // Extract Enhanced values
    public PrescriptionDrugBenefit fetchBenefitDataFromOriginalApi(MultipartFile pdfFile) {
        try {
            // Create a multipart request with the PDF file
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create a multipart body with the file
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Convert MultipartFile to a resource to be sent in the request
            ByteArrayResource resource = new ByteArrayResource(pdfFile.getBytes()) {
                @Override
                public String getFilename() {
                    return pdfFile.getOriginalFilename();
                }
            };

            // Add the file to the request body
            body.add("file", resource);

            // Create the HTTP entity with headers and body
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Send the POST request with the file
            ResponseEntity<PrescriptionDrugBenefit> response = restTemplate.exchange(
                    "http://localhost:8080/api/benefits/extract",
                    HttpMethod.POST,
                    requestEntity,
                    PrescriptionDrugBenefit.class
            );

            return response.getBody();

        } catch (IOException e) {
            throw new RuntimeException("Failed to process the PDF file", e);
        }
    }
    public EnhancedPrescriptionDrugBenefit enhanceBenefitData(PrescriptionDrugBenefit originalBenefit) {
        List<EnhancedPharmacyBenefit> enhancedRetailBenefits = enhancePharmacyBenefits(originalBenefit.getRetailPharmacyBenefits());
        List<EnhancedPharmacyBenefit> enhancedHomeDeliveryBenefits = enhancePharmacyBenefits(originalBenefit.getHomeDeliveryBenefits());
        List<EnhancedPharmacyBenefit> enhancedSpecialtyBenefits = originalBenefit.getSpecialtyPharmacyBenefits() != null ?
                enhancePharmacyBenefits(originalBenefit.getSpecialtyPharmacyBenefits()) : null;

        Map<String, Integer> enhancedDaySupplyLimitations = enhanceDaySupplyLimitations(originalBenefit.getDaySupplyLimitations());

        return EnhancedPrescriptionDrugBenefit.builder()
                .retailPharmacyBenefits(enhancedRetailBenefits)
                .homeDeliveryBenefits(enhancedHomeDeliveryBenefits)
                .specialtyPharmacyBenefits(enhancedSpecialtyBenefits)
                .daySupplyLimitations(enhancedDaySupplyLimitations)
                .build();
    }

    private List<EnhancedPharmacyBenefit> enhancePharmacyBenefits(List<PharmacyBenefit> originalBenefits) {
        if (originalBenefits == null) {
            return null;
        }

        List<EnhancedPharmacyBenefit> enhancedBenefits = new ArrayList<>();

        for (PharmacyBenefit benefit : originalBenefits) {
            EnhancedCost enhancedCost = parseInNetworkCost(benefit.getInNetworkCost());

            enhancedBenefits.add(EnhancedPharmacyBenefit.builder()
                    .tier(benefit.getTier())
                    .tierDescription(benefit.getTierDescription())
                    .inNetworkCost(enhancedCost)
                    .outOfNetworkCost(benefit.getOutOfNetworkCost())
                    .build());
        }

        return enhancedBenefits;
    }

    private EnhancedCost parseInNetworkCost(String costString) {
        EnhancedCost.EnhancedCostBuilder costBuilder = EnhancedCost.builder();

        // Check for copayment format
        Matcher copaymentMatcher = COPAYMENT_PATTERN.matcher(costString);
        if (copaymentMatcher.find()) {
            costBuilder.dollarValue(Integer.parseInt(copaymentMatcher.group(1)));
            costBuilder.type("COPAYMENT");
            return costBuilder.build();
        }

        // Check for coinsurance format
        Matcher coinsuranceMatcher = COINSURANCE_PATTERN.matcher(costString);
        if (coinsuranceMatcher.find()) {
            int percentageValue = Integer.parseInt(coinsuranceMatcher.group(1));
            int maximumValue = Integer.parseInt(coinsuranceMatcher.group(2));
            costBuilder.percentageValue(percentageValue);
            costBuilder.maximumValue(maximumValue);
            costBuilder.type("COINSURANCE");
            return costBuilder.build();
        }

        // Default case if no pattern matches
        costBuilder.originalText(costString);
        costBuilder.type("UNKNOWN");
        return costBuilder.build();
    }

    private Map<String, Integer> enhanceDaySupplyLimitations(Map<String, String> originalLimitations) {
        Map<String, Integer> enhancedLimitations = new HashMap<>();

        originalLimitations.forEach((key, value) -> {
            Matcher matcher = DAY_SUPPLY_PATTERN.matcher(value);
            if (matcher.find()) {
                enhancedLimitations.put(key, Integer.parseInt(matcher.group(1)));
            }
        });

        return enhancedLimitations;
    }

}
