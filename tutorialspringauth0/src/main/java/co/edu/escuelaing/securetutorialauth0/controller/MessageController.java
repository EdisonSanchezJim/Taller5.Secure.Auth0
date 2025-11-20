package co.edu.escuelaing.securetutorialauth0.controller;

import co.edu.escuelaing.securetutorialauth0.model.MessageEntry;
import co.edu.escuelaing.securetutorialauth0.service.MessageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageService svc;

    public MessageController(MessageService svc) {
        this.svc = svc;
    }

    // POST /api/messages
    @PostMapping("/messages")
    public ResponseEntity<?> postMessage(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String msg = body.get("message");
        if (msg == null || msg.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message is required"));
        }

        // Capturamos la IP del cliente
        String ip = extractClientIp(request);
        // Timestamp del servidor
        String ts = Instant.now().toString();

        // Creamos la entrada de mensaje
        MessageEntry entry = new MessageEntry(msg, ip, ts);
        svc.add(entry);

        // Mostramos en consola
        System.out.println("Mensaje recibido: '" + msg + "' desde " + ip + " a las " + ts);

        // Retornamos 201 Created
        return ResponseEntity.status(201).body(Map.of("status", "ok"));
    }

    // GET /api/messages
    @GetMapping(value = "/messages", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<?> getMessages(@RequestParam(value = "format", required = false) String format,
                                         @RequestHeader(value = "Accept", required = false) String acceptHeader) {
        boolean wantsPlain = "plain".equalsIgnoreCase(format) ||
                (acceptHeader != null && acceptHeader.contains(MediaType.TEXT_PLAIN_VALUE));

        if (wantsPlain) {
            String plain = svc.lastMessagesAsPlainText();
            return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(plain);
        } else {
            List<MessageEntry> list = svc.lastMessages();
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list);
        }
    }

    // Extrae la IP real del cliente (soporta proxy)
    private String extractClientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}