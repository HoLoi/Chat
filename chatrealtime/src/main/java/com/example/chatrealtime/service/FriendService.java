package com.example.chatrealtime.service;

import com.example.chatrealtime.entity.BanBe;
import com.example.chatrealtime.entity.BanBeId;
import com.example.chatrealtime.repository.BanBeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class FriendService {

    private final BanBeRepository repo;

    public FriendService(BanBeRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void sendRequest(Integer fromId, Integer toId) {

        System.out.println("SEND FRIEND REQUEST: " + fromId + " -> " + toId);

        if (fromId.equals(toId))
            throw new RuntimeException("Không thể gửi lời mời cho chính mình");

        // 🚫 chỉ chặn nếu CHÍNH CHIỀU đó còn hiệu lực
        if (repo.existsActiveRequest(fromId, toId))
            throw new RuntimeException("Đã gửi lời mời trước đó");

        BanBe bb = new BanBe();
        bb.setId(new BanBeId(fromId, toId));
        bb.setTrangThai("cho");

        repo.save(bb);
    }


    @Transactional
    public void acceptRequest(Integer fromId, Integer toId) {

        int updated = repo.acceptRequest(fromId, toId);
        if (updated == 0)
            throw new RuntimeException("Không tìm thấy lời mời hợp lệ");

        repo.insertReverseFriend(fromId, toId);
    }

    @Transactional
    public void rejectRequest(Integer fromId, Integer toId) {
        repo.deleteRelation(fromId, toId);
    }

    @Transactional
    public void cancelRequest(Integer fromId, Integer toId) {
        repo.deleteRelation(fromId, toId);
    }

    @Transactional
    public void unfriend(Integer a, Integer b) {
        repo.deleteRelation(a, b);
    }
}
