package ro.tuc.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ro.tuc.gateway.service.GatewayService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class GatewayController {

    @Autowired
    private GatewayService gatewayService;


    @PostMapping("/auth/register")
    public void register(@RequestBody String body, HttpServletResponse servletResponse) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = gatewayService.forwardToAuth("/api/auth/register", HttpMethod.POST, body, headers);

        servletResponse.setStatus(response.getStatusCode().value());
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");
        servletResponse.getWriter().write(response.getBody());
    }

    @PostMapping("/auth/login")
    public void login(@RequestBody String body, HttpServletResponse servletResponse) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = gatewayService.forwardToAuth("/api/auth/login", HttpMethod.POST, body, headers);

        servletResponse.setStatus(response.getStatusCode().value());
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");
        servletResponse.getWriter().write(response.getBody());
    }

    @PutMapping("/auth/credentials/{userId}")
    public void updateCredential(@PathVariable UUID userId,
                                 @RequestBody String body,
                                 HttpServletResponse servletResponse) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = gatewayService.forwardToAuth(
                "/api/auth/credentials/" + userId,
                HttpMethod.PUT,
                body,
                headers
        );

        servletResponse.setStatus(response.getStatusCode().value());
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");
        if (response.getBody() != null) {
            servletResponse.getWriter().write(response.getBody());
        }
    }

    @GetMapping("/auth/health")
    public void authHealth(HttpServletResponse servletResponse) throws IOException {
        ResponseEntity<String> response = gatewayService.forwardToAuth("/api/auth/health", HttpMethod.GET, null, new HttpHeaders());

        servletResponse.setStatus(response.getStatusCode().value());
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");
        servletResponse.getWriter().write(response.getBody());
    }


    @GetMapping("/users")
    public void getUsers(HttpServletRequest request, HttpServletResponse servletResponse) throws IOException {
        ResponseEntity<String> response = forwardWithAuth(request, "/users", HttpMethod.GET, null);
        writeResponse(servletResponse, response);
    }

    @PostMapping("/users")
    public void createUser(HttpServletRequest request, @RequestBody String body, HttpServletResponse servletResponse) throws IOException {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"error\":\"Only admins can create users\"}");
            return;
        }
        ResponseEntity<String> response = forwardWithAuth(request, "/users", HttpMethod.POST, body);

        servletResponse.setStatus(response.getStatusCode().value());
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");

        if (response.getHeaders().getLocation() != null) {
            servletResponse.setHeader("Location", response.getHeaders().getLocation().toString());
        }
        if (response.getBody() != null) {
            servletResponse.getWriter().write(response.getBody());
        }
    }

    @GetMapping("/users/{id}")
    public void getUser(HttpServletRequest request, @PathVariable String id, HttpServletResponse servletResponse) throws IOException {
        ResponseEntity<String> response = forwardWithAuth(request, "/users/" + id, HttpMethod.GET, null);
        writeResponse(servletResponse, response);
    }

    @PutMapping("/users/{id}")
    public void updateUser(HttpServletRequest request,
                           @PathVariable String id,
                           @RequestBody String body,
                           HttpServletResponse servletResponse) throws IOException {
        String role = (String) request.getAttribute("role");
        String userId = (String) request.getAttribute("userId");

        if (!"ADMIN".equals(role) && !userId.equals(id)) {
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"error\":\"You can only edit your own profile\"}");
            return;
        }

        ResponseEntity<String> response = forwardWithAuth(request, "/users/" + id, HttpMethod.PUT, body);
        writeResponse(servletResponse, response);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(HttpServletRequest request, @PathVariable String id, HttpServletResponse servletResponse) throws IOException {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"error\":\"Only admins can delete users\"}");
            return;
        }
        ResponseEntity<String> response = forwardWithAuth(request, "/users/" + id, HttpMethod.DELETE, null);
        writeResponse(servletResponse, response);
    }


    @GetMapping("/devices")
    public void getDevices(HttpServletRequest request, HttpServletResponse servletResponse) throws IOException {
        ResponseEntity<String> response = forwardDeviceWithAuth(request, "/devices", HttpMethod.GET, null);
        writeResponse(servletResponse, response);
    }

    @PostMapping("/devices")
    public void createDevice(HttpServletRequest request, @RequestBody String body, HttpServletResponse servletResponse) throws IOException {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"error\":\"Only admins can create devices\"}");
            return;
        }
        ResponseEntity<String> response = forwardDeviceWithAuth(request, "/devices", HttpMethod.POST, body);
        writeResponse(servletResponse, response);
    }

    @GetMapping("/devices/{id}")
    public void getDevice(HttpServletRequest request, @PathVariable String id, HttpServletResponse servletResponse) throws IOException {
        ResponseEntity<String> response = forwardDeviceWithAuth(request, "/devices/" + id, HttpMethod.GET, null);
        writeResponse(servletResponse, response);
    }

    @PutMapping("/devices/{id}")
    public void updateDevice(HttpServletRequest request,
                             @PathVariable String id,
                             @RequestBody String body,
                             HttpServletResponse servletResponse) throws IOException {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"error\":\"Only admins can update devices\"}");
            return;
        }
        ResponseEntity<String> response = forwardDeviceWithAuth(request, "/devices/" + id, HttpMethod.PUT, body);
        writeResponse(servletResponse, response);
    }

    @DeleteMapping("/devices/{id}")
    public void deleteDevice(HttpServletRequest request, @PathVariable String id, HttpServletResponse servletResponse) throws IOException {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"error\":\"Only admins can delete devices\"}");
            return;
        }
        ResponseEntity<String> response = forwardDeviceWithAuth(request, "/devices/" + id, HttpMethod.DELETE, null);
        writeResponse(servletResponse, response);
    }

    @PutMapping("/devices/{deviceId}/assign/{userId}")
    public void assignDevice(HttpServletRequest request,
                             @PathVariable String deviceId,
                             @PathVariable String userId,
                             HttpServletResponse servletResponse) throws IOException {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"error\":\"Only admins can assign devices\"}");
            return;
        }
        ResponseEntity<String> response = forwardDeviceWithAuth(request,
                "/devices/" + deviceId + "/assign/" + userId,
                HttpMethod.PUT, null);
        writeResponse(servletResponse, response);
    }

    @DeleteMapping("/devices/{deviceId}/assign")
    public void unassignDevice(HttpServletRequest request, @PathVariable String deviceId, HttpServletResponse servletResponse) throws IOException {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"error\":\"Only admins can unassign devices\"}");
            return;
        }
        ResponseEntity<String> response = forwardDeviceWithAuth(request,
                "/devices/" + deviceId + "/assign",
                HttpMethod.DELETE, null);
        writeResponse(servletResponse, response);
    }

    @GetMapping("/devices/user/{userId}")
    public void getDevicesByUser(HttpServletRequest request, @PathVariable String userId, HttpServletResponse servletResponse) throws IOException {
        String role = (String) request.getAttribute("role");
        String requestUserId = (String) request.getAttribute("userId");

        if ("CLIENT".equals(role) && !requestUserId.equals(userId)) {
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"error\":\"You can only view your own devices\"}");
            return;
        }

        ResponseEntity<String> response = forwardDeviceWithAuth(request, "/devices/user/" + userId, HttpMethod.GET, null);
        writeResponse(servletResponse, response);
    }

    @GetMapping("/monitoring/{deviceId}")
    public void getDailyConsumption(HttpServletRequest request,
                                    @PathVariable String deviceId,
                                    @RequestParam String date,
                                    HttpServletResponse servletResponse) throws IOException {

        String path = "/monitoring/" + deviceId + "?date=" + date;

        ResponseEntity<String> response = forwardMonitoringWithAuth(
                request,
                path,
                HttpMethod.GET,
                null
        );

        writeResponse(servletResponse, response);
    }


    private ResponseEntity<String> forwardWithAuth(HttpServletRequest request, String path, HttpMethod method, String body) {
        HttpHeaders headers = createAuthHeaders(request);
        return gatewayService.forwardToUser(path, method, body, headers);
    }

    private ResponseEntity<String> forwardDeviceWithAuth(HttpServletRequest request, String path, HttpMethod method, String body) {
        HttpHeaders headers = createAuthHeaders(request);
        return gatewayService.forwardToDevice(path, method, body, headers);
    }

    private ResponseEntity<String> forwardMonitoringWithAuth(HttpServletRequest request,
                                                             String path,
                                                             HttpMethod method,
                                                             String body) {
        HttpHeaders headers = createAuthHeaders(request);
        return gatewayService.forwardToMonitoring(path, method, body, headers);
    }


    private HttpHeaders createAuthHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", (String) request.getAttribute("userId"));
        headers.set("X-Username", (String) request.getAttribute("username"));
        headers.set("X-Role", (String) request.getAttribute("role"));
        return headers;
    }

    private void writeResponse(HttpServletResponse servletResponse, ResponseEntity<String> response) throws IOException {
        servletResponse.setStatus(response.getStatusCode().value());
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");
        if (response.getBody() != null) {
            servletResponse.getWriter().write(response.getBody());
        }
    }


    @PostMapping("/auth/credentials")
    public void createCredential(@RequestBody String body, HttpServletResponse servletResponse) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = gatewayService.forwardToAuth("/api/auth/credentials", HttpMethod.POST, body, headers);

        servletResponse.setStatus(response.getStatusCode().value());
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");
        if (response.getBody() != null) {
            servletResponse.getWriter().write(response.getBody());
        }
    }

    @GetMapping("/auth/credentials/role")
    public void getRoleByUserId(@RequestParam UUID userId, HttpServletResponse servletResponse) throws IOException {
        ResponseEntity<String> response = gatewayService.forwardToAuth(
                "/api/auth/credentials/role?userId=" + userId,
                HttpMethod.GET,
                null,
                new HttpHeaders()
        );

        servletResponse.setStatus(response.getStatusCode().value());
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");
        if (response.getBody() != null) {
            servletResponse.getWriter().write(response.getBody());
        }
    }

    @PutMapping("/auth/credentials/role")
    public void updateRole(@RequestBody String body, HttpServletResponse servletResponse) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = gatewayService.forwardToAuth("/api/auth/credentials/role", HttpMethod.PUT, body, headers);

        servletResponse.setStatus(response.getStatusCode().value());
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");
        if (response.getBody() != null) {
            servletResponse.getWriter().write(response.getBody());
        }
    }
}