/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package iuh.fit.gui;

import iuh.fit.utils.ClientContext;
import service.IKhuyenMaiService;
import entity.KhuyenMai;
import entity.DoiTuong;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import com.toedter.calendar.JDateChooser;

public class Gui_KhuyenMaiHoaDon extends JPanel {
    private static int demKhuyenMai = 1;
    private boolean dangCapNhat = false;
    private DefaultTableModel model;

    private void loadTableData() {
        model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        try {
            IKhuyenMaiService service = ClientContext.getKhuyenMaiService();
            List<KhuyenMai> dsKM = service.getTatCaKhuyenMaiHoaDon();

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            for (KhuyenMai km : dsKM) {
                LocalDateTime start = km.getThoiGianBatDau();
                LocalDateTime end = km.getThoiGianKetThuc();

                boolean trangThaiBool = km.isTrangThai();

                String startFormatted = start != null ? start.format(fmt) : "";
                String endFormatted = end != null ? end.format(fmt) : "";

                Object[] newRow = new Object[]{
                        km.getMaKhuyenMai(),
                        km.getTenKhuyenMai(),
                        km.getDoiTuongApDung(),
                        startFormatted,
                        endFormatted,
                        km.getChietKhau() * 100,
                        (end != null && end.isBefore(now)) ? "Hết hạn" : (trangThaiBool ? "Hoạt động" : "Tạm ngưng")
                };

                model.addRow(newRow);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateInput() {
        Date start = jDateChooser1.getDate();
        Date end = jDateChooser2.getDate();
        String chietKhauStr = jTextField4.getText().trim();

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

        if (chietKhauStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chiết khấu không được để trống!");
            jTextField4.requestFocus();
            return false;
        }

        double chietKhau;
        try {
            chietKhau = Double.parseDouble(chietKhauStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Chiết khấu phải là số!");
            jTextField4.requestFocus();
            return false;
        }

        if (chietKhau <= 0 || chietKhau > 100) {
            JOptionPane.showMessageDialog(this, "Chiết khấu phải trong khoảng 0 - 100 (%)");
            jTextField4.requestFocus();
            return false;
        }

        return true;
    }

    public Gui_KhuyenMaiHoaDon() {
        initComponents();
        jTextField1.setEditable(false);
        initEvent();
        initTable();
        loadTableData();
    }

    private void initTable() {
        model = (DefaultTableModel) jTable1.getModel();
    }

    private void initEvent() {
        jButton1.addActionListener(e -> {
            String keyword = jTextField2.getText().trim().toLowerCase();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khuyến mãi để tìm kiếm!");
                return;
            }

            DefaultTableModel filteredModel = new DefaultTableModel(
                    new Object[]{"Mã khuyến mãi", "Tên khuyến mãi", "Đối tượng",
                            "Thời gian áp dụng", "Thời gian kết thúc",
                            "Chiết khấu", "Trạng thái"}, 0
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
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khuyến mãi nào phù hợp!");
            }
        });

        jDateChooser1.addPropertyChangeListener("date", evt -> {
            Date ngayBatDau = jDateChooser1.getDate();
            if (ngayBatDau != null) {
                try {
                    String maTuDong = KhuyenMai.taoMaKhuyenMaiTheoNgay(ngayBatDau, demKhuyenMai++);
                    jTextField1.setText(maTuDong);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "❌ Lỗi sinh mã: " + ex.getMessage());
                }
            }
        });

        jButton4.addActionListener(e -> {
            if (!validateInput()) return;
            try {
                String ten = jTextField2.getText().trim();
                String dieuKien = jTextField3.getText().trim();
                double chietKhau = Double.parseDouble(jTextField4.getText().trim());
                Date start = jDateChooser1.getDate();
                Date end = jDateChooser2.getDate();

                try {
                    IKhuyenMaiService service = ClientContext.getKhuyenMaiService();
                    SimpleDateFormat sdfDate = new SimpleDateFormat("ddMMyyyy");
                    String ma = "KM" + sdfDate.format(start) + (int) (Math.random() * 90 + 10);

                    KhuyenMai km = new KhuyenMai(
                            ma, ten, "KMKH",
                            new Timestamp(start.getTime()).toLocalDateTime(),
                            new Timestamp(end.getTime()).toLocalDateTime(),
                            true
                    );

                    if (service.themKhuyenMaiHoaDon(km, dieuKien, chietKhau / 100.0)) {
                        loadTableData();
                        JOptionPane.showMessageDialog(this, "✅ Thêm khuyến mãi thành công!");
                    } else {
                        JOptionPane.showMessageDialog(this, "❌ Thêm khuyến mãi thất bại!");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "❌ Lỗi kết nối Server: " + ex.getMessage());
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Lỗi khi thêm khuyến mãi: " + ex.getMessage());
            }
        });
// SỰ KIỆN : LỌC KHUYẾN MÃI THEO NGÀY THÁNG
        jButton3.addActionListener(e -> {

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            jTable1.setRowSorter(sorter);

            // Lấy ngày từ JDateChooser
            Date fromDateValue = jDateChooser1.getDate();
            Date toDateValue = jDateChooser2.getDate();

            if (fromDateValue == null || toDateValue == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu và kết thúc!");
                return;
            }

            LocalDate fromDate = fromDateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate toDate = toDateValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (fromDate.isAfter(toDate)) {
                JOptionPane.showMessageDialog(this,
                        "Thời gian kết thúc phải sau thời gian bắt đầu!");
                return;
            }

            int colStart = 3; // cột ngày bắt đầu của dữ liệu
            int colEnd   = 4; // cột ngày kết thúc của dữ liệu
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    try {
                        String startStr = entry.getValue(colStart).toString().split(" ")[0];
                        String endStr   = entry.getValue(colEnd).toString().split(" ")[0];

                        LocalDate startDate = LocalDate.parse(startStr, dateFormatter);
                        LocalDate endDate   = LocalDate.parse(endStr, dateFormatter);

                        // Lọc các khuyến mãi có giao nhau với khoảng thời gian chọn (Overlap)
                        // Điều kiện: (Ngày bắt đầu KM <= Ngày kết thúc lọc) AND (Ngày kết thúc KM >= Ngày bắt đầu lọc)
                        return !startDate.isAfter(toDate) && !endDate.isBefore(fromDate);

                    } catch (Exception ex) {
                        return false;
                    }
                }
            });

            JOptionPane.showMessageDialog(this, "Đã lọc dữ liệu từ " + fromDate + " đến " + toDate);
        });
// Cập nhật khuyến mãi
        jButton5.addActionListener(e -> {
            int row = jTable1.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khuyến mãi cần cập nhật!");
                return;
            }

            try {
                String maKMCu = (String) jTable1.getValueAt(row, 0);
                String maKMMoi = jTextField1.getText().trim();
                String ten = jTextField2.getText().trim();
                String dieuKien = jTextField3.getText().trim();
                String chietKhauStr = jTextField4.getText().trim();

                // Kiểm tra ràng buộc (trừ mã và tên)
                if (!validateInput()) {
                    return;
                }
                
                // Kiểm tra tên không trống
                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tên khuyến mãi không được để trống!");
                    jTextField2.requestFocus();
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
                    JOptionPane.showMessageDialog(this, "⚠️ Chiết khấu phải nằm trong khoảng 0 - 100 (%)!");
                    jTextField4.requestFocus();
                    return;
                }

                // Nếu nhập 25 → 0.25
                if (chietKhau > 1) {
                    chietKhau /= 100.0;
                }

                // --- Kiểm tra ngày ---
                if (jDateChooser1.getDate() == null || jDateChooser2.getDate() == null) {
                    JOptionPane.showMessageDialog(this, "Ngày bắt đầu hoặc kết thúc không hợp lệ!");
                    return;
                }

                java.sql.Date sqlStart = new java.sql.Date(jDateChooser1.getDate().getTime());
                java.sql.Date sqlEnd = new java.sql.Date(jDateChooser2.getDate().getTime());

                try {
                    IKhuyenMaiService service = ClientContext.getKhuyenMaiService();
                    boolean ok = service.capNhatKhuyenMaiHoaDon(maKMCu, maKMMoi, ten, sqlStart, sqlEnd, chietKhau, dieuKien);

                    if (ok) {
                        JOptionPane.showMessageDialog(this, "✅ Cập nhật khuyến mãi thành công!");
                        loadTableData();
                    } else {
                        JOptionPane.showMessageDialog(this, "❌ Cập nhật thất bại!");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "❌ Lỗi kết nối Server: " + ex.getMessage());
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + ex.getMessage());
            }
        });

        // SỰ KIỆN: Xóa trắng form
        jButton2.addActionListener(e -> clearForm());

        // SỰ KIỆN: Tạm ngưng khuyến mãi (chuyển trạng thái)
        jButton6.addActionListener(e -> {
            int row = jTable1.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "⚠️ Vui lòng chọn khuyến mãi cần tạm ngưng!");
                return;
            }

            String maKM = model.getValueAt(row, 0).toString(); // cột mã KM
            String currentStatus = model.getValueAt(row, 6).toString().trim();

            // ✅ Không cho phép thay đổi trạng thái nếu đã hết hạn
            if (currentStatus.equalsIgnoreCase("Hết hạn")) {
                JOptionPane.showMessageDialog(this,
                    "❌ Không thể thay đổi trạng thái!\nKhuyến mãi này đã hết hạn.",
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
                    JOptionPane.showMessageDialog(this, "❌ Cập nhật trạng thái thất bại!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Lỗi kết nối Server: " + ex.getMessage());
            }
        });


        // SỰ KIỆN: Click vào bảng → load dữ liệu lên form
        jTable1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = jTable1.getSelectedRow();
                if (row != -1) {
                    jTextField1.setText(model.getValueAt(row, 0).toString());
                    jTextField2.setText(model.getValueAt(row, 1).toString());
                    jTextField3.setText(model.getValueAt(row, 2).toString());

                    // ✅ Chiết khấu: nhân 100 để hiển thị (DB lưu 0.09, hiển thị 9)
                    double chietKhau = Double.parseDouble(model.getValueAt(row, 5).toString());
                    jTextField4.setText(String.format("%.0f", chietKhau * 100));

                    try {
                        String startStr = model.getValueAt(row, 3).toString();
                        String endStr = model.getValueAt(row, 4).toString();

                        // Format từ database: "yyyy-MM-dd HH:mm:ss"
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        jDateChooser1.setDate(sdf.parse(startStr));
                        jDateChooser2.setDate(sdf.parse(endStr));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Lỗi định dạng ngày: " + ex.getMessage());
                    }
                }
            }
        });

}
        private void resetTable() {
        try {
            model.setRowCount(0); // xóa dữ liệu cũ
            IKhuyenMaiService service = ClientContext.getKhuyenMaiService();
            List<KhuyenMai> dsKM = service.getTatCaKhuyenMaiHoaDon();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            
            for (KhuyenMai km : dsKM) {
                // ✅ Kiểm tra hết hạn
                String trangThai;
                if (km.getThoiGianKetThuc() != null && km.getThoiGianKetThuc().isBefore(now)) {
                    trangThai = "Hết hạn";
                } else {
                    trangThai = km.isTrangThai() ? "Hoạt động" : "Tạm ngưng";
                }
                
                Object[] row = {
                        km.getMaKhuyenMai(),
                        km.getTenKhuyenMai(),
                        km.getDoiTuongApDung(), // Hiển thị điều kiện ("11-40 vé") thay vì số vé
                        km.getThoiGianBatDau().format(formatter),
                        km.getThoiGianKetThuc().format(formatter),
                        km.getChietKhau(),
                        trangThai
                };
                model.addRow(row);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi reset bảng!");
        }
    }

    // Hàm xóa trắng form
    private void clearForm() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jDateChooser1.setDate(null);
        jDateChooser2.setDate(null);
        
        // Hủy bỏ bộ lọc trên bảng
        jTable1.setRowSorter(null);
        jTable1.clearSelection();

        loadTableData();  // Load lại toàn bộ data từ DB
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new JLabel();
        jPanel1 = new JPanel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        jLabel6 = new JLabel();
        jLabel7 = new JLabel();
        jTextField1 = new JTextField();
        jTextField2 = new JTextField();
        jTextField3 = new JTextField();
        jTextField4 = new JTextField();
        jDateChooser1 = new JDateChooser();
        jDateChooser2 = new JDateChooser();
        jButton1 = new JButton();
        jButton2 = new JButton();
        jButton3 = new JButton();
        jButton4 = new JButton();
        jButton5 = new JButton();
        jButton6 = new JButton();
        jScrollPane1 = new JScrollPane();
        jTable1 = new JTable();

        setBackground(new Color(234, 243, 251));
        setRequestFocusEnabled(false);
        setVerifyInputWhenFocusTarget(false);

        jLabel1.setFont(new Font("Segoe UI", 1, 20)); // NOI18N
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setText("Khuyến Mãi Hóa Đơn");

        jPanel1.setBackground(new Color(234, 243, 251));
        jPanel1.setBorder(BorderFactory.createTitledBorder("Thông tin khuyến mãi"));

        jLabel2.setFont(new Font("Segoe UI", 0, 16)); // NOI18N
        jLabel2.setText("Mã khuyến mãi:");

        jLabel3.setFont(new Font("Segoe UI", 0, 16)); // NOI18N
        jLabel3.setText("Tên khuyến mãi:");

        jLabel4.setFont(new Font("Segoe UI", 0, 16)); // NOI18N
        jLabel4.setText("Số lượng vé:");

        jLabel5.setFont(new Font("Segoe UI", 0, 16)); // NOI18N
        jLabel5.setText("Thời gian áp dụng:");

        jLabel6.setFont(new Font("Segoe UI", 0, 16)); // NOI18N
        jLabel6.setText("Thời gian kết thúc:");

        jLabel7.setFont(new Font("Segoe UI", 0, 16)); // NOI18N
        jLabel7.setText("Chiết khấu:");

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(56, 56, 56)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel6)
                                        .addComponent(jLabel7, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jTextField3, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE))
                                                .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(jDateChooser1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jDateChooser2, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE))
                                                .addComponent(jTextField4, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jTextField2, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE))
                                .addGap(40, 40, 40))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(28, 28, 28)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(27, 27, 27)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(jTextField3, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(20, 20, 20)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel5)
                                        .addComponent(jDateChooser1, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(32, 32, 32)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel6)
                                        .addComponent(jDateChooser2, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(25, 25, 25)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7)
                                        .addComponent(jTextField4, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(36, Short.MAX_VALUE))
        );

        jButton1.setIcon(new ImageIcon(getClass().getResource("/icon/TimKiem.png"))); // NOI18N
        jButton1.setText("Tìm");
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new ImageIcon(getClass().getResource("/icon/clear.png"))); // NOI18N
        jButton2.setText("Xóa trắng");

        jButton3.setIcon(new ImageIcon(getClass().getResource("/icon/filter.png"))); // NOI18N
        jButton3.setText("Lọc");

        jButton4.setIcon(new ImageIcon(getClass().getResource("/icon/add.png"))); // NOI18N
        jButton4.setText("Thêm");

        jButton5.setIcon(new ImageIcon(getClass().getResource("/icon/update.png"))); // NOI18N
        jButton5.setText("Cập nhật");

        jButton6.setIcon(new ImageIcon(getClass().getResource("/icon/stop.png"))); // NOI18N
        jButton6.setText("Tạm ngưng");

        jTable1.setModel(new DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                        "Mã khuyến mãi", "Tên khuyến mãi", "Số vé khả dụng", "Thời gian áp dụng", "Thời gian kết thúc ", "Chiết khấu", "Trạng thái"
                }
        ));
        jScrollPane1.setViewportView(jTable1);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
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
                                                .addGap(29, 29, 29)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addGap(29, 29, 29)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jButton3, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jButton4, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jButton5, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jButton6, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 91, Short.MAX_VALUE))
                                        .addComponent(jScrollPane1))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

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
    private JDateChooser jDateChooser1;
    private JDateChooser jDateChooser2;
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
    private JTextField jTextField3;
    private JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}