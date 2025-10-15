# Credit Card Statement Parser

A Spring Boot application that extracts and parses credit card statement information from PDF files. The application supports multiple Indian banks including HDFC, ICICI, SBI, Axis, and Kotak.

## Features

- PDF text extraction with fallback OCR support using Tesseract
- Multi-bank statement parsing
- Extracts key information: card number (last 4 digits), billing period, payment due date, total amount due
- Transaction extraction with date, description, and amount
- JSON output format
- Batch processing of multiple PDF files

## Supported Banks

- HDFC Bank
- ICICI Bank
- State Bank of India (SBI Card)
- Axis Bank
- Kotak Mahindra Bank

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Tesseract OCR (for scanned PDF support)

## Installation

### 1. Install Tesseract OCR

Download and install Tesseract OCR from the official repository:
- Windows: https://github.com/UB-Mannheim/tesseract/wiki
- Default installation path: `C:/Program Files/Tesseract-OCR/`

Ensure the tessdata folder is present at the installation path.

### 2. Clone the Repository

```bash
git clone <repository-url>
cd creditcardparser
```

### 3. Update Tesseract Path (if needed)

If Tesseract is installed in a different location, update the path in `PdfReaderService.java`:

```java
tesseract.setDatapath("YOUR_TESSERACT_PATH/tessdata");
```

## Dependencies

Add these dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- PDFBox for PDF handling -->
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>2.0.29</version>
    </dependency>

    <!-- Tesseract OCR -->
    <dependency>
        <groupId>net.sourceforge.tess4j</groupId>
        <artifactId>tess4j</artifactId>
        <version>5.7.0</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Gson for JSON -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
</dependencies>
```

## Project Structure

```
creditcardparser/
├── src/
│   └── main/
│       └── java/
│           └── com/credit/parser/creditcardparser/
│               ├── CreditCardParserApplication.java
│               ├── PdfReaderService.java
│               └── StatementInfo.java
├── statements/
│   └── (place your PDF files here)
├── pom.xml
└── README.md
```

## Usage

### 1. Create Statements Folder

Create a folder named `statements` in the project root directory:

```bash
mkdir statements
```

### 2. Add PDF Files

Place your credit card statement PDF files in the `statements` folder.

### 3. Run the Application

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/creditcardparser-0.0.1-SNAPSHOT.jar
```

### 4. View Results

The application will process all PDF files in the statements folder and output JSON results to the console.

## Sample Output

```json
{
  "cardLast4": "1234",
  "billingPeriod": "01 Jan 2024 to 31 Jan 2024",
  "paymentDueDate": "20 Feb 2024",
  "totalAmountDue": "15,432.50",
  "transactions": [
    "01-Jan-2024 | Amazon Purchase | ₹1,299.00",
    "05-Jan-2024 | Fuel Station | ₹2,500.00",
    "12-Jan-2024 | Restaurant | ₹850.00"
  ]
}
```

## How It Works

1. The application scans the `statements` folder for PDF files
2. For each PDF, it attempts text extraction using PDFBox
3. If text extraction fails (scanned PDFs), it falls back to OCR using Tesseract
4. The extracted text is analyzed to detect the bank type
5. Bank-specific regex patterns extract relevant information
6. Results are formatted as JSON and printed to console

## Customization

### Adding New Banks

To add support for a new bank, update `PdfReaderService.java`:

1. Add detection logic in `parseStatement()` method
2. Create a new parser method (e.g., `parseNewBank()`)
3. Define regex patterns for the bank's statement format

### Modifying Regex Patterns

Each bank parser uses regex patterns to extract information. Modify these patterns in the respective parser methods if statement formats change.

## Troubleshooting

### OCR Not Working

- Verify Tesseract installation path
- Ensure tessdata folder exists and contains eng.traineddata
- Check console logs for specific OCR errors

### No Text Extracted

- Verify PDF is not password-protected
- Check if PDF contains actual text or is image-based
- Review console output for error messages

### Bank Not Detected

- Check if bank name appears in the PDF text
- Add additional detection keywords in `parseStatement()` method
- Verify case-insensitive matching is working

## Performance Considerations

- OCR processing is slower than text extraction
- Processing time increases with page count
- Consider implementing parallel processing for large batches

## Security Notes

- Do not commit actual credit card statements to version control
- Add `statements/` folder to `.gitignore`
- Sanitize outputs before sharing or logging

## License

This project is provided as-is for educational and internal use purposes.

## Contributing

Contributions are welcome. Please ensure regex patterns are tested with actual statement samples before submitting pull requests.
