/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package iuh.fit.gui;

import service.*;
import iuh.fit.utils.ClientContext;
import entity.*;
import iuh.fit.utils.ThermalPrinter;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author PC
 */
public class Gui_TraVe extends javax.swing.JPanel {

    private IHoaDonService hoaDonService;
    private IVeService veService;
    private IHoaDonService chiTietHoaDonService; // Assuming HoaDonService handles ChiTiet
    private IDonTreoService donTreoService;
    private DefaultTableModel modelHoaDon;
    private DefaultTableModel modelVe;
    private NumberFormat currencyFormat;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter timeFormatter;

    /**
     * Creates new form Gui_TraVe
     */
    public Gui_TraVe() {
        initComponents();
        initDAO();
        initCustomComponents();
        loadAllHoaDon();
    }
    
    private void initDAO() {
        hoaDonService = ClientContext.getHoaDonService();
        veService = ClientContext.getVeService();
        donTreoService = ClientContext.getDonTreoService();
    }
    
    private void initCustomComponents() {
        // Lấy model của các table
        modelHoaDon = (DefaultTableModel) jTable1.getModel();
        modelVe = (DefaultTableModel) jTable2.getModel();
        
        // Format tiền tệ và ngày giờ
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        // Thêm listener cho table hóa đơn
        jTable1.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = jTable1.getSelectedRow();
                if (selectedRow >= 0) {
                    onHoaDonSelected(selectedRow);
                }
            }
        });
        
        // Thêm listener cho table vé
        jTable2.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = jTable2.getSelectedRow();
                if (selectedRow >= 0) {
                    onVeSelected(selectedRow);
                }
            }
        });
        
        // ⚡ THÊM NHÃN LỊCH SỬ IN VÀO jPanel2
        lblLichSuIn = new javax.swing.JLabel("Thông tin in vé: Chưa chọn vé");
        lblLichSuIn.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblLichSuIn.setForeground(new java.awt.Color(255, 102, 0));
        jPanel2.add(lblLichSuIn);

        // ⚡ TỰ ĐỘNG REFRESH KHI HIỆN PANEL
        // Khi người dùng chuyển tab sang Trả Vé, bảng hóa đơn sẽ tự động load lại đơn mới nhất
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && this.isShowing()) {
                loadAllHoaDon();
            }
        });
    }
    
    /**
     * Load 15 hóa đơn vừa thanh toán gần nhất (Chỉ lấy đến ngày hiện tại)
     */
    private void loadAllHoaDon() {
        System.out.println("📋 Đang tải 15 hóa đơn gần nhất...");
        modelHoaDon.setRowCount(0);
        
        List<HoaDon> danhSach = null;
        try {
            danhSach = hoaDonService.getAll();
        } catch (Exception e) {
            e.printStackTrace();
            danhSach = new java.util.ArrayList<>();
        }
        
        // Lấy ngày hiện tại để lọc (không hiện các đơn hàng "tương lai" từ demo)
        java.time.LocalDate today = java.time.LocalDate.now();
        
        // Sắp xếp và lọc
        List<HoaDon> filteredList = danhSach.stream()
            .filter(hd -> hd.getNgayTao() != null && !hd.getNgayTao().toLocalDate().isAfter(today))
            .sorted((hd1, hd2) -> {
                // 1. So sánh Ngày (Mới nhất lên đầu)
                int dateCompare = hd2.getNgayTao().compareTo(hd1.getNgayTao());
                if (dateCompare != 0) return dateCompare;
                
                // 2. Ưu tiên Hóa đơn "THẬT" lên trên cùng
                // Hóa đơn thật bắt đầu bằng HD và không chứa các ký tự đặc biệt của demo
                String ma1 = hd1.getMaHoaDon();
                String ma2 = hd2.getMaHoaDon();
                boolean is1Real = ma1.startsWith("HD") && !ma1.contains("DEMO") && !ma1.contains("DBHD");
                boolean is2Real = ma2.startsWith("HD") && !ma2.contains("DEMO") && !ma2.contains("DBHD");
                
                if (is1Real && !is2Real) return -1;
                if (!is1Real && is2Real) return 1;
                
                // 3. Nếu cùng loại, so sánh Giờ (Mới nhất lên đầu)
                if (hd1.getGioTao() == null || hd2.getGioTao() == null) return 0;
                int timeCompare = hd2.getGioTao().compareTo(hd1.getGioTao());
                if (timeCompare != 0) return timeCompare;
                
                return hd2.getMaHoaDon().compareTo(hd1.getMaHoaDon());
            })
            .limit(15)
            .collect(java.util.stream.Collectors.toList());
        
        for (HoaDon hd : filteredList) {
            modelHoaDon.addRow(new Object[]{
                hd.getMaHoaDon(),
                hd.getNhanVien() != null ? hd.getNhanVien().getMaNhanVien() : "",
                hd.getKhachHang() != null ? hd.getKhachHang().getCCCD() : "",
                hd.getKhachHang() != null ? hd.getKhachHang().getHoTen() : "",
                hd.getKhachHang() != null ? hd.getKhachHang().getSDT() : "",
                hd.getKhuyenMai() != null ? hd.getKhuyenMai() : "Không",
                hd.getNgayTao() != null ? hd.getNgayTao().toLocalDate().format(dateFormatter) : "",
                hd.getGioTao() != null ? hd.getGioTao().toLocalTime().format(timeFormatter) : "",
                currencyFormat.format(hd.getTongTien())
            });
        }
        
        System.out.println("✅ Đã hiển thị " + filteredList.size() + " hóa đơn gần nhất.");
    }
    
    /**
     * Khi chọn hóa đơn → Load danh sách vé
     */
    private void onHoaDonSelected(int row) {
        String maHoaDon = modelHoaDon.getValueAt(row, 0).toString();
        loadVeByHoaDon(maHoaDon);
        
        // Hiển thị thông tin hóa đơn vào các text field
        jTextField1.setText(modelHoaDon.getValueAt(row, 0).toString()); // Mã hóa đơn
        jTextField2.setText(modelHoaDon.getValueAt(row, 1).toString()); // Mã NV
        jTextField3.setText(modelHoaDon.getValueAt(row, 2).toString()); // CCCD
        jTextField4.setText(modelHoaDon.getValueAt(row, 3).toString()); // Tên KH
        jTextField5.setText(modelHoaDon.getValueAt(row, 4).toString()); // SĐT
    }
    
    /**
     * Khi chọn vé → Hiển thị thông tin vé
     */
    private void onVeSelected(int row) {
        try {
            // Lấy thông tin từ bảng vé
            String maVe = modelVe.getValueAt(row, 0).toString();
            String cccd = modelVe.getValueAt(row, 1).toString();
            String tenKhach = modelVe.getValueAt(row, 2).toString();
            String giaVe = modelVe.getValueAt(row, 10).toString();
            
            // Hiển thị thông tin vé vào các text field
            // jTextField6: Mã vé
            // jTextField7: CCCD
            // jTextField8: Họ tên
            // jTextField9: Giá
            jTextField6.setText(maVe);
            jTextField7.setText(cccd);
            jTextField8.setText(tenKhach);
            jTextField9.setText(giaVe);
            
            // ⚡ HIỂN THỊ LỊCH SỬ IN CHI TIẾT
            int count = 0; // lichSuInVeDAO.countPrintTimes(maVe);
            Object lastPrint = null; // lichSuInVeDAO.findLastPrint(maVe);
            
            // Trạng thái mặc định cho nhãn và ô nhập liệu
            jTextField6.setBackground(java.awt.Color.WHITE);
            jLabel8.setText("Mã vé:");
            jLabel8.setForeground(java.awt.Color.BLACK);
            
            /*
            if (count > 0) {
                String time = lastPrint != null ? lastPrint.getThoiGianIn().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "N/A";
                String nv = (lastPrint != null && lastPrint.getNhanVien() != null) ? lastPrint.getNhanVien().getMaNhanVien() : "N/A";
                
                if (lblLichSuIn != null) {
                    lblLichSuIn.setText("Đã in " + count + " lần. Lần cuối: " + time + " bởi " + nv);
                    lblLichSuIn.setForeground(new java.awt.Color(255, 102, 0)); // Màu cam cho nổi bật vừa phải
                    lblLichSuIn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                }
            } else {
                if (lblLichSuIn != null) {
                    lblLichSuIn.setText("Vé chưa in lần nào.");
                    lblLichSuIn.setForeground(new java.awt.Color(0, 153, 51)); // Màu xanh
                    lblLichSuIn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
                }
            }
            */
            if (lblLichSuIn != null) {
                lblLichSuIn.setText("Thông tin in vé: N/A");
            }
            
            System.out.println("✅ Đã hiển thị thông tin vé: " + maVe);
        } catch (Exception e) {
            System.out.println("❌ Lỗi hiển thị thông tin vé: " + e.getMessage());
        }
    }
    
    /**
     * Parse string thành double (loại bỏ ký tự đặc biệt như ₫, dấu phân cách)
     */
    private double parseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        
        // Xóa tất cả ký tự không phải số (bao gồm cả dấu phân cách, ₫, khoảng trắng, v.v.)
        str = str.replaceAll("[^0-9.]", "");
        
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Tính tiền hoàn trả cho VÉ CÁ NHÂN theo quy định:
     * - Dưới 4 giờ: không trả vé (0%)
     * - Từ 4 đến dưới 24 giờ: thu 20% giá vé (hoàn 80%)
     * - Trên 24 giờ: thu 10% giá vé (hoàn 90%)
     * - Mức trả vé tối thiểu: 10.000đ/vé
     * 
     * @param giaVe Giá vé
     * @param gioKhoiHanh Thời gian khởi hành
     * @return Số tiền hoàn trả
     */
    private double tinhTienHoanTraCaNhan(double giaVe, java.time.LocalDateTime gioKhoiHanh) {
        if (gioKhoiHanh == null) return 0;
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long hoursUntilDeparture = java.time.Duration.between(now, gioKhoiHanh).toHours();
        
        System.out.println("⏱️ [CÁ NHÂN] Thời gian còn lại: " + hoursUntilDeparture + " giờ");
        
        double tienHoan = 0;
        
        if (hoursUntilDeparture < 4) {
            // Dưới 4 giờ: không được trả vé
            System.out.println("❌ Không được trả vé cá nhân: dưới 4 giờ trước khởi hành");
            return -1; 
        } else if (hoursUntilDeparture < 24) {
            // Từ 4 đến dưới 24 giờ: khấu 20% (hoàn 80%)
            tienHoan = giaVe * 0.8;
            System.out.println("🔸 Khấu 20% (Dưới 24h)");
        } else {
            // Trên 24 giờ: khấu 10% (hoàn 90%)
            tienHoan = giaVe * 0.9;
            System.out.println("🔸 Khấu 10% (Trên 24h)");
        }
        
        // Mức trả vé tối thiểu: 10.000đ/vé
        if (tienHoan > 0 && (giaVe - tienHoan) < 1000) { // Nếu phí khấu quá thấp
             // Có thể tùy chỉnh lệ phí tối thiểu ở đây
        }
        
        return tienHoan;
    }
    
    /**
     * Tính tiền hoàn trả cho VÉ TẬP THỂ theo quy định:
     * - Dưới 24 giờ: không trả vé (0%)
     * - Từ 24 đến dưới 72 giờ: thu 20% giá vé (hoàn 80%)
     * - Trên 72 giờ: thu 10% giá vé (hoàn 90%)
     * 
     * @param giaVe Giá vé
     * @param gioKhoiHanh Thời gian khởi hành
     * @return Số tiền hoàn trả
     */
    private double tinhTienHoanTraTapThe(double giaVe, java.time.LocalDateTime gioKhoiHanh) {
        if (gioKhoiHanh == null) return 0;
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long hoursUntilDeparture = java.time.Duration.between(now, gioKhoiHanh).toHours();
        
        System.out.println("⏱️ [TẬP THỂ] Thời gian còn lại đến khởi hành: " + hoursUntilDeparture + " giờ");
        
        double tienHoan = 0;
        
        if (hoursUntilDeparture < 24) {
            // Dưới 24 giờ: không trả vé
            System.out.println("❌ Không được trả vé tập thể: dưới 24 giờ trước khởi hành");
            return -1;
        } else if (hoursUntilDeparture < 72) {
            // Từ 24 đến dưới 72 giờ: hoàn 80%
            tienHoan = giaVe * 0.8;
        } else {
            // Trên 72 giờ: hoàn 90%
            tienHoan = giaVe * 0.9;
        }
        
        System.out.println("💰 [TẬP THỂ] Tiền hoàn trả: " + currencyFormat.format(tienHoan));
        return tienHoan;
    }
    
    /**
     * Load danh sách vé theo mã hóa đơn
     */
    private void loadVeByHoaDon(String maHoaDon) {
        System.out.println("🎫 Loading vé cho hóa đơn: " + maHoaDon);
        modelVe.setRowCount(0);
        
        // Xóa thông tin vé cũ
        jTextField6.setText("");
        jTextField7.setText("");
        jTextField8.setText("");
        jTextField9.setText("");
        if (lblLichSuIn != null) lblLichSuIn.setText("Thông tin in vé: Chưa chọn vé");
        
        List<entity.Ve> danhSachVe = null;
        List<ChiTietHoaDon> chiTietList = new java.util.ArrayList<>();
        try {
            danhSachVe = veService.findByMaHoaDon(maHoaDon);
            chiTietList = hoaDonService.getChiTietByMaHoaDon(maHoaDon);
        } catch (Exception e) {
            e.printStackTrace();
            if (danhSachVe == null) danhSachVe = new java.util.ArrayList<>();
        }
        
        System.out.println("✅ Tải " + danhSachVe.size() + " vé cho hóa đơn " + maHoaDon);
        
        for (Ve ve : danhSachVe) {
            // ⚡ LẤY GIÁ THỰC TẾ TỪ DANH SÁCH CHI TIẾT ĐÃ TẢI
            double giaThucTe = ve.getGiaVe();
            for (ChiTietHoaDon ct : chiTietList) {
                if (ct.getVe().getMaVe().equals(ve.getMaVe())) {
                    giaThucTe = ct.getGiaVe() - ct.getMucGiam();
                    break;
                }
            }
            
            modelVe.addRow(new Object[]{
                ve.getMaVe(),
                ve.getSoCCCD() != null ? ve.getSoCCCD() : "",
                ve.getTenKhachHang() != null ? ve.getTenKhachHang() : "",
                ve.getLoaiVe() != null ? ve.getLoaiVe().getTenLoaiVe() : "",
                ve.getLichTrinh() != null && ve.getLichTrinh().getGaDi() != null ? 
                    ve.getLichTrinh().getGaDi().getTenGa() : "",
                ve.getLichTrinh() != null && ve.getLichTrinh().getGaDen() != null ? 
                    ve.getLichTrinh().getGaDen().getTenGa() : "",
                ve.getLichTrinh() != null && ve.getLichTrinh().getChuyenTau() != null ? 
                    ve.getLichTrinh().getChuyenTau().getSoHieuTau() : "",
                ve.getChoNgoi() != null && ve.getChoNgoi().getToa() != null ? 
                    String.valueOf(ve.getChoNgoi().getToa().getSoToa()) : "",
                ve.getChoNgoi() != null ? String.valueOf(ve.getChoNgoi().getViTri()) : "",
                ve.getThoiGianLenTau() != null ? 
                    ve.getThoiGianLenTau().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "",
                currencyFormat.format(giaThucTe), // ⚡ Giá thực tế chính xác
                0
            });
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        btnTimHoaDon = new javax.swing.JButton();
        btnInHoaDon = new javax.swing.JButton();
        btnXoaTrang = new javax.swing.JButton();
        btnTraTapVe = new javax.swing.JButton();
        btnTimVe = new javax.swing.JButton();
        btnInVe = new javax.swing.JButton();
        btnTraVe = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setBackground(new java.awt.Color(234, 243, 251));

        jPanel1.setBackground(new java.awt.Color(234, 243, 251));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Thông tin hóa đơn"));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Mã hóa đơn:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Mã nhân viên:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("CCCD:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Tên khách hàng:");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Số điện thoại:");

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(42, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(234, 243, 251));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Thông tin vé"));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Mã vé:");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setText("CCCD:");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setText("Họ tên:");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setText("Giá:");

        jTextField6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addGap(90, 90, 90)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Hóa Đơn");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Bảng Vé");

        jTable1.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã hóa đơn", "Mã nhân viên", "CCCD", "Tên khách hàng", "Số điện thoại", "Khuyến mãi", "Ngày lập", "Giờ lập ", "Tổng tiền"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jTable2.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã vé ", "CCCD", "Tên khách hàng", "Đối tượng", "Ga đi ", "Ga đến ", "Mã tàu", "Số toa", "Vị trí chỗ ", "Giờ lên tàu", "Giá", "Số lần in"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        btnTimHoaDon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/TimKiem.png"))); // NOI18N
        btnTimHoaDon.setText("Tìm hóa đơn");
        btnTimHoaDon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTimHoaDonActionPerformed(evt);
            }
        });

        btnInHoaDon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/print.png"))); // NOI18N
        btnInHoaDon.setText("In Hóa Đơn");
        btnInHoaDon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInHoaDonActionPerformed(evt);
            }
        });

        btnXoaTrang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/clear.png"))); // NOI18N
        btnXoaTrang.setText("Xóa trắng");
        btnXoaTrang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaTrangActionPerformed(evt);
            }
        });

        btnTraTapVe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/documents.png"))); // NOI18N
        btnTraTapVe.setText("Trả tập vé");
        btnTraTapVe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTraTapVeActionPerformed(evt);
            }
        });

        btnTimVe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/TimKiem.png"))); // NOI18N
        btnTimVe.setText("Tìm vé");
        btnTimVe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTimVeActionPerformed(evt);
            }
        });

        btnInVe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/print.png"))); // NOI18N
        btnInVe.setText("In vé");
        btnInVe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInVeActionPerformed(evt);
            }
        });

        btnTraVe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/exchange.png"))); // NOI18N
        btnTraVe.setText("Trả vé");
        btnTraVe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTraVeActionPerformed(evt);
            }
        });

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/document-management.png"))); // NOI18N
        jButton1.setText("In tập vé");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(47, 47, 47)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnTimHoaDon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnInHoaDon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(45, 45, 45)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnTraTapVe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnXoaTrang, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 736, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(46, 46, 46)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnInVe, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnTimVe, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnTraVe, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton1))
                                .addGap(51, 51, 51)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnTimHoaDon, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnXoaTrang, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnInHoaDon, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTraTapVe, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnTimVe, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTraVe, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnInVe, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void btnTimHoaDonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimHoaDonActionPerformed
        // ⚡ TÌM HÓA ĐƠN theo bất kỳ thông tin nào
        String maHD = jTextField1.getText().trim();
        String maNV = jTextField2.getText().trim();
        String cccd = jTextField3.getText().trim();
        String tenKH = jTextField4.getText().trim();
        String sdt = jTextField5.getText().trim();
        
        // Nếu tất cả đều trống → Load tất cả
        if (maHD.isEmpty() && maNV.isEmpty() && cccd.isEmpty() && tenKH.isEmpty() && sdt.isEmpty()) {
            loadAllHoaDon();
            return;
        }
        
        System.out.println("🔍 Tìm kiếm hóa đơn với: mã=" + maHD + ", maNV=" + maNV + ", cccd=" + cccd + ", tên=" + tenKH + ", sdt=" + sdt);
        
        // Lọc hóa đơn
        List<HoaDon> allHoaDon = null;
        try {
            allHoaDon = hoaDonService.getAll();
        } catch (Exception e) {
            e.printStackTrace();
            allHoaDon = new java.util.ArrayList<>();
        }
        modelHoaDon.setRowCount(0);
        int count = 0;
        
        for (HoaDon hd : allHoaDon) {
            boolean match = true;
            
            // Kiểm tra từng điều kiện (nếu có nhập)
            if (!maHD.isEmpty() && !hd.getMaHoaDon().toLowerCase().contains(maHD.toLowerCase())) {
                match = false;
            }
            if (!maNV.isEmpty() && hd.getNhanVien() != null && 
                !hd.getNhanVien().getMaNhanVien().toLowerCase().contains(maNV.toLowerCase())) {
                match = false;
            }
            if (!cccd.isEmpty() && hd.getKhachHang() != null && 
                !hd.getKhachHang().getCCCD().toLowerCase().contains(cccd.toLowerCase())) {
                match = false;
            }
            if (!tenKH.isEmpty() && hd.getKhachHang() != null && 
                !hd.getKhachHang().getHoTen().toLowerCase().contains(tenKH.toLowerCase())) {
                match = false;
            }
            if (!sdt.isEmpty() && hd.getKhachHang() != null && 
                !hd.getKhachHang().getSDT().toLowerCase().contains(sdt.toLowerCase())) {
                match = false;
            }
            
            if (match) {
                modelHoaDon.addRow(new Object[]{
                    hd.getMaHoaDon(),
                    hd.getNhanVien() != null ? hd.getNhanVien().getMaNhanVien() : "",
                    hd.getKhachHang() != null ? hd.getKhachHang().getCCCD() : "",
                    hd.getKhachHang() != null ? hd.getKhachHang().getHoTen() : "",
                    hd.getKhachHang() != null ? hd.getKhachHang().getSDT() : "",
                    hd.getKhuyenMai() != null ? hd.getKhuyenMai() : "Không",
                    hd.getNgayTao() != null ? hd.getNgayTao().toLocalDate().format(dateFormatter) : "",
                    hd.getGioTao() != null ? hd.getGioTao().toLocalTime().format(timeFormatter) : "",
                    currencyFormat.format(hd.getTongTien())
                });
                count++;
            }
        }
        
        System.out.println("✅ Tìm thấy " + count + " hóa đơn");
        
        if (count == 0) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy hóa đơn nào!",
                "Kết quả tìm kiếm",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnTimHoaDonActionPerformed

    private void btnTraTapVeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTraTapVeActionPerformed
        // ⚡ TRẢ TẬP VÉ (VÉ TẬP THỂ) - Tính tiền hoàn trả theo quy định
        
        // Lấy thông tin hóa đơn
        String maHoaDon = jTextField1.getText().trim();
        if (maHoaDon.isEmpty()) {
            int selectedRow = jTable1.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn hóa đơn cần trả!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            maHoaDon = modelHoaDon.getValueAt(selectedRow, 0).toString();
        }
        
        HoaDon hoaDonCanTra = null;
        try {
            hoaDonCanTra = hoaDonService.findByMaHoaDon(maHoaDon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (hoaDonCanTra == null) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy thông tin hóa đơn!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        double tongTienHoaDon = hoaDonCanTra.getTongTien();
        
        System.out.println("📋 Trả tập vé cho hóa đơn: " + maHoaDon + " | Tổng tiền: " + String.format("%,.0f đ", tongTienHoaDon));
        
        // Kiểm tra số lượng vé trong bảng vé
        int soLuongVe = modelVe.getRowCount();
        if (soLuongVe == 0) {
            JOptionPane.showMessageDialog(this,
                "Không có vé nào trong hóa đơn này!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        System.out.println("🎫 Số lượng vé: " + soLuongVe);
        
        // Tính tổng tiền hoàn trả cho tất cả vé (theo quy định VÉ TẬP THỂ)
        double tongTienHoanLai = 0;
        double tongGiaThucTeXuLy = 0; 
        boolean coVeKhongDuDieuKien = false;
        StringBuilder chiTietVe = new StringBuilder();
        
        for (int i = 0; i < soLuongVe; i++) {
            String maVe = modelVe.getValueAt(i, 0).toString();
            String thoiGianKhoiHanhStr = modelVe.getValueAt(i, 9).toString();
            
            Ve veHienTai = null;
            try {
                veHienTai = veService.findByMaVe(maVe);
            } catch (Exception e) {}
            
            if (veHienTai == null) continue;
            
            // ⚡ LẤY GIÁ THỰC TẾ TỪ CHI TIẾT HÓA ĐƠN
            double giaVeThucTe = veHienTai.getGiaVe();
            try {
                List<ChiTietHoaDon> ctList = hoaDonService.getChiTietByMaHoaDon(maHoaDon);
                for (ChiTietHoaDon ct : ctList) {
                    if (ct.getVe().getMaVe().equals(veHienTai.getMaVe())) {
                        giaVeThucTe = ct.getGiaVe() - ct.getMucGiam();
                        break;
                    }
                }
            } catch (Exception e) {}
            
            java.time.LocalDateTime gioKhoiHanh = null;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                gioKhoiHanh = java.time.LocalDateTime.parse(thoiGianKhoiHanhStr, formatter);
            } catch (Exception e) { continue; }
            
            double tienHoanVe = tinhTienHoanTraTapThe(giaVeThucTe, gioKhoiHanh);
            
            if (tienHoanVe < 0) {
                coVeKhongDuDieuKien = true;
                chiTietVe.append(String.format("• %s: Không đủ điều kiện trả\n", maVe));
            } else {
                tongTienHoanLai += tienHoanVe;
                tongGiaThucTeXuLy += giaVeThucTe;
                chiTietVe.append(String.format("• %s: %s\n", maVe, currencyFormat.format(tienHoanVe)));
            }
        }
        
        // Nếu có vé không đủ điều kiện → Hỏi xem có muốn tiếp tục không
        if (coVeKhongDuDieuKien) {
            String warning = "Một số vé không đủ điều kiện trả!\n" +
                           "Vé tập thể phải trả trước khi tàu khởi hành ít nhất 24 giờ.\n\n" +
                           "Chi tiết:\n" + chiTietVe.toString() +
                           "\nBạn có muốn trả các vé còn lại không?";
            
            int confirm = JOptionPane.showConfirmDialog(this, warning, "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        }
        
        if (tongTienHoanLai <= 0) {
            JOptionPane.showMessageDialog(this, "Không có vé nào đủ điều kiện trả!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double chietKhau = tongGiaThucTeXuLy - tongTienHoanLai;
        
        String message = String.format(
            "Xác nhận trả tập vé cho hóa đơn %s?\n\n" +
            "Số lượng vé xử lý: %d\n" +
            "Tổng số tiền khách đã trả (cho các vé này): %,.0f đ\n" +
            "Phí khấu trừ (Phí trả vé): %,.0f đ\n" +
            "Số tiền hoàn lại cho khách: %,.0f đ",
            maHoaDon, soLuongVe, tongGiaThucTeXuLy, chietKhau, tongTienHoanLai
        );
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            message,
            "Xác nhận",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // ⚡ Xử lý logic trả tập vé trong database
            System.out.println("✅ Đang xử lý trả tập vé cho hóa đơn: " + maHoaDon);
            System.out.println("💰 Tổng tiền hoàn lại: " + currencyFormat.format(tongTienHoanLai));
            
            int soVeDaTra = 0;
            
            // 1. Cập nhật trạng thái tất cả vé trong hóa đơn thành 0 (đã trả)
            for (int i = 0; i < soLuongVe; i++) {
                String maVe = modelVe.getValueAt(i, 0).toString();
                boolean success = false;
                try {
                    success = veService.delete(maVe); // Set trangThai = 0
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (success) {
                    soVeDaTra++;
                    System.out.println("   ✅ Đã trả vé: " + maVe);
                } else {
                    System.err.println("   ❌ Lỗi khi trả vé: " + maVe);
                }
            }
            
            // 2. Cập nhật tổng tiền hóa đơn (Giữ lại tiền lệ phí/khấu)
            try {
                HoaDon hd = hoaDonService.findByMaHoaDon(maHoaDon);
                if (hd != null) {
                    double tongTienCu = hd.getTongTien();
                    // Tổng tiền mới = Tổng tiền cũ - Số tiền đã hoàn lại cho khách
                    // (Tương đương với: Tiền các vé còn lại + Tiền phí của các vé đã trả)
                    double tongTienMoi = tongTienCu - tongTienHoanLai;
                    if (tongTienMoi < 0) tongTienMoi = 0;
                    
                    hd.setTongTien(tongTienMoi);
                    hoaDonService.update(hd);
                    
                    System.out.println("DEBUG [TraTapVe]: " + maHoaDon);
                    System.out.println("   - Tong tien cu: " + tongTienCu);
                    System.out.println("   - Hoan lai khach: " + tongTienHoanLai);
                    System.out.println("   - Giu lai (Tong moi): " + tongTienMoi);
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi cập nhật hóa đơn: " + e.getMessage());
            }
            
            // 3. Hiển thị thông báo thành công
            JOptionPane.showMessageDialog(this,
                String.format("Đã trả tập vé thành công!\nSố vé đã trả: %d/%d\nSố tiền hoàn lại cho khách: %,.0f đ",
                    soVeDaTra, soLuongVe, tongTienHoanLai),
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            
            // 4. Reload lại danh sách hóa đơn và xóa bảng vé
            loadAllHoaDon();
            modelVe.setRowCount(0); // Clear bảng vé vì đã trả hết
            
            // 5. Refresh Dashboard nếu đang hiển thị
            refreshDashboardIfVisible();
            
            System.out.println("✅ Hoàn tất trả tập vé");
        } else {
            System.out.println("❌ Đã hủy trả tập vé");
        }
    }//GEN-LAST:event_btnTraTapVeActionPerformed

    private void btnXoaTrangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaTrangActionPerformed
        // ⚡ XÓA TRẮNG - Clear tất cả text field
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField5.setText("");
        jTextField6.setText("");
        jTextField7.setText("");
        jTextField8.setText("");
        jTextField9.setText("");
        
        // Clear bảng vé
        modelVe.setRowCount(0);
        
        // Reload tất cả hóa đơn
        loadAllHoaDon();
        
        System.out.println("🧹 Đã xóa trắng tất cả dữ liệu");
    }//GEN-LAST:event_btnXoaTrangActionPerformed

    private void btnTraVeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTraVeActionPerformed
        // ⚡ TRẢ VÉ ĐƠN LẺ (VÉ CÁ NHÂN)
        int selectedRow = jTable2.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn vé cần trả!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String maVe = modelVe.getValueAt(selectedRow, 0).toString();
        String thoiGianKhoiHanhStr = modelVe.getValueAt(selectedRow, 9).toString();
        
        System.out.println("🎫 Trả vé: " + maVe + " | Khởi hành: " + thoiGianKhoiHanhStr);
        
        // Lấy giá vé từ database để tránh lỗi parse từ GUI
        Ve veCanTra = null;
        try {
            veCanTra = veService.findByMaVe(maVe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (veCanTra == null) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy thông tin vé!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        double giaVe = veCanTra.getGiaVe();
        
        // Lấy mã hóa đơn để truy vấn chi tiết
        String maHoaDon = jTextField1.getText().trim();
        if (maHoaDon.isEmpty()) {
            int hoaDonRow = jTable1.getSelectedRow();
            maHoaDon = hoaDonRow >= 0 ? modelHoaDon.getValueAt(hoaDonRow, 0).toString() : null;
        }
        
        // ⚡ LẤY GIÁ THỰC TẾ TỪ CHI TIẾT HÓA ĐƠN
        double giaVeThucTe = giaVe;
        if (maHoaDon != null) {
            try {
                List<ChiTietHoaDon> ctList = hoaDonService.getChiTietByMaHoaDon(maHoaDon);
                for (ChiTietHoaDon ct : ctList) {
                    if (ct.getVe().getMaVe().equals(maVe)) {
                        giaVeThucTe = ct.getGiaVe() - ct.getMucGiam();
                        break;
                    }
                }
            } catch (Exception e) {}
        }
        
        // Parse thời gian khởi hành (format: "dd/MM/yyyy HH:mm")
        java.time.LocalDateTime gioKhoiHanh = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            gioKhoiHanh = java.time.LocalDateTime.parse(thoiGianKhoiHanhStr, formatter);
        } catch (Exception e) {
            System.out.println("❌ Lỗi parse thời gian khởi hành: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Không thể xác định thời gian khởi hành của vé!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Tính tiền hoàn trả dựa trên GIÁ THỰC TẾ KHÁCH TRẢ
        double tienHoanLai = tinhTienHoanTraCaNhan(giaVeThucTe, gioKhoiHanh);
        
        if (tienHoanLai < 0) {
            JOptionPane.showMessageDialog(this,
                "Không thể trả vé!\n" +
                "Vé cá nhân phải trả trước khi tàu khởi hành ít nhất 24 giờ.",
                "Không đủ điều kiện trả vé",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Tính chiết khấu (số tiền bị trừ) = Giá thực tế - Tiền hoàn lại
        double chietKhau = giaVeThucTe - tienHoanLai;
        
        // Hiển thị dialog xác nhận
        String message = String.format(
            "Xác nhận trả vé %s?\n\n" +
            "Số tiền khách đã trả cho vé này: %,.0f đ\n" +
            "Phí khấu trừ (Phí trả vé): %,.0f đ\n" +
            "Số tiền hoàn lại cho khách: %,.0f đ",
            maVe,
            giaVeThucTe,
            chietKhau,
            tienHoanLai
        );
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            message,
            "Xác nhận trả vé",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // ⚡ Xử lý logic trả vé trong database
            System.out.println("✅ Đang xử lý trả vé: " + maVe);
            System.out.println("💰 Tiền hoàn lại: " + currencyFormat.format(tienHoanLai));
            
            // 1. Cập nhật trạng thái vé thành 0 (đã trả)
            boolean updateSuccess = false;
            try {
                updateSuccess = veService.delete(maVe); // delete() = soft delete (set trangThai = 0)
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (!updateSuccess) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi cập nhật trạng thái vé!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 2. Lấy mã hóa đơn và cập nhật tổng tiền (trừ số tiền hoàn lại)
            if (maHoaDon != null) {
                try {
                    HoaDon hoaDon = hoaDonService.findByMaHoaDon(maHoaDon);
                    if (hoaDon != null) {
                        double tongTienCu = hoaDon.getTongTien();
                        // Cập nhật tổng tiền hóa đơn: 
                        // Mới = Tổng cũ - Số tiền thực tế trả lại cho khách
                        double tongTienMoi = tongTienCu - tienHoanLai;
                        if (tongTienMoi < 0) tongTienMoi = 0;
                        
                        hoaDon.setTongTien(tongTienMoi);
                        hoaDonService.update(hoaDon);
                        
                        System.out.println("DEBUG [TraVeDon]: " + maHoaDon);
                        System.out.println("   - Tong tien cu: " + tongTienCu);
                        System.out.println("   - Hoan lai khach: " + tienHoanLai);
                        System.out.println("   - Giu lai (Tong moi): " + tongTienMoi);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // 3. Hiển thị thông báo thành công
            JOptionPane.showMessageDialog(this,
                String.format("Đã trả vé thành công!\nSố tiền hoàn lại: %,.0f đ", tienHoanLai),
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            
            // 4. Reload lại bảng vé và hóa đơn
            if (maHoaDon != null) {
                loadVeByHoaDon(maHoaDon); // Reload bảng vé (vé đã trả sẽ biến mất)
                loadAllHoaDon(); // Reload bảng hóa đơn (tổng tiền đã thay đổi)
            }
            
            // 5. Refresh Dashboard nếu đang hiển thị
            refreshDashboardIfVisible();
            
            System.out.println("✅ Hoàn tất trả vé");
        } else {
            System.out.println("❌ Đã hủy trả vé");
        }
    }//GEN-LAST:event_btnTraVeActionPerformed

    private void btnInVeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInVeActionPerformed
        // ⚡ IN VÉ - In ra máy in nhiệt
        int selectedRow = jTable2.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn vé cần in!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String maVe = modelVe.getValueAt(selectedRow, 0).toString();
        System.out.println("🖨️ In vé: " + maVe);
        
        // Lấy hóa đơn hiện tại
        int hoaDonRow = jTable1.getSelectedRow();
        if (hoaDonRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn hóa đơn trước!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String maHoaDon = modelHoaDon.getValueAt(hoaDonRow, 0).toString();
        
        // Lấy danh sách vé từ database
        List<Ve> danhSachVe = null;
        try {
            danhSachVe = veService.findByMaHoaDon(maHoaDon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (danhSachVe == null) return;

        // Tìm vé có mã trùng khớp
        Ve veCanIn = null;
        for (Ve ve : danhSachVe) {
            if (ve.getMaVe().equals(maVe)) {
                veCanIn = ve;
                break;
            }
        }
        
        if (veCanIn == null) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy thông tin vé!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // ⚡ KIỂM TRA SỐ LẦN IN ĐỂ CẢNH BÁO CHI TIẾT
        /*
        int printCount = lichSuInVeDAO.countPrintTimes(maVe);
        if (printCount > 0) {
            entity.LichSuInVe lastPrint = lichSuInVeDAO.findLastPrint(maVe);
            String lastTime = lastPrint != null ? lastPrint.getThoiGianIn().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
            
            String msg = String.format(
                "⚠️VÉ NÀY ĐÃ ĐƯỢC IN TRƯỚC ĐÓ!\n\n" +
                "• Mã vé: %s\n" +
                "• Số lần in: %d\n" +
                "• Lần in gần nhất: %s\n\n" +
                "Bạn có chắc chắn muốn tiếp tục in lại không?",
                maVe, printCount, lastTime
            );

            int confirm = JOptionPane.showConfirmDialog(this,
                msg,
                "Xác nhận in lại vé",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        */
        
        // In vé
        if (ThermalPrinter.printTicket(veCanIn)) {
            // Đợi một chút để background thread lưu DB xong
            try { Thread.sleep(500); } catch (Exception e) {}
            // Refresh lại bảng vé để cập nhật số lần in mới
            loadVeByHoaDon(maHoaDon);
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi in vé!");
        }
        System.out.println("✅ Đã xử lý lệnh in vé: " + maVe);
    }//GEN-LAST:event_btnInVeActionPerformed
    
    private void btnInHoaDonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInHoaDonActionPerformed
        // ⚡ IN HÓA ĐƠN - In ra máy in nhiệt
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn hóa đơn cần in!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Lấy mã hóa đơn
        String maHoaDon = modelHoaDon.getValueAt(selectedRow, 0).toString();
        
        System.out.println("🖨️ In hóa đơn: " + maHoaDon);
        
        // Load hóa đơn từ database
        HoaDon hoaDon = null;
        try {
            hoaDon = hoaDonService.findByMaHoaDon(maHoaDon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (hoaDon == null) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy thông tin hóa đơn!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Load chi tiết hóa đơn và vé
        List<ChiTietHoaDon> chiTietList = null;
        try {
            chiTietList = hoaDonService.getChiTietByMaHoaDon(maHoaDon);
            
            // Load thông tin vé cho mỗi chi tiết
            for (ChiTietHoaDon cthd : chiTietList) {
                Ve ve = veService.findByMaVe(cthd.getMaVe());
                cthd.setVe(ve);
            }
        } catch (Exception e) {
            e.printStackTrace();
            chiTietList = new java.util.ArrayList<>();
        }
        
        // Set danh sách chi tiết vào hóa đơn (để tính tổng tiền)
        hoaDon.setDanhSachChiTiet(chiTietList);
        
        // In hóa đơn (Kiểm tra lịch sử in để cảnh báo)
        boolean hasBeenPrinted = false;
        int maxPrints = 0;
        /*
        for (ChiTietHoaDon ct : chiTietList) {
            if (ct.getVe() != null) {
                int count = lichSuInVeDAO.countPrintTimes(ct.getVe().getMaVe());
                if (count > 0) {
                    hasBeenPrinted = true;
                    if (count > maxPrints) maxPrints = count;
                }
            }
        }
        */

        if (hasBeenPrinted) {
            String msg = String.format(
                "⚠️ HÓA ĐƠN NÀY ĐÃ TỪNG ĐƯỢC IN!\n\n" +
                "• Mã hóa đơn: %s\n" +
                "• Số lần in tối đa: %d\n\n" +
                "Bạn có chắc chắn muốn in lại hóa đơn này không?",
                maHoaDon, maxPrints
            );

            int confirm = JOptionPane.showConfirmDialog(this,
                msg,
                "Xác nhận in lại hóa đơn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        ThermalPrinter printer = new ThermalPrinter(hoaDon, chiTietList);
        boolean success = printer.printInvoice(); // In trực tiếp ra máy mặc định
        
        if (success) {
            // Đợi một chút để background thread lưu DB xong
            try { Thread.sleep(500); } catch (Exception e) {}
            // Refresh lại bảng vé để cập nhật số lần in mới
            loadVeByHoaDon(maHoaDon);
        } else {
            JOptionPane.showMessageDialog(this,
                "❌ Lỗi khi in hóa đơn!\n" +
                "- Kiểm tra máy in K58 đã được cài đặt chưa?\n" +
                "- Kiểm tra kết nối USB/Bluetooth\n" +
                "- Xem Console để biết chi tiết lỗi",
                "Lỗi In",
                JOptionPane.ERROR_MESSAGE);
        }
        // Không hiển thị thông báo thành công
    }//GEN-LAST:event_btnInHoaDonActionPerformed
    
    private void btnTimVeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimVeActionPerformed
        // ⚡ TÌM VÉ theo thông tin
        String maVe = jTextField6.getText().trim();
        String cccd = jTextField7.getText().trim();
        String hoTen = jTextField8.getText().trim();
        
        // Nếu tất cả đều trống → Yêu cầu chọn hóa đơn
        if (maVe.isEmpty() && cccd.isEmpty() && hoTen.isEmpty()) {
            int hoaDonRow = jTable1.getSelectedRow();
            if (hoaDonRow >= 0) {
                String maHoaDon = modelHoaDon.getValueAt(hoaDonRow, 0).toString();
                loadVeByHoaDon(maHoaDon);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn hóa đơn hoặc nhập thông tin vé để tìm kiếm!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            }
            return;
        }
        
        System.out.println("🔍 Tìm kiếm vé với: mã=" + maVe + ", cccd=" + cccd + ", tên=" + hoTen);
        
        // Lấy danh sách vé hiện tại và lọc
        int hoaDonRow = jTable1.getSelectedRow();
        if (hoaDonRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn hóa đơn trước!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String maHoaDon = modelHoaDon.getValueAt(hoaDonRow, 0).toString();
        List<Ve> allVe = null;
        try {
            allVe = veService.findByMaHoaDon(maHoaDon);
        } catch (Exception e) {
            e.printStackTrace();
            allVe = new java.util.ArrayList<>();
        }
        
        modelVe.setRowCount(0);
        int count = 0;
        
        for (Ve ve : allVe) {
            boolean match = true;
            
            if (!maVe.isEmpty() && !ve.getMaVe().toLowerCase().contains(maVe.toLowerCase())) {
                match = false;
            }
            if (!cccd.isEmpty() && (ve.getSoCCCD() == null || !ve.getSoCCCD().toLowerCase().contains(cccd.toLowerCase()))) {
                match = false;
            }
            if (!hoTen.isEmpty() && (ve.getTenKhachHang() == null || !ve.getTenKhachHang().toLowerCase().contains(hoTen.toLowerCase()))) {
                match = false;
            }
            
            if (match) {
                modelVe.addRow(new Object[]{
                    ve.getMaVe(),
                    ve.getSoCCCD() != null ? ve.getSoCCCD() : "",
                    ve.getTenKhachHang() != null ? ve.getTenKhachHang() : "",
                    ve.getLoaiVe() != null ? ve.getLoaiVe().getTenLoaiVe() : "",
                    ve.getLichTrinh() != null && ve.getLichTrinh().getGaDi() != null ? 
                        ve.getLichTrinh().getGaDi().getTenGa() : "",
                    ve.getLichTrinh() != null && ve.getLichTrinh().getGaDen() != null ? 
                        ve.getLichTrinh().getGaDen().getTenGa() : "",
                    ve.getLichTrinh() != null && ve.getLichTrinh().getChuyenTau() != null ? 
                        ve.getLichTrinh().getChuyenTau().getSoHieuTau() : "",
                    ve.getChoNgoi() != null && ve.getChoNgoi().getToa() != null ? 
                        String.valueOf(ve.getChoNgoi().getToa().getSoToa()) : "",
                    ve.getChoNgoi() != null ? String.valueOf(ve.getChoNgoi().getViTri()) : "",
                    ve.getThoiGianLenTau() != null ? 
                        ve.getThoiGianLenTau().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "",
                    currencyFormat.format(ve.getGiaVe()),
                    0 // lichSuInVeDAO.countPrintTimes(ve.getMaVe()) // ⚡ Cập nhật cột số lần in
                });
                count++;
            }
        }
        
        System.out.println("✅ Tìm thấy " + count + " vé");
        
        if (count == 0) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy vé nào!",
                "Kết quả tìm kiếm",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnTimVeActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // ⚡ IN TẬP VÉ - In tất cả vé trong hóa đơn (không hiển thị thông báo, delay 2s giữa các vé)
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn hóa đơn cần in tập vé!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String maHoaDon = modelHoaDon.getValueAt(selectedRow, 0).toString();
        System.out.println("🖨️ In tập vé cho hóa đơn: " + maHoaDon);
        
        // Lấy danh sách vé từ database
        List<Ve> ds = null;
        try {
            ds = veService.findByMaHoaDon(maHoaDon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (ds == null || ds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Không có vé nào để in!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        final List<Ve> danhSachVe = ds;

        // ⚡ KIỂM TRA LỊCH SỬ IN ĐỂ CẢNH BÁO (GIỐNG NÚT IN HÓA ĐƠN)
        boolean hasBeenPrinted = false;
        int maxPrints = 0;
        /*
        for (Ve v : danhSachVe) {
            int count = lichSuInVeDAO.countPrintTimes(v.getMaVe());
            if (count > 0) {
                hasBeenPrinted = true;
                if (count > maxPrints) maxPrints = count;
            }
        }
        */

        if (hasBeenPrinted) {
            String msg = String.format(
                "⚠️ CẢNH BÁO: TẬP VÉ NÀY ĐÃ TỪNG ĐƯỢC IN!\n\n" +
                "• Số lượng vé: %d\n" +
                "• Số lần in tối đa: %d\n\n" +
                "Bạn có chắc chắn muốn in lại toàn bộ tập vé này không?",
                danhSachVe.size(), maxPrints
            );

            int confirm = JOptionPane.showConfirmDialog(this,
                msg,
                "Xác nhận in lại tập vé",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // In từng vé với delay 2 giây
        new Thread(() -> {
            for (int i = 0; i < danhSachVe.size(); i++) {
                Ve ve = danhSachVe.get(i);
                System.out.println("🖨️ In vé " + (i + 1) + "/" + danhSachVe.size() + ": " + ve.getMaVe());
                
                ThermalPrinter.printTicket(ve, false); // ⚡ In thủ công (in lại)

                
                // Delay 2 giây trước khi in vé tiếp theo (trừ vé cuối cùng)
                if (i < danhSachVe.size() - 1) {
                    try {
                        Thread.sleep(2000); // 2 giây
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("✅ Đã hoàn thành in " + danhSachVe.size() + " vé");
        }).start();
        
        // Không hiển thị thông báo
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnInHoaDon;
    private javax.swing.JButton btnInVe;
    private javax.swing.JButton btnTimHoaDon;
    private javax.swing.JButton btnTimVe;
    private javax.swing.JButton btnTraTapVe;
    private javax.swing.JButton btnTraVe;
    private javax.swing.JButton btnXoaTrang;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JLabel lblLichSuIn;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Tìm và refresh Dashboard nếu nó đang hiển thị
     */
    private void refreshDashboardIfVisible() {
        try {
            // Tìm Main frame chứa Dashboard
            java.awt.Container parent = this.getParent();
            while (parent != null && !(parent instanceof demo.Main)) {
                parent = parent.getParent();
            }
            
            if (parent instanceof demo.Main) {
                demo.Main mainFrame = (demo.Main) parent;
                // Tìm Dashboard trong các component
                java.awt.Component[] components = mainFrame.getContentPane().getComponents();
                for (java.awt.Component comp : components) {
                    if (comp instanceof javax.swing.JPanel) {
                        javax.swing.JPanel panel = (javax.swing.JPanel) comp;
                        // Đệ quy tìm Gui_Dashboard
                        findAndRefreshDashboard(panel);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Không thể refresh Dashboard: " + e.getMessage());
        }
    }
    
    private void findAndRefreshDashboard(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof Gui_Dashboard) {
                System.out.println("🔄 Found Dashboard, refreshing...");
                ((Gui_Dashboard) comp).refreshData();
                return;
            } else if (comp instanceof java.awt.Container) {
                findAndRefreshDashboard((java.awt.Container) comp);
            }
        }
    }
}
