package net.kubepia.loggen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private Environment environment;

    @Autowired
    private InfoEndpoint infoEndpoint;

    @Autowired
    private MetricsEndpoint metricsEndpoint;

    @Autowired
    private HealthEndpoint healthEndpoint;

    /**
     * 현재 활성화된 프로필 정보와 적용된 properties를 반환합니다.
     */
    @GetMapping("/")
    public Map<String, Object> getActiveProfile() {
        Map<String, Object> response = new HashMap<>();
        
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        
        response.put("activeProfiles", activeProfiles);
        response.put("defaultProfiles", defaultProfiles);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        // 현재 적용된 주요 properties 정보 추가
        Map<String, Object> properties = new HashMap<>();
        
        // 애플리케이션 기본 정보
        properties.put("spring.application.name", environment.getProperty("spring.application.name"));
        properties.put("server.port", environment.getProperty("server.port"));
        
        // LogGen 관련 설정
        properties.put("loggen.schedule.interval", environment.getProperty("loggen.schedule.interval"));
        properties.put("loggen.data.size", environment.getProperty("loggen.data.size"));
        properties.put("loggen.message.template", environment.getProperty("loggen.message.template"));
        properties.put("loggen.log.level", environment.getProperty("loggen.log.level"));
        properties.put("loggen.log.source", environment.getProperty("loggen.log.source"));
        
        // Actuator 설정
        properties.put("management.endpoints.web.exposure.include", environment.getProperty("management.endpoints.web.exposure.include"));
        properties.put("management.endpoint.health.show-details", environment.getProperty("management.endpoint.health.show-details"));
        
        // 로깅 설정
        properties.put("logging.level.net.kubepia.loggen", environment.getProperty("logging.level.net.kubepia.loggen"));
        properties.put("logging.pattern.console", environment.getProperty("logging.pattern.console"));
        
        response.put("appliedProperties", properties);
        
        return response;
    }

    /**
     * 애플리케이션 정보를 반환합니다.
     */
    @GetMapping("/info")
    public Map<String, Object> getApplicationInfo() {
        Map<String, Object> response = new HashMap<>();
        
        // Actuator Info Endpoint 사용
        Map<String, Object> info = infoEndpoint.info();
        response.put("applicationInfo", info);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return response;
    }

    /**
     * JVM 상태 정보를 반환합니다.
     */
    @GetMapping("/jvm")
    public Map<String, Object> getJvmStatus() {
        Map<String, Object> response = new HashMap<>();
        
        // JVM 메모리 정보
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("heapMemoryUsage", memoryBean.getHeapMemoryUsage());
        memoryInfo.put("nonHeapMemoryUsage", memoryBean.getNonHeapMemoryUsage());
        
        // 운영체제 정보
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> osInfo = new HashMap<>();
        osInfo.put("osName", osBean.getName());
        osInfo.put("osVersion", osBean.getVersion());
        osInfo.put("osArch", osBean.getArch());
        osInfo.put("availableProcessors", osBean.getAvailableProcessors());
        osInfo.put("systemLoadAverage", osBean.getSystemLoadAverage());
        
        // 스레드 정보
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threadInfo = new HashMap<>();
        threadInfo.put("threadCount", threadBean.getThreadCount());
        threadInfo.put("peakThreadCount", threadBean.getPeakThreadCount());
        threadInfo.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());
        threadInfo.put("daemonThreadCount", threadBean.getDaemonThreadCount());
        
        // JVM 정보
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> runtimeInfo = new HashMap<>();
        runtimeInfo.put("totalMemory", runtime.totalMemory());
        runtimeInfo.put("freeMemory", runtime.freeMemory());
        runtimeInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        runtimeInfo.put("maxMemory", runtime.maxMemory());
        
        response.put("memory", memoryInfo);
        response.put("operatingSystem", osInfo);
        response.put("threads", threadInfo);
        response.put("runtime", runtimeInfo);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return response;
    }

    /**
     * 애플리케이션 상태 정보를 반환합니다.
     */
    @GetMapping("/health")
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> response = new HashMap<>();
        
        // Actuator Health Endpoint 사용
        response.put("health", healthEndpoint.health());
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return response;
    }

    /**
     * 주요 메트릭 정보를 반환합니다.
     */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        Map<String, Object> response = new HashMap<>();
        
        // 주요 메트릭들
        String[] metricNames = {
            "jvm.memory.used",
            "jvm.memory.max",
            "jvm.threads.live",
            "jvm.threads.peak",
            "process.cpu.usage",
            "system.cpu.usage",
            "http.server.requests"
        };
        
        Map<String, Object> metrics = new HashMap<>();
        for (String metricName : metricNames) {
            try {
                var metric = metricsEndpoint.metric(metricName, null);
                if (metric != null) {
                    metrics.put(metricName, metric);
                }
            } catch (Exception e) {
                metrics.put(metricName, "Not available");
            }
        }
        
        response.put("metrics", metrics);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return response;
    }

    /**
     * 전체 프로필 및 시스템 상태 요약을 반환합니다.
     */
    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        Map<String, Object> response = new HashMap<>();
        
        // 활성 프로필
        String[] activeProfiles = environment.getActiveProfiles();
        response.put("activeProfiles", activeProfiles.length > 0 ? activeProfiles : new String[]{"default"});
        
        // JVM 메모리 사용량
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        Map<String, Object> memorySummary = new HashMap<>();
        memorySummary.put("usedMemoryMB", usedMemory / (1024 * 1024));
        memorySummary.put("maxMemoryMB", maxMemory / (1024 * 1024));
        memorySummary.put("memoryUsagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0);
        
        // 스레드 정보
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threadSummary = new HashMap<>();
        threadSummary.put("currentThreads", threadBean.getThreadCount());
        threadSummary.put("peakThreads", threadBean.getPeakThreadCount());
        
        // 시스템 정보
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> systemSummary = new HashMap<>();
        systemSummary.put("availableProcessors", osBean.getAvailableProcessors());
        systemSummary.put("systemLoadAverage", osBean.getSystemLoadAverage());
        
        response.put("memory", memorySummary);
        response.put("threads", threadSummary);
        response.put("system", systemSummary);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return response;
    }

    /**
     * Kubernetes Liveness Probe - 애플리케이션이 살아있는지 확인
     */
    @GetMapping("/liveness")
    public Map<String, Object> getLiveness() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 기본적인 애플리케이션 상태 확인
            boolean isHealthy = true;
            String status = "UP";
            String message = "Application is alive and running";
            
            // 메모리 사용량 체크 (90% 이상 사용 시 위험)
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            
            if (memoryUsagePercent > 90) {
                isHealthy = false;
                status = "DOWN";
                message = "Memory usage is too high: " + Math.round(memoryUsagePercent * 100.0) / 100.0 + "%";
            }
            
            // 스레드 상태 체크
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            int threadCount = threadBean.getThreadCount();
            int peakThreadCount = threadBean.getPeakThreadCount();
            
            // 스레드가 너무 많으면 위험 (1000개 이상)
            if (threadCount > 1000) {
                isHealthy = false;
                status = "DOWN";
                message = "Too many threads: " + threadCount;
            }
            
            response.put("status", status);
            response.put("healthy", isHealthy);
            response.put("message", message);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // 상세 정보
            Map<String, Object> details = new HashMap<>();
            details.put("memoryUsagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0);
            details.put("threadCount", threadCount);
            details.put("peakThreadCount", peakThreadCount);
            details.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
            response.put("details", details);
            
            // HTTP 상태 코드 설정을 위한 응답 헤더
            if (!isHealthy) {
                response.put("_httpStatus", 503); // Service Unavailable
            }
            
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("healthy", false);
            response.put("message", "Liveness check failed: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            response.put("_httpStatus", 503);
        }
        
        return response;
    }

    /**
     * Kubernetes Readiness Probe - 애플리케이션이 요청을 처리할 준비가 되었는지 확인
     */
    @GetMapping("/readiness")
    public Map<String, Object> getReadiness() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isReady = true;
            String status = "UP";
            String message = "Application is ready to serve requests";
            
            // 1. Spring Boot Actuator Health 체크
            var health = healthEndpoint.health();
            if (!"UP".equals(health.getStatus().getCode())) {
                isReady = false;
                status = "DOWN";
                message = "Spring Boot health check failed";
            }
            
            // 2. 메모리 사용량 체크 (80% 이상 사용 시 준비되지 않음)
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            
            if (memoryUsagePercent > 80) {
                isReady = false;
                status = "DOWN";
                message = "Memory usage too high for readiness: " + Math.round(memoryUsagePercent * 100.0) / 100.0 + "%";
            }
            
            // 3. 스레드 풀 상태 체크
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            int threadCount = threadBean.getThreadCount();
            
            // 스레드가 너무 많으면 준비되지 않음 (500개 이상)
            if (threadCount > 500) {
                isReady = false;
                status = "DOWN";
                message = "Too many threads for readiness: " + threadCount;
            }
            
            // 4. 애플리케이션 시작 후 최소 대기 시간 체크 (30초)
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            if (uptime < 30000) { // 30초 미만이면 아직 준비되지 않음
                isReady = false;
                status = "DOWN";
                message = "Application is still starting up (uptime: " + (uptime / 1000) + "s)";
            }
            
            response.put("status", status);
            response.put("ready", isReady);
            response.put("message", message);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // 상세 정보
            Map<String, Object> details = new HashMap<>();
            details.put("springBootHealth", health.getStatus().getCode());
            details.put("memoryUsagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0);
            details.put("threadCount", threadCount);
            details.put("uptimeSeconds", uptime / 1000);
            details.put("activeProfiles", environment.getActiveProfiles());
            response.put("details", details);
            
            // HTTP 상태 코드 설정을 위한 응답 헤더
            if (!isReady) {
                response.put("_httpStatus", 503); // Service Unavailable
            }
            
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("ready", false);
            response.put("message", "Readiness check failed: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            response.put("_httpStatus", 503);
        }
        
        return response;
    }
} 