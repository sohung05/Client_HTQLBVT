/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package iuh.fit.gui;

import service.*;
import iuh.fit.utils.ClientContext;
import entity.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

/**
 * @description: Giao diện bán vé tàu
 * @author PC
 */
public class Gui_BanVe extends JPanel {

    // DAOs
    // Services
    private IGaService gaService;
    private ILichTrinhService lichTrinhService;
    private IToaService toaService;
    private IChoNgoiService choNgoiService;
    private IKhachHangService khachHangService;
    private IVeService veService;
    
    // Data
    private List<LichTrinh> danhSachLichTrinh;
    private LichTrinh lichTrinhDangChon;
    private Toa toaDangChon;
    private List<ChoNgoi> danhSachGheDangChon;
    private Map<ChoNgoi, LichTrinh> mapGheLichTrinh; // Lưu lịch trình của từng ghế đã chọn
    private Map<ChoNgoi, Ga> mapGheGaDi;   // 🆕 Lưu ga đi thực tế của từng ghế
    private Map<ChoNgoi, Ga> mapGheGaDen;  // 🆕 Lưu ga đến thực tế của từng ghế
    
    // Lưu ga gốc để swap khi chuyển chiều
    private String gaDiGoc;
    private String gaDenGoc;
    private Date ngayDiGoc;
    private Date ngayVeGoc;
    
    // 🆕 Lưu mã ga hiện tại (để quản lý chặng)
    private String maGaDiHienTai;
    private String maGaDenHienTai;
    
    // Models
    private DefaultTableModel modelGioVe;
    private ButtonGroup groupChieu;
    private ButtonGroup groupLoaiVe;

    /**
     * Creates new form Gui_BanVe
     */
    public Gui_BanVe() {
        initComponents();
        initDAO();
        initCustomComponents();
    }
    
    /**
     * Constructor nhận thông tin hành trình từ form nhập
     */
    public Gui_BanVe(Gui_NhapThongTinHanhTrinh.ThongTinHanhTrinh info) {
        initComponents();
        initDAO();
        initCustomComponents();
        
        // Điền thông tin tự động
        txtGaDi.setText(info.getGaDi());
        txtGaDen.setText(info.getGaDen());
        dchNgayDi.setDate(info.getNgayDi());
        dchNgayVe.setDate(info.getNgayVe());
        
        if (info.isMotChieu()) {
            radMotChieu.setSelected(true);
            dchNgayVe.setEnabled(false);
        } else {
            radKhuHoi.setSelected(true);
            lblNgayVe.setVisible(true);
            dchNgayVe.setVisible(true);
            dchNgayVe.setEnabled(true);
            
            // ⚡ LƯU THÔNG TIN GỐC CHO KHỨ HỒI (để swap chiều đi/về)
            gaDiGoc = info.getGaDi();
            gaDenGoc = info.getGaDen();
            ngayDiGoc = info.getNgayDi();
            ngayVeGoc = info.getNgayVe();
        }
        
        // Tự động tìm kiếm
        SwingUtilities.invokeLater(() -> {
            btnTimKiemActionPerformed(null);
        });
    }
    
    /**
     * Khởi tạo các DAO
     */
    private void initDAO() {
        gaService = ClientContext.getGaService();
        lichTrinhService = ClientContext.getLichTrinhService();
        toaService = ClientContext.getToaService();
        choNgoiService = ClientContext.getChoNgoiService();
        khachHangService = ClientContext.getKhachHangService();
        veService = ClientContext.getVeService();
    }
    
    /**
     * Khởi tạo các component tùy chỉnh
     */
    private void initCustomComponents() {
        // Khởi tạo danh sách ghế đang chọn
        danhSachGheDangChon = new ArrayList<>();
        mapGheLichTrinh = new LinkedHashMap<>(); // Khởi tạo map lưu lịch trình của từng ghế
        mapGheGaDi = new HashMap<>();
        mapGheGaDen = new HashMap<>();
        
        // Group radio buttons
        groupChieu = new ButtonGroup();
        groupChieu.add(radMotChieu);
        groupChieu.add(radKhuHoi);
        radMotChieu.setSelected(true);
        
        ButtonGroup groupChieuMua = new ButtonGroup();
        groupChieuMua.add(radChieuDi);
        groupChieuMua.add(radChieuVe);
        radChieuDi.setSelected(true);
        
        // Thêm listener cho radio button chiều
        radChieuVe.addActionListener(e -> chuyenChieuVe());
        radChieuDi.addActionListener(e -> chuyenChieuDi());
        
        // Setup table giỏ vé
        modelGioVe = (DefaultTableModel) tblGioVe.getModel();
        modelGioVe.setRowCount(0);
        
        // Group radio buttons cho Loại hành trình
        javax.swing.ButtonGroup groupLoaiHanhTrinh = new javax.swing.ButtonGroup();
        groupLoaiHanhTrinh.add(radMotChieu);
        groupLoaiHanhTrinh.add(radKhuHoi);
        
        // Group radio buttons cho Chiều mua vé
        javax.swing.ButtonGroup groupChieuMuaVe = new javax.swing.ButtonGroup();
        groupChieuMuaVe.add(radChieuDi);
        groupChieuMuaVe.add(radChieuVe);
        
        // Set ngày mặc định
        dchNgayDi.setDate(new Date());
        dchNgayVe.setDate(new Date());
        
        // Không cho chọn ngày trong quá khứ
        dchNgayDi.setMinSelectableDate(new Date());
        dchNgayVe.setMinSelectableDate(new Date());
        
        // Disable ngày về ban đầu
        dchNgayVe.setEnabled(false);
        radKhuHoi.addActionListener(e -> {
            boolean isKhuHoi = radKhuHoi.isSelected();
            dchNgayVe.setEnabled(isKhuHoi);
            lblNgayVe.setEnabled(isKhuHoi);
            
            // Luôn hiện panel này nếu đã có kết quả tìm kiếm, nhưng chỉ cho chọn chiều về nếu là khứ hồi
            if (pnlTuyen.isVisible()) {
                pnlChieuMuaVe.setVisible(true);
                radChieuVe.setEnabled(isKhuHoi);
            }
        });
        
        radMotChieu.addActionListener(e -> {
            dchNgayVe.setEnabled(false);
            lblNgayVe.setEnabled(false);
            
            // Vẫn hiện panel nhưng disable chiều về
            if (pnlTuyen.isVisible()) {
                pnlChieuMuaVe.setVisible(true);
                radChieuVe.setEnabled(false);
                
                // Nếu đang ở chiều về thì phải quay lại chiều đi
                if (radChieuVe.isSelected()) {
                    radChieuDi.setSelected(true);
                    chuyenChieuDi();
                }
            }
        });
        
        // Ẩn các panel kết quả ban đầu
        pnlTuyen.setVisible(false);
        pnlToaTau.setVisible(false);
        pnlSoDoGhe.setVisible(false);
        pnlChieuMuaVe.setVisible(false);
        
        // Cập nhật số lượng đơn treo lên nút
        capNhatSoLuongDonTreo();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlThongTinHanhTrinh = new javax.swing.JPanel();
        lblGaDi = new javax.swing.JLabel();
        lblGaDen = new javax.swing.JLabel();
        btnTimKiem = new javax.swing.JButton();
        radMotChieu = new javax.swing.JRadioButton();
        radKhuHoi = new javax.swing.JRadioButton();
        lblNgayDi = new javax.swing.JLabel();
        lblNgayVe = new javax.swing.JLabel();
        txtGaDi = new javax.swing.JTextField();
        txtGaDen = new javax.swing.JTextField();
        dchNgayDi = new com.toedter.calendar.JDateChooser();
        dchNgayVe = new com.toedter.calendar.JDateChooser();
        pnlGioVe = new javax.swing.JPanel();
        btnMuaVe = new javax.swing.JButton();
        btnHuyCho = new javax.swing.JButton();
        scrGioVe = new javax.swing.JScrollPane();
        tblGioVe = new javax.swing.JTable();
        btnHuyTatCa = new javax.swing.JButton();
        pnlChieuMuaVe = new javax.swing.JPanel();
        radChieuDi = new javax.swing.JRadioButton();
        radChieuVe = new javax.swing.JRadioButton();
        pnlTuyen = new javax.swing.JPanel();
        pnlToaTau = new javax.swing.JPanel();
        pnlSoDoGhe = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        btnXuLyDonTam = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(234, 243, 251));
        setToolTipText("");

        pnlThongTinHanhTrinh.setBackground(new java.awt.Color(255, 255, 255));
        pnlThongTinHanhTrinh.setBorder(javax.swing.BorderFactory.createTitledBorder("Thông Tin Hành Trình"));

        lblGaDi.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblGaDi.setText("Ga đi:");

        lblGaDen.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblGaDen.setText("Ga đến:");

        btnTimKiem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/TimKiem.png"))); // NOI18N
        btnTimKiem.setText("Tìm Kiếm");
        btnTimKiem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTimKiemActionPerformed(evt);
            }
        });

        radMotChieu.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        radMotChieu.setText("Một chiều");
        radMotChieu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radMotChieuActionPerformed(evt);
            }
        });

        radKhuHoi.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        radKhuHoi.setText("Khứ hồi");

        lblNgayDi.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblNgayDi.setText("Ngày đi:");

        lblNgayVe.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblNgayVe.setText("Ngày về:");

        txtGaDen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtGaDenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlThongTinHanhTrinhLayout = new javax.swing.GroupLayout(pnlThongTinHanhTrinh);
        pnlThongTinHanhTrinh.setLayout(pnlThongTinHanhTrinhLayout);
        pnlThongTinHanhTrinhLayout.setHorizontalGroup(
            pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThongTinHanhTrinhLayout.createSequentialGroup()
                .addGroup(pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlThongTinHanhTrinhLayout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addGroup(pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlThongTinHanhTrinhLayout.createSequentialGroup()
                                .addGroup(pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblGaDi)
                                    .addComponent(lblGaDen))
                                .addGap(35, 35, 35)
                                .addGroup(pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtGaDi, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                                    .addComponent(txtGaDen)))
                            .addGroup(pnlThongTinHanhTrinhLayout.createSequentialGroup()
                                .addGroup(pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblNgayVe)
                                    .addComponent(lblNgayDi))
                                .addGap(27, 27, 27)
                                .addGroup(pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(dchNgayDi, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                                    .addComponent(dchNgayVe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(pnlThongTinHanhTrinhLayout.createSequentialGroup()
                        .addGap(106, 106, 106)
                        .addComponent(radMotChieu)
                        .addGap(28, 28, 28)
                        .addComponent(radKhuHoi)))
                .addContainerGap(77, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlThongTinHanhTrinhLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnTimKiem)
                .addGap(120, 120, 120))
        );
        pnlThongTinHanhTrinhLayout.setVerticalGroup(
            pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThongTinHanhTrinhLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtGaDi, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblGaDi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25)
                .addGroup(pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGaDen)
                    .addComponent(txtGaDen, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radMotChieu)
                    .addComponent(radKhuHoi))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNgayDi)
                    .addComponent(dchNgayDi, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(pnlThongTinHanhTrinhLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dchNgayVe, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNgayVe))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnTimKiem, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        pnlGioVe.setBackground(new java.awt.Color(255, 255, 255));
        pnlGioVe.setBorder(javax.swing.BorderFactory.createTitledBorder("Giỏ Vé"));

        btnMuaVe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/ticket.png"))); // NOI18N
        btnMuaVe.setText("Bán vé");
        btnMuaVe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMuaVeActionPerformed(evt);
            }
        });

        btnHuyCho.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/trash.png"))); // NOI18N
        btnHuyCho.setText("Hủy Chỗ");
        btnHuyCho.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHuyChoActionPerformed(evt);
            }
        });

        tblGioVe.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tuyến", "Chỗ ngồi", "Chiều "
            }
        ));
        scrGioVe.setViewportView(tblGioVe);

        btnHuyTatCa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/clear.png"))); // NOI18N
        btnHuyTatCa.setText("Hủy Tất Cả");
        btnHuyTatCa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHuyTatCaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlGioVeLayout = new javax.swing.GroupLayout(pnlGioVe);
        pnlGioVe.setLayout(pnlGioVeLayout);
        pnlGioVeLayout.setHorizontalGroup(
            pnlGioVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGioVeLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnMuaVe, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnHuyCho)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnHuyTatCa)
                .addGap(12, 12, 12))
            .addComponent(scrGioVe, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
        );
        pnlGioVeLayout.setVerticalGroup(
            pnlGioVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGioVeLayout.createSequentialGroup()
                .addComponent(scrGioVe, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addGroup(pnlGioVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMuaVe, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHuyCho, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHuyTatCa, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );

        pnlChieuMuaVe.setBackground(new java.awt.Color(255, 255, 255));
        pnlChieuMuaVe.setBorder(javax.swing.BorderFactory.createTitledBorder("Chiều mua vé"));

        radChieuDi.setText("Chiều đi");
        radChieuDi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radChieuDiActionPerformed(evt);
            }
        });

        radChieuVe.setText("Chiều Về");

        javax.swing.GroupLayout pnlChieuMuaVeLayout = new javax.swing.GroupLayout(pnlChieuMuaVe);
        pnlChieuMuaVe.setLayout(pnlChieuMuaVeLayout);
        pnlChieuMuaVeLayout.setHorizontalGroup(
            pnlChieuMuaVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlChieuMuaVeLayout.createSequentialGroup()
                .addGap(229, 229, 229)
                .addComponent(radChieuDi)
                .addGap(185, 185, 185)
                .addComponent(radChieuVe)
                .addContainerGap(849, Short.MAX_VALUE))
        );
        pnlChieuMuaVeLayout.setVerticalGroup(
            pnlChieuMuaVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlChieuMuaVeLayout.createSequentialGroup()
                .addContainerGap(8, Short.MAX_VALUE)
                .addGroup(pnlChieuMuaVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radChieuDi, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radChieuVe))
                .addGap(16, 16, 16))
        );

        pnlTuyen.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout pnlTuyenLayout = new javax.swing.GroupLayout(pnlTuyen);
        pnlTuyen.setLayout(pnlTuyenLayout);
        pnlTuyenLayout.setHorizontalGroup(
            pnlTuyenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1408, Short.MAX_VALUE)
        );
        pnlTuyenLayout.setVerticalGroup(
            pnlTuyenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 176, Short.MAX_VALUE)
        );

        pnlToaTau.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout pnlToaTauLayout = new javax.swing.GroupLayout(pnlToaTau);
        pnlToaTau.setLayout(pnlToaTauLayout);
        pnlToaTauLayout.setHorizontalGroup(
            pnlToaTauLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlToaTauLayout.setVerticalGroup(
            pnlToaTauLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 65, Short.MAX_VALUE)
        );

        pnlSoDoGhe.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout pnlSoDoGheLayout = new javax.swing.GroupLayout(pnlSoDoGhe);
        pnlSoDoGhe.setLayout(pnlSoDoGheLayout);
        pnlSoDoGheLayout.setHorizontalGroup(
            pnlSoDoGheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlSoDoGheLayout.setVerticalGroup(
            pnlSoDoGheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 490, Short.MAX_VALUE)
        );

        jLabel5.setText("Ghế đang chọn");

        jPanel7.setBackground(new java.awt.Color(255, 51, 51));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 46, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        jPanel8.setBackground(new java.awt.Color(153, 204, 255));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 42, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        jPanel9.setBackground(new java.awt.Color(102, 255, 102));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 46, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        jLabel6.setText("Ghế đã bán");

        btnXuLyDonTam.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/document-management.png"))); // NOI18N
        btnXuLyDonTam.setText("Xử lí đơn tạm");
        btnXuLyDonTam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXuLyDonTamActionPerformed(evt);
            }
        });

        jLabel1.setText("Ghế đanng giữ chỗ");

        jPanel1.setBackground(new java.awt.Color(255, 255, 0));
        jPanel1.setForeground(new java.awt.Color(255, 204, 51));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 49, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 14, Short.MAX_VALUE)
        );

        jLabel7.setText("Toa đang chọn");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlChieuMuaVe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlTuyen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlToaTau, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlSoDoGhe, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(59, 59, 59)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(59, 59, 59)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(74, 74, 74)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(49, 49, 49)
                                .addComponent(jLabel7)
                                .addGap(26, 26, 26)
                                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(307, 307, 307)
                        .addComponent(btnXuLyDonTam, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlThongTinHanhTrinh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlGioVe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlThongTinHanhTrinh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlGioVe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlTuyen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlToaTau, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel6)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1)
                    .addComponent(jLabel7)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSoDoGhe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlChieuMuaVe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnXuLyDonTam)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnMuaVeActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnMuaVeActionPerformed
        // Kiểm tra giỏ vé có vé không
        if (modelGioVe.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Giỏ vé đang trống!\nVui lòng chọn ghế/giường trước khi mua vé.", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Kiểm tra đã chọn lịch trình chưa
        if (lichTrinhDangChon == null) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi: Chưa chọn lịch trình!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Chuyển sang màn hình nhập thông tin bán vé
        // Truyền instance hiện tại để có thể quay lại với dữ liệu giữ nguyên
        iuh.fit.gui.menu.form.MainForm mainForm = (iuh.fit.gui.menu.form.MainForm) this.getParent();
        mainForm.showForm(new Gui_NhapThongTinBanVe(this));
    }//GEN-LAST:event_btnMuaVeActionPerformed

    private void btnHuyChoActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnHuyChoActionPerformed
        int selectedRow = tblGioVe.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn vé cần hủy!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Lấy thông tin vé từ bảng - Cột 1 là "Chỗ ngồi"
        String choNgoiStr = (String) modelGioVe.getValueAt(selectedRow, 1); // "Toa X - Ghế Y"
        
        // Parse để lấy số toa và vị trí
        try {
            String[] parts = choNgoiStr.split(" - Ghế ");
            int soToa = Integer.parseInt(parts[0].replace("Toa ", "").trim());
            int viTri = Integer.parseInt(parts[1].trim());
            
            // Tìm ChoNgoi trong danh sách đang chọn
            ChoNgoi choCanXoa = null;
            for (ChoNgoi cho : danhSachGheDangChon) {
                if (cho.getToa().getSoToa() == soToa && cho.getViTri() == viTri) {
                    choCanXoa = cho;
                    break;
                }
            }
            
            if (choCanXoa != null) {
                // Xóa khỏi danh sách
                danhSachGheDangChon.remove(choCanXoa);
                mapGheLichTrinh.remove(choCanXoa); // ⚡ FIX: Xóa khỏi map lịch trình để đồng bộ
                
                // Xóa khỏi bảng
                modelGioVe.removeRow(selectedRow);
                
                // Refresh sơ đồ ghế để cập nhật màu
                if (toaDangChon != null && lichTrinhDangChon != null) {
                    hienThiSoDoGheTrongPanel(toaDangChon, lichTrinhDangChon);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi hủy vé: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnHuyChoActionPerformed

    private void btnTimKiemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnTimKiemActionPerformed
        // Validate input
        String gaDi = txtGaDi.getText().trim();
        String gaDen = txtGaDen.getText().trim();
        Date ngayDi = dchNgayDi.getDate();
        Date ngayVe = dchNgayVe.getDate();
        
        if (gaDi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ga đi!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtGaDi.requestFocus();
            return;
        }
        
        if (gaDen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ga đến!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtGaDen.requestFocus();
            return;
        }
        
        if (ngayDi == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày đi!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (radKhuHoi.isSelected() && ngayVe == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày về cho vé khứ hồi!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (gaDi.equalsIgnoreCase(gaDen)) {
            JOptionPane.showMessageDialog(this, "Ga đi và ga đến không được trùng nhau!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Convert Date to LocalDate
        LocalDate localNgayDi = ngayDi.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        
        // 🆕 Tìm mã ga từ tên ga để quản lý chặng
        java.util.List<entity.Ga> listGaDi = null;
        java.util.List<entity.Ga> listGaDen = null;
        try {
            listGaDi = gaService.findByTenGa(gaDi);
            listGaDen = gaService.findByTenGa(gaDen);
        } catch (Exception e) { e.printStackTrace(); }
        
        entity.Ga objGaDi = (listGaDi == null || listGaDi.isEmpty()) ? null : listGaDi.get(0);
        entity.Ga objGaDen = (listGaDen == null || listGaDen.isEmpty()) ? null : listGaDen.get(0);
        
        if (objGaDi != null) maGaDiHienTai = objGaDi.getMaGa();
        if (objGaDen != null) maGaDenHienTai = objGaDen.getMaGa();
        
        // Tìm lịch trình
        try {
            danhSachLichTrinh = lichTrinhService.timLichTrinh(gaDi, gaDen, localNgayDi);
        } catch (Exception e) {
            e.printStackTrace();
            danhSachLichTrinh = new ArrayList<>();
        }
        
        if (danhSachLichTrinh == null || danhSachLichTrinh.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy chuyến tàu nào phù hợp!\nVui lòng thử lại với thông tin khác.", 
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            anPanelKetQua();
            return;
        }
        
        // 🆕 RESET TRẠNG THÁI CHO TÌM KIẾM MỚI (Nếu là bấm nút Tìm kiếm thủ công)
        if (evt != null) {
            // Lưu lại thông tin gốc cho chuyến đi mới
            gaDiGoc = gaDi;
            gaDenGoc = gaDen;
            ngayDiGoc = ngayDi;
            ngayVeGoc = ngayVe;
            
            // Nếu là khứ hồi, luôn bắt đầu bằng Chiều đi
            if (radKhuHoi.isSelected()) {
                radChieuDi.setSelected(true);
            }
        }
        
        // Clear trạng thái chọn cũ
        lichTrinhDangChon = null;
        toaDangChon = null;
        
        // Hiển thị panel kết quả
        pnlTuyen.setVisible(true);
        pnlChieuMuaVe.setVisible(true);
        radChieuVe.setEnabled(radKhuHoi.isSelected());
        
        // Auto chọn chuyến có giờ xuất phát sớm nhất
        if (!danhSachLichTrinh.isEmpty()) {
            lichTrinhDangChon = timChuyenXuatPhatSomNhat(danhSachLichTrinh);
        }
        
        // Hiển thị danh sách chuyến tàu
        hienThiDanhSachChuyenTauTrongPanel(danhSachLichTrinh);
        
        // Hiển thị danh sách toa của chuyến đã chọn
        if (lichTrinhDangChon != null) {
            hienThiDanhSachToaTrongPanel(lichTrinhDangChon);
        }
    }//GEN-LAST:event_btnTimKiemActionPerformed

    private void radMotChieuActionPerformed(ActionEvent evt) {//GEN-FIRST:event_radMotChieuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_radMotChieuActionPerformed

    private void txtGaDenActionPerformed(ActionEvent evt) {//GEN-FIRST:event_txtGaDenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtGaDenActionPerformed

    private void btnHuyTatCaActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnHuyTatCaActionPerformed
        if (modelGioVe.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ vé đang trống!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa tất cả vé trong giỏ?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            modelGioVe.setRowCount(0);
            danhSachGheDangChon.clear();
            mapGheLichTrinh.clear(); // ⚡ FIX: Xóa sạch map lịch trình để đồng bộ
            
            // Refresh sơ đồ ghế
            if (toaDangChon != null && lichTrinhDangChon != null) {
                hienThiSoDoGheTrongPanel(toaDangChon, lichTrinhDangChon);
            }
        }
    }//GEN-LAST:event_btnHuyTatCaActionPerformed

    private void radChieuDiActionPerformed(ActionEvent evt) {//GEN-FIRST:event_radChieuDiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_radChieuDiActionPerformed

    private void btnXuLyDonTamActionPerformed(ActionEvent evt) {//GEN-FIRST:event_btnXuLyDonTamActionPerformed
        // Mở Dialog Treo Đơn
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        Dialog_TreoDon dialogTreoDon = new Dialog_TreoDon(parentFrame, true);
        dialogTreoDon.setVisible(true);
        
        // Sau khi đóng dialog (hủy đơn hoặc xử lý), reload lại sơ đồ ghế
        if (toaDangChon != null && lichTrinhDangChon != null) {
            hienThiSoDoGheTrongPanel(toaDangChon, lichTrinhDangChon);
        }
        
        // Cập nhật lại số lượng đơn treo lên nút
        capNhatSoLuongDonTreo();
    }//GEN-LAST:event_btnXuLyDonTamActionPerformed

    // ==================== HELPER METHODS ====================
    
    /**
     * Ẩn các panel kết quả
     */
    private void anPanelKetQua() {
        pnlTuyen.setVisible(false);
        pnlToaTau.setVisible(false);
        pnlSoDoGhe.setVisible(false);
        pnlChieuMuaVe.setVisible(false);
    }
    
    /**
     * Tìm chuyến tàu có giờ xuất phát sớm nhất
     */
    private LichTrinh timChuyenXuatPhatSomNhat(List<LichTrinh> danhSach) {
        if (danhSach == null || danhSach.isEmpty()) {
            return null;
        }
        
        LichTrinh somNhat = danhSach.get(0);
        
        for (LichTrinh lt : danhSach) {
            if (lt.getGioKhoiHanh() != null && somNhat.getGioKhoiHanh() != null) {
                if (lt.getGioKhoiHanh().isBefore(somNhat.getGioKhoiHanh())) {
                    somNhat = lt;
                }
            }
        }
        
        return somNhat;
    }
    
    /**
     * Hiển thị danh sách chuyến tàu trong panel (chiều ngang với nút scroll)
     */
    private void hienThiDanhSachChuyenTauTrongPanel(List<LichTrinh> danhSach) {
        pnlTuyen.removeAll();
        pnlTuyen.setLayout(new BorderLayout());
        
        // Panel chứa các card chuyến tàu - chiều ngang
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        containerPanel.setBackground(Color.WHITE);
        
        for (LichTrinh lt : danhSach) {
            JPanel cardChuyenTau = taoCardChuyenTauCompact(lt);
            containerPanel.add(cardChuyenTau);
        }
        
        // Thêm scroll pane KHÔNG hiển thị scrollbar
        JScrollPane scrollPane = new JScrollPane(containerPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        
        pnlTuyen.add(scrollPane, BorderLayout.CENTER);
        
        pnlTuyen.revalidate();
        pnlTuyen.repaint();
    }
    
    /**
     * Tạo card chuyến tàu sử dụng TauIteam component
     */
    private JPanel taoCardChuyenTauCompact(LichTrinh lt) {
        // Tạo TauIteam component
        compoment.TauIteam tauItem = new compoment.TauIteam();
        
        // Set dữ liệu
        String soHieuTau = lt.getChuyenTau() != null ? lt.getChuyenTau().getSoHieuTau() : "SE1";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String tgDi = lt.getGioKhoiHanh() != null ? lt.getGioKhoiHanh().format(formatter) : "N/A";
        String tgDen = lt.getGioDen() != null ? lt.getGioDen().format(formatter) : "N/A";
        
        tauItem.setData(soHieuTau, tgDi, tgDen);
        
        // Set màu theo trạng thái chọn
        boolean isSelected = lichTrinhDangChon != null && 
                            lichTrinhDangChon.getMaLichTrinh().equals(lt.getMaLichTrinh());
        tauItem.setSelected(isSelected);
        
        // Click vào card để chọn chuyến
        tauItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tauItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                lichTrinhDangChon = lt;
                
                // 🔍 DEBUG: Log lịch trình đang chọn
                System.out.println("🚆 Chọn tàu: Lịch trình " + lt.getMaLichTrinh() + 
                    " | Giờ KH: " + (lt.getGioKhoiHanh() != null ? lt.getGioKhoiHanh() : "null"));
                
                // Reload lại danh sách chuyến tàu để cập nhật màu
                if (danhSachLichTrinh != null) {
                    hienThiDanhSachChuyenTauTrongPanel(danhSachLichTrinh);
                }
                
                // Hiển thị danh sách toa
                hienThiDanhSachToaTrongPanel(lt);
            }
        });
        
        return tauItem;
    }
    
    /**
     * Hiển thị danh sách toa trong panel
     */
    private void hienThiDanhSachToaTrongPanel(LichTrinh lt) {
        pnlToaTau.removeAll();
        pnlToaTau.setVisible(true);
        pnlToaTau.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 5));
        
        if (lt == null || lt.getChuyenTau() == null) {
            return;
        }
        
        // Thêm icon đầu tàu sử dụng TrainHeaderIteam component
        compoment.TrainHeaderIteam trainHeader = new compoment.TrainHeaderIteam();
        pnlToaTau.add(trainHeader);
        
        String soHieuTau = lt.getChuyenTau().getSoHieuTau();
        List<Toa> danhSachToa = null;
        try {
            danhSachToa = toaService.getToaBySoHieuTau(soHieuTau);
        } catch (Exception e) { e.printStackTrace(); }
        
        // Tạo 10 toa (5 ngồi mềm + 5 giường nằm)
        for (int i = 1; i <= 10; i++) {
            // Tìm toa tương ứng hoặc tạo toa giả
            Toa toa = null;
            if (danhSachToa != null) {
                for (Toa t : danhSachToa) {
                    if (t.getSoToa() == i) {
                        toa = t;
                        break;
                    }
                }
            }
            
            // Nếu không có toa trong DB, tạo toa giả
            if (toa == null) {
                toa = new Toa();
                toa.setSoToa(i);
                if (i <= 5) {
                    LoaiToa loaiToa = new LoaiToa("LTOA001", "Ngồi mềm điều hòa");
                    toa.setLoaiToa(loaiToa);
                } else {
                    LoaiToa loaiToa = new LoaiToa("LTOA002", "Giường nằm khoang 6 điều hòa");
                    toa.setLoaiToa(loaiToa);
                }
                toa.setMaToa("FAKE-T" + String.format("%02d", i));
            } else if (i >= 6 && i <= 10) {
                // Fix: Nếu toa 6-10 từ DB có tên sai (VIP, v.v.), override thành "Giường nằm khoang 6 điều hòa"
                if (toa.getLoaiToa() != null) {
                    String tenLoai = toa.getLoaiToa().getTenLoaiToa();
                    if (tenLoai == null || !tenLoai.contains("Giường nằm")) {
                        LoaiToa loaiToaMoi = new LoaiToa(toa.getLoaiToa().getMaLoaiToa(), "Giường nằm khoang 6 điều hòa");
                        toa.setLoaiToa(loaiToaMoi);
                    }
                }
            }
            
            JPanel panelToa = taoBtnToaVoiIcon(toa, lt, i);
            pnlToaTau.add(panelToa);
            
            // Auto load toa 1 và highlight nó
            if (i == 1) {
                if (panelToa instanceof compoment.ToaIteam) {
                    ((compoment.ToaIteam) panelToa).setSelected(true);
                }
                toaDangChon = toa;
                
                // Tạo biến final để dùng trong lambda
                final Toa toaFinal = toa;
                
                // Delay một chút để UI đã render xong
                SwingUtilities.invokeLater(() -> {
                    hienThiSoDoGheTrongPanel(toaFinal, lt);
                });
            }
        }
        
        pnlToaTau.revalidate();
        pnlToaTau.repaint();
    }
    
    /**
     * Tạo button toa sử dụng ToaIteam component
     */
    private JPanel taoBtnToaVoiIcon(Toa toa, LichTrinh lt, int soToa) {
        // Tạo ToaIteam component
        compoment.ToaIteam toaItem = new compoment.ToaIteam();
        
        // Set số toa
        toaItem.setSoToa(soToa);
        toaItem.setSelected(false); // Mặc định chưa chọn
        toaItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect và click event
        toaItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                toaItem.setHover(true);
                toaItem.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                toaItem.setHover(false);
                toaItem.repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Reset tất cả toa về màu trắng
                Component[] components = pnlToaTau.getComponents();
                for (Component c : components) {
                    if (c instanceof compoment.ToaIteam && c != toaItem) {
                        ((compoment.ToaIteam) c).setSelected(false);
                        c.repaint();
                    }
                }
                
                // Highlight toa được chọn
                toaItem.setSelected(true);
                toaItem.repaint();
                
                toaDangChon = toa;
                hienThiSoDoGheTrongPanel(toa, lt);
            }
        });
        
        return toaItem;
    }
    
    /**
     * Hiển thị sơ đồ ghế trong panel
     */
    private void hienThiSoDoGheTrongPanel(Toa toa, LichTrinh lt) {
        pnlSoDoGhe.removeAll();
        pnlSoDoGhe.setVisible(true);
        pnlChieuMuaVe.setVisible(true);
        
        if (toa == null || lt == null) {
            return;
        }
        
        // Lưu toa đang hiển thị để reload sau khi thanh toán
        this.toaDangChon = toa;
        
        // Sử dụng BorderLayout để thêm title
        pnlSoDoGhe.setLayout(new BorderLayout(5, 5));
        
        // Panel title
        String tenLoaiToa = toa.getLoaiToa() != null ? toa.getLoaiToa().getTenLoaiToa() : "";
        JLabel lblTitle = new JLabel("Toa số " + toa.getSoToa() + ": " + tenLoaiToa);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(0, 102, 204));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pnlSoDoGhe.add(lblTitle, BorderLayout.NORTH);
        
        // Query danh sách chỗ ngồi từ DB
        List<ChoNgoi> danhSachCho = null;
        try {
            danhSachCho = choNgoiService.getChoNgoiByMaToa(toa.getMaToa());
        } catch (Exception e) { e.printStackTrace(); }
        if (danhSachCho == null || danhSachCho.isEmpty()) {
            JLabel lblError = new JLabel("Không có dữ liệu chỗ ngồi cho toa này!");
            lblError.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblError.setForeground(Color.RED);
            lblError.setHorizontalAlignment(SwingConstants.CENTER);
            pnlSoDoGhe.add(lblError, BorderLayout.CENTER);
            pnlSoDoGhe.revalidate();
            pnlSoDoGhe.repaint();
            return;
        }
        
        // ⚡ TỐI ƯU: Query danh sách ghế đã đặt THEO CHẶNG
        Set<String> gheDaDatSet = null;
        try {
            gheDaDatSet = veService.layDanhSachGheDaDat(lt.getMaLichTrinh());
        } catch (Exception e) { e.printStackTrace(); }
        
        // ⚡ MỚI: Làm mới danh sách ghế đang treo THEO CHẶNG
        QuanLyGheGiuCho.refreshDanhSachGheTreo(lt.getMaLichTrinh(), maGaDiHienTai, maGaDenHienTai);
        
        System.out.println("📊 Chặng " + maGaDiHienTai + "-" + maGaDenHienTai + " của LT " + lt.getMaLichTrinh() + " có " + gheDaDatSet.size() + " ghế đã bán");
        System.out.println("🟡 Số ghế đang TREO từ Server: " + QuanLyGheGiuCho.getDanhSachMaGheDangTreoRemote().size());
        
        // Kiểm tra toa ngồi hay nằm
        int soToa = toa.getSoToa();
        
        if (soToa <= 5) {
            // Toa 1-5: Ngồi mềm - 64 ghế (4 góc, mỗi góc 2 dãy x 8 ghế)
            JPanel pnlGheContainer = new JPanel(new BorderLayout(0, 8));
            pnlGheContainer.setBackground(Color.WHITE);
            
            // Panel phía trên (2 góc trên)
            JPanel pnlTop = new JPanel();
            pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.X_AXIS));
            pnlTop.setBackground(Color.WHITE);
            
            // Thêm glue để căn giữa
            pnlTop.add(Box.createHorizontalGlue());
            
            // Góc trên trái (2 dãy x 8 ghế)
            JPanel gocTrenTrai = taoGocGhe(1, 16, danhSachCho, gheDaDatSet, lt); // Ghế 1-16
            pnlTop.add(gocTrenTrai);
            
            pnlTop.add(Box.createHorizontalStrut(6));
            
            // Line dọc giữa (lối đi)
            JLabel lblLineTop = taoLineDoc();
            pnlTop.add(lblLineTop);
            
            pnlTop.add(Box.createHorizontalStrut(6));
            
            // Góc trên phải (2 dãy x 8 ghế)
            JPanel gocTrenPhai = taoGocGhe(17, 32, danhSachCho, gheDaDatSet, lt); // Ghế 17-32
            pnlTop.add(gocTrenPhai);
            
            // Thêm glue để căn giữa
            pnlTop.add(Box.createHorizontalGlue());
            
            pnlGheContainer.add(pnlTop, BorderLayout.NORTH);
            
            // Khoảng trắng giữa (lối đi ngang)
            JPanel pnlGap = new JPanel();
            pnlGap.setBackground(Color.WHITE);
            pnlGap.setPreferredSize(new Dimension(0, 15));
            pnlGheContainer.add(pnlGap, BorderLayout.CENTER);
            
            // Panel phía dưới (2 góc dưới)
            JPanel pnlBottom = new JPanel();
            pnlBottom.setLayout(new BoxLayout(pnlBottom, BoxLayout.X_AXIS));
            pnlBottom.setBackground(Color.WHITE);
            
            // Thêm glue để căn giữa
            pnlBottom.add(Box.createHorizontalGlue());
            
            // Góc dưới trái (2 dãy x 8 ghế)
            JPanel gocDuoiTrai = taoGocGhe(33, 48, danhSachCho, gheDaDatSet, lt); // Ghế 33-48
            pnlBottom.add(gocDuoiTrai);
            
            pnlBottom.add(Box.createHorizontalStrut(6));
            
            // Line dọc giữa (lối đi)
            JLabel lblLineBottom = taoLineDoc();
            pnlBottom.add(lblLineBottom);
            
            pnlBottom.add(Box.createHorizontalStrut(6));
            
            // Góc dưới phải (2 dãy x 8 ghế)
            JPanel gocDuoiPhai = taoGocGhe(49, 64, danhSachCho, gheDaDatSet, lt); // Ghế 49-64
            pnlBottom.add(gocDuoiPhai);
            
            // Thêm glue để căn giữa
            pnlBottom.add(Box.createHorizontalGlue());
            
            pnlGheContainer.add(pnlBottom, BorderLayout.SOUTH);
            
            pnlSoDoGhe.add(pnlGheContainer, BorderLayout.CENTER);
        } else {
            // Toa 6-10: Giường nằm - 7 khoang x 6 giường
            
            // Panel wrapper với vertical box để chiếm đủ không gian
            JPanel wrapperPanel = new JPanel();
            wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.Y_AXIS));
            wrapperPanel.setBackground(Color.WHITE);
            
            // Thêm vertical glue phía trên
            wrapperPanel.add(Box.createVerticalGlue());
            
            JPanel pnlKhoangContainer = new JPanel();
            pnlKhoangContainer.setLayout(new BoxLayout(pnlKhoangContainer, BoxLayout.X_AXIS));
            pnlKhoangContainer.setBackground(Color.WHITE);
            
            // Thêm glue để căn giữa
            pnlKhoangContainer.add(Box.createHorizontalGlue());
            
            int viTriGhe = 1;
            for (int k = 1; k <= 7; k++) {
                // Mỗi khoang
                JPanel khoang = new JPanel();
                khoang.setLayout(new BorderLayout(5, 5));
                khoang.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 100), 2), 
                    "Khoang " + k,
                    javax.swing.border.TitledBorder.CENTER,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 11)
                ));
                khoang.setBackground(Color.WHITE);
                khoang.setPreferredSize(new Dimension(130, 210)); // Cố định kích thước
                khoang.setMaximumSize(new Dimension(130, 210));
                
                // 6 giường trong khoang (3 hàng x 2 cột - 3 tầng mỗi bên)
                JPanel pnlGiuong = new JPanel(new GridLayout(3, 2, 4, 4));
                pnlGiuong.setBackground(Color.WHITE);
                pnlGiuong.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
                
                for (int i = 0; i < 6; i++) {
                    final int viTriHienTai = viTriGhe;
                    
                    // Tìm ChoNgoi tương ứng với vị trí này
                    ChoNgoi choNgoi = null;
                    for (ChoNgoi cho : danhSachCho) {
                        if (cho.getViTri() == viTriHienTai) {
                            choNgoi = cho;
                            break;
                        }
                    }
                    
                    if (choNgoi == null) {
                        // Không tìm thấy chỗ ngồi => hiển thị panel trống
                        JPanel emptyPanel = new JPanel();
                        emptyPanel.setBackground(Color.WHITE);
                        pnlGiuong.add(emptyPanel);
                        viTriGhe++;
                        continue;
                    }
                    
                    final ChoNgoi choFinal = choNgoi; // For lambda
                    
                    // ⚡ TỐI ƯU: Check trong Set thay vì query DB
                    boolean daDat = gheDaDatSet.contains(choNgoi.getMaChoNgoi());
                    
                    JButton btnGhe = new JButton(String.valueOf(viTriHienTai));
                    btnGhe.setPreferredSize(new Dimension(52, 48));
                    btnGhe.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    btnGhe.setFocusPainted(false);
                    btnGhe.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    
                    // Set màu dựa trên trạng thái
                    if (daDat) {
                        // Ghế đã được đặt => màu đỏ đậm full, không cho click
                        btnGhe.setBackground(new Color(220, 53, 69));
                        btnGhe.setForeground(Color.WHITE);
                        btnGhe.setEnabled(false);
                        btnGhe.setBorder(BorderFactory.createLineBorder(new Color(180, 40, 55), 2));
                    } else if (QuanLyGheGiuCho.kiemTraGheDangGiuCho(choNgoi.getMaChoNgoi(), lt.getMaLichTrinh())) {
                        // Ghế đang được giữ chỗ (15 phút) => màu vàng đậm full, không cho click
                        btnGhe.setBackground(new Color(255, 193, 7));
                        btnGhe.setForeground(Color.WHITE);
                        btnGhe.setEnabled(false);
                        btnGhe.setBorder(BorderFactory.createLineBorder(new Color(220, 165, 0), 2));
                        btnGhe.setToolTipText("Ghế đang được giữ chỗ (15 phút)");
                    } else if (kiemTraGheDangDuocChon(choFinal)) {
                        // Ghế đang được chọn => màu xanh đậm full
                        btnGhe.setBackground(new Color(0, 120, 215));
                        btnGhe.setForeground(Color.WHITE);
                        btnGhe.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 180), 2));
                        
                        // Click để bỏ chọn
                        btnGhe.addActionListener(e -> {
                            xuLyChonGhe(choFinal, lt, btnGhe);
                        });
                    } else {
                        // Ghế còn trống => màu trắng, cho phép click
                        btnGhe.setBackground(Color.WHITE);
                        btnGhe.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                        
                        // Click để chọn/bỏ chọn ghế
                        btnGhe.addActionListener(e -> {
                            xuLyChonGhe(choFinal, lt, btnGhe);
                        });
                    }
                    
                    pnlGiuong.add(btnGhe);
                    viTriGhe++;
                }
                
                khoang.add(pnlGiuong, BorderLayout.CENTER);
                pnlKhoangContainer.add(khoang);
                
                // Thêm line ngăn cách giữa các khoang (trừ khoang cuối)
                if (k < 7) {
                    JLabel line = new JLabel();
                    try {
                        ImageIcon lineIcon = new ImageIcon(getClass().getResource("/icon/line.jpg"));
                        Image img = lineIcon.getImage().getScaledInstance(6, 210, Image.SCALE_SMOOTH);
                        line.setIcon(new ImageIcon(img));
                    } catch (Exception e) {
                        // Fallback: dùng panel màu xám
                        line.setText("");
                        line.setOpaque(true);
                        line.setBackground(new Color(100, 100, 100));
                        line.setPreferredSize(new Dimension(6, 210));
                    }
                    pnlKhoangContainer.add(line);
                }
            }
            
            // Thêm glue để căn giữa
            pnlKhoangContainer.add(Box.createHorizontalGlue());
            
            wrapperPanel.add(pnlKhoangContainer);
            
            // Thêm vertical glue phía dưới
            wrapperPanel.add(Box.createVerticalGlue());
            
            pnlSoDoGhe.add(wrapperPanel, BorderLayout.CENTER);
        }
        
        pnlSoDoGhe.revalidate();
        pnlSoDoGhe.repaint();
    }
    
    /**
     * Tạo 1 góc ghế (2 dãy x 8 ghế nằm ngang) - TỐI ƯU
     */
    private JPanel taoGocGhe(int gheStart, int gheEnd, List<ChoNgoi> danhSachCho, Set<String> gheDaDatSet, LichTrinh lt) {
        JPanel goc = new JPanel(new GridLayout(2, 8, 3, 3)); // 2 dãy, 8 ghế mỗi dãy
        goc.setBackground(Color.WHITE);
        
        for (int viTri = gheStart; viTri <= gheEnd; viTri++) {
            // Tìm ChoNgoi tương ứng với vị trí này
            ChoNgoi choNgoi = null;
            for (ChoNgoi cho : danhSachCho) {
                if (cho.getViTri() == viTri) {
                    choNgoi = cho;
                    break;
                }
            }
            
            if (choNgoi == null) {
                // Không tìm thấy chỗ ngồi => hiển thị panel trống
                JPanel emptyPanel = new JPanel();
                emptyPanel.setBackground(Color.WHITE);
                goc.add(emptyPanel);
                continue;
            }
            
            // Tạo button ghế với dữ liệu thực
            final ChoNgoi choFinal = choNgoi; // For lambda
            
            // ⚡ TỐI ƯU: Check trong Set thay vì query DB
            boolean daDat = gheDaDatSet.contains(choNgoi.getMaChoNgoi());
            
            JButton btnGhe = new JButton(String.valueOf(viTri));
            btnGhe.setPreferredSize(new Dimension(46, 38));
            btnGhe.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnGhe.setFocusPainted(false);
            btnGhe.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Set màu dựa trên trạng thái
            if (daDat) {
                // Ghế đã được đặt => màu đỏ đậm full, không cho click
                btnGhe.setBackground(new Color(220, 53, 69));
                btnGhe.setForeground(Color.WHITE);
                btnGhe.setEnabled(false);
                btnGhe.setBorder(BorderFactory.createLineBorder(new Color(180, 40, 55), 2));
            } else if (QuanLyGheGiuCho.kiemTraGheDangGiuCho(choNgoi.getMaChoNgoi(), lt.getMaLichTrinh())) {
                // Ghế đang được giữ chỗ (5 phút) => màu vàng đậm full, không cho click
                btnGhe.setBackground(new Color(255, 193, 7));
                btnGhe.setForeground(Color.WHITE);
                btnGhe.setEnabled(false);
                btnGhe.setBorder(BorderFactory.createLineBorder(new Color(220, 165, 0), 2));
                btnGhe.setToolTipText("Ghế đang được giữ chỗ (5 phút)");
            } else if (kiemTraGheDangDuocChon(choFinal)) {
                // Ghế đang được chọn => màu xanh đậm full
                btnGhe.setBackground(new Color(0, 120, 215));
                btnGhe.setForeground(Color.WHITE);
                btnGhe.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 180), 2));
                
                // Click để bỏ chọn
                btnGhe.addActionListener(e -> {
                    xuLyChonGhe(choFinal, lt, btnGhe);
                });
            } else {
                // Ghế còn trống => màu trắng, cho phép click
                btnGhe.setBackground(Color.WHITE);
                btnGhe.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                
                // Click để chọn/bỏ chọn ghế
                btnGhe.addActionListener(e -> {
                    xuLyChonGhe(choFinal, lt, btnGhe);
                });
            }
            
            goc.add(btnGhe);
        }
        
        return goc;
    }
    
    /**
     * Tạo line dọc (lối đi)
     */
    private JLabel taoLineDoc() {
        JLabel line = new JLabel();
        try {
            ImageIcon lineIcon = new ImageIcon(getClass().getResource("/icon/line.jpg"));
            Image img = lineIcon.getImage().getScaledInstance(6, 88, Image.SCALE_SMOOTH);
            line.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            // Fallback: dùng panel màu xám
            line.setText("");
            line.setOpaque(true);
            line.setBackground(new Color(150, 150, 150));
            line.setPreferredSize(new Dimension(6, 88));
        }
        return line;
    }
    
    /**
     * Tạo button ghế compact
     */
    private JButton taoBtnGheCompact(ChoNgoi cho, LichTrinh lt) {
        JButton btn = new JButton(String.valueOf(cho.getViTri()));
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Kiểm tra trạng thái ghế
        boolean daDat = false;
        try {
            daDat = veService.kiemTraGheDaDat(cho.getMaChoNgoi(), lt.getMaLichTrinh());
        } catch (Exception e) { e.printStackTrace(); }
        boolean dangGiuCho = QuanLyGheGiuCho.kiemTraGheDangGiuCho(cho.getMaChoNgoi(), lt.getMaLichTrinh());
        boolean dangChon = kiemTraGheDangDuocChon(cho);
        
        if (daDat) {
            // Ghế đã đặt - màu đỏ đậm full, disable
            btn.setBackground(new Color(220, 53, 69));
            btn.setForeground(Color.WHITE);
            btn.setEnabled(false);
            btn.setBorder(BorderFactory.createLineBorder(new Color(180, 40, 55), 2));
        } else if (dangGiuCho) {
            // Ghế đang được giữ chỗ (15 phút) - màu vàng đậm full, disable
            btn.setBackground(new Color(255, 193, 7));
            btn.setForeground(Color.WHITE);
            btn.setEnabled(false);
            btn.setBorder(BorderFactory.createLineBorder(new Color(220, 165, 0), 2));
            btn.setToolTipText("Ghế đang được giữ chỗ (15 phút)");
        } else if (dangChon) {
            // Ghế đang chọn - màu xanh đậm full
            btn.setBackground(new Color(0, 120, 215));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 180), 2));
        } else {
            // Ghế trống - màu trắng
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }
        
        btn.addActionListener(e -> {
            xuLyChonGhe(cho, lt, btn);
        });
        
        return btn;
    }
    
    /**
     * Kiểm tra ghế đã được chọn chưa (so sánh theo MaChoNgoi)
     */
    private boolean kiemTraGheDangDuocChon(ChoNgoi cho) {
        for (ChoNgoi choChon : danhSachGheDangChon) {
            if (choChon.getMaChoNgoi().equals(cho.getMaChoNgoi())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Lấy ghế đã chọn theo MaChoNgoi
     */
    private ChoNgoi layGheDangChon(ChoNgoi cho) {
        for (ChoNgoi choChon : danhSachGheDangChon) {
            if (choChon.getMaChoNgoi().equals(cho.getMaChoNgoi())) {
                return choChon;
            }
        }
        return null;
    }
    
    /**
     * Xử lý khi chọn/bỏ chọn ghế
     */
    private void xuLyChonGhe(ChoNgoi cho, LichTrinh lt, JButton btn) {
        // Kiểm tra ghế đã được chọn chưa (so sánh theo MaChoNgoi)
        ChoNgoi gheDaChon = layGheDangChon(cho);
        
        if (gheDaChon != null) {
            // Bỏ chọn ghế
            danhSachGheDangChon.remove(gheDaChon);
            btn.setBackground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            
            // Xóa khỏi giỏ vé
            xoaKhoiGioVe(gheDaChon);
        } else {
            // Chọn ghế
            danhSachGheDangChon.add(cho);
            btn.setBackground(new Color(153, 204, 255));
            btn.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            
            // Thêm vào giỏ vé
            themVaoGioVe(cho, lt);
        }
    }
    
    /**
     * Thêm vé vào giỏ
     */
    private void themVaoGioVe(ChoNgoi cho, LichTrinh lt) {
        // Format: SE1 | Sài Gòn - Hà Nội
        String soHieuTau = lt.getChuyenTau() != null ? lt.getChuyenTau().getSoHieuTau() : "N/A";
        String gaDi = txtGaDi.getText().trim();
        String gaDen = txtGaDen.getText().trim();
        String tuyen = soHieuTau + " | " + gaDi + " - " + gaDen;
        String choNgoi = "Toa " + cho.getToa().getSoToa() + " - Ghế " + cho.getViTri();
        String chieu = radChieuDi.isSelected() ? "Chiều đi" : "Chiều về";
        
        // 🔍 DEBUG: Log lịch trình được thêm vào giỏ
        System.out.println("➕ Thêm vào giỏ: Ghế " + cho.getMaChoNgoi() + " | Lịch trình " + lt.getMaLichTrinh() + 
            " | Giờ KH: " + (lt.getGioKhoiHanh() != null ? lt.getGioKhoiHanh() : "null"));
        
        // Lưu lịch trình và ga chặng của ghế này vào map
        mapGheLichTrinh.put(cho, lt);
        try {
            mapGheGaDi.put(cho, gaService.findByMaGa(maGaDiHienTai));
            mapGheGaDen.put(cho, gaService.findByMaGa(maGaDenHienTai));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Thêm vào bảng: Tuyến | Chỗ ngồi | Chiều (giữ nguyên 3 cột như giao diện cũ)
        modelGioVe.addRow(new Object[]{tuyen, choNgoi, chieu});
    }
    
    /**
     * Xóa vé khỏi giỏ dựa vào chỗ ngồi
     */
    private void xoaKhoiGioVe(ChoNgoi cho) {
        // Xóa khỏi map lưu lịch trình
        mapGheLichTrinh.remove(cho);
        
        for (int i = 0; i < modelGioVe.getRowCount(); i++) {
            String choNgoi = (String) modelGioVe.getValueAt(i, 1); // Cột thứ 2: Chỗ ngồi
            String gheCurrent = "Toa " + cho.getToa().getSoToa() + " - Ghế " + cho.getViTri();
            
            if (choNgoi.equals(gheCurrent)) {
                modelGioVe.removeRow(i);
                break;
            }
        }
    }
    
    // ===================== CHIỀU MUA VÉ =====================
    
    /**
     * Chuyển sang chiều về (swap ga đi/đến)
     */
    private void chuyenChieuVe() {
        System.out.println("🔄 ========== CHUYỂN CHIỀU VỀ ==========");
        
        // Kiểm tra xem đã có thông tin gốc chưa
        if (gaDiGoc == null || gaDenGoc == null || ngayVeGoc == null) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn đầy đủ thông tin khứ hồi!",
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            radChieuDi.setSelected(true);
            return;
        }
        
        // Cập nhật UI: Đảo ga gốc
        txtGaDi.setText(gaDenGoc);
        txtGaDen.setText(gaDiGoc);
        dchNgayDi.setDate(ngayVeGoc);
        // Giữ nguyên ngày về để hiển thị đầy đủ thông tin khứ hồi
        dchNgayVe.setDate(ngayVeGoc); 
        
        // Cập nhật mã ga hiện tại để tìm kiếm chặng về
        updateMaGaHienTai(gaDenGoc, gaDiGoc);
        
        System.out.println("✅ ĐÃ SET UI CHIỀU VỀ: " + gaDenGoc + " -> " + gaDiGoc);
        
        // Tìm kiếm chuyến tàu chiều về
        btnTimKiemActionPerformed(null);
        
        // Highlight lại các ghế đã có trong giỏ vé (của chiều về)
        SwingUtilities.invokeLater(() -> highlightGheDaCoTrongGioVe());
    }
    
    /**
     * Chuyển về chiều đi (restore lại ga gốc)
     */
    private void chuyenChieuDi() {
        System.out.println("🔄 ========== CHUYỂN VỀ CHIỀU ĐI ==========");
        
        if (gaDiGoc == null || gaDenGoc == null) return;
        
        // Restore lại ga đi/đến gốc
        txtGaDi.setText(gaDiGoc);
        txtGaDen.setText(gaDenGoc);
        dchNgayDi.setDate(ngayDiGoc);
        dchNgayVe.setDate(ngayVeGoc);
        
        // Cập nhật mã ga hiện tại để tìm kiếm chặng đi
        updateMaGaHienTai(gaDiGoc, gaDenGoc);
        
        System.out.println("✅ ĐÃ RESTORE UI CHIỀU ĐI: " + gaDiGoc + " -> " + gaDenGoc);
        
        // Tìm kiếm lại chuyến tàu chiều đi
        btnTimKiemActionPerformed(null);
        
        // Highlight lại các ghế đã có trong giỏ vé (của chiều đi)
        SwingUtilities.invokeLater(() -> highlightGheDaCoTrongGioVe());
    }

    /**
     * Helper cập nhật mã ga đi/đến hiện tại
     */
    private void updateMaGaHienTai(String tenGaDi, String tenGaDen) {
        try {
            List<Ga> listGaDi = gaService.findByTenGa(tenGaDi);
            List<Ga> listGaDen = gaService.findByTenGa(tenGaDen);
            
            if (listGaDi != null && !listGaDi.isEmpty()) maGaDiHienTai = listGaDi.get(0).getMaGa();
            if (listGaDen != null && !listGaDen.isEmpty()) maGaDenHienTai = listGaDen.get(0).getMaGa();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Highlight lại các ghế đã có trong giỏ vé cho lịch trình hiện tại
     */
    private void highlightGheDaCoTrongGioVe() {
        if (lichTrinhDangChon == null) return;
        
        // Xóa danh sách ghế đang chọn trên UI hiện tại
        danhSachGheDangChon.clear();
        
        // ⚡ TỐI ƯU: Duyệt qua mapGheLichTrinh để tìm ghế thuộc lịch trình hiện tại
        // Không cần parse chuỗi và query database nhiều lần
        for (Map.Entry<ChoNgoi, LichTrinh> entry : mapGheLichTrinh.entrySet()) {
            ChoNgoi cho = entry.getKey();
            LichTrinh lt = entry.getValue();
            
            if (lt != null && lt.getMaLichTrinh().equals(lichTrinhDangChon.getMaLichTrinh())) {
                if (!danhSachGheDangChon.contains(cho)) {
                    danhSachGheDangChon.add(cho);
                }
            }
        }
        
        // Refresh sơ đồ ghế của toa đang chọn để hiển thị màu xanh cho các ghế trong giỏ
        if (toaDangChon != null) {
            hienThiSoDoGheTrongPanel(toaDangChon, lichTrinhDangChon);
        } else if (!danhSachGheDangChon.isEmpty()) {
            // Nếu có ghế nhưng chưa chọn toa, hiển thị toa của ghế đầu tiên
            ChoNgoi gheFirst = danhSachGheDangChon.get(0);
            if (gheFirst.getToa() != null) {
                hienThiSoDoGheTrongPanel(gheFirst.getToa(), lichTrinhDangChon);
            }
        }
    }
    
    // ===================== GETTER METHODS =====================
    
    /**
     * Lấy danh sách ghế đang chọn (chiều hiện tại)
     */
    public List<ChoNgoi> getDanhSachGheDangChon() {
        return danhSachGheDangChon;
    }
    
    /**
     * Lấy TẤT CẢ vé trong giỏ (cả chiều đi + chiều về) với thông tin đầy đủ
     * @return Map<ChoNgoi, LichTrinh> - Ghế và lịch trình tương ứng
     */
    public Map<ChoNgoi, LichTrinh> getAllVeTrongGioVe() {
        // ⚡ TỐI ƯU: Trả về trực tiếp map đã lưu, không cần parse và query lại database
        return new LinkedHashMap<>(mapGheLichTrinh);
    }
    
    /**
     * Lấy lịch trình đang chọn
     */
    public LichTrinh getLichTrinhDangChon() {
        return lichTrinhDangChon;
    }
    
    /**
     * Reload sơ đồ ghế sau khi thanh toán (để cập nhật ghế đã bán)
     */
    public void reloadSoDoGhe() {
        if (lichTrinhDangChon == null || toaDangChon == null) {
            return;
        }
        
        // Clear giỏ vé và danh sách ghế đang chọn (vì đã thanh toán xong)
        modelGioVe.setRowCount(0);
        danhSachGheDangChon.clear();
        mapGheLichTrinh.clear(); // Clear map lưu lịch trình
        mapGheGaDi.clear();      // Clear map lưu ga đi
        mapGheGaDen.clear();     // Clear map lưu ga đến
        
        // Reload sơ đồ ghế của toa đang hiển thị
        hienThiSoDoGheTrongPanel(toaDangChon, lichTrinhDangChon);
        
        // Cập nhật lại số lượng đơn treo
        capNhatSoLuongDonTreo();
    }
    
    /**
     * Cập nhật số lượng đơn treo lên text của nút btnXuLyDonTam
     */
    public void capNhatSoLuongDonTreo() {
        int soLuong = QuanLyDonTreo.demSoLuong();
        if (soLuong > 0) {
            btnXuLyDonTam.setText("Xử lý đơn tạm (" + soLuong + ")");
            btnXuLyDonTam.setForeground(new Color(0, 102, 204));
            btnXuLyDonTam.setFont(new Font("Segoe UI", Font.BOLD, 12));
        } else {
            btnXuLyDonTam.setText("Xử lý đơn tạm");
            btnXuLyDonTam.setForeground(Color.BLACK);
            btnXuLyDonTam.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHuyCho;
    private javax.swing.JButton btnHuyTatCa;
    private javax.swing.JButton btnMuaVe;
    private javax.swing.JButton btnTimKiem;
    private javax.swing.JButton btnXuLyDonTam;
    private com.toedter.calendar.JDateChooser dchNgayDi;
    private com.toedter.calendar.JDateChooser dchNgayVe;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel lblGaDen;
    private javax.swing.JLabel lblGaDi;
    private javax.swing.JLabel lblNgayDi;
    private javax.swing.JLabel lblNgayVe;
    private javax.swing.JPanel pnlChieuMuaVe;
    private javax.swing.JPanel pnlGioVe;
    private javax.swing.JPanel pnlSoDoGhe;
    private javax.swing.JPanel pnlThongTinHanhTrinh;
    private javax.swing.JPanel pnlToaTau;
    private javax.swing.JPanel pnlTuyen;
    private javax.swing.JRadioButton radChieuDi;
    private javax.swing.JRadioButton radChieuVe;
    private javax.swing.JRadioButton radKhuHoi;
    private javax.swing.JRadioButton radMotChieu;
    private javax.swing.JScrollPane scrGioVe;
    private javax.swing.JTable tblGioVe;
    private javax.swing.JTextField txtGaDen;
    private javax.swing.JTextField txtGaDi;
    // End of variables declaration//GEN-END:variables
    
    //=========================================================================
    // PUBLIC METHODS FOR SYNC WITH Gui_NhapThongTinBanVe
    //=========================================================================
    
    /**
     * Xóa 1 ghế đã chọn theo index (gọi từ Gui_NhapThongTinBanVe)
     * @param index Index của ghế trong danh sách
     */
    public void xoaGheDaChon(int index) {
        if (index < 0 || index >= danhSachGheDangChon.size()) {
            return;
        }
        
        // Xóa khỏi danh sách
        danhSachGheDangChon.remove(index);
        
        // Xóa khỏi table giỏ vé
        if (index < modelGioVe.getRowCount()) {
            modelGioVe.removeRow(index);
        }
        
        // RELOAD sơ đồ ghế để cập nhật UI (button đổi màu)
        if (toaDangChon != null && lichTrinhDangChon != null) {
            hienThiSoDoGheTrongPanel(toaDangChon, lichTrinhDangChon);
        }
    }
    
    /**
     * Xóa tất cả ghế đã chọn (gọi từ Gui_NhapThongTinBanVe)
     */
    public void xoaTatCaGheDaChon() {
        // Clear danh sách
        danhSachGheDangChon.clear();
        mapGheLichTrinh.clear(); // Clear map lưu lịch trình
        
        // Clear table giỏ vé
        modelGioVe.setRowCount(0);
        
        // RELOAD sơ đồ ghế để reset tất cả button về trắng
        if (toaDangChon != null && lichTrinhDangChon != null) {
            hienThiSoDoGheTrongPanel(toaDangChon, lichTrinhDangChon);
        }
    }
    
    public Map<ChoNgoi, Ga> getMapGheGaDi() {
        return mapGheGaDi;
    }
    
    public Map<ChoNgoi, Ga> getMapGheGaDen() {
        return mapGheGaDen;
    }
}
