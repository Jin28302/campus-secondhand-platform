package com.example.backend.service;

import java.awt.image.BufferedImage;

public interface CaptchaService {

    String generateKey();

    BufferedImage generateImage(String key);

    boolean validate(String key, String code);
}
