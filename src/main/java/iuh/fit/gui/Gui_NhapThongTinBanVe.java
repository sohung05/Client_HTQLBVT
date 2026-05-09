package iuh.fit.gui;

import iuh.fit.utils.ClientContext;
import iuh.fit.utils.SessionManager;
import entity.ChoNgoi;
import entity.DoiTuong;
import entity.LichTrinh;
import entity.LoaiVe;
import entity.Ga;
import service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author PC
 */
public class Gui_NhapThongTinBanVe extends JPanel {

    private Gui_BanVe previousGuiBanVe; // Lưu instance cũ để quay lại
    private DefaultTableModel modelThongTinVe;
    private ILoaiVeService loaiVeService;
    private IKhuyenMaiService khuyenMaiService;
    private IKhachHangService khachHangService;
    private IHoaDonService hoaDonService;
    private IGaService gaService;
    private ILichTrinhService lichTrinhService;
    private boolean isUpdatingTable = false; // Cờ chặn vòng lặp sự kiện
    private List<LoaiVe> danhSachLoaiVe;
    private Map<String, LoaiVe> mapLoaiVe; // Map tên loại vé -> LoaiVe
    private NumberFormat currencyFormat;
    
    // Lưu Map vé để dùng khi thanh toán (cả chiều đi + chiều về)
    private List<ChoNgoi> danhSachChoNgoi; // Danh sách ghế theo thứ tự trong table
    private List<LichTrinh> danhSachLichTrinh; // Danh sách lịch trình tương ứng
    private List<entity.Ga> danhSachGaDi;
    private List<entity.Ga> danhSachGaDen;
    
    /**
     * Creates new form Gui_NhapThongTinBanVe
     */
    public Gui_NhapThongTinBanVe() {
        initComponents();
        initCustomComponents();
    }
    
    /**
     * Constructor với Gui_BanVe trước đó
     */
    public Gui_NhapThongTinBanVe(Gui_BanVe previousGui) {
        this.previousGuiBanVe = previousGui;
        initComponents();
        initCustomComponents();
        loadDataFromPreviousGui();
    }
    
    /**
     * Khởi tạo các component custom
     */
    private void initCustomComponents() {
        // Khởi tạo DAO và format
        loaiVeService = ClientContext.getLoaiVeService();
        khuyenMaiService = ClientContext.getKhuyenMaiService();
        khachHangService = ClientContext.getKhachHangService();
        hoaDonService = ClientContext.getHoaDonService();
        gaService = ClientContext.getGaService();
        lichTrinhService = ClientContext.getLichTrinhService();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        // Load danh sách loại vé từ DB
        try {
            danhSachLoaiVe = loaiVeService.findAll();
            mapLoaiVe = new HashMap<>();
            for (LoaiVe lv : danhSachLoaiVe) {
                mapLoaiVe.put(lv.getTenLoaiVe(), lv);
            }
        } catch (Exception e) {
            e.printStackTrace();
            danhSachLoaiVe = new java.util.ArrayList<>();
            mapLoaiVe = new HashMap<>();
        }
        
        // Tạo custom table model với isCellEditable override
        setupSingleClickEdit();
        
        // Set chiều cao dòng để hiển thị nội dung nhiều dòng (HTML)
        tblThongTinVe.setRowHeight(80);
        
        // Setup ComboBox cho cột "Đối Tượng" (cột index 2)
        setupComboBoxColumn();
        
        // Thêm listener cho table để xử lý nhiều sự kiện (Đối tượng, CCCD)
        modelThongTinVe.addTableModelListener(e -> {
            if (isUpdatingTable) return;
            
            int row = e.getFirstRow();
            int column = e.getColumn();
            
            if (column == 2) { // 🔹 Cột "Đối Tượng"
                String doiTuong = (String) modelThongTinVe.getValueAt(row, 2);
                if (doiTuong != null && doiTuong.equals("Trẻ em")) {
                    SwingUtilities.invokeLater(() -> hienDialogNhapNgaySinh(row, doiTuong));
                } else if (doiTuong != null && !doiTuong.isEmpty()) {
                    tinhLaiGiaVe(row);
                    capNhatTongTien();
                }
                
                // Nếu là dòng đầu tiên, đồng bộ xuống thông tin người đặt (nếu cần)
                if (row == 0) capNhatThongTinNguoiDatTuDong();
                
            } else if (column == 0) { // 🔹 Cột "Số giấy tờ / CCCD"
                String cccd = (String) modelThongTinVe.getValueAt(row, 0);
                if (cccd != null && (cccd.length() == 9 || cccd.length() == 12)) {
                    xuLyTraCuuKhachHang(row, cccd);
                }
                
                // Nếu là dòng đầu tiên, đồng bộ xuống thông tin người đặt
                if (row == 0) capNhatThongTinNguoiDatTuDong();
            } else if (column == 1) { // 🔹 Cột "Họ Tên"
                if (row == 0) capNhatThongTinNguoiDatTuDong();
            }
        });
        
        // Thêm listener cho nút "Xóa"
        btnXoa.addActionListener(evt -> btnXoaActionPerformed(evt));
    }
    
    /**
     * Tra cứu khách hàng từ CCCD và tự động điền thông tin
     */
    private void xuLyTraCuuKhachHang(int row, String cccd) {
        new Thread(() -> {
            try {
                entity.KhachHang kh = khachHangService.findByCCCD(cccd);
            if (kh != null) {
                SwingUtilities.invokeLater(() -> {
                    isUpdatingTable = true;
                    // Điền họ tên vào bảng
                    modelThongTinVe.setValueAt(kh.getHoTen(), row, 1);
                    
                    // Điền đối tượng nếu chưa có
                    if (modelThongTinVe.getValueAt(row, 2).toString().isEmpty()) {
                        String dtDisplay = switch (kh.getDoiTuong()) {
                            case "TreEm" -> "Trẻ em";
                            case "NguoiLon" -> "Người lớn";
                            case "NguoiCaoTuoi" -> "Người cao tuổi";
                            case "SinhVien" -> "Sinh viên";
                            default -> kh.getDoiTuong();
                        };
                        modelThongTinVe.setValueAt(dtDisplay, row, 2);
                    }
                    
                    // Nếu là dòng đầu tiên, điền xuống thông tin người đặt vé
                    if (row == 0) {
                        txtCCCD.setText(kh.getCCCD());
                        txtHoTen.setText(kh.getHoTen());
                        txtSDT.setText(kh.getSDT());
                        txtEmail.setText(kh.getEmail());
                    }
                    
                    isUpdatingTable = false;
                    tinhLaiGiaVe(row);
                    capNhatTongTien();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
}

    /**
     * Tự động điền thông tin người đặt vé từ vé đầu tiên (row 0)
     */
    private void capNhatThongTinNguoiDatTuDong() {
        if (modelThongTinVe.getRowCount() > 0) {
            String soGiayTo = modelThongTinVe.getValueAt(0, 0) != null ? modelThongTinVe.getValueAt(0, 0).toString() : "";
            String hoTen = modelThongTinVe.getValueAt(0, 1) != null ? modelThongTinVe.getValueAt(0, 1).toString() : "";
            
            // Chỉ điền nếu CCCD hoặc Họ tên ở dưới đang trống để tránh ghi đè khi user tự nhập
            if (txtCCCD.getText().trim().isEmpty() || txtCCCD.getText().equals(soGiayTo)) {
                txtCCCD.setText(soGiayTo);
            }
            if (txtHoTen.getText().trim().isEmpty() || txtHoTen.getText().equals(hoTen)) {
                txtHoTen.setText(hoTen);
            }
        }
    }
    
    /**
     * Xóa 1 vé được chọn
     */
    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = tblThongTinVe.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn vé cần xóa!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa vé này?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Xóa trong table này
            modelThongTinVe.removeRow(selectedRow);
            capNhatTongTien();
            
            // ĐỒ' BỘ với Gui_BanVe
            if (previousGuiBanVe != null) {
                previousGuiBanVe.xoaGheDaChon(selectedRow);
            }
        }
    }
    
    /**
     * Cho phép SINGLE CLICK để edit các cột văn bản
     */
    private void setupSingleClickEdit() {
        // Override isCellEditable để cho phép edit cột 0 và 1 dễ dàng hơn
        // Cột 0: Số giấy tờ
        // Cột 1: Họ Tên
        // Các cột khác giữ nguyên
        
        // Không cần custom editor, chỉ cần override model
        DefaultTableModel customModel = new DefaultTableModel(
            new Object[][] {},
            new String[] {"Số giấy tờ", "Họ Tên", "Đối Tượng", "Thông Tin Chỗ", "Giá Vé", "Giảm Giá", "Thành Tiền"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Chỉ cho edit cột 0 (Số giấy tờ) và cột 1 (Họ Tên)
                return column == 0 || column == 1 || column == 2; // Thêm cột 2 (Đối Tượng) vì có ComboBox
            }
        };
        
        tblThongTinVe.setModel(customModel);
        modelThongTinVe = customModel;
    }
    
    /**
     * Setup ComboBox cho cột "Đối Tượng"
     */
    private void setupComboBoxColumn() {
        TableColumn doiTuongColumn = tblThongTinVe.getColumnModel().getColumn(2); // Cột "Đối Tượng"
        
        // Tạo ComboBox với 4 loại chính (không hiển thị "Trẻ em dưới 6 tuổi")
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.addItem(""); // Mặc định trống
        
        for (LoaiVe lv : danhSachLoaiVe) {
            // Chỉ hiển thị 4 loại chính, ẩn "Trẻ em dưới 6 tuổi"
            String tenLoaiVe = lv.getTenLoaiVe();
            if (tenLoaiVe != null && !tenLoaiVe.trim().equals("Trẻ em dưới 6 tuổi")) {
                comboBox.addItem(tenLoaiVe);
            }
        }
        
        // Set editor cho cột với SINGLE CLICK
        DefaultCellEditor editor = new DefaultCellEditor(comboBox);
        editor.setClickCountToStart(1); // Chỉ cần 1 click để mở ComboBox
        doiTuongColumn.setCellEditor(editor);
    }
    
    /**
     * Load dữ liệu từ Gui_BanVe trước đó
     */
    private void loadDataFromPreviousGui() {
        if (previousGuiBanVe == null) {
            return;
        }
        
        // Lấy TẤT CẢ vé trong giỏ (cả chiều đi + chiều về)
        Map<ChoNgoi, LichTrinh> allVe = previousGuiBanVe.getAllVeTrongGioVe();
        
        if (allVe == null || allVe.isEmpty()) {
            return;
        }
        
        // Khởi tạo danh sách lưu trữ
        danhSachChoNgoi = new java.util.ArrayList<>();
        danhSachLichTrinh = new java.util.ArrayList<>();
        danhSachGaDi = new java.util.ArrayList<>();
        danhSachGaDen = new java.util.ArrayList<>();
        
        Map<ChoNgoi, entity.Ga> mapGheGaDi = previousGuiBanVe.getMapGheGaDi();
        Map<ChoNgoi, entity.Ga> mapGheGaDen = previousGuiBanVe.getMapGheGaDen();
        
        // Clear table trước
        modelThongTinVe.setRowCount(0);
        
        // Thêm từng vé vào table
        for (Map.Entry<ChoNgoi, LichTrinh> entry : allVe.entrySet()) {
            ChoNgoi cho = entry.getKey();
            LichTrinh lichTrinh = entry.getValue();
            
            // Lưu vào danh sách (theo thứ tự)
            danhSachChoNgoi.add(cho);
            danhSachLichTrinh.add(lichTrinh);
            danhSachGaDi.add(mapGheGaDi.get(cho));
            danhSachGaDen.add(mapGheGaDen.get(cho));
            // Format thông tin chỗ chi tiết
            String soHieuTau = lichTrinh.getChuyenTau() != null ? lichTrinh.getChuyenTau().getSoHieuTau() : "N/A";
            String gaDi = lichTrinh.getGaDi() != null ? lichTrinh.getGaDi().getTenGa() : "";
            String gaDen = lichTrinh.getGaDen() != null ? lichTrinh.getGaDen().getTenGa() : "";
            String thoiGianKH = lichTrinh.getGioKhoiHanh() != null 
                ? lichTrinh.getGioKhoiHanh().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
                : "";
            String toaCho = "Toa " + cho.getToa().getSoToa() + " - Chỗ " + cho.getViTri();
            String moTa = cho.getMoTa() != null ? cho.getMoTa() : "";
            
            // Kết hợp thành 1 string với xuống dòng (dùng HTML)
            String thongTinCho = "<html>" +
                soHieuTau + " | " + gaDi + " - " + gaDen + "<br/>" +
                "Thời gian: " + thoiGianKH + "<br/>" +
                toaCho + "<br/>" +
                moTa +
                "</html>";
            
            double giaVe = cho.getGia();
            
            modelThongTinVe.addRow(new Object[]{
                "", // Số giấy tờ - Để trống
                "", // Họ Tên - Để trống
                "", // Đối Tượng - MẶC ĐỊNH TRỐNG (user phải chọn)
                thongTinCho, // Thông Tin Chỗ (HTML format)
                currencyFormat.format(giaVe), // Giá Vé
                "", // Giảm Giá - Chưa chọn đối tượng nên để trống
                currencyFormat.format(giaVe) // Thành Tiền - Ban đầu = Giá vé gốc
            });
        }
        
        // Cập nhật tổng tiền
        capNhatTongTien();
    }
    
    /**
     * Tính lại giá vé cho 1 dòng khi thay đổi đối tượng
     */
    private void tinhLaiGiaVe(int row) {
        if (row < 0 || row >= modelThongTinVe.getRowCount()) {
            return;
        }
        
        try {
            // Lấy giá vé gốc (parse từ string đã format)
            String giaVeStr = ((String) modelThongTinVe.getValueAt(row, 4)).replaceAll("[^0-9]", "");
            double giaVe = Double.parseDouble(giaVeStr);
            
            // Lấy loại vé đã chọn
            String tenLoaiVe = (String) modelThongTinVe.getValueAt(row, 2);
            LoaiVe loaiVe = mapLoaiVe.get(tenLoaiVe);
            
            double mucGiamGia = 0; // Phần trăm giảm giá
            
            // Chuyển đổi tên loại vé sang enum DoiTuong
            DoiTuong doiTuong = DoiTuong.fromString(tenLoaiVe);
            
            // Ưu tiên lấy chiết khấu từ khuyến mãi đối tượng đang có hiệu lực
            double chietKhauKhuyenMai = -1;
            try {
                chietKhauKhuyenMai = khuyenMaiService.getChietKhauHieuLucTheoDoiTuong(doiTuong.name());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Xử lý đặc biệt cho "Trẻ em"
            if ("Trẻ em".equals(tenLoaiVe)) {
                // Kiểm tra cột "Số giấy tờ" có chứa ngày sinh không
                String soGiayTo = (String) modelThongTinVe.getValueAt(row, 0);
                
                if (soGiayTo != null && soGiayTo.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    // Có ngày sinh → Tính tuổi
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                        java.util.Date ngaySinh = sdf.parse(soGiayTo);
                        
                        java.time.LocalDate birthDate = ngaySinh.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                        java.time.LocalDate currentDate = java.time.LocalDate.now();
                        
                        int tuoi = java.time.Period.between(birthDate, currentDate).getYears();
                        
                        // Áp dụng giảm giá theo tuổi
                        // Lưu ý: Trẻ em dưới 6 tuổi sẽ không có trong bảng (đã bị xóa)
                        // Nên chỉ còn trường hợp 6-10 tuổi
                        if (tuoi >= 6 && tuoi <= 10) {
                            // Ưu tiên lấy từ khuyến mãi, nếu không có thì lấy từ LoaiVe
                            if (chietKhauKhuyenMai >= 0) {
                                mucGiamGia = chietKhauKhuyenMai;
                            } else {
                                mucGiamGia = loaiVe != null ? loaiVe.getMucGiamGia() : 0.25;
                            }
                        } else {
                            // Fallback (không nên xảy ra)
                            if (chietKhauKhuyenMai >= 0) {
                                mucGiamGia = chietKhauKhuyenMai;
                            } else {
                                mucGiamGia = loaiVe != null ? loaiVe.getMucGiamGia() : 0.25;
                            }
                        }
                    } catch (Exception ex) {
                        // Không parse được ngày sinh → Dùng mức giảm mặc định
                        if (chietKhauKhuyenMai >= 0) {
                            mucGiamGia = chietKhauKhuyenMai;
                        } else {
                            mucGiamGia = loaiVe != null ? loaiVe.getMucGiamGia() : 0.25;
                        }
                    }
                } else {
                    // Không có ngày sinh → Dùng mức giảm mặc định
                    if (chietKhauKhuyenMai >= 0) {
                        mucGiamGia = chietKhauKhuyenMai;
                    } else {
                        mucGiamGia = loaiVe != null ? loaiVe.getMucGiamGia() : 0.25;
                    }
                }
            } else {
                // Các loại vé khác: Người lớn, Sinh viên, Người cao tuổi
                // Ưu tiên lấy từ khuyến mãi đối tượng
                if (chietKhauKhuyenMai >= 0) {
                    mucGiamGia = chietKhauKhuyenMai;
                    System.out.println("✅ Áp dụng khuyến mãi cho " + tenLoaiVe + ": " + (chietKhauKhuyenMai * 100) + "%");
                } else {
                    // Không có khuyến mãi → Dùng mức giảm từ LoaiVe
                    mucGiamGia = loaiVe != null ? loaiVe.getMucGiamGia() : 0;
                    System.out.println("⚠️ Không có khuyến mãi, dùng giảm giá cơ bản cho " + tenLoaiVe + ": " + (mucGiamGia * 100) + "%");
                }
            }
            
            // Tính giảm giá và thành tiền
            // mucGiamGia đã ở dạng thập phân (0.10 = 10%, 0.25 = 25%)
            double giamGia = giaVe * mucGiamGia;
            double thanhTien = giaVe - giamGia;
            
            // Cập nhật vào table
            modelThongTinVe.setValueAt(currencyFormat.format(giamGia), row, 5);
            modelThongTinVe.setValueAt(currencyFormat.format(thanhTien), row, 6);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Cập nhật tổng tiền
     */
    private void capNhatTongTien() {
        double tongTien = 0;
        
        for (int i = 0; i < modelThongTinVe.getRowCount(); i++) {
            try {
                String thanhTienStr = ((String) modelThongTinVe.getValueAt(i, 6)).replaceAll("[^0-9]", "");
                tongTien += Double.parseDouble(thanhTienStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        lblTongTienValue.setText(currencyFormat.format(tongTien));
    }
    
    /**
     * Hiển thị dialog nhập ngày sinh cho trẻ em
     */
    private void hienDialogNhapNgaySinh(int row, String doiTuongDaChon) {
        // Sử dụng Dialog_DoiTuongTreEm đã tạo sẵn
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        Dialog_DoiTuongTreEm dialog = new Dialog_DoiTuongTreEm(parentFrame, true);
        
        // Hiển thị dialog (modal - chờ user đóng)
        dialog.setVisible(true);
        
        // Sau khi dialog đóng, xử lý kết quả
        String doiTuong = dialog.getDoiTuongResult();
        String ngaySinh = dialog.getNgaySinhResult();
        
        if (!dialog.isConfirmed() && "".equals(doiTuong)) {
            // Trẻ em dưới 6 tuổi → GIỮ vé nhưng để trống thông tin
            modelThongTinVe.setValueAt("", row, 0); // Số giấy tờ = trống
            modelThongTinVe.setValueAt("", row, 1); // Họ tên = trống
            modelThongTinVe.setValueAt("", row, 2); // Đối tượng = trống
            
            // Không cần tính giá vì đối tượng trống
            // Giữ nguyên giá vé gốc, giảm giá = 0, thành tiền = giá vé
            
        } else if (dialog.isConfirmed()) {
            // User đã xác nhận → Cập nhật thông tin
            
            // Ghi ngày sinh vào cột "Số giấy tờ" (cột 0)
            if (ngaySinh != null) {
                modelThongTinVe.setValueAt(ngaySinh, row, 0);
            }
            
            // Cập nhật đối tượng (cột 2)
            if (doiTuong != null) {
                modelThongTinVe.setValueAt(doiTuong, row, 2);
            }
            
            // Tính lại giá vé và tổng tiền
                    tinhLaiGiaVe(row);
                    capNhatTongTien();
            
        } else {
            // User hủy bỏ HOẶC tuổi >= 10
            
            if ("Người lớn".equals(doiTuong)) {
                // Trường hợp tuổi >= 10 → Cập nhật về "Người lớn" và XÓA ngày sinh
                modelThongTinVe.setValueAt("", row, 0); // Xóa "Số giấy tờ" (để trống)
                modelThongTinVe.setValueAt("Người lớn", row, 2); // Đổi đối tượng
                    tinhLaiGiaVe(row);
                    capNhatTongTien();
                } else {
                // User hủy → Reset về trống
                modelThongTinVe.setValueAt("", row, 2);
            }
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

        lblTitle = new javax.swing.JLabel();
        btnQuayLai = new javax.swing.JButton();
        pnlThongTinVe = new javax.swing.JPanel();
        scrThongTinVe = new javax.swing.JScrollPane();
        tblThongTinVe = new javax.swing.JTable();
        btnXoaTatCa = new javax.swing.JButton();
        lblTongTien = new javax.swing.JLabel();
        btnXoa = new javax.swing.JButton();
        lblTongTienValue = new javax.swing.JLabel();
        pnlNguoiDatVe = new javax.swing.JPanel();
        lblCCCD = new javax.swing.JLabel();
        lblSDT = new javax.swing.JLabel();
        lblHoTen = new javax.swing.JLabel();
        lblEmail = new javax.swing.JLabel();
        txtCCCD = new javax.swing.JTextField();
        txtSDT = new javax.swing.JTextField();
        txtHoTen = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        btnTiepTucThanhToan = new javax.swing.JButton();

        setBackground(new java.awt.Color(234, 243, 251));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("Chi Tiết Giỏ Vé");

        btnQuayLai.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/arrow.png"))); // NOI18N
        btnQuayLai.setText("Quay Lại");
        btnQuayLai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuayLaiActionPerformed(evt);
            }
        });

        pnlThongTinVe.setBackground(new java.awt.Color(255, 255, 255));
        pnlThongTinVe.setBorder(javax.swing.BorderFactory.createTitledBorder("Thông Tin Vé"));

        tblThongTinVe.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Số Giấy Tờ", "Họ Tên", "Đối Tượng", "Thông Tin Chỗ", "Giá Vé", "Giảm Giá", "Thành Tiền"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, true, true, true, true, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrThongTinVe.setViewportView(tblThongTinVe);

        btnXoaTatCa.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnXoaTatCa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/clear.png"))); // NOI18N
        btnXoaTatCa.setText("Xóa tất cả các vé");
        btnXoaTatCa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaTatCaActionPerformed(evt);
            }
        });

        lblTongTien.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTongTien.setText("Tổng Tiền:");

        btnXoa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/trash.png"))); // NOI18N
        btnXoa.setText("Xóa");

        lblTongTienValue.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTongTienValue.setText(" ");

        javax.swing.GroupLayout pnlThongTinVeLayout = new javax.swing.GroupLayout(pnlThongTinVe);
        pnlThongTinVe.setLayout(pnlThongTinVeLayout);
        pnlThongTinVeLayout.setHorizontalGroup(
            pnlThongTinVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThongTinVeLayout.createSequentialGroup()
                .addGroup(pnlThongTinVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrThongTinVe)
                    .addGroup(pnlThongTinVeLayout.createSequentialGroup()
                        .addGap(248, 248, 248)
                        .addComponent(btnXoa, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(60, 60, 60)
                        .addComponent(btnXoaTatCa)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 276, Short.MAX_VALUE)
                        .addComponent(lblTongTien)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblTongTienValue, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlThongTinVeLayout.setVerticalGroup(
            pnlThongTinVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThongTinVeLayout.createSequentialGroup()
                .addComponent(scrThongTinVe, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlThongTinVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnXoa, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                    .addComponent(btnXoaTatCa, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTongTien)
                    .addComponent(lblTongTienValue))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlNguoiDatVe.setBorder(javax.swing.BorderFactory.createTitledBorder("Thông Tin Người Đặt Vé"));

        lblCCCD.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblCCCD.setText("CCCD:");

        lblSDT.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSDT.setText("Số diện thoại:");

        lblHoTen.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblHoTen.setText("Họ tên: ");

        lblEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblEmail.setText("Email:");

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        javax.swing.GroupLayout pnlNguoiDatVeLayout = new javax.swing.GroupLayout(pnlNguoiDatVe);
        pnlNguoiDatVe.setLayout(pnlNguoiDatVeLayout);
        pnlNguoiDatVeLayout.setHorizontalGroup(
            pnlNguoiDatVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNguoiDatVeLayout.createSequentialGroup()
                .addGap(167, 167, 167)
                .addGroup(pnlNguoiDatVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSDT)
                    .addComponent(lblCCCD))
                .addGap(33, 33, 33)
                .addGroup(pnlNguoiDatVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtCCCD)
                    .addComponent(txtSDT, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 180, Short.MAX_VALUE)
                .addGroup(pnlNguoiDatVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHoTen)
                    .addComponent(lblEmail))
                .addGap(52, 52, 52)
                .addGroup(pnlNguoiDatVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtHoTen)
                    .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                .addContainerGap(180, Short.MAX_VALUE))
        );
        pnlNguoiDatVeLayout.setVerticalGroup(
            pnlNguoiDatVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNguoiDatVeLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(pnlNguoiDatVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCCCD)
                    .addComponent(lblHoTen)
                    .addComponent(txtCCCD, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHoTen, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addGroup(pnlNguoiDatVeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSDT)
                    .addComponent(lblEmail)
                    .addComponent(txtSDT, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22))
        );

        txtSDT.getAccessibleContext().setAccessibleDescription("30");
        txtHoTen.getAccessibleContext().setAccessibleDescription("30");
        txtEmail.getAccessibleContext().setAccessibleDescription("30");

        btnTiepTucThanhToan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnTiepTucThanhToan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/payment.png"))); // NOI18N
        btnTiepTucThanhToan.setText("Tiếp tục thanh toán");
        btnTiepTucThanhToan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTiepTucThanhToanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlNguoiDatVe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(btnQuayLai, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(455, 455, 455)
                .addComponent(btnTiepTucThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(pnlThongTinVe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnQuayLai, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 642, Short.MAX_VALUE)
                .addComponent(pnlNguoiDatVe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnTiepTucThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(93, 93, 93)
                    .addComponent(pnlThongTinVe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(506, Short.MAX_VALUE)))
        );

        getAccessibleContext().setAccessibleDescription("30");
    }// </editor-fold>//GEN-END:initComponents

    private void btnQuayLaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuayLaiActionPerformed
        iuh.fit.gui.menu.form.MainForm mainForm =
                (iuh.fit.gui.menu.form.MainForm) SwingUtilities.getAncestorOfClass(
                        iuh.fit.gui.menu.form.MainForm.class, this);
        
        // Quay lại instance cũ của Gui_BanVe (giữ nguyên dữ liệu)
        if (previousGuiBanVe != null) {
            mainForm.showForm(previousGuiBanVe);
        } else {
            // Fallback: Tạo instance mới nếu không có instance cũ
            mainForm.showForm(new Gui_BanVe());
        }
    }//GEN-LAST:event_btnQuayLaiActionPerformed

    private void btnXoaTatCaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaTatCaActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa tất cả các vé?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Xóa trong table này
            modelThongTinVe.setRowCount(0);
            capNhatTongTien();
            
            // ĐỒNG BỘ với Gui_BanVe
            if (previousGuiBanVe != null) {
                previousGuiBanVe.xoaTatCaGheDaChon();
            }
            
            JOptionPane.showMessageDialog(this, "Đã xóa tất cả các vé!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnXoaTatCaActionPerformed

    private void btnTiepTucThanhToanActionPerformed(java.awt.event.ActionEvent evt) {
        if (!validateThongTinNguoiDatVe()) {
            return;
        }//GEN-FIRST:event_btnTiepTucThanhToanActionPerformed
        // Validate dữ liệu trước khi thanh toán
        if (modelThongTinVe.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Chưa có vé nào trong giỏ!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Lấy thông tin khách hàng từ "Thông tin người đặt vé"
        String cccd = txtCCCD.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String sdt = txtSDT.getText().trim();
        String email = txtEmail.getText().trim();
        
        // Validate thông tin người đặt vé (tùy chọn - bỏ comment nếu cần bắt buộc)
        // if (cccd.isEmpty() || hoTen.isEmpty() || sdt.isEmpty()) {
        //     JOptionPane.showMessageDialog(this,
        //         "Vui lòng nhập đầy đủ thông tin người đặt vé!",
        //         "Lỗi",
        //         JOptionPane.ERROR_MESSAGE);
        //     return;
        // }
        
        int soLuongVe = modelThongTinVe.getRowCount();
        
        // Lấy tổng tiền từ label
        String tongTienText = lblTongTienValue.getText().replaceAll("[^0-9]", "");
        double tongTien = 0;
        try {
            tongTien = Double.parseDouble(tongTienText);
        } catch (NumberFormatException e) {
            tongTien = 0;
        }
        
        // Tính khuyến mãi hóa đơn dựa trên số lượng vé
        double chietKhauKhuyenMai = 0;
        try {
            chietKhauKhuyenMai = khuyenMaiService.getChietKhauHieuLucTheoSoVe(soLuongVe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        double tienKhuyenMai = tongTien * chietKhauKhuyenMai;
        
        System.out.println("📊 Tính khuyến mãi hóa đơn:");
        System.out.println("   - Số lượng vé: " + soLuongVe);
        System.out.println("   - Tổng tiền: " + tongTien);
        System.out.println("   - Chiết khấu: " + (chietKhauKhuyenMai * 100) + "%");
        System.out.println("   - Tiền khuyến mãi: " + tienKhuyenMai);
        
        // Mở Dialog Thanh Toán
        Window owner = SwingUtilities.getWindowAncestor(this);
        Diglog_ThanhToan dlg = new Diglog_ThanhToan(
            (Frame) owner, true,
            cccd, hoTen, sdt, email,
            soLuongVe, tongTien, tienKhuyenMai,
            this
        );
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        
        // Xử lý sau khi đóng dialog
        if (dlg.isThanhToanThanhCong()) {
            // ⚠️ Logic quay về Gui_BanVe đã xử lý TRONG Diglog_ThanhToan
            // → Không cần xử lý lại ở đây nữa
            
        } else if (dlg.isNhapLai()) {
            // Quay lại form này (giữ nguyên dữ liệu)
            
        } else if (dlg.isTreoDon()) {
            // Đã treo đơn → Quay lại Gui_BanVe với giỏ vé rỗng
            if (previousGuiBanVe != null) {
                // Xóa tất cả ghế đã chọn trong Gui_BanVe
                previousGuiBanVe.xoaTatCaGheDaChon();
                
                // Quay lại Gui_BanVe (hiển thị lại thông tin hành trình nhưng giỏ rỗng)
                iuh.fit.gui.menu.form.MainForm mainForm = 
                    (iuh.fit.gui.menu.form.MainForm) SwingUtilities.getAncestorOfClass(
                        iuh.fit.gui.menu.form.MainForm.class, this);
                
                if (mainForm != null) {
                    mainForm.showForm(previousGuiBanVe);
                }
            }
        }
    }
    private boolean validateThongTinNguoiDatVe() {
        String cccd = txtCCCD.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String sdt = txtSDT.getText().trim();
        String email = txtEmail.getText().trim();

        // ===== KIỂM TRA KHÔNG RỖNG =====
        if (cccd.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập CCCD!",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            txtCCCD.requestFocus();
            return false;
        }

        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập họ tên!",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            txtHoTen.requestFocus();
            return false;
        }

        if (sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập số điện thoại!",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            txtSDT.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập email!",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }

        // ===== KIỂM TRA REGEX =====
        // CCCD: 12 số
        if (!cccd.matches("^\\d{12}$")) {
            JOptionPane.showMessageDialog(this,
                    "CCCD phải gồm đúng 12 chữ số!",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE);
            txtCCCD.requestFocus();
            return false;
        }

        // Họ tên: chữ + khoảng trắng, có dấu
        if (!hoTen.matches("^[A-Za-zÀ-ỹà-ỹ\\s]{2,50}$")) {
            JOptionPane.showMessageDialog(this,
                    "Họ tên không hợp lệ (chỉ chữ, tối thiểu 2 ký tự)!",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE);
            txtHoTen.requestFocus();
            return false;
        }

        // SĐT Việt Nam
        if (!sdt.matches("^(0|\\+84)\\d{9}$")) {
            JOptionPane.showMessageDialog(this,
                    "Số điện thoại không hợp lệ!",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE);
            txtSDT.requestFocus();
            return false;
        }

        // Email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this,
                    "Email không hợp lệ!",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }

        return true;
    }

//GEN-LAST:event_btnTiepTucThanhToanActionPerformed

    // ==================== GETTERS ====================
    
    /**
     * Getter để Diglog_ThanhToan lấy dữ liệu bảng vé
     */
    public javax.swing.table.TableModel getModelThongTinVe() {
        return modelThongTinVe;
    }
    
    /**
     * Getter để lấy instance Gui_BanVe
     */
    public Gui_BanVe getPreviousGuiBanVe() {
        return previousGuiBanVe;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnQuayLai;
    private javax.swing.JButton btnTiepTucThanhToan;
    private javax.swing.JButton btnXoa;
    private javax.swing.JButton btnXoaTatCa;
    private javax.swing.JLabel lblCCCD;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblHoTen;
    private javax.swing.JLabel lblSDT;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTongTien;
    private javax.swing.JLabel lblTongTienValue;
    private javax.swing.JPanel pnlNguoiDatVe;
    private javax.swing.JPanel pnlThongTinVe;
    private javax.swing.JScrollPane scrThongTinVe;
    private javax.swing.JTable tblThongTinVe;
    private javax.swing.JTextField txtCCCD;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtHoTen;
    private javax.swing.JTextField txtSDT;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Getter cho table thông tin vé (dùng trong Dialog_HoaDon)
     */
    public JTable getTableThongTinVe() {
        return tblThongTinVe;
    }
    
    /**
     * Getter cho danh sách ChoNgoi (dùng trong Diglog_ThanhToan)
     */
    public List<ChoNgoi> getDanhSachChoNgoi() {
        return danhSachChoNgoi;
    }
    
    /**
     * Getter cho danh sách LichTrinh (dùng trong Diglog_ThanhToan)
     */
    public List<LichTrinh> getDanhSachLichTrinh() {
        return danhSachLichTrinh;
    }

    public List<entity.Ga> getDanhSachGaDi() {
        return danhSachGaDi;
    }

    public List<entity.Ga> getDanhSachGaDen() {
        return danhSachGaDen;
    }
}
