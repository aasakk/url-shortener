package com.example.shortener.service;

import org.springframework.stereotype.Service;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class URLService {
    private final Map<String, String> urlToCode = new HashMap<>();
    private final Map<String, String> codeToUrl = new HashMap<>();
    private final File storageFile;
    private final StatsDClient statsd = new NonBlockingStatsDClient("urlshortener", "localhost", 8125);

    public URLService() {
        // This ensures the file works even when running as JAR
        String filePath = System.getProperty("user.dir") + File.separator + "urls.txt";
        storageFile = new File(filePath);
        System.out.println("Loading from: " + storageFile.getAbsolutePath());
        loadFromFile();
    }

    public String shortenUrl(String originalUrl) {
        if (originalUrl.contains("localhost") || originalUrl.contains("127.0.0.1")) {
            throw new IllegalArgumentException("Shortening localhost URLs is not allowed.");
        }

        if (urlToCode.containsKey(originalUrl)) {
            return urlToCode.get(originalUrl);
        }

        String shortCode = generateShortCode(originalUrl);
        urlToCode.put(originalUrl, shortCode);
        codeToUrl.put(shortCode, originalUrl);
        saveToFile(shortCode, originalUrl);
        statsd.incrementCounter("shorten.success");

        return shortCode;
    }

    public String getOriginalUrl(String shortCode) {
        System.out.println("Looking up shortCode: " + shortCode);
        statsd.incrementCounter("resolve.success");

        return codeToUrl.get(shortCode);
    }

    private String generateShortCode(String url) {
        return Integer.toHexString(url.hashCode()).substring(0, 6);
    }

    private void saveToFile(String code, String url) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(storageFile, true))) {
            bw.write(code + "=" + url);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error saving to file: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        if (!storageFile.exists()) {
            System.out.println("No existing URL file found.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String code = parts[0];
                    String url = parts[1];
                    codeToUrl.put(code, url);
                    urlToCode.put(url, code);
                }
            }
            System.out.println("URL data loaded from file.");
        } catch (IOException e) {
            System.err.println("Error loading from file: " + e.getMessage());
        }
    }
}
