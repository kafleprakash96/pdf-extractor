# Prescription Drug Benefit Extraction API

## Overview
This Spring Boot API provides endpoints to extract prescription drug benefits from a PDF file. It also includes an enhanced version that fetches data from the original API and applies transformations to improve the extracted information.

## Features
- Extracts prescription drug benefits from an uploaded PDF file.
- Fetches benefit data from an original API.
- Enhances the benefit data for improved insights.

## Endpoints

<h2> Run the application and open project root(where test.pdf is) in terminal for curl request. </h2>

### 1. Extract Benefits
**Endpoint:** `POST /extract`

**Description:**
Extracts prescription drug benefits from the provided PDF file.

**Request:**
- `file` (MultipartFile) - The PDF file to be processed.

**Response:**
- `200 OK` - Returns a `PrescriptionDrugBenefit` object.
- `400 BAD REQUEST` - If the file processing fails.

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/benefits/extract" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@test.pdf"
```

### 2. Get Enhanced Benefits
**Endpoint:** `POST /enhanced`

**Description:**
Processes the provided PDF file by forwarding it to the original API, retrieves the benefit data, and applies enhancements.

**Request:**
- `file` (MultipartFile) - The PDF file to be processed.

**Response:**
- `200 OK` - Returns an `EnhancedPrescriptionDrugBenefit` object.
- `400 BAD REQUEST` - If the file processing or API request fails.

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/benefits/enhanced" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@test.pdf"
```

## Setup & Installation

### Prerequisites
- Java 17+
- Maven
- Spring Boot 3+

### Steps to Run
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd <repository-folder>
   ```
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Technologies Used
- **Spring Boot** - For building REST APIs.
- **Spring Web** - For handling HTTP requests.
- **Spring MVC** - For request handling and processing.
- **Apache PDFBox** *(or similar library)* - For PDF extraction.

## License
This project is licensed under the MIT License.

## Contact
For questions or support, please reach out to [your-email@example.com].

## Table of Contents
- Quick Start
- API Models (example responses)
- Configuration
- Docker
- Testing
- CI / GitHub Actions
- Troubleshooting
- Contributing
- Security & Privacy

## Quick Start
- Start the app:
   ```bash
   mvn spring-boot:run
   ```
- Send a request (see Examples above). For long-running uploads, increase timeouts via configuration (below).

## API Models (example responses)
Example `PrescriptionDrugBenefit`:
```json
{
   "planId": "12345",
   "planName": "Acme Health Basic",
   "drugName": "Atorvastatin",
   "tier": "Generic",
   "copay": 5.00,
   "coinsurance": null,
   "priorAuthorization": false,
   "stepTherapy": false,
   "quantityLimit": null,
   "notes": "30-day supply"
}
```

Example `EnhancedPrescriptionDrugBenefit` (post-processing/enrichment):
```json
{
   "original": { /* PrescriptionDrugBenefit */ },
   "normalizedDrugName": "atorvastatin",
   "pricing": {
      "estimatedRetailPrice": 12.5,
      "preferredPharmacyPrice": 8.0,
      "currency": "USD"
   },
   "coverageConfidence": 0.92,
   "recommendations": [
      "Consider generic alternative",
      "Verify prior authorization requirements"
   ],
   "sources": ["original-api", "ndc-lookup-service"]
}
```

## Configuration
Configure via `application.yml` / `application.properties` or environment variables:
- ORIGINAL_API_URL — URL of the original API used by the /enhanced endpoint.
- ORIGINAL_API_KEY — API key or token (if required).
- CONNECT_TIMEOUT_MS — HTTP client connect timeout.
- READ_TIMEOUT_MS — HTTP client read timeout.
- MAX_FILE_SIZE — Max upload size (Spring multipart property).

Example env usage:
```bash
export ORIGINAL_API_URL="https://api.example.com/benefits"
export ORIGINAL_API_KEY="s3cr3t"
```

## Docker
Build and run with Docker:
```bash
mvn clean package -DskipTests
docker build -t pdf-extractor:latest .
docker run -p 8080:8080 \
   -e ORIGINAL_API_URL=$ORIGINAL_API_URL \
   -e ORIGINAL_API_KEY=$ORIGINAL_API_KEY \
   pdf-extractor:latest
```

## Testing
- Unit tests: `mvn test`
- Integration tests: use a local mocked original API (WireMock) to validate `/enhanced` network behavior.
- Test PDFs: place sample PDFs in `src/test/resources` and add tests that assert expected model fields.

## CI / GitHub Actions (suggestion)
- Run: `mvn -B clean verify`
- Steps: checkout, setup JDK 17, cache Maven, run tests, build image (optional), publish artifacts.

## Troubleshooting
- 400 BAD REQUEST: check PDF is a valid file and not corrupted.
- 502/504 on /enhanced: verify ORIGINAL_API_URL and network connectivity; increase timeouts.
- Empty extraction results: confirm PDF text is selectable (not scanned images) or add OCR step if needed.

## Contributing
- Fork, branch, make changes, open a PR.
- Follow Java formatting conventions; run `mvn -Dspotless.apply=true spotless:apply` if configured.
- Add tests for new behavior and update README with any new configuration or endpoints.

## Security & Privacy
- PDFs may contain PHI/PII. Treat uploaded files as sensitive; enable TLS in production and limit log verbosity (do not log raw PDF contents).
- Rotate API keys regularly and store secrets in a secure vault or environment variables.

## Contact & Support
For questions, bug reports, or feature requests, open an issue in the repository or email the maintainer listed above.

