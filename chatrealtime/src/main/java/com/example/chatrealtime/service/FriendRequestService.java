package com.example.chatrealtime.service;

import com.example.chatrealtime.repository.BanBeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class FriendRequestService {
    private final BanBeRepository banBeRepo;

    public FriendRequestService(BanBeRepository banBeRepo) {
        this.banBeRepo = banBeRepo;
    }

    @Transactional
    public void acceptRequest(Integer fromId, Integer toId) {
        // cập nhật chiều gốc
        banBeRepo.acceptRequest(fromId, toId);

        // thêm chiều ngược
        banBeRepo.insertReverseFriend(fromId, toId);
    }

    @Transactional
    public void rejectRequest(Integer fromId, Integer toId) {
        banBeRepo.rejectRequest(fromId, toId);
    }
}
