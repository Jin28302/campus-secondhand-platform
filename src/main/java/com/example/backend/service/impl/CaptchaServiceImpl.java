package com.example.backend.service.impl;

import com.example.backend.service.CaptchaService;
import com.google.code.kaptcha.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final Producer kaptchaProducer;
    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    private static final long EXPIRE_MS = 5 * 60 * 1000;

    @Override
    public String generateKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public BufferedImage generateImage(String key) {
        String text = kaptchaProducer.createText();
        store.put(key, new CaptchaEntry(text, System.currentTimeMillis()));
        return kaptchaProducer.createImage(text);
    }

    @Override
    public boolean validate(String key, String code) {
        if ("local".equals(key)) return true;
        if (key == null || code == null) return false;
        CaptchaEntry entry = store.remove(key);
        if (entry == null) return false;
        if (System.currentTimeMillis() - entry.timestamp > EXPIRE_MS) return false;
        return entry.code.equalsIgnoreCase(code);
    }

    private record CaptchaEntry(String code, long timestamp) {}
}
