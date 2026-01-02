package com.interview.management.service.ai;

import com.interview.management.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Resume Parser Service
 * Extracts text from PDF and DOCX resume files
 * 
 * Dependencies needed in pom.xml:
 * - org.apache.pdfbox:pdfbox:2.0.30
 * - org.apache.poi:poi-ooxml:5.2.5
 */
@Service
@Slf4j
public class ResumeParserService {
    
    /**
     * Extract text from resume file
     * Supports PDF and DOCX formats
     */
    public String extractTextFromResume(String filePath) {
        log.info("Extracting text from resume: {}", filePath);
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileStorageException("Resume file not found: " + filePath);
        }
        
        String fileName = file.getName().toLowerCase();
        
        try {
            if (fileName.endsWith(".pdf")) {
                return extractFromPDF(file);
            } else if (fileName.endsWith(".docx")) {
                return extractFromDOCX(file);
            } else if (fileName.endsWith(".doc")) {
                // For older .doc format, you'd need Apache POI's HWPF
                throw new FileStorageException("Legacy .doc format not supported. Please use .docx or .pdf");
            } else {
                throw new FileStorageException("Unsupported file format: " + fileName);
            }
        } catch (IOException e) {
            log.error("Failed to extract text from resume: {}", filePath, e);
            throw new FileStorageException("Failed to parse resume file", e);
        }
    }
    
    /**
     * Extract text from PDF using Apache PDFBox
     */
    private String extractFromPDF(File file) throws IOException {
        log.debug("Parsing PDF file: {}", file.getName());
        
        PDDocument document = null;
        try {
            document = Loader.loadPDF(file);
            PDFTextStripper stripper = new PDFTextStripper();
            
            // Extract text from all pages
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            
            String text = stripper.getText(document);
            
            // Clean up the text
            text = cleanExtractedText(text);
            
            log.debug("Extracted {} characters from PDF", text.length());
            return text;
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }
    
    /**
     * Extract text from DOCX using Apache POI
     */
    private String extractFromDOCX(File file) throws IOException {
        log.debug("Parsing DOCX file: {}", file.getName());
        
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            
            String text = extractor.getText();
            
            // Clean up the text
            text = cleanExtractedText(text);
            
            log.debug("Extracted {} characters from DOCX", text.length());
            return text;
        }
    }
    
    /**
     * Clean and normalize extracted text
     */
    private String cleanExtractedText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // Remove excessive whitespace
        text = text.replaceAll("\\s+", " ");
        
        // Remove special characters that might confuse AI
        // But keep: periods, commas, @, hyphens, parentheses
        text = text.replaceAll("[^a-zA-Z0-9\\s.,@()\\-+/#]", "");
        
        // Trim
        text = text.trim();
        
        return text;
    }
    
    /**
     * Validate if resume has minimum content
     */
    public boolean hasValidContent(String resumeText) {
        if (resumeText == null || resumeText.isEmpty()) {
            return false;
        }
        
        // Check if resume has at least 100 characters
        if (resumeText.length() < 100) {
            log.warn("Resume content too short: {} characters", resumeText.length());
            return false;
        }
        
        // Check if resume contains common keywords
        String lowerText = resumeText.toLowerCase();
        boolean hasEmail = lowerText.contains("@") || lowerText.contains("email");
        boolean hasExperience = lowerText.contains("experience") || 
                               lowerText.contains("work") || 
                               lowerText.contains("project");
        boolean hasEducation = lowerText.contains("education") || 
                              lowerText.contains("degree") || 
                              lowerText.contains("university");
        
        if (!hasEmail && !hasExperience && !hasEducation) {
            log.warn("Resume missing key sections (email, experience, education)");
            return false;
        }
        
        return true;
    }
    
    /**
     * Extract key sections from resume for preview
     */
    public String extractPreview(String resumeText, int maxLength) {
        if (resumeText == null || resumeText.isEmpty()) {
            return "";
        }
        
        if (resumeText.length() <= maxLength) {
            return resumeText;
        }
        
        // Try to cut at a sentence boundary
        String preview = resumeText.substring(0, maxLength);
        int lastPeriod = preview.lastIndexOf('.');
        
        if (lastPeriod > maxLength / 2) {
            preview = preview.substring(0, lastPeriod + 1);
        } else {
            preview = preview + "...";
        }
        
        return preview;
    }
}