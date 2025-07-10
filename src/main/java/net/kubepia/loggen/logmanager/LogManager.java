package net.kubepia.loggen.logmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/log")
@CrossOrigin(origins = "*")
public class LogManager {
    
    private static final Logger logger = LoggerFactory.getLogger(LogManager.class);
    
    
    /**
     * GET /log - Return hello message
     * @return hello message
     */
    @GetMapping
    public ResponseEntity<String> getHello() {
        logger.info("GET /log endpoint called");
        return ResponseEntity.ok("hello");
    }
    
    /**
     * POST /log - Create a new log entry
     * @param logData the log data as a map
     * @return the created log with timestamp
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createLog(@RequestBody Map<String, Object> logData) {
        logger.info("POST /log endpoint called with data: {}", logData);
        
        Map<String, Object> response = new HashMap<>();
        
        // Validate required fields
        if (logData == null || !logData.containsKey("message")) {
            logger.warn("Invalid log data received: missing message field");
            response.put("error", "Message is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Set default values if not provided
        String message = (String) logData.get("message");
        String level = logData.containsKey("level") ? (String) logData.get("level") : "INFO";
        String source = logData.containsKey("source") ? (String) logData.get("source") : "unknown";
        
        // Create response with log data
        response.put("id", System.currentTimeMillis());
        response.put("message", message);
        response.put("level", level);
        response.put("source", source);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", "created");
        
        logger.info("Log created successfully: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
