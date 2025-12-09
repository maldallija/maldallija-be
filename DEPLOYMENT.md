# Deployment Guide

## Environment Configuration

This application uses environment variables for sensitive configuration in production.

### Required Environment Variables (Production Only)

| Variable | Description | Example |
|----------|-------------|---------|
| `DISCORD_WEBHOOK_EXCEPTION_URL` | Discord webhook URL for **500+ error notifications** (comma-separated for multiple) | `https://discord.com/api/webhooks/123/abc...` or `https://discord.com/api/webhooks/1/a,https://discord.com/api/webhooks/2/b` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `production` |

### Important Notes

- **Discord exception notifications are ONLY enabled when `discord.webhook.exception.url` is configured**
- Notifications are sent for **HTTP 500+ status codes only**
- Local/development environments do NOT send notifications (no configuration needed)
- Multiple webhook URLs can be provided (comma-separated) for:
  - Redundancy (backup webhook)
  - Different channels (e.g., team channel + alert channel)
- Uses `@ConditionalOnProperty` - Bean is not created if property is missing

---

## Local Development

### Setup

Local development does NOT require Discord webhook configuration.

Simply run with local profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

No Discord notifications will be sent in local environment.

---

## Production Deployment

### Option 1: Docker (Recommended)

```bash
# Build
./gradlew bootBuildImage

# Run with single webhook URL
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DISCORD_WEBHOOK_EXCEPTION_URL="https://discord.com/api/webhooks/..." \
  maldallija-be:latest

# Run with multiple webhook URLs (comma-separated)
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DISCORD_WEBHOOK_EXCEPTION_URL="https://discord.com/api/webhooks/1/abc,https://discord.com/api/webhooks/2/def" \
  maldallija-be:latest
```

### Option 2: Kubernetes

```yaml
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: maldallija-secrets
type: Opaque
stringData:
  # Single webhook
  discord-webhook-exception-url: https://discord.com/api/webhooks/...

  # Multiple webhooks (comma-separated)
  # discord-webhook-exception-url: https://discord.com/api/webhooks/1/a,https://discord.com/api/webhooks/2/b

---
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: maldallija-be
spec:
  template:
    spec:
      containers:
      - name: app
        image: maldallija-be:latest
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DISCORD_WEBHOOK_EXCEPTION_URL
          valueFrom:
            secretKeyRef:
              name: maldallija-secrets
              key: discord-webhook-exception-url
```

### Option 3: AWS ECS/Fargate

1. Store secret in AWS Secrets Manager:
   ```bash
   # Single webhook
   aws secretsmanager create-secret \
     --name maldallija/discord-webhook-exception \
     --secret-string "https://discord.com/api/webhooks/..."

   # Multiple webhooks (comma-separated)
   aws secretsmanager create-secret \
     --name maldallija/discord-webhook-exception \
     --secret-string "https://discord.com/api/webhooks/1/a,https://discord.com/api/webhooks/2/b"
   ```

2. Reference in ECS Task Definition:
   ```json
   {
     "containerDefinitions": [{
       "name": "maldallija-be",
       "environment": [
         {"name": "SPRING_PROFILES_ACTIVE", "value": "production"}
       ],
       "secrets": [{
         "name": "DISCORD_WEBHOOK_EXCEPTION_URL",
         "valueFrom": "arn:aws:secretsmanager:region:account:secret:maldallija/discord-webhook-exception"
       }]
     }]
   }
   ```

### Option 4: JAR with Environment Variables

```bash
# Build
./gradlew clean build

# Run with single webhook
export SPRING_PROFILES_ACTIVE=production
export DISCORD_WEBHOOK_EXCEPTION_URL="https://discord.com/api/webhooks/..."
java -jar build/libs/maldallija-be-0.0.1-SNAPSHOT.jar

# Run with multiple webhooks
export SPRING_PROFILES_ACTIVE=production
export DISCORD_WEBHOOK_EXCEPTION_URL="https://discord.com/api/webhooks/1/a,https://discord.com/api/webhooks/2/b"
java -jar build/libs/maldallija-be-0.0.1-SNAPSHOT.jar
```

---

## Security Checklist

- [ ] `application-local.yaml` is in `.gitignore`
- [ ] Discord webhook URLs are never committed to Git
- [ ] Production secrets are stored in secret management system (AWS Secrets Manager, K8s Secrets, etc.)
- [ ] Environment variables are injected at runtime
- [ ] Health check endpoint (`/api/health-check`) is accessible
- [ ] Discord exception notifications are only enabled when webhook URL is configured
- [ ] Local/development environments do NOT send Discord notifications

---

## Monitoring

### Health Check

```bash
curl http://localhost:8080/api/health-check
```

Expected response:
```json
{"status":"UP"}
```

### Discord Exception Notifications

Server errors (HTTP 500+) are automatically sent to Discord webhooks in **production only**.

Notification format:
- 🚨 Error icon with status code
- Status code (500, 502, 503, etc.)
- HTTP method (GET, POST, etc.)
- Request path
- Timestamp (ISO 8601 format)
- User-Agent header
- Error message (if available)

Multiple webhooks:
- If multiple webhook URLs are configured (comma-separated), notifications are sent to all of them
- Useful for redundancy or sending to different channels (e.g., team channel + ops-alert channel)
- Each webhook failure is logged independently without affecting others

Logging:
- Success: `DEBUG` level log
- Failure: `ERROR` level log with full exception stack trace
- No `println` statements - uses SLF4J logger

---

## Troubleshooting

### Discord notifications not working

1. Check environment variable is set:
   ```bash
   echo $DISCORD_WEBHOOK_EXCEPTION_URL
   ```

2. Verify Bean is created (check application logs):
   ```
   ConditionalOnProperty matched: discord.webhook.exception.url
   ```

3. Test manually by triggering a 500 error:
   ```bash
   curl http://localhost:8080/api/test-error
   ```

4. Check application logs for errors:
   ```bash
   grep "Discord exception notification" application.log
   ```

### Local environment accidentally sending notifications

- Ensure `discord.webhook.exception.url` is NOT set in `application-local.yaml`
- Bean should NOT be created in local profile
- Check logs: No `DiscordNotificationAdapter` Bean should appear
