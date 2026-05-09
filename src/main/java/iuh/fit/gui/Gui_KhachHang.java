/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package iuh.fit.gui;

import service.IKhachHangService;
import iuh.fit.utils.ClientContext;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Objects;
import iuh.fit.utils.JTableExporter;

/**
 *
 * @author PC
 */
public class Gui_KhachHang extends JPanel {

    private DefaultTableModel modelKhachHang;
    private IKhachHangService khachHangService;
    private String maKHChon = null;

    /**
     * Creates new form Gui_KhachHang
     */
    public Gui_KhachHang() {
        khachHangService = ClientContext.getKhachHangService();
        initComponents();
        modelKhachHang = (DefaultTableModel) tblKhachHang.getModel();
        loadData();
        addTableSelectionListener();
    }

    // Sự kiện chọn dòng trong bảng để hiển thị dữ liệu lên form
    private void addTableSelectionListener() {
        tblKhachHang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblKhachHang.getSelectedRow();
                if (row != -1) {
                    int modelRow = tblKhachHang.convertRowIndexToModel(row);
                    
                    // Kiểm tra null trước khi gọi toString()
                    Object maKH = modelKhachHang.getValueAt(modelRow, 0);
                    maKHChon = maKH != null ? maKH.toString() : "";

                    Object cccd = modelKhachHang.getValueAt(modelRow, 4);
                    txtCCCD.setText(cccd != null ? cccd.toString() : "");

                    Object hoTen = modelKhachHang.getValueAt(modelRow, 1);
                    txtHoTen.setText(hoTen != null ? hoTen.toString() : "");

                    Object email = modelKhachHang.getValueAt(modelRow, 2);
                    txtEmail.setText(email != null ? email.toString() : "");

                    Object sdt = modelKhachHang.getValueAt(modelRow, 3);
                    txtSoDienThoai.setText(sdt != null ? sdt.toString() : "");

                    Object doiTuong = modelKhachHang.getValueAt(modelRow, 5);
                    cmbDoiTuong.setSelectedItem(doiTuong != null ? doiTuong.toString() : "");
                }
            }
        });
    }
    private void loadData() {
        modelKhachHang.setRowCount(0);
        try {
            List<KhachHang> ds = khachHangService.getAll();
            for (KhachHang kh : ds) {
                modelKhachHang.addRow(new Object[]{
                        kh.getMaKH(),
                        kh.getHoTen(),
                        kh.getEmail(),
                        kh.getSDT(),
                        kh.getCCCD(),
                        kh.getDoiTuong()
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }


    private void clearForm() {
        txtCCCD.setText("");
        txtHoTen.setText("");
        txtEmail.setText("");
        txtSoDienThoai.setText("");
        cmbDoiTuong.setSelectedIndex(0);
        tblKhachHang.clearSelection();
    }

    private boolean isValidCCCD(String cccd) {
        return cccd.matches("\\d{9,13}");
    }

    private boolean isValidHoTen(String hoTen) {
        return hoTen.matches("([\\p{Lu}][\\p{Ll}\\s]*)+");
    }

    private boolean isValidEmail(String email) {
        return email.length() <= 30 && email.matches("[A-Za-z0-9]+@gmail\\.com");
    }

    private boolean isValidSDT(String sdt) {
        return sdt.matches("\\d{10}");
    }

    private String generateMaKH(String cccd) {
        // Lấy 4 số cuối CCCD
        String base = "KH" + cccd.substring(Math.max(0, cccd.length() - 4));
        String ma = base;
        int i = 1;
        try {
            while (khachHangService.exists(ma)) {
                ma = base + "_" + i;
                i++;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ma;
    }


    private KhachHang getKhachHangFromForm() {
        String cccd = txtCCCD.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String email = txtEmail.getText().trim();
        String soDienThoai = txtSoDienThoai.getText().trim();
        String doiTuong = Objects.requireNonNull(cmbDoiTuong.getSelectedItem()).toString();

        // Validate CCCD
        if (!isValidCCCD(cccd)) {
            JOptionPane.showMessageDialog(this, "CCCD phải là 9-13 số và không được trống!");
            return null;
        }

        // Validate Họ tên
        if (!isValidHoTen(hoTen)) {
            JOptionPane.showMessageDialog(this, "Họ tên không được trống và mỗi chữ cái đầu phải viết hoa!");
            return null;
        }

        // Validate Email
        if (!email.isEmpty() && !isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Email không quá 30 ký tự và phải kết thúc bằng @gmail.com!");
            return null;
        }

        // Validate SDT
        if (!isValidSDT(soDienThoai)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải đủ 10 số và không được trống!");
            return null;
        }

        // Validate Đối tượng
        if (cmbDoiTuong.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đối tượng khách hàng!");
            return null;
        }

        String maKH;
        if (maKHChon != null) {
            maKH = maKHChon;
        } else {
            maKH = generateMaKH(cccd);
        }

        return new KhachHang(maKH, cccd, hoTen, email, soDienThoai, doiTuong);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new JLabel();
        lblTitle = new JLabel();
        scrollKhachHang = new JPanel();
        lblCCCD = new JLabel();
        lblHoTen = new JLabel();
        lblEmail = new JLabel();
        lblSoDienThoai = new JLabel();
        lblDoiTuong = new JLabel();
        txtCCCD = new JTextField();
        txtHoTen = new JTextField();
        txtEmail = new JTextField();
        txtSoDienThoai = new JTextField();
        cmbDoiTuong = new JComboBox<>();
        pnlThongTin = new JScrollPane();
        tblKhachHang = new JTable();
        btnCapNhat = new JButton();
        btnTimKiem = new JButton();
        btnXoaTrang = new JButton();
        btnXuatExcel = new JButton();

        jLabel2.setText("jLabel2");

        setBackground(new java.awt.Color(234, 243, 251));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setText("Quản Lý Khách Hàng");

        scrollKhachHang.setBackground(new java.awt.Color(234, 243, 251));
        scrollKhachHang.setBorder(BorderFactory.createTitledBorder("Thông tin khách hàng"));

        lblCCCD.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblCCCD.setText("CCCD:");

        lblHoTen.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblHoTen.setText("Họ và tên:");

        lblEmail.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblEmail.setText("Email:");

        lblSoDienThoai.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblSoDienThoai.setText("Số điện thoại:");

        lblDoiTuong.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblDoiTuong.setText("Đối tượng:");

        cmbDoiTuong.setModel(new DefaultComboBoxModel<>(new String[] { "  ", "Trẻ em", "Người lớn", "Sinh viên", "Người cao tuổi" }));

        GroupLayout scrollKhachHangLayout = new GroupLayout(scrollKhachHang);
        scrollKhachHang.setLayout(scrollKhachHangLayout);
        scrollKhachHangLayout.setHorizontalGroup(
                scrollKhachHangLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(scrollKhachHangLayout.createSequentialGroup()
                                .addGap(55, 55, 55)
                                .addComponent(lblHoTen, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(scrollKhachHangLayout.createSequentialGroup()
                                .addGroup(scrollKhachHangLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(scrollKhachHangLayout.createSequentialGroup()
                                                .addGap(55, 55, 55)
                                                .addGroup(scrollKhachHangLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(lblEmail, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblCCCD)
                                                        .addComponent(lblDoiTuong)))
                                        .addGroup(scrollKhachHangLayout.createSequentialGroup()
                                                .addGap(53, 53, 53)
                                                .addComponent(lblSoDienThoai)))
                                .addGap(56, 56, 56)
                                .addGroup(scrollKhachHangLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(scrollKhachHangLayout.createSequentialGroup()
                                                .addGroup(scrollKhachHangLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(txtHoTen, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtCCCD, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtEmail, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtSoDienThoai, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE))
                                                .addGap(33, 39, Short.MAX_VALUE))
                                        .addGroup(scrollKhachHangLayout.createSequentialGroup()
                                                .addComponent(cmbDoiTuong, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))))
        );
        scrollKhachHangLayout.setVerticalGroup(
                scrollKhachHangLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(scrollKhachHangLayout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(scrollKhachHangLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblCCCD)
                                        .addComponent(txtCCCD, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(scrollKhachHangLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtHoTen, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblHoTen))
                                .addGap(22, 22, 22)
                                .addGroup(scrollKhachHangLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblEmail)
                                        .addComponent(txtEmail, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(scrollKhachHangLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblSoDienThoai)
                                        .addComponent(txtSoDienThoai, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(27, 27, 27)
                                .addGroup(scrollKhachHangLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblDoiTuong)
                                        .addComponent(cmbDoiTuong, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(26, Short.MAX_VALUE))
        );

        tblKhachHang.setModel(new DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                        "Mã khách hàng", "Họ tên", "Email", "Số điện thoại", "CCCD", "Đối tượng"
                }
        ));
        pnlThongTin.setViewportView(tblKhachHang);

        btnCapNhat.setIcon(new ImageIcon(getClass().getResource("/icon/update.png"))); // NOI18N
        btnCapNhat.setText("Cập nhật");
        btnCapNhat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCapNhatActionPerformed(evt);
            }
        });

        btnTimKiem.setIcon(new ImageIcon(getClass().getResource("/icon/TimKiem.png"))); // NOI18N
        btnTimKiem.setText("Tìm kiếm");
        btnTimKiem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTimKiemActionPerformed(evt);
            }
        });

        btnXoaTrang.setIcon(new ImageIcon(getClass().getResource("/icon/clear.png"))); // NOI18N
        btnXoaTrang.setText("Xóa trắng");
        btnXoaTrang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaTrangActionPerformed(evt);
            }
        });

        btnXuatExcel.setIcon(new ImageIcon(getClass().getResource("/icon/excel.png"))); // NOI18N
        btnXuatExcel.setText("Xuất Excel");
        btnXuatExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXuatExcelActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(lblTitle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(pnlThongTin, GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(scrollKhachHang, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(74, 74, 74)
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(btnTimKiem)
                                                                        .addComponent(btnCapNhat))
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(btnXoaTrang, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                        .addComponent(btnXuatExcel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                .addGap(48, 48, 48)))))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(scrollKhachHang, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(btnTimKiem, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnXoaTrang, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
                                                .addGap(26, 26, 26)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(btnCapNhat, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnXuatExcel, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 138, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(pnlThongTin)
                                                .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnXoaTrangActionPerformed(java.awt.event.ActionEvent evt) {
        clearForm();
        loadData();
    }

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {
        String cccd = txtCCCD.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSoDienThoai.getText().trim();
        String doiTuong = (String) cmbDoiTuong.getSelectedItem();

        if (doiTuong != null && doiTuong.trim().isEmpty()) {
            doiTuong = null; // bỏ qua nếu người dùng để trống
        }

        // Gọi Service để tìm kiếm
        try {
            List<KhachHang> ds = khachHangService.timKiem(cccd, hoTen, email, sdt, doiTuong);

            // Cập nhật lại bảng
            DefaultTableModel model = (DefaultTableModel) tblKhachHang.getModel();
            model.setRowCount(0); // xóa dữ liệu cũ

            for (KhachHang kh : ds) {
                model.addRow(new Object[]{
                        kh.getMaKH(),
                        kh.getHoTen(),
                        kh.getEmail(),
                        kh.getSDT(),
                        kh.getCCCD(),
                        kh.getDoiTuong()
                });
            }

            // Nếu không có kết quả
            if (ds.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng phù hợp!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {
        KhachHang kh = getKhachHangFromForm();
        if (kh != null) {
            try {
                if (khachHangService.sua(kh)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng để cập nhật!");
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void btnXuatExcelActionPerformed(java.awt.event.ActionEvent evt) {
        JTableExporter.exportJTableToExcel(tblKhachHang);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnCapNhat;
    private JButton btnTimKiem;
    private JButton btnXoaTrang;
    private JButton btnXuatExcel;
    private JComboBox<String> cmbDoiTuong;
    private JLabel jLabel2;
    private JLabel lblCCCD;
    private JLabel lblDoiTuong;
    private JLabel lblEmail;
    private JLabel lblHoTen;
    private JLabel lblSoDienThoai;
    private JLabel lblTitle;
    private JScrollPane pnlThongTin;
    private JPanel scrollKhachHang;
    private JTable tblKhachHang;
    private JTextField txtCCCD;
    private JTextField txtEmail;
    private JTextField txtHoTen;
    private JTextField txtSoDienThoai;
    // End of variables declaration//GEN-END:variables
}