package com.example.chatrealtime.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.chatrealtime.entity.YeuCauThamGiaNhom;

public interface YeuCauThamGiaNhomRepository extends JpaRepository<YeuCauThamGiaNhom, Integer> {

    List<YeuCauThamGiaNhom> findByMaPhongChatAndTrangThai(Integer maPhongChat, String trangThai);

    @Modifying
    @Query("""
        UPDATE YeuCauThamGiaNhom y
        SET y.trangThai = :trangThai,
            y.lyDoTuChoi = :lyDo,
            y.nguoiXuLy = :nguoiXuLy,
            y.ngayXuLy = CURRENT_TIMESTAMP
        WHERE y.id = :id
    """)
    int updateTrangThai(
            @Param("id") Integer id,
            @Param("trangThai") String trangThai,
            @Param("lyDo") String lyDo,
            @Param("nguoiXuLy") Integer nguoiXuLy
    );

    boolean existsByMaPhongChatAndMaTaiKhoanAndTrangThaiIn(Integer maPhongChat, Integer maTaiKhoan, List<String> trangThais);
}
