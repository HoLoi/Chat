package com.example.chatrealtime.service;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.chatrealtime.dto.ModerationRequest;
import com.example.chatrealtime.dto.ModerationResponse;
import com.example.chatrealtime.entity.ModerationLog;
import com.example.chatrealtime.repository.ModerationLogRepository;

@Service
public class ModerationService {

    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);

    private final ModerationLogRepository logRepo;
    private final WebClient moderationClient;
    private final boolean moderationEnabled;
    private final long timeoutMs;

    public ModerationService(
            ModerationLogRepository logRepo,
            WebClient moderationWebClient,
            @Value("${moderation.enabled:true}") boolean moderationEnabled,
            @Value("${moderation.timeout.ms:3000}") long timeoutMs
    ) {
        this.logRepo = logRepo;
        this.moderationClient = moderationWebClient;
        this.moderationEnabled = moderationEnabled;
        this.timeoutMs = timeoutMs;
    }

    public ModerationDecision evaluate(String content, Integer userId, Integer roomId) {
        String safeContent = content != null ? content : "";

        if (!moderationEnabled) {
            return allowDecision();
        }

        try {
            ModerationRequest body = new ModerationRequest(safeContent, userId, roomId);

            ModerationResponse response = moderationClient.post()
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(ModerationResponse.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            ModerationDecision decision = buildDecision(response);
            log.debug("AI moderation result action={} score={} label={} room={} user={} ",
                    decision.action(), decision.score(), decision.label(), roomId, userId);
            return decision;
        } catch (Exception ex) {
            log.warn("AI moderation fallback to allow. cause={}", ex.getMessage());
            return allowDecision();
        }
    }

    private ModerationDecision buildDecision(ModerationResponse response) {
        double score = response != null && response.getScore() != null ? response.getScore() : 0.0;
        String action = deriveAction(score);
        String label = response != null && response.getLabel() != null ? response.getLabel() : "clean";
        String severity = deriveSeverity(score);
        return new ModerationDecision(action, label, score, severity, null);
    }

    private String deriveAction(double score) {
        if (score < 0.6) return "allow";
        if (score < 0.85) return "warn";
        return "block";
    }

    private String deriveSeverity(double score) {
        if (score < 0.6) return "mild";
        if (score < 0.85) return "medium";
        return "severe";
    }

    private ModerationDecision allowDecision() {
        return new ModerationDecision("allow", "clean", 0.0, "mild", null);
    }

    public void log(Integer maTinNhan, Integer maPhongChat, Integer senderId, ModerationDecision decision, String rawContent) {
        ModerationLog moderationLog = new ModerationLog();
        moderationLog.setMaTinNhan(maTinNhan);
        moderationLog.setMaPhongChat(maPhongChat);
        moderationLog.setMaTaiKhoanGui(senderId);
        moderationLog.setNhanViPham(decision.label());
        moderationLog.setMucDoViPham(decision.severity());
        moderationLog.setDiemScore(decision.score());
        moderationLog.setHanhDong(decision.action());
        moderationLog.setNoiDungGoc(rawContent);
        logRepo.save(moderationLog);
    }

    public record ModerationDecision(String action, String label, double score, String severity, String message) {
    }
}
