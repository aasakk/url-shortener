package com.example.shortener.service;

import org.springframework.stereotype.Service;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Service
public class URLService {
	private MetricRegistry metricRegistry;
	private final Counter shortenCounter;
    private final Map<String, String> urlToCode = new HashMap<>();
    private final Map<String, String> codeToUrl = new HashMap<>();
    private final File storageFile;
    private StatsDClient statsd;
    
    @Autowired
    public URLService(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.shortenCounter = metricRegistry.counter("shorten-url-counter");
        Graphite graphite = new Graphite(new InetSocketAddress("graphite", 2003));
        GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
                .prefixedWith("urlshortener")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(graphite);
        reporter.start(10, TimeUnit.SECONDS);
        this.statsd = new NonBlockingStatsDClient("urlshortener", "graphite", 8125);
        String filePath = System.getProperty("user.dir") + File.separator + "urls.txt";
        storageFile = new File(filePath);
        System.out.println("Loading from: " + storageFile.getAbsolutePath());
        loadFromFile();
    }


    public String shortenUrl(String originalUrl) {
    	shortenCounter.inc();
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
