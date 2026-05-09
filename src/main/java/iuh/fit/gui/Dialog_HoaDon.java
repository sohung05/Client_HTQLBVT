/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package iuh.fit.gui;
import service.*;
import iuh.fit.utils.ClientContext;
import entity.*;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author PC
 */
public class Dialog_HoaDon extends javax.swing.JDialog {

    private String maHoaDon;
    private String cccd;
    private String hoTen;
    private String sdt;
    private String email;
    private double tongTien;
    private double khuyenMai;
    private int soLuongVe;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateTimeFormat;
    private Gui_NhapThongTinBanVe previousGui;
    private entity.DonTreoDat donTreo; // Đơn treo (nếu xử lý từ đơn tạm)
    private java.util.List<entity.Ve> danhSachVeTuDB; // Danh sách vé load từ DB (cho In hóa đơn)

    /**
     * Creates new form Dialog_HoaDon2
     */
    public Dialog_HoaDon(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initCustomComponents();
    }
    
    /**
     * Constructor với dữ liệu hóa đơn (từ flow bán vé thường)
     */
    public Dialog_HoaDon(java.awt.Frame parent, boolean modal,
                         String maHoaDon, String cccd, String hoTen, String sdt, String email,
                         int soLuongVe, double tongTien, double khuyenMai,
                         Gui_NhapThongTinBanVe previousGui) {
        super(parent, modal);
        this.maHoaDon = maHoaDon;
        this.cccd = cccd;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.email = email;
        this.soLuongVe = soLuongVe;
        this.tongTien = tongTien;
        this.khuyenMai = khuyenMai;
        this.previousGui = previousGui;
        this.donTreo = null; // Không phải đơn treo
        
        initComponents();
        initCustomComponents();
        loadData();
    }
    
    /**
     * Constructor với dữ liệu hóa đơn (từ xử lý đơn tạm)
     */
    public Dialog_HoaDon(java.awt.Frame parent, boolean modal,
                         String maHoaDon, String cccd, String hoTen, String sdt, String email,
                         int soLuongVe, double tongTien, double khuyenMai,
                         entity.DonTreoDat donTreo) {
        super(parent, modal);
        this.maHoaDon = maHoaDon;
        this.cccd = cccd;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.email = email;
        this.soLuongVe = soLuongVe;
        this.tongTien = tongTien;
        this.khuyenMai = khuyenMai;
        this.previousGui = null; // Không có previousGui
        this.donTreo = donTreo; // Lưu đơn treo
        
        initComponents();
        initCustomComponents();
        loadData();
    }
    
    /**
     * Constructor để IN LẠI hóa đơn (load từ database)
     * Dùng khi bấm nút "In hóa đơn" từ Gui_TraVe
     */
    public Dialog_HoaDon(java.awt.Frame parent, boolean modal, String maHoaDonCanIn) {
        super(parent, modal);
        this.maHoaDon = maHoaDonCanIn;
        this.previousGui = null;
        this.donTreo = null;
        
        initComponents();
        initCustomComponents();
        
        // Load toàn bộ dữ liệu từ database
        loadFromDatabase(maHoaDonCanIn);
        loadData();
    }
    
    private void initCustomComponents() {
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy  HH:mm:ss");
        setLocationRelativeTo(null);
        
        // Ẩn tất cả các label thông tin vé ngay từ đầu
        anTatCaThongTinVe();
    }
    
    /**
     * Ẩn tất cả các label thông tin vé (4 rows)
     */
    private void anTatCaThongTinVe() {
        // Row 1
        lblLoaiVe_Row1.setVisible(false);
        lblGaDi_Row1.setVisible(false);
        lblGaDen_Row1.setVisible(false);
        lblSoLuong_Row1.setVisible(false);
        lblThanhTien_Row1.setVisible(false);
        
        // Row 2
        lblLoaiVe_Row2.setVisible(false);
        lblGaDi_Row2.setVisible(false);
        lblGaDen_Row2.setVisible(false);
        lblSoLuong_Row2.setVisible(false);
        lblThanhTien_Row2.setVisible(false);
        
        // Row 3
        lblLoaiVe_Row3.setVisible(false);
        lblGaDi_Row3.setVisible(false);
        lblGaDen_Row3.setVisible(false);
        lblSoLuong_Row3.setVisible(false);
        lblThanhTien_Row3.setVisible(false);
        
        // Row 4
        lblLoaiVe_Row4.setVisible(false);
        lblGaDi_Row4.setVisible(false);
        lblGaDen_Row4.setVisible(false);
        lblSoLuong_Row4.setVisible(false);
        lblThanhTien_Row4.setVisible(false);
    }
    
    /**
     * Load dữ liệu từ database (dùng cho In lại hóa đơn)
     */
    private void loadFromDatabase(String maHoaDonCanLoad) {
        try {
            IHoaDonService hoaDonService = ClientContext.getHoaDonService();
            IVeService veService = ClientContext.getVeService();
            
            // 1. Load HoaDon
            entity.HoaDon hoaDon = hoaDonService.findByMaHoaDon(maHoaDonCanLoad);
            
            if (hoaDon == null) {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "Không tìm thấy hóa đơn: " + maHoaDonCanLoad,
                    "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 2. Load thông tin khách hàng
            entity.KhachHang kh = hoaDon.getKhachHang();
            if (kh != null) {
                this.cccd = kh.getCCCD();
                this.hoTen = kh.getHoTen();
                this.sdt = kh.getSDT();
                this.email = kh.getEmail();
            }
            
            // 3. Load tổng tiền và khuyến mãi
            this.tongTien = hoaDon.getTongTien();
            this.khuyenMai = 0; // TODO: Tính từ ChiTietKhuyenMai nếu có
            
            // 4. Load danh sách vé từ ChiTietHoaDon thông qua HoaDonService
            java.util.List<entity.ChiTietHoaDon> danhSachCTHD = hoaDonService.getChiTietByMaHoaDon(maHoaDonCanLoad);
            
            // 5. Load thông tin đầy đủ của từng vé
            danhSachVeTuDB = new java.util.ArrayList<>();
            
            for (entity.ChiTietHoaDon cthd : danhSachCTHD) {
                entity.Ve ve = veService.findByMaVe(cthd.getMaVe());
                if (ve != null) {
                    danhSachVeTuDB.add(ve);
                }
            }
            
            this.soLuongVe = danhSachVeTuDB.size();
            
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                "Lỗi khi load hóa đơn từ database: " + e.getMessage(),
                "Lỗi",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadData() {
        // Load thông tin hóa đơn
        lbMaHD.setText(maHoaDon != null ? maHoaDon : "HD" + System.currentTimeMillis());
        lbNgayGio.setText(dateTimeFormat.format(new Date()));
        lbKhachHang.setText(hoTen != null && !hoTen.isEmpty() ? hoTen : "(Chưa cung cấp)");
        lbSdt.setText(sdt != null && !sdt.isEmpty() ? sdt : "(Chưa cung cấp)");
        lbEmail.setText(email != null && !email.isEmpty() ? email : "(Chưa cung cấp)");
        
        // Load tổng tiền và khuyến mãi
        lbTongTien.setText(currencyFormat.format(tongTien));
        lbKhuyenMai.setText(khuyenMai > 0 ? currencyFormat.format(khuyenMai) : "Không");
        
        // Load thông tin vé tổng hợp
        loadThongTinVeTongHop();
    }
    
    /**
     * Load thông tin vé TỔNG HỢP (nhóm theo đối tượng)
     * Hiển thị vào các label tĩnh Row1-Row4
     */
    public void loadThongTinVeTongHop() {
        // Nhóm vé theo "Đối tượng"
        java.util.Map<String, Integer> soLuongTheoDoiTuong = new java.util.LinkedHashMap<>();
        java.util.Map<String, Double> tongTienTheoDoiTuong = new java.util.HashMap<>();
        String gaDi = "";
        String gaDen = "";
        
        // ⚡ XỬ LÝ 2 TRƯỜNG HỢP
        if (donTreo != null) {
            // ==> TRƯỜNG HỢP 1: Từ đơn tạm (có donTreo)
            
            // Lấy danh sách vé từ đơn treo
            java.util.List<entity.DonTreoDat.ThongTinVeTam> danhSachVe = donTreo.getDanhSachVe();
            if (danhSachVe == null || danhSachVe.isEmpty()) {
            return;
        }
        
            // ⚡ Lấy thông tin ga từ donTreo
            gaDi = donTreo.getGaDi() != null && !donTreo.getGaDi().isEmpty() 
                   ? donTreo.getGaDi() 
                   : "Chưa xác định";
            gaDen = donTreo.getGaDen() != null && !donTreo.getGaDen().isEmpty() 
                    ? donTreo.getGaDen() 
                    : "Chưa xác định";
            
            // Nhóm vé theo đối tượng
            for (entity.DonTreoDat.ThongTinVeTam ve : danhSachVe) {
                String doiTuong = ve.getDoiTuong() != null ? ve.getDoiTuong() : "Không xác định";
                double thanhTien = ve.getThanhTien();
                
                soLuongTheoDoiTuong.put(doiTuong, soLuongTheoDoiTuong.getOrDefault(doiTuong, 0) + 1);
                tongTienTheoDoiTuong.put(doiTuong, tongTienTheoDoiTuong.getOrDefault(doiTuong, 0.0) + thanhTien);
            }
            
        } else if (previousGui != null) {
            // ==> TRƯỜNG HỢP 2: Từ flow bán vé thường (có previousGui)
            
            // Lấy table từ Gui_NhapThongTinBanVe
            javax.swing.JTable table = previousGui.getTableThongTinVe();
            if (table == null || table.getModel().getRowCount() == 0) {
                return;
            }
            
            javax.swing.table.TableModel model = table.getModel();
            
            // Lấy thông tin Ga đi, Ga đến từ LichTrinh
            try {
                entity.LichTrinh lt = previousGui.getPreviousGuiBanVe().getLichTrinhDangChon();
                if (lt != null) {
                    gaDi = lt.getGaDi() != null ? lt.getGaDi().getTenGa() : "";
                    gaDen = lt.getGaDen() != null ? lt.getGaDen().getTenGa() : "";
                }
            } catch (Exception e) {
                // Không lấy được thông tin ga
            }
            
            // Nhóm vé theo đối tượng
            for (int i = 0; i < model.getRowCount(); i++) {
                String doiTuong = model.getValueAt(i, 2) != null ? model.getValueAt(i, 2).toString() : "Không xác định";
                String thanhTienStr = model.getValueAt(i, 6) != null ? model.getValueAt(i, 6).toString() : "0";
                
                // Parse thành tiền (loại bỏ tất cả ký tự không phải số)
                double thanhTien = 0;
                try {
                    thanhTien = Double.parseDouble(thanhTienStr.replaceAll("[^0-9]", ""));
                } catch (Exception e) {
                    thanhTien = 0;
                }
                
                // Cộng dồn
                soLuongTheoDoiTuong.put(doiTuong, soLuongTheoDoiTuong.getOrDefault(doiTuong, 0) + 1);
                tongTienTheoDoiTuong.put(doiTuong, tongTienTheoDoiTuong.getOrDefault(doiTuong, 0.0) + thanhTien);
            }
            
        } else if (danhSachVeTuDB != null && !danhSachVeTuDB.isEmpty()) {
            // ==> TRƯỜNG HỢP 3: In lại hóa đơn (load từ database)
            
            // Lấy thông tin Ga đi, Ga đến từ vé đầu tiên
            entity.Ve veFirst = danhSachVeTuDB.get(0);
            if (veFirst.getLichTrinh() != null) {
                gaDi = veFirst.getLichTrinh().getGaDi() != null 
                       ? veFirst.getLichTrinh().getGaDi().getTenGa() 
                       : "Chưa xác định";
                gaDen = veFirst.getLichTrinh().getGaDen() != null 
                        ? veFirst.getLichTrinh().getGaDen().getTenGa() 
                        : "Chưa xác định";
            }
            
            // Nhóm vé theo đối tượng
            for (entity.Ve ve : danhSachVeTuDB) {
                String doiTuong = ve.getLoaiVe() != null && ve.getLoaiVe().getTenLoaiVe() != null
                                  ? ve.getLoaiVe().getTenLoaiVe()
                                  : "Không xác định";
                double thanhTien = ve.getGiaVe();
                
                // Cộng dồn
                soLuongTheoDoiTuong.put(doiTuong, soLuongTheoDoiTuong.getOrDefault(doiTuong, 0) + 1);
                tongTienTheoDoiTuong.put(doiTuong, tongTienTheoDoiTuong.getOrDefault(doiTuong, 0.0) + thanhTien);
            }
            
        } else {
            return;
        }
        
        // Ẩn tất cả trước khi load dữ liệu mới
        anTatCaThongTinVe();
        
        // Hiển thị lên các label (tối đa 4 rows)
        javax.swing.JLabel[][] rowLabels = {
            {lblLoaiVe_Row1, lblGaDi_Row1, lblGaDen_Row1, lblSoLuong_Row1, lblThanhTien_Row1},
            {lblLoaiVe_Row2, lblGaDi_Row2, lblGaDen_Row2, lblSoLuong_Row2, lblThanhTien_Row2},
            {lblLoaiVe_Row3, lblGaDi_Row3, lblGaDen_Row3, lblSoLuong_Row3, lblThanhTien_Row3},
            {lblLoaiVe_Row4, lblGaDi_Row4, lblGaDen_Row4, lblSoLuong_Row4, lblThanhTien_Row4}
        };
        
        // Hiển thị dữ liệu
        int rowIndex = 0;
        for (String doiTuong : soLuongTheoDoiTuong.keySet()) {
            if (rowIndex >= 4) break; // Chỉ hiển thị tối đa 4 dòng
            
            int soLuong = soLuongTheoDoiTuong.get(doiTuong);
            double tongTien = tongTienTheoDoiTuong.get(doiTuong);
            
            rowLabels[rowIndex][0].setText(doiTuong);
            rowLabels[rowIndex][1].setText(gaDi);
            rowLabels[rowIndex][2].setText(gaDen);
            rowLabels[rowIndex][3].setText(String.valueOf(soLuong));
            rowLabels[rowIndex][4].setText(currencyFormat.format(tongTien) + " VNĐ");
            
            // Hiển thị row
            for (javax.swing.JLabel label : rowLabels[rowIndex]) {
                label.setVisible(true);
            }
            
            rowIndex++;
        }
        
        // Revalidate layout để không bị khoảng trống
        this.revalidate();
        this.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbTitle1 = new javax.swing.JLabel();
        lbTitle2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lbMaHD = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lbNgayGio = new javax.swing.JLabel();
        lbKhachHang = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        lbSdt = new javax.swing.JLabel();
        lbEmail = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lblLoaiVe_Row1 = new javax.swing.JLabel();
        lblGaDi_Row1 = new javax.swing.JLabel();
        lblGaDen_Row1 = new javax.swing.JLabel();
        lblSoLuong_Row1 = new javax.swing.JLabel();
        lblThanhTien_Row1 = new javax.swing.JLabel();
        lblLoaiVe_Row2 = new javax.swing.JLabel();
        lblGaDi_Row2 = new javax.swing.JLabel();
        lblGaDen_Row2 = new javax.swing.JLabel();
        lblSoLuong_Row2 = new javax.swing.JLabel();
        lblThanhTien_Row2 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        scrollPaneVe = new javax.swing.JScrollPane();
        tblThongTinVe = new javax.swing.JTable();
        lbKhuyenMai = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        lbTongTien = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        lblLoaiVe_Row3 = new javax.swing.JLabel();
        lblGaDi_Row3 = new javax.swing.JLabel();
        lblGaDen_Row3 = new javax.swing.JLabel();
        lblSoLuong_Row3 = new javax.swing.JLabel();
        lblLoaiVe_Row4 = new javax.swing.JLabel();
        lblGaDi_Row4 = new javax.swing.JLabel();
        lblGaDen_Row4 = new javax.swing.JLabel();
        lblSoLuong_Row4 = new javax.swing.JLabel();
        lblThanhTien_Row4 = new javax.swing.JLabel();
        lblThanhTien_Row3 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lbTitle1.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lbTitle1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbTitle1.setText("CÔNG TY CỔ PHẦN VẬN TẢI");

        lbTitle2.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lbTitle2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbTitle2.setText("ĐƯỜNG SẮT SÀI GÒN");

        jLabel3.setFont(new java.awt.Font("SansSerif", 0, 17)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("64 Cộng Hòa, Q. Tân Bình, TP Hồ Chí Minh");

        jLabel4.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("www.dailybanvetauahktv.com");

        jLabel5.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Hostline: 18009898");

        jLabel11.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("HÓA ĐƠN BÁN HÀNG");

        lbMaHD.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lbMaHD.setText("HD981729837121321");

        jLabel7.setFont(new java.awt.Font("SansSerif", 0, 16)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setText("Mã hóa đơn:  ");

        jLabel6.setFont(new java.awt.Font("SansSerif", 0, 16)); // NOI18N
        jLabel6.setText("Ngày:");

        lbNgayGio.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lbNgayGio.setText("23/04/2024  08:45:30");

        lbKhachHang.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lbKhachHang.setText("Trương Trần Hùng");

        jLabel8.setFont(new java.awt.Font("SansSerif", 0, 16)); // NOI18N
        jLabel8.setText("Khách hàng: ");

        jLabel9.setFont(new java.awt.Font("SansSerif", 0, 16)); // NOI18N
        jLabel9.setText("Số điện thoại: ");

        lbSdt.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lbSdt.setText("093874849983");

        lbEmail.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lbEmail.setText("hung@gmail.com");

        jLabel10.setFont(new java.awt.Font("SansSerif", 0, 16)); // NOI18N
        jLabel10.setText("Email:");

        jLabel15.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel15.setText("Thông tin vé");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("--------------------------------------------------------------------------------------------------");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("--------------------------------------------------------------------------------------------------");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Loại Vé");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("Ga đi");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel12.setText("Ga đến");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel13.setText("Số lượng");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel14.setText("Thành tiền");

        jPanel2.setOpaque(false);

        lblLoaiVe_Row1.setText("Người Lớn");

        lblGaDi_Row1.setText("Sài Gòn");

        lblGaDen_Row1.setText("Sài Gòn");

        lblSoLuong_Row1.setText("1");

        lblThanhTien_Row1.setText("1000000000");

        lblLoaiVe_Row2.setText("Trẻ em");

        lblGaDi_Row2.setText("Sài Gòn");

        lblGaDen_Row2.setText("Sài Gòn");

        lblSoLuong_Row2.setText("1");

        lblThanhTien_Row2.setText("100000000");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLoaiVe_Row1, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                    .addComponent(lblLoaiVe_Row2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(44, 44, 44)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblGaDi_Row1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblGaDi_Row2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(74, 74, 74)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblGaDen_Row1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblGaDen_Row2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(88, 88, 88)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSoLuong_Row1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoLuong_Row2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblThanhTien_Row2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblThanhTien_Row1, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))
                .addGap(29, 29, 29))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLoaiVe_Row1)
                    .addComponent(lblGaDi_Row1)
                    .addComponent(lblGaDen_Row1)
                    .addComponent(lblSoLuong_Row1)
                    .addComponent(lblThanhTien_Row1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLoaiVe_Row2)
                    .addComponent(lblGaDi_Row2)
                    .addComponent(lblGaDen_Row2)
                    .addComponent(lblSoLuong_Row2)
                    .addComponent(lblThanhTien_Row2))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jLabel19.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        jLabel19.setText("Thông tin liên hệ: Công ty cổ phần đường sắt Sài Gòn - Số 01 Nguyễn Thông, Phường Nhiêu ");

        jLabel20.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        jLabel20.setText(" Lộc, Quận 3, thành phố Hồ Chí Minh. Hotline: 18009898.");

        jLabel21.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        jLabel21.setText("Khách hàng có thể đổi-trả trước thời gian tàu khởi hành 1 ngày. ");

        jLabel24.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        jLabel24.setText("Lưu ý: ");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("--------------------------------------------------------------------------------------------------");

        scrollPaneVe.setBorder(javax.swing.BorderFactory.createTitledBorder("Chi tiết vé"));

        tblThongTinVe.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Số giấy tờ", "Họ tên", "Đối tượng", "Thông tin chỗ", "Giá vé", "Giảm giá", "Thành tiền"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrollPaneVe.setViewportView(tblThongTinVe);

        lbKhuyenMai.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lbKhuyenMai.setText("Không");

        jLabel18.setFont(new java.awt.Font("SansSerif", 0, 16)); // NOI18N
        jLabel18.setText("Khuyến mãi: ");

        jLabel16.setFont(new java.awt.Font("SansSerif", 0, 16)); // NOI18N
        jLabel16.setText("Tổng tiền:");

        lbTongTien.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lbTongTien.setText("300.000");

        jLabel17.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel17.setText("VNĐ");

        lblLoaiVe_Row3.setText("Sinh viên");

        lblGaDi_Row3.setText("Sài Gòn");

        lblGaDen_Row3.setText("Sài Gòn");

        lblSoLuong_Row3.setText("1");

        lblLoaiVe_Row4.setText("Người cao tuổi");

        lblGaDi_Row4.setText("Sài Gòn");

        lblGaDen_Row4.setText("Sài Gòn");

        lblSoLuong_Row4.setText("1");

        lblThanhTien_Row4.setText("1000000000");

        lblThanhTien_Row3.setText("100000000");

        jLabel47.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel47.setText("--------------------------------------------------------------------------------------------------");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(58, 58, 58)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel12)
                                .addGap(68, 68, 68)
                        .addComponent(jLabel13)
                                .addGap(51, 51, 51)
                        .addComponent(jLabel14)
                                .addGap(31, 31, 31))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(lbTitle2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel18)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lbKhuyenMai)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lbTongTien)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel17)
                                        .addGap(11, 11, 11))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lblLoaiVe_Row4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lblLoaiVe_Row3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(41, 41, 41)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblGaDi_Row3, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblGaDi_Row4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(74, 74, 74)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblGaDen_Row4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblGaDen_Row3, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(93, 93, 93)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblSoLuong_Row4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblSoLuong_Row3, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lblThanhTien_Row3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lblThanhTien_Row4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(30, 30, 30))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel20)
                                    .addComponent(jLabel19))
                                .addGap(11, 11, 11))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel23)
                                    .addComponent(jLabel22)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(17, 17, 17)
                                        .addComponent(jLabel24)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel21)))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(164, 164, 164)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel10))
                        .addGap(48, 48, 48)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbKhachHang)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(lbNgayGio)
                                .addComponent(lbMaHD))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lbEmail)
                                    .addComponent(lbSdt))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel25)))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                        .addComponent(lbTitle1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel47)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(lbTitle2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(lbMaHD))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(lbNgayGio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(lbKhachHang))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(lbSdt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(lbEmail))
                .addGap(22, 22, 22)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                        .addComponent(jLabel13)
                        .addComponent(jLabel14))
                .addGap(2, 2, 2)
                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGaDi_Row3)
                    .addComponent(lblGaDen_Row3)
                    .addComponent(lblSoLuong_Row3)
                    .addComponent(lblLoaiVe_Row3)
                    .addComponent(lblThanhTien_Row3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblGaDi_Row4)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblSoLuong_Row4)
                        .addComponent(lblThanhTien_Row4)
                        .addComponent(lblLoaiVe_Row4)
                        .addComponent(lblGaDen_Row4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 21, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbKhuyenMai)
                    .addComponent(jLabel18)
                    .addComponent(jLabel16)
                    .addComponent(lbTongTien)
                    .addComponent(jLabel17))
                .addGap(18, 18, 18)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jLabel24))
                .addGap(20, 20, 20))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lbTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(638, Short.MAX_VALUE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(656, Short.MAX_VALUE)
                    .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Dialog_HoaDon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dialog_HoaDon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dialog_HoaDon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dialog_HoaDon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Dialog_HoaDon dialog = new Dialog_HoaDon(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lbEmail;
    private javax.swing.JLabel lbKhachHang;
    private javax.swing.JLabel lbKhuyenMai;
    private javax.swing.JLabel lbMaHD;
    private javax.swing.JLabel lbNgayGio;
    private javax.swing.JLabel lbSdt;
    private javax.swing.JLabel lbTitle1;
    private javax.swing.JLabel lbTitle2;
    private javax.swing.JLabel lbTongTien;
    private javax.swing.JLabel lblGaDen_Row1;
    private javax.swing.JLabel lblGaDen_Row2;
    private javax.swing.JLabel lblGaDen_Row3;
    private javax.swing.JLabel lblGaDen_Row4;
    private javax.swing.JLabel lblGaDi_Row1;
    private javax.swing.JLabel lblGaDi_Row2;
    private javax.swing.JLabel lblGaDi_Row3;
    private javax.swing.JLabel lblGaDi_Row4;
    private javax.swing.JLabel lblLoaiVe_Row1;
    private javax.swing.JLabel lblLoaiVe_Row2;
    private javax.swing.JLabel lblLoaiVe_Row3;
    private javax.swing.JLabel lblLoaiVe_Row4;
    private javax.swing.JLabel lblSoLuong_Row1;
    private javax.swing.JLabel lblSoLuong_Row2;
    private javax.swing.JLabel lblSoLuong_Row3;
    private javax.swing.JLabel lblSoLuong_Row4;
    private javax.swing.JLabel lblThanhTien_Row1;
    private javax.swing.JLabel lblThanhTien_Row2;
    private javax.swing.JLabel lblThanhTien_Row3;
    private javax.swing.JLabel lblThanhTien_Row4;
    private javax.swing.JScrollPane scrollPaneVe;
    private javax.swing.JTable tblThongTinVe;
    // End of variables declaration//GEN-END:variables
}
