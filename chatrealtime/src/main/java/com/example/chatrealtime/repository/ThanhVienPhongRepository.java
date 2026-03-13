package com.example.chatrealtime.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.chatrealtime.entity.ThanhVienPhong;
import com.example.chatrealtime.entity.ThanhVienPhongId;

import jakarta.transaction.Transactional;

@Repository
public interface ThanhVienPhongRepository
        extends JpaRepository<ThanhVienPhong, ThanhVienPhongId> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE ThanhVienPhong tv
        SET tv.ngayXoa = CURRENT_TIMESTAMP
        WHERE tv.id.maPhongChat = :maPhongChat
          AND tv.id.maTaiKhoan = :maTaiKhoan
    """)
    int updateNgayXoa(
            @Param("maPhongChat") Integer maPhongChat,
            @Param("maTaiKhoan") Integer maTaiKhoan
    );

        @Modifying
        @Transactional
        @Query("""
                UPDATE ThanhVienPhong tv
                SET tv.ngayXoa = CURRENT_TIMESTAMP
                WHERE tv.id.maPhongChat = :maPhongChat
        """)
        int updateNgayXoaForAllMembers(@Param("maPhongChat") Integer maPhongChat);

                @Modifying
                @Transactional
                @Query("""
                                UPDATE ThanhVienPhong tv
                                SET tv.ngayXoa = NULL
                                WHERE tv.id.maPhongChat = :maPhongChat
                                        AND tv.ngayXoa IS NOT NULL
                """)
                int restoreDeletedMembers(@Param("maPhongChat") Integer maPhongChat);

                @Modifying
                @Transactional
                @Query("""
                                UPDATE ThanhVienPhong tv
                                SET tv.ngayXoa = NULL
                                WHERE tv.id.maPhongChat = :maPhongChat
                                        AND tv.id.maTaiKhoan <> :excludeUserId
                                        AND tv.ngayXoa IS NOT NULL
                """)
                int restoreDeletedMembersExcept(
                                @Param("maPhongChat") Integer maPhongChat,
                                @Param("excludeUserId") Integer excludeUserId
                );

                @Modifying
                @Transactional
                @Query("""
                                UPDATE ThanhVienPhong tv
                                SET tv.ngayXoa = NULL
                                WHERE tv.id.maPhongChat = :maPhongChat
                                        AND tv.id.maTaiKhoan = :maTaiKhoan
                                        AND tv.ngayXoa IS NOT NULL
                """)
                int restoreDeletedMember(
                                                @Param("maPhongChat") Integer maPhongChat,
                                                @Param("maTaiKhoan") Integer maTaiKhoan
                );

    @Query("""
        SELECT new map(
            tk.maTaiKhoan as maTaiKhoan,
            nd.tenNguoiDung as tenNguoiDung,
                        nd.anhDaiDien as anhDaiDien,
                        tv.vaiTro as vaiTro
        )
        FROM ThanhVienPhong tv
        JOIN TaiKhoan tk ON tv.id.maTaiKhoan = tk.maTaiKhoan
        JOIN tk.nguoiDung nd
        WHERE tv.id.maPhongChat = :maPhongChat
                    AND tv.ngayXoa IS NULL
                    AND tv.trangThaiThamGia = 'approved'
    """)
    List<Map<String, Object>> getMembers(
            @Param("maPhongChat") Integer maPhongChat
    );

    @Query("""
        SELECT new map(
            tk.maTaiKhoan as maTaiKhoan,
            nd.tenNguoiDung as tenNguoiDung,
            nd.anhDaiDien as anhDaiDien,
            tv.vaiTro as vaiTro
        )
        FROM ThanhVienPhong tv
        JOIN TaiKhoan tk ON tv.id.maTaiKhoan = tk.maTaiKhoan
        JOIN tk.nguoiDung nd
        WHERE tv.id.maPhongChat = :maPhongChat
            AND tv.trangThaiThamGia = 'approved'
    """)
    List<Map<String, Object>> getMembersIncludeDeleted(
            @Param("maPhongChat") Integer maPhongChat
    );

    @Query("""
        SELECT tv.id.maTaiKhoan
        FROM ThanhVienPhong tv
        WHERE tv.id.maPhongChat = :maPhongChat
                    AND tv.ngayXoa IS NULL
                    AND tv.trangThaiThamGia = 'approved'
    """)
    List<Integer> getMemberIds(
            @Param("maPhongChat") Integer maPhongChat
    );

    @Query("""
        SELECT tv.id.maTaiKhoan
        FROM ThanhVienPhong tv
        WHERE tv.id.maPhongChat = :maPhongChat
                    AND tv.trangThaiThamGia = 'approved'
    """)
    List<Integer> getMemberIdsForDelivery(
            @Param("maPhongChat") Integer maPhongChat
    );

    @Modifying
    @Transactional
    @Query("""
                                DELETE FROM ThanhVienPhong tv
        WHERE tv.id.maPhongChat = :maPhongChat
          AND tv.id.maTaiKhoan = :maTaiKhoan
    """)
    int removeMember(
            @Param("maPhongChat") Integer maPhongChat,
            @Param("maTaiKhoan") Integer maTaiKhoan
    );

        // Dùng khi cần kiểm tra thành viên đang hoạt động (chưa xóa)
        boolean existsByIdMaPhongChatAndIdMaTaiKhoanAndNgayXoaIsNull(Integer maPhongChat, Integer maTaiKhoan);

        // Dùng khi cần bỏ qua ràng buộc ngàyXoa (ví dụ hiển thị avatar 1-1 sau khi xóa cuộc trò chuyện)
        boolean existsByIdMaPhongChatAndIdMaTaiKhoan(Integer maPhongChat, Integer maTaiKhoan);

        @Query("""
                SELECT new map(
                        tv.id.maTaiKhoan as id,
                        tv.id.maTaiKhoan as maTaiKhoan,
                        nd.tenNguoiDung as tenNguoiDung,
                        nd.anhDaiDien as anhDaiDien,
                        tv.trangThaiThamGia as trangThai
                )
                FROM ThanhVienPhong tv
                JOIN TaiKhoan tk ON tv.id.maTaiKhoan = tk.maTaiKhoan
                JOIN tk.nguoiDung nd
                WHERE tv.id.maPhongChat = :maPhongChat
                        AND tv.ngayXoa IS NULL
                        AND tv.trangThaiThamGia = 'pending'
        """)
        List<Map<String, Object>> getPendingMembers(@Param("maPhongChat") Integer maPhongChat);

        @Modifying
        @Transactional
        @Query("""
                UPDATE ThanhVienPhong tv
                SET tv.trangThaiThamGia = 'approved',
                        tv.nguoiDuyet = :nguoiDuyet,
                        tv.ngayThamGia = CURRENT_TIMESTAMP
                WHERE tv.id.maPhongChat = :maPhongChat
                    AND tv.id.maTaiKhoan = :maTaiKhoan
        """)
        int approveMember(
                        @Param("maPhongChat") Integer maPhongChat,
                        @Param("maTaiKhoan") Integer maTaiKhoan,
                        @Param("nguoiDuyet") Integer nguoiDuyet
        );

        @Modifying
        @Transactional
        @Query("""
                UPDATE ThanhVienPhong tv
                SET tv.trangThaiThamGia = 'rejected',
                        tv.nguoiDuyet = :nguoiDuyet
                WHERE tv.id.maPhongChat = :maPhongChat
                    AND tv.id.maTaiKhoan = :maTaiKhoan
        """)
        int rejectMember(
                        @Param("maPhongChat") Integer maPhongChat,
                        @Param("maTaiKhoan") Integer maTaiKhoan,
                        @Param("nguoiDuyet") Integer nguoiDuyet
        );
}
