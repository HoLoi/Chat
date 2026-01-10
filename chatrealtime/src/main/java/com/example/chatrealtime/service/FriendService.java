package com.example.chatrealtime.service;

import com.example.chatrealtime.entity.BanBe;
import com.example.chatrealtime.entity.BanBeId;
import com.example.chatrealtime.repository.BanBeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class FriendService {
    private final BanBeRepository banBeRepo;

    public FriendService(BanBeRepository banBeRepo) {
        this.banBeRepo = banBeRepo;
    }

    @Transactional
    public void sendRequest(Integer myId, Integer friendId) {
        if (myId.equals(friendId)) {
            throw new RuntimeException("Không thể tự kết bạn với chính mình");
        }

        banBeRepo.findStatus(myId, friendId).ifPresent(s -> {
            throw new RuntimeException("Đã tồn tại quan hệ bạn bè");
        });

        BanBe bb = new BanBe();
        bb.setId(new BanBeId(myId, friendId));
        bb.setTrangThai("cho");

        banBeRepo.save(bb);
    }

    @Transactional
    public void cancelRequest(Integer myId, Integer friendId) {
        banBeRepo.deleteById(new BanBeId(myId, friendId));
    }

    @Transactional
    public void unfriend(Integer myId, Integer friendId) {
        banBeRepo.deleteById(new BanBeId(myId, friendId));
        banBeRepo.deleteById(new BanBeId(friendId, myId));
    }
}
