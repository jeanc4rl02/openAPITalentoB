package com.openAPITalentoB.openAPITalentoB.services;

import com.openAPITalentoB.openAPITalentoB.models.Document;
import com.openAPITalentoB.openAPITalentoB.repositories.DocumentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    public List<Document> searchDocuments(String keyword) {
        return documentRepository.findByTextContentContaining(keyword);
    }

    public void saveDocument(MultipartFile file, String title) throws Exception {
        if (file != null && !file.isEmpty()) {
            Document document = new Document();
            document.setTitle(title);
            document.setContent(file.getBytes());

            String textContent = extractTextContent(file);
            document.setTextContent(textContent);

            documentRepository.save(document);
        } else {
            throw new IllegalArgumentException("El archivo no puede ser nulo o vacío.");
        }
    }

    public String extractTextContent(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            try (PDDocument pdfDocument = PDDocument.load(file.getInputStream())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(pdfDocument);
            }
        } else if (fileName.endsWith(".docx")) {
            try (XWPFDocument docx = new XWPFDocument(file.getInputStream())) {
                List<XWPFParagraph> paragraphs = docx.getParagraphs();
                return paragraphs.stream().map(XWPFParagraph::getText).collect(Collectors.joining("\n"));
            }
        } else {
            return new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    public void saveDocumentFromUrl(String url, String title) throws Exception {
        Document document = new Document();
        document.setTitle(title);

        String content = fetchContentFromUrl(url);
        document.setTextContent(content);

        documentRepository.save(document);
    }

    public String fetchContentFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine).append("\n");
        }
        in.close();
        return Jsoup.parse(content.toString()).text();
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }
}
