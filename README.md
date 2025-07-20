# LogGen - Spring Boot Log Generator with Profile & JVM Monitoring

Spring Boot 기반의 로그 생성기 애플리케이션으로, 프로필 정보와 JVM 상태를 모니터링할 수 있는 RESTful API를 제공합니다.

## 주요 기능

- **로그 생성**: 설정 가능한 간격으로 로그 메시지 자동 생성
- **프로필 모니터링**: 현재 활성화된 Spring 프로필 정보 및 적용된 설정 확인
- **JVM 상태 모니터링**: 메모리, 스레드, CPU 사용량 등 JVM 상태 정보 제공
- **애플리케이션 상태**: Spring Boot Actuator를 통한 헬스 체크 및 메트릭 제공
- **프로필별 설정**: 개발/운영 환경별 다른 설정 적용 및 확인

## 상세 기능

- **로그 생성**: 설정 가능한 간격으로 로그 메시지 자동 생성
- **프로필 모니터링**: 현재 활성화된 Spring 프로필 정보 확인
- **JVM 상태 모니터링**: 메모리, 스레드, CPU 사용량 등 JVM 상태 정보 제공
- **애플리케이션 상태**: Spring Boot Actuator를 통한 헬스 체크 및 메트릭 제공

## API 엔드포인트

### 프로필 및 시스템 정보 API

| 엔드포인트 | 설명 | HTTP 메서드 |
|-----------|------|------------|
| `/api/profile/` | 현재 활성화된 프로필 정보 및 적용된 properties | GET |
| `/api/profile/info` | 애플리케이션 정보 | GET |
| `/api/profile/jvm` | JVM 상태 상세 정보 | GET |
| `/api/profile/health` | 애플리케이션 헬스 상태 | GET |
| `/api/profile/metrics` | 주요 메트릭 정보 | GET |
| `/api/profile/summary` | 전체 상태 요약 | GET |
| `/api/profile/liveness` | Kubernetes Liveness Probe | GET |
| `/api/profile/readiness` | Kubernetes Readiness Probe | GET |

### Spring Boot Actuator 엔드포인트

| 엔드포인트 | 설명 |
|-----------|------|
| `/actuator/health` | 애플리케이션 헬스 상태 |
| `/actuator/info` | 애플리케이션 정보 |
| `/actuator/metrics` | 모든 메트릭 정보 |

## 실행 방법

### 1. 애플리케이션 빌드 및 실행

```bash
# Maven을 사용한 빌드
./mvnw clean package

# 애플리케이션 실행
java -jar target/loggen-0.0.1-SNAPSHOT.jar
```

### 2. 프로필 설정

```bash
# 개발 환경으로 실행
java -jar target/loggen-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# 운영 환경으로 실행
java -jar target/loggen-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# ConfigMap 방식으로 로컬 테스트
SPRING_CONFIG_LOCATION="classpath:/,file:./config/" java -jar target/loggen-0.0.1-SNAPSHOT.jar
```

## API 사용 예시

### 프로필 정보 및 적용된 설정 확인
```bash
curl http://localhost:8080/api/profile/
```

### JVM 상태 확인
```bash
curl http://localhost:8080/api/profile/jvm
```

### 시스템 요약 정보
```bash
curl http://localhost:8080/api/profile/summary
```

### Kubernetes Health Checks
```bash
# Liveness Probe
curl http://localhost:8080/api/profile/liveness

# Readiness Probe
curl http://localhost:8080/api/profile/readiness
```

## API 응답 예시

### 프로필 정보 및 적용된 설정
```json
{
  "activeProfiles": ["dev"],
  "defaultProfiles": ["default"],
  "appliedProperties": {
    "spring.application.name": "loggen-dev",
    "server.port": "8080",
    "loggen.schedule.interval": "2000",
    "loggen.data.size": "512",
    "loggen.message.template": "dev environment test",
    "loggen.log.level": "DEBUG",
    "loggen.log.source": "dev-scheduler",
    "logging.level.net.kubepia.loggen": "DEBUG",
    "logging.pattern.console": "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [DEV] %logger{36} - %msg%n",
    "management.endpoints.web.exposure.include": "health,info,metrics",
    "management.endpoint.health.show-details": "always"
  },
  "timestamp": "2025-07-20T16:12:41.420513"
}
```

### Liveness Probe 응답
```json
{
  "status": "UP",
  "healthy": true,
  "message": "Application is alive and running",
  "details": {
    "memoryUsagePercent": 0.95,
    "threadCount": 22,
    "peakThreadCount": 22,
    "uptime": 10546
  },
  "timestamp": "2025-07-20T16:17:27.128888"
}
```

### Readiness Probe 응답
```json
{
  "status": "UP",
  "ready": true,
  "message": "Application is ready to serve requests",
  "details": {
    "springBootHealth": "UP",
    "memoryUsagePercent": 0.98,
    "threadCount": 22,
    "uptimeSeconds": 34,
    "activeProfiles": []
  },
  "timestamp": "2025-07-20T16:17:51.581629"
}
```

## 설정

### application.properties 주요 설정

```properties
# 로그 생성 설정
loggen.schedule.interval=1000
loggen.data.size=1024
loggen.message.template=hello world
loggen.log.level=INFO
loggen.log.source=scheduler

# Actuator 설정
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# 서버 설정
server.port=8080

## 프로필별 설정 예시

### 개발 환경 (application-dev.properties)
```properties
loggen.schedule.interval=2000
loggen.data.size=512
loggen.message.template=dev environment test
loggen.log.level=DEBUG
loggen.log.source=dev-scheduler
logging.level.net.kubepia.loggen=DEBUG
spring.application.name=loggen-dev
```

### 운영 환경 (application-prod.properties)
```properties
loggen.schedule.interval=5000
loggen.data.size=2048
loggen.message.template=production log message
loggen.log.level=WARN
loggen.log.source=prod-scheduler
logging.level.net.kubepia.loggen=WARN
spring.application.name=loggen-prod

## Kubernetes 배포

### ConfigMap을 통한 설정 관리

Kubernetes에서는 ConfigMap을 통해 애플리케이션 설정을 외부화하여 관리합니다.

#### 장점
- **설정 외부화**: 애플리케이션 코드와 설정 분리
- **환경별 설정**: 개발/운영 환경별 다른 설정 적용
- **동적 설정 변경**: ConfigMap 업데이트로 설정 변경 가능
- **보안**: 민감한 정보를 Secret으로 관리 가능
- **버전 관리**: 설정 변경 이력 추적 가능

#### 설정 우선순위
1. **환경 변수** (가장 높음)
2. **ConfigMap** (`/config/application.properties`)
3. **기본 설정** (`classpath:application.properties`)

### Docker 이미지 빌드
```bash
docker build -t loggen:latest .
```

### Kubernetes 배포

#### 기본 환경 배포
```bash
# ConfigMap 적용
kubectl apply -f k8s/configmap.yaml
# Deployment 및 Service 배포
kubectl apply -f k8s/deployment.yaml
```

#### 개발 환경 배포
```bash
# 개발용 ConfigMap 적용
kubectl apply -f k8s/configmap-dev.yaml
# 개발용 Deployment 및 Service 배포
kubectl apply -f k8s/deployment-dev.yaml
```

#### 운영 환경 배포
```bash
# 운영용 ConfigMap 적용
kubectl apply -f k8s/configmap-prod.yaml
# 운영용 Deployment 및 Service 배포
kubectl apply -f k8s/deployment-prod.yaml
```

### Health Check 상세 정보

#### Liveness Probe (`/api/profile/liveness`)
- **목적**: 애플리케이션이 살아있는지 확인
- **체크 항목**:
  - 메모리 사용량 (90% 이상 시 위험)
  - 스레드 수 (1000개 이상 시 위험)
  - 기본적인 애플리케이션 상태
- **실패 시**: Pod 재시작

#### Readiness Probe (`/api/profile/readiness`)
- **목적**: 애플리케이션이 요청을 처리할 준비가 되었는지 확인
- **체크 항목**:
  - Spring Boot Actuator Health 상태
  - 메모리 사용량 (80% 이상 시 준비되지 않음)
  - 스레드 수 (500개 이상 시 준비되지 않음)
  - 애플리케이션 시작 후 최소 30초 대기
- **실패 시**: Service에서 Pod 제외 (트래픽 라우팅 중단)

### Kubernetes 설정 예시

#### ConfigMap을 통한 설정 관리
```yaml
# ConfigMap
apiVersion: v1
kind: ConfigMap
metadata:
  name: loggen-config
data:
  application.properties: |
    spring.application.name=loggen-k8s
    loggen.schedule.interval=3000
    loggen.data.size=1536
    loggen.message.template=kubernetes log message
    loggen.log.level=INFO
    loggen.log.source=k8s-scheduler

# Deployment에서 ConfigMap 마운트
spec:
  containers:
  - name: loggen
    env:
    - name: SPRING_CONFIG_LOCATION
      value: "classpath:/,file:/config/"
    volumeMounts:
    - name: config-volume
      mountPath: /config
      readOnly: true
  volumes:
  - name: config-volume
    configMap:
      name: loggen-config
```

#### Health Check 설정
```yaml
livenessProbe:
  httpGet:
    path: /api/profile/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30
  timeoutSeconds: 5
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /api/profile/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 3
  failureThreshold: 3
```

## VSCode 설정

### launch.json
``` json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "LoggenApplication",
            "request": "launch",
            "mainClass": "net.kubepia.loggen.LoggenApplication",
            "projectName": "loggen"
        }
    ]
}
```
