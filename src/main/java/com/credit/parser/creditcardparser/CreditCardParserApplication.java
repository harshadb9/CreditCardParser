package com.credit.parser.creditcardparser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class CreditCardParserApplication implements CommandLineRunner {

	@Autowired
	private PdfReaderService pdfReaderService;

	public static void main(String[] args) {
		SpringApplication.run(CreditCardParserApplication.class, args);
	}

	public void run(String... args) throws Exception {
		File folder = new File("statements");
		File[] files = folder.listFiles((dir, name) -> name.endsWith(".pdf"));

		if (files == null || files.length == 0) {
			System.out.println("No PDF files found in 'statements' folder!");
			return;
		}

		for (File file : files) {
			System.out.println("\n==============================");
			System.out.println("Parsing: " + file.getName());
			System.out.println("==============================");

			try {
				String text = pdfReaderService.extractText(file.getPath());
				StatementInfo info = pdfReaderService.parseStatement(text);

				String jsonOutput = new com.google.gson.GsonBuilder()
						.setPrettyPrinting()
						.create()
						.toJson(info);

				System.out.println(jsonOutput);

			} catch (Exception e) {
				System.out.println("Error parsing " + file.getName() + ": " + e.getMessage());
			}
		}
	}
}
