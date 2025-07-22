package net.kubepia.loggen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LogGen {

    private static final Logger logger = LoggerFactory.getLogger(LogGen.class);

    @Value("${loggen.schedule.interval:1000}")
    private long scheduleInterval;

    @Value("${loggen.data.size:1024}")
    private int dataSize;

    @Value("${loggen.message.template:hello world}")
    private String messageTemplate;

    @Value("${loggen.log.level:INFO}")
    private String logLevel;

    @Value("${loggen.log.source:scheduler}")
    private String logSource;

    @Value("${POD_ID:PODID}")
    private String podId;

    @Value("${loggen.max.count:1000}")
    private long maxLogCount;

    private final AtomicLong sequenceId = new AtomicLong(1);
    private volatile boolean logGenerationStopped = false;

    /**
     * Scheduled task that runs based on configured interval to create log
     */
    @Scheduled(fixedRateString = "${loggen.schedule.interval:1000}")
    public void generateHelloWorldLog() {
        // Check if log generation has been stopped
        if (logGenerationStopped) {
            return;
        }

        // Check if maximum log count has been reached
        long currentId = sequenceId.get();
        if (currentId > maxLogCount) {
            if (!logGenerationStopped) {
                logGenerationStopped = true;
                logger.warn("[{}] Maximum log count ({}) reached. Log generation stopped.", podId, maxLogCount);
            }
            return;
        }

        // Generate data based on configured size
        String message = generateMessageWithSize(messageTemplate, dataSize);

        Map<String, Object> logData = new HashMap<>();
        logData.put("message", message);
        logData.put("level", logLevel);
        logData.put("source", logSource);

        // Create the log entry
        Map<String, Object> response = new HashMap<>();
        currentId = sequenceId.getAndIncrement();
        response.put("id", currentId);
        response.put("podId", podId);
        response.put("podIdWithId", podId + "-" + currentId);
        response.put("message", message);
        response.put("level", logLevel);
        response.put("source", logSource);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", "auto-generated");
        response.put("dataSize", dataSize);
        response.put("maxLogCount", maxLogCount);
        response.put("remainingLogs", maxLogCount - currentId);

        // Log using SLF4J/Logback
        logGeneratedData(response);
    }

    /**
     * Generate message with specified size
     * @param template base message template
     * @param targetSize target size in bytes
     * @return message with specified size
     */
    private String generateMessageWithSize(String template, int targetSize) {
        if (template.length() >= targetSize) {
            return template.substring(0, targetSize);
        }

        StringBuilder message = new StringBuilder(template);
        while (message.length() < targetSize*1024) {
            message.append(" ").append(template);
        }

        return message.substring(0, targetSize);
    }

    /**
     * Log the generated data using appropriate log level
     * @param logData the log data to be logged
     */
    private void logGeneratedData(Map<String, Object> logData) {
        // 고유한 logid(UUID) 생성
        String logId = UUID.randomUUID().toString();

        // logid 포함된 로그 메시지 생성
        String logMessage = String.format(
                "[logid:%s] TestLog: id=%s, podId=%s, podIdWithId=%s, message=%s, level=%s, source=%s, timestamp=%s, status=%s, dataSize=%s, maxLogCount=%s, remainingLogs=%s",
                logId,
                logData.get("id"),
                logData.get("podId"),
                logData.get("podIdWithId"),
                logData.get("message"),
                logData.get("level"),
                logData.get("source"),
                logData.get("timestamp"),
                logData.get("status"),
                logData.get("dataSize"),
                logData.get("maxLogCount"),
                logData.get("remainingLogs"));

        // 로그 레벨에 따라 출력
        switch (logLevel.toUpperCase()) {
            case "ERROR":
                logger.error(logMessage);
                break;
            case "WARN":
                logger.warn(logMessage);
                break;
            case "DEBUG":
                logger.debug(logMessage);
                break;
            case "TRACE":
                logger.trace(logMessage);
                break;
            case "INFO":
            default:
                logger.info(logMessage);
                break;
        }
    }

    /**
     * Reset log generation counter and restart log generation
     */
    public void restartLogGeneration() {
        sequenceId.set(1);
        logGenerationStopped = false;
        logger.info("[{}] Log generation restarted. Counter reset to 1.", podId);
    }

    /**
     * Get current log generation status
     */
    public Map<String, Object> getLogGenerationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("podId", podId);
        status.put("currentId", sequenceId.get());
        status.put("maxLogCount", maxLogCount);
        status.put("remainingLogs", Math.max(0, maxLogCount - sequenceId.get()));
        status.put("logGenerationStopped", logGenerationStopped);
        status.put("timestamp", LocalDateTime.now().toString());
        return status;
    }
}
