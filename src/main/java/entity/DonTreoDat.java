package entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ToString(exclude = "danhSachVe")
@Builder
@Entity
@Table(name = "DonTreoDat")
public class DonTreoDat implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "maDonTreo", length = 20)
    private String maDonTreo;

    @Column(length = 20)
    private String cccdNguoiDat;

    @Column(columnDefinition = "nvarchar(100)")
    private String hoTenNguoiDat;

    @Column(length = 15)
    private String sdtNguoiDat;

    @Column(length = 100)
    private String emailNguoiDat;

    private LocalDateTime ngayLap;
    private LocalDateTime gioLap;
    private int soLuongVe;
    private double tongTien;

    @Column(columnDefinition = "nvarchar(255)")
    private String ghiChu;

    @Column(columnDefinition = "nvarchar(100)")
    private String gaDi;

    @Column(columnDefinition = "nvarchar(100)")
    private String gaDen;

    private String ngayDi;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "maLichTrinh")
    private LichTrinh lichTrinh;

    public DonTreoDat() {
        this.ngayLap = LocalDateTime.now();
        this.gioLap = LocalDateTime.now();
        this.danhSachVe = new ArrayList<>();
    }
    
    @OneToMany(mappedBy = "donTreoDat", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ThongTinVeTam> danhSachVe;

    public void themVe(ThongTinVeTam ve) {
        if (this.danhSachVe == null) this.danhSachVe = new ArrayList<>();
        this.danhSachVe.add(ve);
        ve.setDonTreoDat(this);
    }

    public boolean conTrongThoiHan() {
        if (ngayLap == null) return false;
        LocalDateTime thoiGianHetHan = ngayLap.plusMinutes(15);
        return LocalDateTime.now().isBefore(thoiGianHetHan);
    }

    public String getThoiGianConLaiFormatted() {
        if (ngayLap == null) return "00:00";
        LocalDateTime thoiGianHetHan = ngayLap.plusMinutes(15);
        java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), thoiGianHetHan);
        long seconds = duration.getSeconds();
        if (seconds <= 0) return "00:00";
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Entity
    @Table(name = "ThongTinVeTam")
    public static class ThongTinVeTam implements Serializable {
        private static final long serialVersionUID = 1L;

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "maDonTreo", nullable = false)
        private DonTreoDat donTreoDat; // Tham chiếu ngược lại đơn treo

        @Column(length = 20)
        private String soGiayTo;

        @Column(columnDefinition = "nvarchar(100)")
        private String hoTen;

        @Column(columnDefinition = "nvarchar(50)")
        private String doiTuong;

        @Column(columnDefinition = "nvarchar(255)")
        private String thongTinCho;

        private double giaVe;
        private double giamGia;
        private double thanhTien;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "maChoNgoi")
        private ChoNgoi choNgoi;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "maLichTrinh")
        private LichTrinh lichTrinh;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "maGaDi")
        private Ga gaDi;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "maGaDen")
        private Ga gaDen;
    }
}
