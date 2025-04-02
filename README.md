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

