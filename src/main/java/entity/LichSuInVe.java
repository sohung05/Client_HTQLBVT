package entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "LichSuInVe")
public class LichSuInVe implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maLichSu")
    private int maLichSu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "maVe")
    private Ve ve;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "maNhanVien")
    private NhanVien nhanVien;

    @Column(name = "thoiGianIn")
    private LocalDateTime thoiGianIn;

    @Column(name = "loaiIn", length = 50)
    private String loaiIn;

    @Column(name = "ghiChu", length = 250)
    private String ghiChu;
}
