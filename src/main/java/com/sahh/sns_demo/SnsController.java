package com.sahh.sns_demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
public class SnsController {

    private final SnsService snsService;

    @Autowired
    public SnsController(SnsService snsService) {
        this.snsService = snsService;
    }

    @PostMapping("/send")
    public String sendSms(@RequestParam String phoneNumber, @RequestParam String message) {
        System.out.println("Sending SMS to " + phoneNumber + " with message: " + message);
        String phone = phoneNumber;
        System.out.println("Phone: " + phone);
        return snsService.subscribePhoneNumberToTopic(phone, message);
    }

    @GetMapping("/receive")
    public String getSms() {
        return "Received SMS Up";
    }
}