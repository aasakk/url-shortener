package com.example.shortener.controller;

import com.example.shortener.model.URLMapping;
import com.example.shortener.service.URLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class URLController {

    @Autowired
    private URLService urlService;

    @PostMapping("/shorten")
    public URLMapping shortenUrl(@RequestBody URLMapping request) {
        String code = urlService.shortenUrl(request.getOriginalUrl());
        return new URLMapping(request.getOriginalUrl(), code);
    }

    @GetMapping("/original/{code}")
    public URLMapping getOriginalUrl(@PathVariable String code) {
        String originalUrl = urlService.getOriginalUrl(code);
        if (originalUrl != null) {
            return new URLMapping(originalUrl, code);
        } else {
            return new URLMapping("Not found", code);
        }
    }

    // ✅ This is the missing part for redirection
    @GetMapping("/go/{code}")
    public RedirectView redirectToOriginal(@PathVariable String code) {
        String originalUrl = urlService.getOriginalUrl(code);
        return new RedirectView(originalUrl != null ? originalUrl : "/not-found");
    }
}
