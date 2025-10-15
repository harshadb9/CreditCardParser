package com.credit.parser.creditcardparser;

import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;
import javax.imageio.ImageIO;

@Service
public class PdfReaderService {

    // ---------------------- TEXT EXTRACTION ----------------------

    public String extractText(String filePath) throws IOException {
        File file = new File(filePath);
        PDDocument document = PDDocument.load(file);

        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);

        // If no text extracted, try OCR
        if (text.trim().isEmpty()) {
            System.out.println("Text extraction failed, attempting OCR...");
            text = extractTextWithOCR(document);
        }

        document.close();
        return text;
    }

    private String extractTextWithOCR(PDDocument document) {
        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
            tesseract.setLanguage("eng");

            PDFRenderer renderer = new PDFRenderer(document);
            StringBuilder ocrText = new StringBuilder();

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, 300);
                String pageText = tesseract.doOCR(image);
                ocrText.append(pageText).append("\n");
            }

            System.out.println("OCR extraction successful");
            return ocrText.toString();

        } catch (Exception e) {
            System.out.println("OCR failed: " + e.getMessage());
            return "";
        }
    }

    // ---------------------- BANK DETECTION ----------------------

    public StatementInfo parseStatement(String text) {
        String lowerText = text.toLowerCase();
        StatementInfo info = new StatementInfo();

        if (text.trim().isEmpty()) {
            System.out.println("No text available for parsing");
            return info;
        }

        if (lowerText.contains("hdfc")) {
            System.out.println("Detected: HDFC Bank");
            return parseHdfc(text);
        } else if (lowerText.contains("icici")) {
            System.out.println("Detected: ICICI Bank");
            return parseIcici(text);
        } else if (lowerText.contains("state bank") || lowerText.contains("sbi")) {
            System.out.println("Detected: SBI Card");
            return parseSbi(text);
        } else if (lowerText.contains("axis")) {
            System.out.println("Detected: Axis Bank");
            return parseAxis(text);
        } else if (lowerText.contains("kotak")) {
            System.out.println("Detected: Kotak Bank");
            return parseKotak(text);
        } else {
            System.out.println("Unknown Bank Type!");
            return info;
        }
    }

    // ---------------------- COMMON TRANSACTION REGEX ----------------------

    private List<String> extractTransactions(String text) {
        List<String> transactions = new ArrayList<>();
        Pattern txnPattern = Pattern.compile(
                "(\\d{2}[-/][A-Za-z]{3,}[-/]\\d{2,4})\\s+([A-Za-z0-9\\s&.,]+?)\\s+[₹■Rs.\\s]*([0-9,]+\\.\\d{2})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = txnPattern.matcher(text);
        while (matcher.find()) {
            String date = matcher.group(1);
            String desc = matcher.group(2).trim();
            String amt = matcher.group(3);
            transactions.add(date + " | " + desc + " | ₹" + amt);
        }
        return transactions;
    }

    // ---------------------- PARSERS ----------------------

    private StatementInfo parseHdfc(String text) {
        StatementInfo info = new StatementInfo();

        info.setCardLast4(findValue(text, "(?:card number|card no\\.?)[\\s:]*?(?:XXXX[\\s-]*){3}(\\d{4})"));
        info.setBillingPeriod(findValue(text, "billing period\\s*:?\\s*([^\\n]+)"));
        info.setPaymentDueDate(findValue(text, "payment due date\\s*:?\\s*([^\\n]+)"));
        info.setTotalAmountDue(findValue(text, "total amount due\\s*:?\\s*[₹■Rs.\\s]*([\\d,]+\\.?\\d*)"));
        info.setTransactions(extractTransactions(text));

        return info;
    }

    private StatementInfo parseIcici(String text) {
        StatementInfo info = new StatementInfo();

        info.setCardLast4(findValue(text, "card\\s+(?:number|ending|no\\.?)\\s*:?\\s*(?:XXXX[\\s-]*){3}(\\d{4})"));
        info.setBillingPeriod(findValue(text, "statement\\s+(?:period|date)\\s*:?\\s*([^\\n]+)"));
        info.setPaymentDueDate(findValue(text, "(?:payment\\s+)?due\\s+date\\s*:?\\s*([^\\n]+)"));
        info.setTotalAmountDue(findValue(text, "(?:total amount due|amount payable)\\s*:?\\s*[₹■Rs.\\s]*([\\d,]+\\.?\\d*)"));
        info.setTransactions(extractTransactions(text));

        return info;
    }

    private StatementInfo parseSbi(String text) {
        StatementInfo info = new StatementInfo();

        info.setCardLast4(findValue(text, "card\\s+no\\.?\\s*:?\\s*(?:XXXX[\\s-]*){3}(\\d{4})"));
        info.setBillingPeriod(findValue(text, "statement\\s+(?:period|from)\\s*:?\\s*([^\\n]+?)(?:\\s+to\\s+([^\\n]+))?"));
        info.setPaymentDueDate(findValue(text, "(?:payment\\s+)?due\\s+(?:date|by)\\s*:?\\s*([^\\n]+)"));
        info.setTotalAmountDue(findValue(text, "(?:total amount due|total outstanding)\\s*:?\\s*[₹■Rs.\\s]*([\\d,]+\\.?\\d*)"));
        info.setTransactions(extractTransactions(text));

        return info;
    }

    private StatementInfo parseAxis(String text) {
        StatementInfo info = new StatementInfo();

        info.setCardLast4(findValue(text, "(?:card number|credit card number|card no\\.?)\\s*:?\\s*(?:XXXX[\\s-]*){3}(\\d{4})"));
        info.setBillingPeriod(findValue(text, "(?:statement period|billing period)\\s*:?\\s*([^\\n]+)"));
        info.setPaymentDueDate(findValue(text, "payment due date\\s*:?\\s*([^\\n]+)"));
        info.setTotalAmountDue(findValue(text, "(?:total amount due|amount due)\\s*:?\\s*[₹■Rs.\\s]*([\\d,]+\\.?\\d*)"));
        info.setTransactions(extractTransactions(text));

        return info;
    }

    private StatementInfo parseKotak(String text) {
        StatementInfo info = new StatementInfo();

        info.setCardLast4(findValue(text, "card\\s+(?:ending with|number)\\s*:?\\s*(?:XXXX[\\s-]*){3}(\\d{4})"));
        info.setBillingPeriod(findValue(text, "(?:billing|statement)\\s+(?:period|cycle)\\s*:?\\s*([^\\n]+)"));
        info.setPaymentDueDate(findValue(text, "payment due date\\s*:?\\s*([^\\n]+)"));
        info.setTotalAmountDue(findValue(text, "(?:total amount due|amount payable)\\s*:?\\s*[₹■Rs.\\s]*([\\d,]+\\.?\\d*)"));
        info.setTransactions(extractTransactions(text));

        return info;
    }

    // ---------------------- HELPER ----------------------

    private String findValue(String text, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
