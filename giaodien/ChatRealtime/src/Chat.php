<?php
// src/Chat.php
namespace MyApp;
use Ratchet\MessageComponentInterface;
use Ratchet\ConnectionInterface;

echo "💡 Chat.php loaded from: " . __FILE__ . "\n";

class Chat implements MessageComponentInterface {
    protected $clients;
    protected $userConnections; // userId => connection
    protected $userRooms;       // userId => roomId

    public function __construct() {
        $this->clients = new \SplObjectStorage;
        $this->userConnections = [];
        $this->userRooms = [];
        echo "✅ WebSocket Chat Server started...\n";
    }

    public function onOpen(ConnectionInterface $conn) {
        $this->clients->attach($conn);
        echo "✅ New connection ({$conn->resourceId})\n";
    }

    public function onMessage(ConnectionInterface $from, $msg) {
        $data = json_decode($msg, true);
        if (!$data) return;

        $type = $data['type'] ?? '';

        switch ($type) {
            // 🔹 Khi client mới đăng ký userId
            case 'init':
                $userId = $data['userId'] ?? null;
                if ($userId) {
                    $this->userConnections[$userId] = $from;
                    echo "🔗 User {$userId} connected (resource {$from->resourceId})\n";
                    $from->send(json_encode([
                        'type' => 'init',
                        'message' => "User {$userId} registered successfully"
                    ], JSON_UNESCAPED_UNICODE));
                }
                break;

            // 🔹 Khi client tham gia phòng chat
            case 'join_room':
                $userId = $data['userId'] ?? null;
                $roomId = $data['roomId'] ?? null;
                if ($userId && $roomId) {
                    $this->userRooms[$userId] = $roomId;
                    echo "🏠 User {$userId} joined room {$roomId}\n";
                }
                break;

            // 🔹 Khi client gửi tin nhắn chat
            case 'chat_message':
                $maPhongChat = $data['maPhongChat'] ?? null;
                $maTaiKhoanGui = $data['maTaiKhoanGui'] ?? null;
                $noiDung = $data['noiDung'] ?? '';
                $loaiTinNhan = $data['loaiTinNhan'] ?? 'text';

                if ($maPhongChat && $maTaiKhoanGui) {
                    echo "💬 Tin nhắn từ {$maTaiKhoanGui} trong phòng {$maPhongChat}: {$noiDung}\n";

                    // 🔸 Gửi tới tất cả người đang ở cùng phòng
                    foreach ($this->userConnections as $userId => $connUser) {
                        if (($this->userRooms[$userId] ?? null) == $maPhongChat) {
                            $connUser->send(json_encode([
                                'type' => 'chat_message',
                                'maPhongChat' => $maPhongChat,
                                'maTaiKhoanGui' => $maTaiKhoanGui,
                                'noiDung' => $noiDung,
                                'loaiTinNhan' => $loaiTinNhan,
                                'thoiGianGui' => date('Y-m-d H:i:s')
                            ], JSON_UNESCAPED_UNICODE));
                        }
                    }
                } else {
                    echo "⚠️ Thiếu thông tin maPhongChat hoặc maTaiKhoanGui.\n";
                }
                break;

            // 🔹 Khi người dùng gửi lời mời kết bạn
            case 'friend_request':
                $toUser = $data['toUser'] ?? null;
                $fromUser = $data['fromUser'] ?? null;
                $message = $data['message'] ?? '';

                if ($toUser && isset($this->userConnections[$toUser])) {
                    $this->userConnections[$toUser]->send(json_encode([
                        'type' => 'friend_request',
                        'fromUser' => $fromUser,
                        'message' => $message
                    ], JSON_UNESCAPED_UNICODE));
                    echo "🤝 Friend request sent from {$fromUser} → {$toUser}\n";
                } else {
                    echo "⚠️ User {$toUser} not online, cannot deliver friend request.\n";
                }
                break;

            // Khi người dùng hủy lời mời kết bạn
            case 'friend_cancel':
                $fromUser = $data['fromUser'] ?? null;
                $toUser = $data['toUser'] ?? null;

                if ($toUser && isset($this->userConnections[$toUser])) {
                    $this->userConnections[$toUser]->send(json_encode([
                        'type' => 'friend_cancel',
                        'fromUser' => $fromUser,
                        'message' => 'Người dùng đã hủy lời mời kết bạn'
                    ], JSON_UNESCAPED_UNICODE));

                    echo "❌ Friend request canceled from {$fromUser} → {$toUser}\n";
                } else {
                    echo "⚠️ User {$toUser} not online, cannot deliver friend cancel.\n";
                }
                break;

            // 🔹 Khi đồng ý kết bạn
            case 'friend_accepted':
                $fromUser = $data['fromUser'] ?? null;
                $toUser = $data['toUser'] ?? null;

                if (!$fromUser || !$toUser) {
                    echo "⚠️ friend_accepted thiếu userId.\n";
                    break;
                }

                if (isset($this->userConnections[$toUser])) {
                    $this->userConnections[$toUser]->send(json_encode([
                        'type' => 'friend_accepted',
                        'fromUser' => $fromUser,
                        'toUser' => $toUser,
                        'message' => 'Đã chấp nhận lời mời kết bạn.'
                    ], JSON_UNESCAPED_UNICODE));
                }

                if (isset($this->userConnections[$fromUser])) {
                    $this->userConnections[$fromUser]->send(json_encode([
                        'type' => 'friend_accepted',
                        'fromUser' => $fromUser,
                        'toUser' => $toUser,
                        'message' => 'Đã chấp nhận kết bạn thành công.'
                    ], JSON_UNESCAPED_UNICODE));
                }
                break;

            default:
                echo "⚠️ Unknown message type: {$type}\n";
                break;
        }
    }

    public function onClose(ConnectionInterface $conn) {
        $this->clients->detach($conn);
        foreach ($this->userConnections as $userId => $connection) {
            if ($connection === $conn) {
                unset($this->userConnections[$userId], $this->userRooms[$userId]);
                echo "❌ User {$userId} disconnected\n";
                break;
            }
        }
    }

    public function onError(ConnectionInterface $conn, \Exception $e) {
        echo "💥 Error: {$e->getMessage()}\n";
        $conn->close();
    }
}
