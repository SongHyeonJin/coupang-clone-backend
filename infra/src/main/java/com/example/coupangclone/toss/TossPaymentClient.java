package com.example.coupangclone.toss;

import com.example.coupangclone.auth.PaymentGatewayPort;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TossPaymentClient implements PaymentGatewayPort {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String CANCEL_URL = "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel";

    private final RestTemplate tossRestTemplate;
    private final TossProperties tossProperties;

    @Override
    public String confirm(String paymentKey, String orderId, int amount) {
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        try {
            Map<String, Object> response = tossRestTemplate.exchange(
                    CONFIRM_URL, HttpMethod.POST, new HttpEntity<>(body, authHeaders()),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            ).getBody();

            return response == null ? null : (String) response.get("method");
        } catch (RestClientException e) {
            throw new ErrorException(ExceptionEnum.PAYMENT_FAILED);
        }
    }

    @Override
    public void cancel(String paymentKey, String cancelReason) {
        Map<String, Object> body = Map.of("cancelReason", cancelReason);

        try {
            tossRestTemplate.exchange(
                    CANCEL_URL, HttpMethod.POST, new HttpEntity<>(body, authHeaders()),
                    new ParameterizedTypeReference<Map<String, Object>>() {}, paymentKey
            );
        } catch (RestClientException e) {
            throw new ErrorException(ExceptionEnum.PAYMENT_CANCEL_FAILED);
        }
    }

    private HttpHeaders authHeaders() {
        String credentials = Base64.getEncoder().encodeToString(
                (tossProperties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + credentials);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}
