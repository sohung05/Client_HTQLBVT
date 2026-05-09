/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package iuh.fit.gui;
import service.*;
import iuh.fit.utils.ClientContext;
import entity.NhanVien;
import iuh.fit.gui.menu.form.MainForm;

import javax.swing.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author PC
 */
public class Gui_TaiKhoan extends JPanel {

    private ITaiKhoanService taiKhoanService;
    private INhanVienService nhanVienService;
    private javax.swing.table.DefaultTableModel modelTaiKhoan;
    private String maNVchon = null;
    private boolean hienMatKhau = false;
    private String matKhauGoc = "";


    /**
     * Creates new form Gui_TaiKhoan
     */
    public Gui_TaiKhoan() {
        initComponents();
        taiKhoanService = ClientContext.getTaiKhoanService();
        nhanVienService = ClientContext.getNhanVienService();
        modelTaiKhoan = (javax.swing.table.DefaultTableModel) tblTaiKhoan.getModel();
        loadData();
        addTableSelectionListener();
        txtMaNV.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                loadNhanVienInfo(txtMaNV.getText().trim());
            }
        });
        loadNhanVienInfo(txtMaNV.getText().trim());
        chkHienThiMatKhau.setSelected(true);
    }

    public Gui_TaiKhoan(String maNV) {
        this();

        txtMaNV.setText(maNV);

        loadNhanVienInfo(maNV);

        timKiemVaChonDong(maNV);
    }
    
    /**
     * Lấy mã nhân viên hiện tại đang hiển thị trên form
     * @return Mã nhân viên hoặc null
     */
    public String getMaNVHienTai() {
        String maNV = txtMaNV.getText().trim();
        return maNV.isEmpty() ? null : maNV;
    }

    private void loadData() {
        modelTaiKhoan.setRowCount(0);
        List<Object[]> ds = null;
        try {
            ds = taiKhoanService.getAll(); // Use raw for compatibility with table logic
        } catch (Exception e) {
            e.printStackTrace();
            ds = new java.util.ArrayList<>();
        }
        for (Object[] row : ds) {
            modelTaiKhoan.addRow(row);
        }
    }

    private void clearForm() {
        txtMaNV.setText("");
        txtTenNV.setText("");
        txtTenTaiKhoan.setText("");
        txtMatKhau.setText("");
        chkHienThiMatKhau.setSelected(false);
        tblTaiKhoan.clearSelection();
        maNVchon = null;

        loadNhanVienInfo(txtMaNV.getText().trim());
    }

    private entity.TaiKhoan getTaiKhoanFromForm() {
        String maNV = txtMaNV.getText().trim();
        String tenNV = txtTenNV.getText().trim();
        String tenTK = txtTenTaiKhoan.getText().trim();
        String matKhau = txtMatKhau.getText();

        if (tenTK.isEmpty() || matKhau.isEmpty() || maNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã NV, Tên tài khoản và Mật khẩu.");
            return null;
        }

        entity.TaiKhoan tk = new entity.TaiKhoan();
        tk.setMaNhanVien(maNV);
        tk.setTenTaiKhoan(tenTK);
        tk.setMatKhau(matKhau);
        // nếu entity có ngayTao: tk.setNgayTao(LocalDate.now());
        return tk;
    }

    private void addTableSelectionListener() {
        tblTaiKhoan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblTaiKhoan.getSelectedRow();
                if (row != -1) {
                    int modelRow = tblTaiKhoan.convertRowIndexToModel(row);
                    maNVchon = String.valueOf(modelTaiKhoan.getValueAt(modelRow, 0));
                    txtMaNV.setText(String.valueOf(modelTaiKhoan.getValueAt(modelRow, 0)));
                    txtTenNV.setText(String.valueOf(modelTaiKhoan.getValueAt(modelRow, 1)));
                    txtTenTaiKhoan.setText(String.valueOf(modelTaiKhoan.getValueAt(modelRow, 2)));
                    txtMatKhau.setText(String.valueOf(modelTaiKhoan.getValueAt(modelRow, 3)));

                    matKhauGoc = txtMatKhau.getText();

                    loadNhanVienInfo(txtMaNV.getText().trim());
                }
            }
        });
    }


    private void loadNhanVienInfo(String maNV) {
        txtThongTinNhanVien.setText(""); // reset
        txtTenNV.setText("");
        if (maNV == null || maNV.trim().isEmpty()) {
            txtThongTinNhanVien.setText("Vui lòng chọn nhân viên");
            return;
        }
        NhanVien nv = null;
        try {
            nv = nhanVienService.findById(maNV.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (nv == null) {
            txtThongTinNhanVien.setText("Không có nhân viên");
        } else {
            // set tạm tên lên ô tên
            txtTenNV.setText(nv.getHoTen() != null ? nv.getHoTen() : "");

            // format ngày (nếu có)
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String ngaySinh = nv.getNgaySinh() != null ? nv.getNgaySinh().format(fmt) : "N/A";
            String ngayVaoLam = nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().format(fmt) : "N/A";
            String trangThai = nv.isTrangThai() ? "Đang làm" : "Nghỉ làm";

            StringBuilder sb = new StringBuilder();
            sb.append("Mã NV: ").append(nv.getMaNhanVien()).append("\n");
            sb.append("Họ và tên: ").append(nv.getHoTen() != null ? nv.getHoTen() : "").append("\n");
            sb.append("CCCD: ").append(nv.getCCCD() != null ? nv.getCCCD() : "").append("\n");
            sb.append("Email: ").append(nv.getEmail() != null ? nv.getEmail() : "").append("\n");
            sb.append("SĐT: ").append(nv.getSDT() != null ? nv.getSDT() : "").append("\n");
            sb.append("Địa chỉ: ").append(nv.getDiaChi() != null ? nv.getDiaChi() : "").append("\n");
            sb.append("Giới tính: ").append(nv.getGioiTinh() != null ? nv.getGioiTinh() : "").append("\n");
            sb.append("Ngày sinh: ").append(ngaySinh).append("\n");
            sb.append("Ngày vào làm: ").append(ngayVaoLam).append("\n");
            sb.append("Trạng thái: ").append(trangThai).append("\n");

            txtThongTinNhanVien.setText(sb.toString());
        }
    }

    private void timKiemVaChonDong(String maNV) {
        List<Object[]> ds = null;
        try {
            ds = taiKhoanService.timKiem(maNV, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            ds = new java.util.ArrayList<>();
        }

        if (!ds.isEmpty()) {
            Object[] taiKhoanDaCo = ds.get(0);
            String tenTaiKhoanDaCo = String.valueOf(taiKhoanDaCo[2]);

            txtMaNV.setText(String.valueOf(taiKhoanDaCo[0]));
            txtTenNV.setText(String.valueOf(taiKhoanDaCo[1]));
            txtTenTaiKhoan.setText(tenTaiKhoanDaCo);
            txtMatKhau.setText(String.valueOf(taiKhoanDaCo[3]));

            matKhauGoc = txtMatKhau.getText();
            loadNhanVienInfo(maNV);

            for (int i = 0; i < modelTaiKhoan.getRowCount(); i++) {
                if (tenTaiKhoanDaCo.equals(modelTaiKhoan.getValueAt(i, 2))) { // So sánh tên tài khoản
                    tblTaiKhoan.setRowSelectionInterval(i, i);
                    tblTaiKhoan.scrollRectToVisible(tblTaiKhoan.getCellRect(i, 0, true)); // Cuộn đến dòng đó
                    break;
                }
            }

            JOptionPane.showMessageDialog(this, "Nhân viên này đã có tài khoản. Dữ liệu đã được tải lên form.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

        } else {

            loadNhanVienInfo(maNV);

            txtTenTaiKhoan.setText("");
            txtMatKhau.setText("");

            tblTaiKhoan.clearSelection();
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

        tblNhanVien = new JButton();
        btnTaiKhoan = new JButton();
        pnlThongTinTaiKhoan = new JPanel();
        lblMaNV = new JLabel();
        lblTenNV = new JLabel();
        lblTenTaiKhoan = new JLabel();
        lblMatKhau = new JLabel();
        txtMaNV = new JTextField();
        txtTenNV = new JTextField();
        txtTenTaiKhoan = new JTextField();
        txtMatKhau = new JTextField();
        chkHienThiMatKhau = new JCheckBox();
        lblTitle = new JLabel();
        scrollTaiKhoan = new JScrollPane();
        tblTaiKhoan = new JTable();
        btnTim = new JButton();
        btnXoaTrang = new JButton();
        btnThem = new JButton();
        btnCapNhat = new JButton();
        pnlThongTinNhanVien = new JPanel();
        scrollThongTinNhanVien = new JScrollPane();
        txtThongTinNhanVien = new JTextArea();

        setBackground(new java.awt.Color(234, 243, 251));

        tblNhanVien.setBackground(new java.awt.Color(102, 204, 255));
        tblNhanVien.setIcon(new ImageIcon(getClass().getResource("/icon/accountant.png"))); // NOI18N
        tblNhanVien.setText("Nhân Viên");
        tblNhanVien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tblNhanVienActionPerformed(evt);
            }
        });

        btnTaiKhoan.setBackground(new java.awt.Color(102, 204, 255));
        btnTaiKhoan.setIcon(new ImageIcon(getClass().getResource("/icon/profile.png"))); // NOI18N
        btnTaiKhoan.setText("Tài Khoản");

        pnlThongTinTaiKhoan.setBackground(new java.awt.Color(234, 243, 251));
        pnlThongTinTaiKhoan.setBorder(BorderFactory.createTitledBorder("Thông tin tài khoản"));

        lblMaNV.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblMaNV.setText("Mã nhân viên:");

        lblTenNV.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblTenNV.setText("Tên nhân viên:");

        lblTenTaiKhoan.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblTenTaiKhoan.setText("Tên tài khoản:");

        lblMatKhau.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblMatKhau.setText("Mật khẩu:");

        chkHienThiMatKhau.setText("Hiển thị mật khẩu");
        chkHienThiMatKhau.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkHienThiMatKhauActionPerformed(evt);
            }
        });

        GroupLayout pnlThongTinTaiKhoanLayout = new GroupLayout(pnlThongTinTaiKhoan);
        pnlThongTinTaiKhoan.setLayout(pnlThongTinTaiKhoanLayout);
        pnlThongTinTaiKhoanLayout.setHorizontalGroup(
                pnlThongTinTaiKhoanLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, pnlThongTinTaiKhoanLayout.createSequentialGroup()
                                .addGroup(pnlThongTinTaiKhoanLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addGroup(pnlThongTinTaiKhoanLayout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(chkHienThiMatKhau))
                                        .addGroup(pnlThongTinTaiKhoanLayout.createSequentialGroup()
                                                .addGap(20, 20, 20)
                                                .addGroup(pnlThongTinTaiKhoanLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(lblTenNV)
                                                        .addComponent(lblMaNV, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblTenTaiKhoan, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblMatKhau, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                                                .addGroup(pnlThongTinTaiKhoanLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(txtMatKhau, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtTenTaiKhoan, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtTenNV, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtMaNV, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE))))
                                .addGap(30, 30, 30))
        );
        pnlThongTinTaiKhoanLayout.setVerticalGroup(
                pnlThongTinTaiKhoanLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(pnlThongTinTaiKhoanLayout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(pnlThongTinTaiKhoanLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblMaNV)
                                        .addComponent(txtMaNV, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlThongTinTaiKhoanLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblTenNV)
                                        .addComponent(txtTenNV, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlThongTinTaiKhoanLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblTenTaiKhoan)
                                        .addComponent(txtTenTaiKhoan, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlThongTinTaiKhoanLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtMatKhau, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblMatKhau))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(chkHienThiMatKhau))
        );

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setText("Quản Lý Tài Khoản");

        tblTaiKhoan.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                        "Mã nhân viên", "Tên nhân viên", "Tên tài khoản", "Mật khẩu"
                }
        ));
        scrollTaiKhoan.setViewportView(tblTaiKhoan);

        btnTim.setIcon(new ImageIcon(getClass().getResource("/icon/TimKiem.png"))); // NOI18N
        btnTim.setText("Tìm");
        btnTim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTimActionPerformed(evt);
            }
        });

        btnXoaTrang.setIcon(new ImageIcon(getClass().getResource("/icon/clear.png"))); // NOI18N
        btnXoaTrang.setText("Xóa trắng");
        btnXoaTrang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaTrangActionPerformed(evt);
            }
        });

        btnThem.setIcon(new ImageIcon(getClass().getResource("/icon/add.png"))); // NOI18N
        btnThem.setText("Thêm");
        btnThem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemActionPerformed(evt);
            }
        });

        btnCapNhat.setIcon(new ImageIcon(getClass().getResource("/icon/update.png"))); // NOI18N
        btnCapNhat.setText("Cập nhật");
        btnCapNhat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCapNhatActionPerformed(evt);
            }
        });

        pnlThongTinNhanVien.setBackground(new java.awt.Color(234, 243, 251));
        pnlThongTinNhanVien.setBorder(BorderFactory.createTitledBorder("Thông tin nhân viên"));

        txtThongTinNhanVien.setColumns(20);
        txtThongTinNhanVien.setRows(5);
        scrollThongTinNhanVien.setViewportView(txtThongTinNhanVien);

        GroupLayout pnlThongTinNhanVienLayout = new GroupLayout(pnlThongTinNhanVien);
        pnlThongTinNhanVien.setLayout(pnlThongTinNhanVienLayout);
        pnlThongTinNhanVienLayout.setHorizontalGroup(
                pnlThongTinNhanVienLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(pnlThongTinNhanVienLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scrollThongTinNhanVien)
                                .addContainerGap())
        );
        pnlThongTinNhanVienLayout.setVerticalGroup(
                pnlThongTinNhanVienLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(scrollThongTinNhanVien)
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(tblNhanVien, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnTaiKhoan)
                                .addGap(0, 0, Short.MAX_VALUE))
                        .addComponent(lblTitle, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(scrollTaiKhoan, GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                        .addComponent(btnThem, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnTim, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE))
                                                .addGap(40, 40, 40)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                        .addComponent(btnCapNhat, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnXoaTrang))
                                                .addGap(51, 51, 51))
                                        .addComponent(pnlThongTinTaiKhoan, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(pnlThongTinNhanVien, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addContainerGap())))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(tblNhanVien, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnTaiKhoan, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(pnlThongTinTaiKhoan, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(btnTim, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnXoaTrang, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(btnThem, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnCapNhat, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(pnlThongTinNhanVien, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(scrollTaiKhoan, GroupLayout.PREFERRED_SIZE, 629, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(22, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tblNhanVienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tblNhanVienActionPerformed
        // TODO add your handling code here
        MainForm main = (MainForm) SwingUtilities.getAncestorOfClass(MainForm.class, this);
        if (main != null) {
            main.showForm(new Gui_NhanVien());
        }
    }

    private void btnTimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimActionPerformed
        String maNV = txtMaNV.getText().trim();
        String tenTK = txtTenTaiKhoan.getText().trim();
        String tenNV = txtTenNV.getText().trim();

        List<Object[]> ds = null;
        try {
            ds = taiKhoanService.timKiem(maNV, tenTK, tenNV);
        } catch (Exception e) {
            e.printStackTrace();
            ds = new java.util.ArrayList<>();
        }
        modelTaiKhoan.setRowCount(0);
        for (Object[] r : ds) {
            modelTaiKhoan.addRow(new Object[] {
                    r[0],                       // maNhanVien
                    r[1] != null ? r[1] : "",   // hoTen
                    r[2],                       // tenTaiKhoan
                    r[3]                        // matKhau
            });
        }

    }

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        entity.TaiKhoan tk = getTaiKhoanFromForm();
        if (tk == null) return;

        // Kiểm tra trống
        if (tk.getMaNhanVien().isEmpty() || tk.getTenTaiKhoan().isEmpty() || tk.getMatKhau().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // KIỂM TRA NHÂN VIÊN ĐÃ CÓ TÀI KHOẢN CHƯA
        boolean existsMaNV = false;
        try {
            existsMaNV = taiKhoanService.kiemTraTonTaiTheoMaNV(tk.getMaNhanVien());
        } catch (Exception e) { e.printStackTrace(); }
        
        if (existsMaNV) {
            JOptionPane.showMessageDialog(this, "Nhân viên này đã có tài khoản!");
            return;
        }

        // KIỂM TRA TÊN TÀI KHOẢN ĐÃ TỒN TẠI CHƯA (Tránh lỗi PK Violation)
        boolean existsTenTK = false;
        String newTenTK = tk.getTenTaiKhoan();
        for (int i = 0; i < modelTaiKhoan.getRowCount(); i++) {
            if (newTenTK.equalsIgnoreCase(modelTaiKhoan.getValueAt(i, 2).toString())) {
                existsTenTK = true;
                break;
            }
        }

        if (existsTenTK) {
            JOptionPane.showMessageDialog(this, "Tên tài khoản này đã tồn tại! Vui lòng chọn tên khác.");
            return;
        }

        boolean ok = false;
        try {
            ok = taiKhoanService.them(tk);
        } catch (Exception e) { e.printStackTrace(); }
        if (ok) {
            JOptionPane.showMessageDialog(this, "Thêm tài khoản thành công!");
            loadData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại! Kiểm tra tên tài khoản hoặc mã nhân viên đã tồn tại.");
        }
    }

    private void btnXoaTrangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaTrangActionPerformed
        clearForm();
        loadData();
    }

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCapNhatActionPerformed
        entity.TaiKhoan tk = getTaiKhoanFromForm();
        if (tk == null) return;

        // Nếu bạn muốn ưu tiên mã NV được chọn từ bảng:
        if (maNVchon != null && !maNVchon.isEmpty()) {
            tk.setMaNhanVien(maNVchon);
        }

        boolean ok = false;
        try {
            ok = taiKhoanService.sua(tk);
        } catch (Exception e) { e.printStackTrace(); }
        if (ok) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
        }
    }

    private void chkHienThiMatKhauActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkHienThiMatKhauActionPerformed
        hienMatKhau = chkHienThiMatKhau.isSelected();

        if (hienMatKhau) {
            // Khi chọn hiển thị, dùng mật khẩu gốc đã lưu từ lúc load/chọn dòng
            txtMatKhau.setText(matKhauGoc);
        } else {
            if (matKhauGoc == null || matKhauGoc.isEmpty()) {
                matKhauGoc = txtMatKhau.getText();
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < matKhauGoc.length(); i++) {
                sb.append("*");
            }
            txtMatKhau.setText(sb.toString());
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnCapNhat;
    private JButton btnTaiKhoan;
    private JButton btnThem;
    private JButton btnTim;
    private JButton btnXoaTrang;
    private JCheckBox chkHienThiMatKhau;
    private JLabel lblMaNV;
    private JLabel lblMatKhau;
    private JLabel lblTenNV;
    private JLabel lblTenTaiKhoan;
    private JLabel lblTitle;
    private JPanel pnlThongTinNhanVien;
    private JPanel pnlThongTinTaiKhoan;
    private JScrollPane scrollTaiKhoan;
    private JScrollPane scrollThongTinNhanVien;
    private JButton tblNhanVien;
    private JTable tblTaiKhoan;
    private JTextField txtMaNV;
    private JTextField txtMatKhau;
    private JTextField txtTenNV;
    private JTextField txtTenTaiKhoan;
    private JTextArea txtThongTinNhanVien;
    // End of variables declaration//GEN-END:variables
}