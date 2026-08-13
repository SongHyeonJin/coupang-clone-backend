package com.example.coupangclone.controller.devtools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Profile("local")
@Controller
public class TossTestController {

    @Value("${toss.client-key}")
    private String clientKey;

    @GetMapping("/toss-test")
    public String index(Model model) {
        model.addAttribute("clientKey", clientKey);
        return "toss-test/index";
    }

    @GetMapping("/toss-test/success")
    public String success(@RequestParam String orderId,
                           @RequestParam String paymentKey,
                           @RequestParam String amount,
                           Model model) {
        model.addAttribute("orderId", orderId.replace("ORDER-", ""));
        model.addAttribute("paymentKey", paymentKey);
        model.addAttribute("amount", amount);
        return "toss-test/success";
    }

    @GetMapping("/toss-test/fail")
    public String fail(@RequestParam(required = false) String code,
                        @RequestParam(required = false) String message,
                        @RequestParam(required = false) String orderId,
                        Model model) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "toss-test/fail";
    }

}
