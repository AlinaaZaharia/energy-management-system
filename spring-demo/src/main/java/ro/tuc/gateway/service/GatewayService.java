package ro.tuc.gateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@Service
public class GatewayService {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${device.service.url}")
    private String deviceServiceUrl;

    @Value("${monitoring.service.url}")
    private String monitoringServiceUrl;


    public GatewayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> forwardToAuth(String path, HttpMethod method, String body, HttpHeaders headers) {
        String url = authServiceUrl + path;
        return forward(url, method, body, headers);
    }

    public ResponseEntity<String> forwardToUser(String path, HttpMethod method, String body, HttpHeaders headers) {
        String url = userServiceUrl + path;
        return forward(url, method, body, headers);
    }

    public ResponseEntity<String> forwardToDevice(String path, HttpMethod method, String body, HttpHeaders headers) {
        String url = deviceServiceUrl + path;
        return forward(url, method, body, headers);
    }

    private ResponseEntity<String> forward(String url, HttpMethod method, String body, HttpHeaders headers) {
        try {
            if (headers.getContentType() == null) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, method, entity, String.class);

        } catch (HttpClientErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {
            System.err.println("Gateway error: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Gateway error: " + e.getMessage() + "\"}");
        }
    }

    public ResponseEntity<String> forwardToMonitoring(String path, HttpMethod method, String body, HttpHeaders headers) {
        String url = monitoringServiceUrl + path;
        return forward(url, method, body, headers);
    }

}