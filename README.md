## ChatRealtime - Hệ thống chat thời gian thực với AI kiểm duyệt nội dung

### 📌 Giới thiệu

ChatRealtime là hệ thống chat thời gian thực gồm:
- **Ứng dụng Android** cho người dùng cuối.
- **Backend Spring Boot** quản lý tài khoản, bạn bè, phòng chat, tin nhắn, WebSocket, thông báo.
- **Dịch vụ AI kiểm duyệt nội dung (FastAPI)** dùng mô hình machine learning để đánh giá mức độ độc hại của tin nhắn.

Hệ thống hướng tới:
- Trò chuyện 1-1 và nhóm theo thời gian thực.
- Kiểm soát nội dung tin nhắn (chặn, cảnh báo) để hạn chế spam, toxic, xúc phạm.
- Gửi thông báo khi có tin nhắn mới qua Firebase Cloud Messaging (FCM).

---

### Kiến trúc hệ thống

Luồng dữ liệu tổng quát:

1. **Client (Android app)**  
   - Gọi REST API tới backend qua các endpoint `/api/...` (đăng nhập, bạn bè, phòng, tin nhắn...).  
   - Kết nối WebSocket tới `/ws` để nhận tin nhắn realtime, thông báo sự kiện (lời mời kết bạn, tin nhắn mới...).  
   - Nhận thông báo đẩy từ FCM khi app ở nền hoặc không mở.

2. **Backend Spring Boot (`chatrealtime`)**
   - Cung cấp REST API cho:
     - Xác thực & đăng ký tài khoản qua email + OTP.
     - Quản lý thông tin người dùng, avatar.
     - Quản lý bạn bè, gợi ý bạn bè, lời mời kết bạn.
     - Quản lý phòng chat 1-1 và nhóm (tạo phòng, thêm/xóa thành viên, yêu cầu tham gia, duyệt...).  
     - Gửi/nhận tin nhắn (text, hình ảnh, video).
   - WebSocket (`/ws`):
     - Broadcast tin nhắn theo thời gian thực tới các client đang kết nối.
     - Gửi sự kiện kết bạn (lời mời, chấp nhận).
   - Kết nối **MySQL** lưu trữ dữ liệu người dùng, bạn bè, phòng, tin nhắn, log kiểm duyệt.
   - Gọi **Moderation AI service** qua HTTP (`WebClient`) để đánh giá nội dung tin nhắn:
     - Nhận về `label`, `score`, `action` (`allow` / `warn` / `block`).
     - Quyết định lưu/không lưu tin nhắn, cộng điểm cảnh báo, khóa tạm tài khoản khi vi phạm nhiều.
     - Ghi log vào bảng `MODERATION_LOG`.
   - Tích hợp **Firebase Admin SDK** để gửi thông báo đẩy FCM cho các thiết bị di động.
   - Gửi OTP qua **SMTP Gmail** để xác thực đăng ký tài khoản, đổi mật khẩu (qua `MailService`).

3. **Moderation AI service (`moderation-ai`)**
   - API FastAPI với endpoint:
     - `POST /moderate`: Nhận nội dung tin nhắn và trả về `label` (clean/toxic), `score` (0–1), `action` (allow/warn/block).
     - `GET /health`: healthcheck.
   - Sử dụng mô hình **Logistic Regression + TF-IDF** (scikit-learn) được huấn luyện trên tập câu tiếng Việt synthetic:
     - `train.py` tạo dữ liệu `clean/toxic`, train, lưu `vectorizer.pkl` và `model.pkl` vào thư mục `saved_model`.
   - Backend Spring Boot gọi service này qua HTTP (mặc định `http://localhost:5000/moderate`).
   - Ngoài ra có file `moderation_stub.py` (Flask) là stub kiểm duyệt đơn giản (blacklist/warnlist) dùng cho mục đích thử nghiệm, không được backend dùng mặc định.

4. **Database MySQL**
   - Dùng script `app_chat.sql` để khởi tạo schema:
     - `NGUOIDUNG`, `TAIKHOAN`: Thông tin cá nhân và tài khoản đăng nhập.
     - `PHONGCHAT`, `THANHVIEN_PHONG`, `YEUCAU_THAMGIA_NHOM`: Phòng chat, thành viên, yêu cầu tham gia.
     - `TINNHAN`, `TRANGTHAI_TINNHAN`: Nội dung tin nhắn, trạng thái gửi/đã nhận/đã đọc.
     - `BANBE`: Mối quan hệ bạn bè (chờ, đồng ý, từ chối).
     - `MODERATION_LOG`: Log chi tiết kết quả kiểm duyệt AI cho từng tin nhắn.

5. **Firebase Cloud Messaging (FCM)**
   - Backend sử dụng `FcmService` để:
     - Gửi notification đa thiết bị (multicast) cho các thành viên khác trong phòng khi có tin nhắn mới.
     - Payload `data` kèm thông tin phòng, người gửi, nội dung/ngữ cảnh tin nhắn (text/image/video...).
   - Ứng dụng Android dùng `MyFirebaseMessagingService` để nhận và hiển thị thông báo.

Sơ đồ luồng đơn giản:

> Android App ⇆ REST API (`/api/*`) ⇆ Spring Boot ⇆ MySQL  
> Android App ⇆ WebSocket (`/ws`) ⇆ Spring Boot  
> Spring Boot ⇆ HTTP (`/moderate`) ⇆ Moderation AI (FastAPI)  
> Spring Boot ⇆ Firebase Admin ⇆ FCM ⇆ Android App

---

### Công nghệ sử dụng

- **Backend**
  - Java 17
  - Spring Boot 4.0.1:
    - `spring-boot-starter-webmvc`
    - `spring-boot-starter-webflux` (dùng `WebClient` gọi AI)
    - `spring-boot-starter-data-jpa`
    - `spring-boot-starter-websocket`
    - `spring-boot-starter-mail`
  - MySQL Connector/J
  - Spring Security Crypto (BCrypt password encoder)
  - Reactor Netty (`WebClient` timeout, HTTP client)
  - Firebase Admin SDK (gửi FCM)

- **Moderation AI service**
  - Python 3.x
  - FastAPI, Uvicorn
  - scikit-learn (LogisticRegression, TfidfVectorizer)
  - pandas, numpy
  - joblib (lưu/tải model)

- **Client (Android)**
  - Android (Java, AndroidX)
  - Gradle (Kotlin DSL, AGP + google-services plugin)
  - Thư viện:
    - Volley (gửi request HTTP tới backend)
    - OkHttp (kết nối HTTP hiệu năng)
    - Picasso / Glide (load ảnh từ URL)
    - Firebase Analytics, Firebase Cloud Messaging
    - Android Lifecycle (quản lý trạng thái online/offline)

- **Database**
  - MySQL (khuyến nghị 8.x)

- **Khác**
  - WebSocket (Spring WebSocket)
  - SMTP Gmail (gửi OTP qua `spring-boot-starter-mail`)

---

### Yêu cầu môi trường

- **Backend (Spring Boot)**
  - Java Development Kit (JDK) **17**
  - Maven (hoặc dùng `mvnw` đi kèm dự án)
  - MySQL **8.x** (hoặc tương thích với Connector/J)

- **Moderation AI service**
  - Python **3.10+** (khuyến nghị, tương thích với phiên bản thư viện trong `requirements.txt`)
  - Pip / virtualenv

- **Android client**
  - Android Studio (phiên bản mới, hỗ trợ targetSdk 36)
  - Android SDK:
    - `minSdk = 24`
    - `targetSdk = 36`

- **Khác**
  - Tài khoản Gmail dùng để gửi OTP (SMTP)
  - Project Firebase đã cấu hình FCM, `google-services.json` cho Android và service account JSON cho backend.

---

### Cài đặt và chạy dự án (Local)

#### 1. Clone source

```bash
git clone <url-repo> ChatRealtime
cd ChatRealtime
```

#### 2. Chuẩn bị database MySQL

1. Tạo database và schema:
   - Mở MySQL client (Workbench, CLI, DBeaver, ...).
   - Chạy script:

   ```sql
   SOURCE app_chat.sql;
   ```

   - Script sẽ tạo database `app_chat2` và toàn bộ bảng cần thiết.

2. Cấu hình kết nối trong backend:
   - File: `chatrealtime/src/main/resources/application.properties`
   - Các key liên quan:
     - `spring.datasource.url=jdbc:mysql://localhost:3306/app_chat2?...`
     - `spring.datasource.username=...`
     - `spring.datasource.password=...`
   - Điều chỉnh lại cho đúng user/password và host của MySQL trên máy bạn.

#### 3. Cấu hình backend Spring Boot (`chatrealtime`)

File cấu hình chính: `chatrealtime/src/main/resources/application.properties`.

Các cấu hình quan trọng cần kiểm tra/chỉnh sửa:

- **Cổng dịch vụ backend**
  - `server.port=8080`

- **Base URL dùng trong logic (avatar, API cho client)**
  - `app.base-url=http://<IP-hoặc-host-backend>:8080`

- **Kết nối MySQL**
  - `spring.datasource.url=jdbc:mysql://<host>:3306/app_chat2?...`
  - `spring.datasource.username=<mysql_user>`
  - `spring.datasource.password=<mysql_password>`

- **Email (OTP, quên mật khẩu,...)**
  - `spring.mail.host=smtp.gmail.com`
  - `spring.mail.port=587`
  - `spring.mail.username=<gmail_otp_account>`
  - `spring.mail.password=<app_password>`

- **Firebase (FCM)**
  - `firebase.credentials.path=src/main/resources/firebase/<service-account>.json`
  - `firebase.project-id=<firebase_project_id>`
  - Đảm bảo file JSON service account tồn tại đúng đường dẫn.

- **Moderation AI service**
  - `moderation.enabled=true`
  - `moderation.service.url=http://localhost:5000/moderate`
  - `moderation.timeout.ms=3000`

- **Giới hạn upload file**
  - `spring.servlet.multipart.max-file-size=20MB`
  - `spring.servlet.multipart.max-request-size=20MB`

Sau khi cấu hình xong:

Chạy trực tiếp class `ChatrealtimeApplication` từ IDE (IntelliJ, Eclipse, VS Code...).

Backend sẽ chạy trên `http://localhost:8080`.

#### 4. Chạy Moderation AI service (`moderation-ai`)

1. Tạo và kích hoạt virtualenv (khuyến nghị):

```bash
cd moderation-ai
.venv\Scripts\activate         # Windows
```

2. Cài đặt phụ thuộc:

```bash
pip install -r requirements.txt
```

3. (Tuỳ chọn nhưng khuyến nghị) Huấn luyện và lưu model:

```bash
python train.py
```

Lệnh này sẽ:
- Sinh dataset tiếng Việt synthetic `clean/toxic`.
- Huấn luyện Logistic Regression + TF-IDF.
- Lưu `vectorizer.pkl` và `model.pkl` vào thư mục `saved_model/`.

4. Chạy service FastAPI:

```bash
python -m uvicorn app.main:app --reload --port 5000
```

Moderation AI sẽ lắng nghe tại `http://localhost:5000`:
- `POST /moderate`
- `GET /health`

Đảm bảo `application.properties` của backend trỏ tới đúng URL (`moderation.service.url`).

#### 5. Cấu hình & chạy ứng dụng Android (`giaodien/ChatRealtime`)

1. Mở Android Studio, chọn:
   - **Open an Existing Project** → trỏ vào thư mục `giaodien/ChatRealtime`.

2. Kiểm tra file cấu hình build:
   - `settings.gradle.kts`, `build.gradle.kts` (root), `app/build.gradle.kts`.
   - Đảm bảo Android Studio tải đủ plugin và dependency từ `mavenCentral()` và `google()`.

3. Cấu hình endpoint backend trong code Android:
   - File: `app/src/main/java/com/example/chatrealtime/Constants.java`
   - Các hằng số:
     - `BASE_URL = "http://<IP-hoặc-host-backend>:8080/api/";`
     - `WEBSOCKET_URL = "ws://<IP-hoặc-host-backend>:8080/ws";`
     - `IMAGE_BASE_URL = "http://<IP-hoặc-host-backend>:8080";`
   - Sửa lại `<IP-hoặc-host-backend>` cho phù hợp (ví dụ IP máy phát triển cùng mạng nội bộ với điện thoại Android).

4. Firebase cho Android:
   - File `app/google-services.json` cần khớp với project Firebase của bạn.
   - Đảm bảo đã enable FCM trong Firebase console.

5. Build và chạy:
   - Chọn module `app`.
   - Build APK hoặc chạy trực tiếp lên emulator/thiết bị thật.

> Gợi ý: với Android emulator, để truy cập backend trên máy host, có thể dùng `http://10.0.2.2:8080` (điều này không nằm trong code, cần tự chỉnh lại `Constants.java` cho phù hợp môi trường của bạn).

---

### Cấu trúc thư mục

Ở mức cao:

- `chatrealtime/` – Backend Spring Boot
  - `pom.xml` – cấu hình Maven, dependency Spring Boot, Firebase, MySQL...
  - `src/main/java/com/example/chatrealtime/`
    - `ChatrealtimeApplication.java` – entry point backend.
    - `config/`
      - `WebSocketConfig.java` – cấu hình endpoint WebSocket `/ws`.
      - `ModerationConfig.java` – cấu hình `WebClient` gọi Moderation AI.
      - `WebConfig.java` – cấu hình Web MVC (CORS, static resources, ... nếu có).
    - `controller/`
      - `AuthController.java` – đăng ký, gửi OTP, xác thực OTP, đăng nhập.
      - `UserController.java` – tạo/cập nhật thông tin người dùng, avatar, trạng thái online/offline, cập nhật FCM token.
      - `FriendController.java` – quản lý bạn bè (gửi/huỷ/chấp nhận/từ chối lời mời, danh sách bạn, gợi ý bạn bè).
      - `GroupController.java` – xử lý yêu cầu tham gia phòng nhóm, duyệt/ từ chối.
      - `ChatRoomController.java` – tạo phòng chat, mở chat 1-1, cập nhật phòng, quản lý thành viên phòng, tìm kiếm phòng/bạn.
      - `MessageController.java` – gửi tin nhắn text/file, upload file (ảnh/video), lấy danh sách tin nhắn.
      - `RoomQueryController.java` – các API truy vấn phòng chat (chi tiết trong service).
      - `ChatController.java` – các API liên quan chat/websocket khác (nếu được sử dụng).
    - `service/`
      - `MessageService.java` – logic gửi tin nhắn, lưu DB, cập nhật trạng thái, gọi AI kiểm duyệt, gửi thông báo FCM.
      - `ModerationService.java` – gọi Moderation AI, chuyển đổi kết quả thành `ModerationDecision`, lưu log.
      - `FriendService.java`, `GroupService.java`, `PrivateChatService.java`, `RoomQueryService.java` – logic nghiệp vụ về bạn bè, nhóm, phòng chat.
      - `FcmService.java` – khởi tạo FirebaseApp, gửi FCM đơn lẻ/multicast.
      - `MailService.java`, `OtpService.java` – gửi email OTP, quản lý OTP.
    - `entity/` – ánh xạ JPA tới các bảng: `TaiKhoan`, `NguoiDung`, `PhongChat`, `TinNhan`, `ThanhVienPhong`, `TrangThaiTinNhan`, `YeuCauThamGiaNhom`, `BanBe`, `ModerationLog`, ...
    - `repository/` – các repository JPA tương ứng (truy vấn danh sách bạn, phòng, tin nhắn, log...).
    - `websocket/`
      - `ChatWebSocketHandler.java` – xử lý message WebSocket (init, join_room, chat_message, friend_request, friend_accepted...).
      - `WebSocketSessionManager.java` – quản lý map userId ↔ session.
    - `dto/`
      - `SocketMessage.java` – payload WebSocket.
      - `ModerationRequest.java`, `ModerationResponse.java` – DTO gửi/nhận từ Moderation AI.
  - `src/main/resources/`
    - `application.properties` – cấu hình môi trường backend.
    - `firebase/...json` – service account cho Firebase (đường dẫn cấu hình trong properties).
    - `static/`, `templates/` – static content (nếu dùng).
  - `uploads/`
    - `avatars/` – thư mục ảnh đại diện người dùng được upload.
    - `chat/` – file ảnh/video đính kèm trong phòng chat.

- `moderation-ai/` – Dịch vụ AI kiểm duyệt
  - `requirements.txt` – dependency Python (FastAPI, scikit-learn, ...).
  - `app/`
    - `main.py` – khởi tạo FastAPI, endpoint `/moderate` và `/health`, load model từ `saved_model/`.
    - `model.py` – lớp `ToxicityModel` (TF-IDF + Logistic Regression).
    - `schemas.py` – Pydantic models `ModerateRequest`, `ModerateResponse`.
    - `utils.py` – `ModelRegistry`, logic map `score` → action.
  - `train.py` – script sinh dataset và huấn luyện/ lưu model.
  - `saved_model/` – chứa `vectorizer.pkl`, `model.pkl` sau khi train.
  - `data/` – chứa `dev.csv`, `test.csv`, `train.csv` là dữ liệu để train AI.

- `giaodien/ChatRealtime/` – Ứng dụng Android
  - `settings.gradle.kts`, `build.gradle.kts` – cấu hình toàn project.
  - `app/`
    - `build.gradle.kts` – cấu hình module app (min/target SDK, dependency Firebase, Volley, OkHttp, Glide...).
    - `src/main/java/com/example/chatrealtime/`
      - `Constants.java` – định nghĩa `BASE_URL`, `WEBSOCKET_URL`, `IMAGE_BASE_URL`.
      - `activity/` – các màn hình chính (đăng nhập, đăng ký, tạo thông tin user, trang chủ, chat, tạo nhóm, yêu cầu bạn bè...).
      - `adapter/` – adapter cho RecyclerView (danh sách bạn bè, phòng chat, tin nhắn, gợi ý, yêu cầu kết bạn...).
      - `model/` – model cục bộ: `Message`, `Room`, `FriendRequest`, `MessageModerationStatus`, `SessionManager`, `WebSocketService`...
      - `network/VolleyMultipartRequest` – hỗ trợ upload file multipart lên backend.
      - `service/MyFirebaseMessagingService` – nhận và xử lý notification FCM.
      - `status/` – quản lý lifecycle toàn app, update online/offline.
      - `database/ChatDatabaseHelper` – SQLite/local storage (nếu sử dụng).
    - `src/main/res/` – layout XML, drawable, string, style, icon, animation...
    - `google-services.json` – cấu hình Firebase cho Android.

- `app_chat.sql` – script khởi tạo database MySQL `app_chat2` (bảng, khóa ngoại, index).

---

### Cấu hình biến môi trường / cấu hình ứng dụng

#### Backend – `application.properties`

Các key chính (giá trị trong code có thể đã được gán mặc định, nên chỉnh lại cho phù hợp môi trường thật):

- **Server & ứng dụng**
  - `server.port` – cổng chạy backend (mặc định `8080`).
  - `spring.application.name` – tên ứng dụng Spring Boot.
  - `app.base-url` – base URL phía backend, dùng để build URL ảnh, API cho client.

- **Database MySQL**
  - `spring.datasource.url` – JDBC URL đến database `app_chat2`.
  - `spring.datasource.username` – user kết nối database.
  - `spring.datasource.password` – mật khẩu database.
  - `spring.datasource.driver-class-name` – driver MySQL.

- **JPA/Hibernate**
  - `spring.jpa.hibernate.naming.physical-strategy`
  - `spring.jpa.hibernate.naming.implicit-strategy`  
  → Giữ nguyên để mapping đúng với schema trong `app_chat.sql`.

- **Email (SMTP)**
  - `spring.mail.host`
  - `spring.mail.port`
  - `spring.mail.username`
  - `spring.mail.password`
  - `spring.mail.properties.mail.smtp.auth`
  - `spring.mail.properties.mail.smtp.starttls.enable`
  - `spring.mail.default-encoding`

- **Firebase / FCM**
  - `firebase.credentials.path` – đường dẫn tới file service account JSON (có thể là đường dẫn file system hoặc trong classpath).
  - `firebase.project-id` – ID project Firebase.
  - `logging.level.com.example.chatrealtime.service.FcmService` – mức log cho FCM (DEBUG/INFO...).

- **Upload**
  - `spring.servlet.multipart.max-file-size` – giới hạn kích thước file upload.
  - `spring.servlet.multipart.max-request-size` – giới hạn kích thước request multipart.

- **Moderation AI**
  - `moderation.enabled` – bật/tắt gọi AI (true/false).
  - `moderation.service.url` – URL endpoint `/moderate` của Moderation AI.
  - `moderation.timeout.ms` – timeout khi gọi AI (ms).

#### Moderation AI service

Hiện mã nguồn không sử dụng biến môi trường riêng, chỉ phụ thuộc:
- `requirements.txt` – phiên bản thư viện.
- Thư mục `saved_model/` chứa `vectorizer.pkl` và `model.pkl`.

#### Android client

Cấu hình kết nối backend nằm trong `Constants.java`:
- `BASE_URL` – base URL REST API (kết thúc bằng `/api/`).
- `WEBSOCKET_URL` – URL WebSocket (`ws://.../ws`).
- `IMAGE_BASE_URL` – base URL để load ảnh/ file từ backend.

Ngoài ra, cấu hình Firebase FCM nằm trong `google-services.json`.

---

### Chức năng chính

- **Quản lý tài khoản & người dùng**
  - Đăng ký tài khoản qua email, gửi OTP xác thực (`AuthController`, `MailService`, `OtpService`).
  - Xác thực OTP và tạo tài khoản với mật khẩu được băm bằng BCrypt.
  - Đăng nhập bằng email/mật khẩu, trả về thông tin tài khoản (`maTaiKhoan`, `maNguoiDung`, trạng thái).
  - Tạo thông tin người dùng (họ tên, giới tính, ngày sinh, số điện thoại, avatar).
  - Cập nhật thông tin cá nhân và avatar.
  - Cập nhật trạng thái online/offline khi người dùng mở/đóng app.

- **Quản lý bạn bè**
  - Gửi lời mời kết bạn, huỷ lời mời, chấp nhận, từ chối.
  - Xem danh sách bạn bè, kiểm tra trạng thái quan hệ bạn bè.
  - Lấy danh sách yêu cầu kết bạn đang chờ.
  - Gợi ý bạn bè dựa trên thông tin quan hệ hiện có.

- **Phòng chat & nhóm**
  - Chat 1-1 hoặc chat nhóm:
    - Tự động kiểm tra phòng 1-1 đã tồn tại hay chưa, nếu có thì mở lại, nếu chưa thì tạo mới.
  - Tạo phòng chat nhóm với danh sách thành viên được chọn.
  - Cập nhật thông tin phòng (tên, kiểu nhóm, ảnh đại diện).
  - Thêm/xóa thành viên trong phòng (trưởng nhóm mới có quyền).
  - Đánh dấu xóa phòng phía người dùng (không xóa hẳn dữ liệu phòng).
  - Phân loại phòng: public/private, nhóm/1-1.
  - Hệ thống yêu cầu tham gia phòng (YEUCAU_THAMGIA_NHOM) cho nhóm private:
    - Gửi yêu cầu tham gia.
    - Trưởng nhóm duyệt/từ chối.
    - Trạng thái pending/approved/rejected.

- **Tin nhắn & realtime**
  - Gửi tin nhắn:
    - Text.
    - Ảnh/video/file (upload qua `/api/chat/upload-file`, lưu file vào `uploads/chat/`).
  - Lưu tin nhắn vào bảng `TINNHAN` với:
    - Loại tin (`loaiTinNhan`): text/image/video...
    - Trạng thái kiểm duyệt (`trangThaiKiemDuyet`): clean/warning/blocked.
    - Điểm kiểm duyệt (`diemKiemDuyet`).
  - Trạng thái tin nhắn theo người nhận (`TRANGTHAI_TINNHAN`):
    - `sent`, `delivered`, `read`.
  - WebSocket:
    - Sự kiện `init`, `join_room`, `chat_message`, `friend_request`, `friend_accepted`...
    - Broadcast tin nhắn realtime tới các user đang kết nối.
  - FCM:
    - Gửi notification tới các thành viên khác trong phòng khi có tin nhắn mới (title = tên người gửi, body = nội dung hoặc message “Đã gửi hình ảnh/video”).

- **AI kiểm duyệt nội dung**
  - Mọi tin nhắn text gửi đi đều được gửi sang Moderation AI:
    - Nếu `action = block`: Không lưu tin nhắn, cộng điểm cảnh cáo mạnh, ghi log, có thể khóa tài khoản khi vượt ngưỡng.
    - Nếu `action = warn`: Lưu tin nhắn nhưng gắn trạng thái warning, cộng điểm cảnh cáo nhẹ, ghi log.
    - Nếu `action = allow`: Lưu tin nhắn với trạng thái clean.
  - Cơ chế tích điểm cảnh cáo & khóa tài khoản:
    - `diemCanhCao`, `soLanWarningHomNay`, `ngayTinhWarning`, `thoiGianKhoa`, `trangThai` trong bảng `TAIKHOAN`.
    - Khi quá ngưỡng (theo số lần cảnh báo trong ngày/điểm tích lũy) tài khoản có thể bị chuyển sang trạng thái `banned`.
    - Tự động mở khóa sau một khoảng thời gian (theo logic trong `MessageService`).
  - Lưu chi tiết log vào bảng `MODERATION_LOG` để audit, theo dõi hành vi vi phạm.

- **Push notification & trạng thái**
  - Lưu token FCM cho mỗi tài khoản (`TAIKHOAN.token`) từ client gửi lên (`/api/user/update-token`).
  - Gửi thông báo push cho các thiết bị tương ứng khi có sự kiện (tin nhắn mới).
  - Cập nhật trạng thái online/offline khi app thay đổi lifecycle (Android lifecycle + API `/api/user/update-status`).

---

### Tài khoản demo

Trong source hiện tại:
- Không có sẵn script SQL chèn dữ liệu mẫu cho người dùng/tài khoản.
- Không có cấu hình hard-code tài khoản demo trong backend.

Do đó, để trải nghiệm hệ thống, bạn cần:
- Đăng ký tài khoản mới từ ứng dụng Android (gửi OTP qua email).
- Sau khi xác thực OTP, tạo thông tin người dùng và sử dụng bình thường.

Bạn cũng có thể tự thêm dữ liệu demo bằng cách chạy thêm các lệnh `INSERT` vào database `app_chat2` nếu muốn.

---