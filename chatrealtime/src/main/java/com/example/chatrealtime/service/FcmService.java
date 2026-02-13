package com.example.chatrealtime.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;

@Service
public class FcmService {

	private static final Logger log = LoggerFactory.getLogger(FcmService.class);

	public FcmService(@Value("${firebase.credentials.path:}") String credentialsPath,
					  @Value("${firebase.project-id:}") String projectId) {
		initialize(credentialsPath, projectId);
	}

	private void initialize(String credentialsPath, String projectId) {
		if (!FirebaseApp.getApps().isEmpty()) {
			return;
		}

		if (credentialsPath == null || credentialsPath.isBlank()) {
			log.warn("firebase.credentials.path is empty; FCM will be disabled until configured");
			return;
		}

		try (InputStream serviceAccount = openCredentials(credentialsPath)) {
			if (serviceAccount == null) {
				log.error("Cannot open firebase credentials at {}", credentialsPath);
				return;
			}

			FirebaseOptions.Builder builder = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount));

			if (projectId != null && !projectId.isBlank()) {
				builder.setProjectId(projectId);
			}

			FirebaseApp.initializeApp(builder.build());
			log.info("Initialized FirebaseApp for FCM");
		} catch (IOException e) {
			log.error("Failed to initialize FirebaseApp: {}", e.getMessage());
		}
	}

	/**
	 * Mở file credentials từ filesystem (ưu tiên) hoặc classpath (khi chạy jar).
	 */
	private InputStream openCredentials(String path) throws IOException {
		File file = new File(path);
		if (file.exists()) {
			return new FileInputStream(file);
		}

		// Thử đọc từ classpath khi đường dẫn là relative vào resources
		ClassPathResource resource = new ClassPathResource(path);
		if (resource.exists()) {
			return resource.getInputStream();
		}

		return null;
	}

	public void sendToToken(String token, String title, String body, Map<String, String> data) {
		if (token == null || token.isBlank()) return;

		Message.Builder builder = Message.builder()
				.setToken(token)
				.setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build())
				.putData("title", title)
				.putData("body", body);

		if (data != null) {
			builder.putAllData(data);
		}

		try {
			log.info("FCM sendToToken -> token={}, title={}, body={}, data={}", token, title, body, data);
			String messageId = FirebaseMessaging.getInstance().send(builder.build());
			log.info("Send to token success: id={}", messageId);
		} catch (FirebaseMessagingException e) {
			log.warn("Send to token failed: code={}, msg={}, response={}",
					e.getMessagingErrorCode(), e.getMessage(), e.getHttpResponse());
		} catch (Exception e) {
			log.warn("Send to token failed (generic): {}", e.getMessage());
		}
	}

	public void sendMulticast(List<String> tokens, String title, String body, Map<String, String> data) {
		if (tokens == null || tokens.isEmpty()) return;

		log.debug("FCM sendMulticast -> tokens: {}", tokens.size());

		MulticastMessage.Builder builder = MulticastMessage.builder()
				.addAllTokens(tokens)
				.setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build())
				.putData("title", title)
				.putData("body", body);

		if (data != null) {
			builder.putAllData(data);
		}

		MulticastMessage message = builder.build();

		try {
			log.info("FCM sending multicast -> tokens={}, title={}, body={}, data={}", tokens.size(), title, body, data);
			BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
			log.info("Send multicast result: success={}, failure={}", response.getSuccessCount(), response.getFailureCount());
			if (response.getFailureCount() > 0) {
				StringBuilder sb = new StringBuilder();
				for (SendResponse r : response.getResponses()) {
					if (!r.isSuccessful()) {
						sb.append("[error=").append(r.getException().getMessagingErrorCode())
							.append(", msg=").append(r.getException().getMessage()).append("]");
					}
				}
				log.warn("Send multicast partial failures: success={}, failure={}, detail={}",
						response.getSuccessCount(), response.getFailureCount(), sb.toString());
			}
		} catch (FirebaseMessagingException e) {
			log.warn("Send multicast failed: code={}, msg={}, response={}",
					e.getMessagingErrorCode(), e.getMessage(), e.getHttpResponse());
		} catch (Exception e) {
			log.warn("Send multicast failed (generic): {}", e.getMessage());
		}
	}
}
