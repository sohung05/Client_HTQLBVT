package iuh.fit.gui;

import iuh.fit.utils.ClientContext;
import service.IKhuyenMaiService;
import entity.KhuyenMai;
import entity.DoiTuong;

import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.toedter.calendar.JDateChooser;


/**
 * @description Quản lý khuyến mãi theo đối tượng
 * @author : Anh
 * @date : 25/10/2025
 * @version : 1.0
 */
public class Gui_KhuyenMaiDoiTuong extends JPanel {
    private static int demKhuyenMai = 1;
    private boolean dangCapNhat = false;
    private DefaultTableModel model;
    private void loadTableData() {
        model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        try {
            IKhuyenMaiService service = ClientContext.getKhuyenMaiService();
            List<KhuyenMai> dsKM = service.getTatCaKhuyenMaiDoiTuong();

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            for (KhuyenMai km : dsKM) {
                LocalDateTime start = km.getThoiGianBatDau();
                LocalDateTime end = km.getThoiGianKetThuc();

                boolean trangThaiBool = km.isTrangThai();

                // Định dạng thời gian → chuỗi
                String startFormatted = start != null ? start.format(fmt) : "";
                String endFormatted = end != null ? end.format(fmt) : "";

                // Tạo hàng mới để add vào JTable
                Object[] newRow = new Object[]{
                        km.getMaKhuyenMai(),
                        km.getTenKhuyenMai(),
                        km.getDoiTuongApDung(),
                        startFormatted,
                        endFormatted,
                        km.getChietKhau(), // Hiển thị dạng số thực (0.x) tương ứng demo
                        (end != null && end.isBefore(now)) ? "Hết hạn" : (trangThaiBool ? "Hoạt động" : "Tạm ngưng")
                };

                model.addRow(newRow);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LocalDate convertToLocalDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDate) return (LocalDate) obj;
        if (obj instanceof LocalDateTime) return ((LocalDateTime) obj).toLocalDate();
        if (obj instanceof java.sql.Date) return ((java.sql.Date) obj).toLocalDate();
        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime().toLocalDate();
        return null;
    }

    public Gui_KhuyenMaiDoiTuong() {
        initComponents();
        // Disable mã khuyến mãi ngay từ đầu
        jTextField1.setEditable(false);
        initEvent();
        initTable();
        loadTableData();
    }


    private void initTable() {
        model = (DefaultTableModel) jTable1.getModel();
    }
    /**
     * 🔍 Kiểm tra dữ liệu người dùng nhập (BỎ RÀNG BUỘC TÊN)
     * @return true nếu hợp lệ, false nếu có lỗi
     */
    private boolean validateInput() {
        String doiTuongStr = (String) jComboBox1.getSelectedItem();
        Date start = jDateChooser1.getDate();
        Date end = jDateChooser2.getDate();
        String chietKhauStr = jTextField4.getText().trim();

        // --- Kiểm tra đối tượng ---
        if (doiTuongStr == null || doiTuongStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đối tượng khuyến mãi!");
            jComboBox1.requestFocus();
            return false;
        }

        // --- Kiểm tra ngày ---
        if (start == null) {
            JOptionPane.showMessageDialog(this, "Thời gian bắt đầu không được để trống!");
            jDateChooser1.requestFocus();
            return false;
        }
        if (end == null) {
            JOptionPane.showMessageDialog(this, "Thời gian kết thúc không được để trống!");
            jDateChooser2.requestFocus();
            return false;
        }
        if (end.before(start)) {
            JOptionPane.showMessageDialog(this, "Thời gian kết thúc phải sau thời gian bắt đầu!");
            return false;
        }

        // --- Kiểm tra chiết khấu ---
        if (chietKhauStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chiết khấu không được để trống!");
            jTextField4.requestFocus();
            return false;
        }

        double chietKhau;
        try {
            chietKhau = Double.parseDouble(chietKhauStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Chiết khấu phải là số hợp lệ!");
            jTextField4.requestFocus();
            return false;
        }

        if (chietKhau <= 0 || chietKhau > 100) {
            JOptionPane.showMessageDialog(this, "Chiết khấu phải trong khoảng 0 - 100 (%)!");
            jTextField4.requestFocus();
            return false;
        }

        return true;
    }

    private void initEvent() {

        jButton4.addActionListener(e -> {
            if (!validateInput()) { // Kiểm tra ràng buộc
                return;
            }

            try {
                // --- Lấy dữ liệu từ form ---
                String ten = jTextField2.getText().trim();
                String doiTuongStr = jComboBox1.getSelectedItem().toString().trim();
                Date start = jDateChooser1.getDate();
                Date end = jDateChooser2.getDate();
                String chietKhauStr = jTextField4.getText().trim();

                // --- Kiểm tra chiết khấu ---
                if (chietKhauStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "⚠️ Chiết khấu không được để trống!");
                    jTextField4.requestFocus();
                    return;
                }

                double chietKhau;
                try {
                    chietKhau = Double.parseDouble(chietKhauStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "⚠️ Chiết khấu phải là số hợp lệ!");
                    jTextField4.requestFocus();
                    return;
                }

                if (chietKhau <= 0 || chietKhau > 100) {
                    JOptionPane.showMessageDialog(this, "⚠️ Chiết khấu phải trong khoảng 0 - 100 (%)");
                    jTextField4.requestFocus();
                    return;
                }

                // Chuyển về dạng 0.x
                chietKhau /= 100.0;

                // --- Kiểm tra ngày ---
                if (start == null || end == null) {
                    JOptionPane.showMessageDialog(this, "⚠️ Ngày bắt đầu hoặc kết thúc không hợp lệ!");
                    return;
                }

                // --- Chuyển ComboBox sang Enum DoiTuong ---
                DoiTuong doiTuong = switch (doiTuongStr) {
                    case "Sinh viên" -> DoiTuong.SinhVien;
                    case "Trẻ em" -> DoiTuong.TreEm;
                    case "Người lớn" -> DoiTuong.NguoiLon;
                    case "Người cao tuổi" -> DoiTuong.NguoiCaoTuoi;
                    default -> throw new IllegalArgumentException("Đối tượng không hợp lệ: " + doiTuongStr);
                };

                // --- SERVICE ---
                IKhuyenMaiService service = ClientContext.getKhuyenMaiService();

                // --- Sinh mã duy nhất ---
                SimpleDateFormat sdfDate = new SimpleDateFormat("ddMMyyyy");
                String datePart = sdfDate.format(start);
                String ma = "KM" + datePart + (int) (Math.random() * 90 + 10);

                // --- Tạo đối tượng KhuyenMai ---
                KhuyenMai km = new KhuyenMai(
                        ma,
                        ten,
                        "KMKH",
                        new Timestamp(start.getTime()).toLocalDateTime(),
                        new Timestamp(end.getTime()).toLocalDateTime(),
                        true
                );

                // --- Thêm vào DB ---
                boolean success = service.themKhuyenMaiDoiTuong(km, doiTuong.name(), chietKhau);

                if (success) {
                    JOptionPane.showMessageDialog(this, "✅ Thêm khuyến mãi thành công!");
                    loadTableData(); // Load lại JTable
                    clearForm();     // Xóa form
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Thêm khuyến mãi thất bại!");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Lỗi: " + ex.getMessage());
            }
        });
       // Lọc
        jButton3.addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            jTable1.setRowSorter(sorter);

            Date fromDateValue = jDateChooser1.getDate();
            Date toDateValue = jDateChooser2.getDate();

            if (fromDateValue == null || toDateValue == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu và kết thúc!");
                return;
            }

            LocalDate fromDate = fromDateValue.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate toDate = toDateValue.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            if (fromDate.isAfter(toDate)) {
                JOptionPane.showMessageDialog(this,
                        "Thời gian kết thúc phải sau thời gian bắt đầu!");
                return;
            }
            int colStart = 3;
            int colEnd   = 4;

            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {

                    try {
                        String startStr = entry.getValue(colStart).toString();
                        String endStr   = entry.getValue(colEnd).toString();

                        // ======= CHỈ LẤY NGÀY =======
                        if (startStr.contains(" "))
                            startStr = startStr.substring(0, startStr.indexOf(" "));

                        if (endStr.contains(" "))
                            endStr = endStr.substring(0, endStr.indexOf(" "));

                        // Parse LocalDate
                        LocalDate startDate = LocalDate.parse(startStr);
                        LocalDate endDate   = LocalDate.parse(endStr);

                        // Overlap
                        return !endDate.isBefore(fromDate) && !startDate.isAfter(toDate);

                    } catch (Exception ex) {
                        return false;
                    }
                }
            });

            JOptionPane.showMessageDialog(this,
                    "Đã lọc dữ liệu từ " + fromDate + " đến " + toDate);
        });


// Cập nhật
        jButton5.addActionListener(e -> {


            int row = jTable1.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi để cập nhật!");
                return;
            }

            // Lấy dữ liệu cũ từ bảng
            String maKMCu = model.getValueAt(row, 0).toString().trim();

            // Lấy dữ liệu mới từ form
            String tenMoi = jTextField2.getText().trim();
            Date ngayBD = jDateChooser1.getDate();
            Date ngayKT = jDateChooser2.getDate();
            String doiTuongStr = jComboBox1.getSelectedItem().toString().trim();
            String chietKhauStr = jTextField4.getText().trim();

            // Kiểm tra ràng buộc (trừ mã và tên)
            if (!validateInput()) {
                return;
            }
            
            // Kiểm tra tên không trống
            if (tenMoi.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên khuyến mãi không được để trống!");
                jTextField2.requestFocus();
                return;
            }

            double chietKhau;
            try {
                chietKhau = Double.parseDouble(chietKhauStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Chiết khấu phải là số hợp lệ!");
                return;
            }

            // Kiểm tra chiết khấu hợp lệ (0 < x ≤ 100)
            if (chietKhau <= 0 || chietKhau > 100) {
                JOptionPane.showMessageDialog(this, "Chiết khấu phải trong khoảng 0 - 100 (%)!");
                return;
            }

            // --- Chuyển ComboBox sang Enum DoiTuong ---
            DoiTuong doiTuong;
            try {
                doiTuong = switch (doiTuongStr) {
                    case "Sinh viên" -> DoiTuong.SinhVien;
                    case "Trẻ em" -> DoiTuong.TreEm;
                    case "Người lớn" -> DoiTuong.NguoiLon;
                    case "Người cao tuổi" -> DoiTuong.NguoiCaoTuoi;
                    default -> throw new IllegalArgumentException("Đối tượng không hợp lệ: " + doiTuongStr);
                };
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
                return;
            }

            // --- Chuyển java.util.Date sang java.sql.Date ---
            java.sql.Date sqlNgayBD = new java.sql.Date(ngayBD.getTime());
            java.sql.Date sqlNgayKT = new java.sql.Date(ngayKT.getTime());

            // Gọi SERVICE cập nhật
            try {
                IKhuyenMaiService service = ClientContext.getKhuyenMaiService();
                boolean result = service.capNhatKhuyenMaiDoiTuong(
                        maKMCu,
                        maKMCu, // maMoi
                        tenMoi, // ten
                        sqlNgayBD,
                        sqlNgayKT,
                        chietKhau / 100.0,
                        doiTuong.name()
                );

                if (result) {
                    JOptionPane.showMessageDialog(this, "Cập nhật khuyến mãi thành công!");
                    loadTableData();

                    // Tự động chọn lại dòng đã cập nhật
                    for (int i = 0; i < jTable1.getRowCount(); i++) {
                        if (jTable1.getValueAt(i, 0).toString().equals(maKMCu)) {
                            jTable1.setRowSelectionInterval(i, i);
                            break;
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi kết nối Server: " + ex.getMessage());
            }
        });

        // SỰ KIỆN: Xóa trắng form
        jButton2.addActionListener(e -> clearForm());

        // --- SỰ KIỆN: Tạm ngưng / Kích hoạt khuyến mãi ---
        jButton6.addActionListener(e -> {
            int row = jTable1.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng chọn khuyến mãi cần tạm ngưng!");
                return;
            }

            String maKM = model.getValueAt(row, 0).toString(); // Mã KM
            String currentStatus = model.getValueAt(row, 6).toString().trim(); // Cột trạng thái

            // ✅ Không cho phép thay đổi trạng thái nếu đã hết hạn
            if (currentStatus.equalsIgnoreCase("Hết hạn")) {
                JOptionPane.showMessageDialog(this, 
                    "Không thể thay đổi trạng thái!\nKhuyến mãi này đã hết hạn.",
                    "Thông báo", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean newTrangThai; // true = Hoạt động, false = Tạm ngưng
            String message;

            if (currentStatus.equalsIgnoreCase("Hoạt động")) {
                newTrangThai = false;
                message = "✅ Khuyến mãi đã được tạm ngưng!";
            } else {
                newTrangThai = true;
                message = "✅ Khuyến mãi đã được kích hoạt lại!";
            }

            try {
                IKhuyenMaiService service = ClientContext.getKhuyenMaiService();
                boolean success = service.tamNgungTrangThai(maKM, newTrangThai);

                if (success) {
                    JOptionPane.showMessageDialog(this, message);
                    loadTableData(); // ✅ làm mới dữ liệu từ SQL
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thất bại!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi kết nối Server: " + ex.getMessage());
            }
        });


        jTable1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = jTable1.getSelectedRow();
                if (row != -1) {
                    dangCapNhat = true; // 👉 Đánh dấu là đang cập nhật

                    jTextField1.setText(model.getValueAt(row, 0).toString());
                    jTextField2.setText(model.getValueAt(row, 1).toString());
                    
                    // ✅ Mapping đối tượng từ DB sang ComboBox
                    String doiTuongDB = model.getValueAt(row, 2).toString();
                    String doiTuongDisplay = switch (doiTuongDB) {
                        case "TreEm" -> "Trẻ em";
                        case "NguoiLon" -> "Người lớn";
                        case "NguoiCaoTuoi" -> "Người cao tuổi";
                        case "SinhVien" -> "Sinh viên";
                        default -> doiTuongDB; // fallback
                    };
                    jComboBox1.setSelectedItem(doiTuongDisplay);
                    
                    // ✅ Chiết khấu: nhân 100 để hiển thị (DB lưu 0.25, hiển thị 25)
                    double chietKhau = Double.parseDouble(model.getValueAt(row, 5).toString());
                    jTextField4.setText(String.format("%.0f", chietKhau * 100));

                    try {
                        Object startObj = model.getValueAt(row, 3);
                        Object endObj = model.getValueAt(row, 4);
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        if (startObj instanceof LocalDateTime) {
                            jDateChooser1.setDate(Timestamp.valueOf((LocalDateTime) startObj));
                            jDateChooser2.setDate(Timestamp.valueOf((LocalDateTime) endObj));
                        } else {
                            // Nếu là String, parse với format đúng
                            String startStr = startObj.toString();
                            String endStr = endObj.toString();
                            jDateChooser1.setDate(sdf.parse(startStr));
                            jDateChooser2.setDate(sdf.parse(endStr));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "❌ Lỗi định dạng ngày: " + ex.getMessage());
                    }
                }
            }
        });

        // --- SỰ KIỆN: TÌM KIẾM TRONG BẢNG ---
        jButton1.addActionListener(e -> {
            String keyword = jTextField2.getText().trim().toLowerCase();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhập tên khuyến mãi để tìm kiếm!");
                return;
            }

            DefaultTableModel filteredModel = new DefaultTableModel(
                    new Object[]{"Mã khuyến mãi", "Tên khuyến mãi", "Đối tượng",
                            "Thời gian áp dụng", "Thời gian kết thúc", "Chiết khấu", "Trạng thái"}, 0
            );

            for (int i = 0; i < model.getRowCount(); i++) {
                String ten = model.getValueAt(i, 1).toString().toLowerCase();
                if (ten.contains(keyword)) {
                    Object[] row = new Object[model.getColumnCount()];
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        row[j] = model.getValueAt(i, j);
                    }
                    filteredModel.addRow(row);
                }
            }

            if (filteredModel.getRowCount() > 0) {
                jTable1.setModel(filteredModel);
                JOptionPane.showMessageDialog(this,
                        "Tìm thấy " + filteredModel.getRowCount() + " khuyến mãi có tên chứa: " + keyword);
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khuyến mãi nào phù hợp!");
            }
        });

        // 🔹 Khi người dùng chọn ngày bắt đầu → tự đổi mã theo ngày
        jDateChooser1.addPropertyChangeListener("date", evt -> {
            Date ngayBatDau = jDateChooser1.getDate();
            if (ngayBatDau == null) return;

            try {
                // Nếu người dùng đang sửa (có sẵn mã)
                String maHienTai = jTextField1.getText().trim();

                // Lấy ngày hiện tại để sinh phần giữa mã
                String ngayMoi = new SimpleDateFormat("ddMMyyyy").format(ngayBatDau);

                // Nếu đang sửa và mã có dạng hợp lệ (KM + ngày + số)
                if (!maHienTai.isEmpty() && maHienTai.matches("^KM\\d{8}\\d{2}$")) {
                    // Giữ phần số thứ tự (2 ký tự cuối)
                    String soThuTu = maHienTai.substring(maHienTai.length() - 2);
                    // Ghép lại mã mới với ngày mới
                    String maMoi = "KM" + ngayMoi + soThuTu;
                    jTextField1.setText(maMoi);
                } else {
                    // Nếu đang thêm mới thì sinh mã mới hoàn toàn
                    String maTuDong = KhuyenMai.taoMaKhuyenMaiTheoNgay(ngayBatDau, demKhuyenMai++);
                    jTextField1.setText(maTuDong);
                }

                jTextField1.setEditable(false);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Lỗi sinh mã: " + ex.getMessage());
            }
        });

    }


    private void clearForm() {
        jTextField2.setText("");
        jComboBox1.setSelectedIndex(0);
        jDateChooser1.setDate(null);
        jDateChooser2.setDate(null);
        jTextField4.setText("");

        jTextField1.setText("");
        jTextField1.setEditable(false);

        // Hủy bỏ bộ lọc trên bảng
        jTable1.setRowSorter(null);
        jTable1.clearSelection();

        loadTableData();  // Load lại toàn bộ data từ DB
    }


    @SuppressWarnings("unchecked")
    private void initComponents() {

        jLabel1 = new JLabel();
        jScrollPane1 = new JScrollPane();
        jTable1 = new JTable();
        jPanel1 = new JPanel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        jLabel6 = new JLabel();
        jLabel7 = new JLabel();
        jTextField1 = new JTextField();
        jTextField2 = new JTextField();
        jTextField4 = new JTextField();
        jComboBox1 = new JComboBox<>();
        jButton1 = new JButton();
        jButton2 = new JButton();
        jButton3 = new JButton();
        jButton4 = new JButton();
        jButton5 = new JButton();
        jButton6 = new JButton();

        // ➕ Thêm hai JDateChooser
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();

        setBackground(new java.awt.Color(234, 243, 251));

        jLabel1.setBackground(new java.awt.Color(234, 243, 251));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setText("Khuyến Mãi Đối tượng");

        jTable1.setModel(new DefaultTableModel(
                new Object [][] {},
                new String [] {
                        "Mã khuyến mãi", "Tên khuyến mãi", "Đối tượng", "Thời gian áp dụng", "Thời gian kết thúc", "Chiết khấu", "Trạng thái"
                }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.setBackground(new java.awt.Color(234, 243, 251));
        jPanel1.setBorder(BorderFactory.createTitledBorder("Thông tin khuyến mãi"));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 16));
        jLabel2.setText("Mã khuyến mãi:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 16));
        jLabel3.setText("Tên khuyến mãi:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 16));
        jLabel4.setText("Đối tượng:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 16));
        jLabel5.setText("Thời gian áp dụng:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 16));
        jLabel6.setText("Thời gian kết thúc:");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 16));
        jLabel7.setText("Chiết khấu:");

        jComboBox1.setModel(new DefaultComboBoxModel<>(
                new String[] { " ", "Trẻ em", "Người lớn", "Người cao tuổi", "Sinh viên" }
        ));

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(56, 56, 56)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel3)
                                        .addComponent(jLabel4)
                                        .addComponent(jLabel5)
                                        .addComponent(jLabel6)
                                        .addComponent(jLabel7))
                                .addGap(51, 51, 51)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jTextField1, GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                                        .addComponent(jTextField2)
                                        .addComponent(jComboBox1, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jDateChooser1, GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                                        .addComponent(jDateChooser2, GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                                        .addComponent(jTextField4))
                                .addGap(40, 40, 40))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(26)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(28)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(29)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(jComboBox1, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(34)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel5)
                                        .addComponent(jDateChooser1, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(32)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel6)
                                        .addComponent(jDateChooser2, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(33)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7)
                                        .addComponent(jTextField4, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(36, Short.MAX_VALUE))
        );

        jButton1.setIcon(new ImageIcon(getClass().getResource("/icon/TimKiem.png")));
        jButton1.setText("Tìm");

        jButton2.setIcon(new ImageIcon(getClass().getResource("/icon/clear.png")));
        jButton2.setText("Xóa trắng");

        jButton3.setIcon(new ImageIcon(getClass().getResource("/icon/filter.png")));
        jButton3.setText("Lọc");

        jButton4.setIcon(new ImageIcon(getClass().getResource("/icon/add.png")));
        jButton4.setText("Thêm");

        jButton5.setIcon(new ImageIcon(getClass().getResource("/icon/update.png")));
        jButton5.setText("Cập nhật");

        jButton6.setIcon(new ImageIcon(getClass().getResource("/icon/stop.png")));
        jButton6.setText("Tạm ngưng");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap())
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(jButton1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jButton3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jButton5, GroupLayout.PREFERRED_SIZE, 126, GroupLayout.PREFERRED_SIZE))
                                                .addGap(31, 31, 31)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(jButton2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jButton6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jButton4, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE))
                                                .addGap(84, 84, 84))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(29)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addGap(29)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jButton3, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jButton4, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addGap(18)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jButton5, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jButton6, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(jScrollPane1))
                                .addContainerGap())
        );
    }


    private void jButton1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton jButton1;
    private JButton jButton2;
    private JButton jButton3;
    private JButton jButton4;
    private JButton jButton5;
    private JButton jButton6;
    private JComboBox<String> jComboBox1;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JTable jTable1;
    private JTextField jTextField1;
    private JTextField jTextField2;
    private JTextField jTextField4;
    private JDateChooser jDateChooser1;
    private JDateChooser jDateChooser2;

    // End of variables declaration//GEN-END:variables
}