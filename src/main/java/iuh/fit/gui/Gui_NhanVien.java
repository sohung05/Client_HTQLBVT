/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package iuh.fit.gui;
import service.*;
import iuh.fit.utils.ClientContext;
import entity.NhanVien;
import iuh.fit.gui.menu.form.MainForm;
import iuh.fit.utils.JTableExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author PC
 */
public class Gui_NhanVien extends JPanel {

    private DefaultTableModel modelNhanVien;
    private INhanVienService nhanVienService;
    private ITaiKhoanService taiKhoanService;
    private String maNVChon = null;

    /**
     * Creates new form Gui_NhanVien
     */
    public Gui_NhanVien() {
        initComponents();
        nhanVienService = ClientContext.getNhanVienService();
        taiKhoanService = ClientContext.getTaiKhoanService();
        modelNhanVien = (DefaultTableModel) tblNhanVien.getModel();
        loadData();
        addTableSelectionListener();
        addRealTimeMaNVListener();
        
        // ⚡ Không cho phép nhập mã nhân viên thủ công (tự động sinh)
        txtMaNV.setEditable(false);
    }

    private void addRealTimeMaNVListener() {
        // Lắng nghe sự thay đổi ngày sinh để tự sinh mã NV
        dcNgaySinh.addPropertyChangeListener("date", evt -> {
            // Chỉ tự sinh mã khi đang thêm mới (maNVChon == null) và đã chọn ngày sinh
            if (maNVChon == null && dcNgaySinh.getDate() != null) {
                Date d = dcNgaySinh.getDate();
                LocalDate ngaySinh = Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                
                // Mặc định ngày vào làm là hôm nay cho nhân viên mới
                LocalDate ngayVaoLam = LocalDate.now();
                
                String maDuKien = "";
                try {
                    maDuKien = nhanVienService.generateMaNhanVien(ngayVaoLam, ngaySinh);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                txtMaNV.setText(maDuKien);
            }
        });
    }

    private void addTableSelectionListener() {
        tblNhanVien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblNhanVien.getSelectedRow();
                if (row != -1) {
                    int modelRow = tblNhanVien.convertRowIndexToModel(row);
                    maNVChon = modelNhanVien.getValueAt(modelRow, 0).toString(); // mã NV
                    txtCCCD.setText(String.valueOf(modelNhanVien.getValueAt(modelRow, 1)));
                    txtHoTen.setText(String.valueOf(modelNhanVien.getValueAt(modelRow, 2)));
                    // Ngày sinh có thể là null hoặc dạng LocalDate -> hiển thị lên JDateChooser
                    Object ngaySinhObj = modelNhanVien.getValueAt(modelRow, 3);
                    if (ngaySinhObj instanceof LocalDate) {
                        LocalDate ld = (LocalDate) ngaySinhObj;
                        Date d = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        dcNgaySinh.setDate(d);
                    } else {
                        dcNgaySinh.setDate(null);
                    }
                    String gioiTinh = String.valueOf(modelNhanVien.getValueAt(modelRow, 4));
                    if ("Nam".equalsIgnoreCase(gioiTinh)) {
                        radNam.setSelected(true);
                        radNu.setSelected(false);
                    } else {
                        radNu.setSelected(true);
                        radNam.setSelected(false);
                    }
                    txtDiaChi.setText(String.valueOf(modelNhanVien.getValueAt(modelRow, 5)));
                    txtEmail.setText(String.valueOf(modelNhanVien.getValueAt(modelRow, 6)));
                    txtSoDienThoai.setText(String.valueOf(modelNhanVien.getValueAt(modelRow, 7)));
                    cmbTrangThai.setSelectedItem(String.valueOf(modelNhanVien.getValueAt(modelRow, 8)));
                    txtMaNV.setText(maNVChon);
                }
            }
        });
    }

    private void loadData() {
        modelNhanVien.setRowCount(0);
        List<NhanVien> ds = null;
        try {
            ds = nhanVienService.getAll();
        } catch (Exception e) {
            e.printStackTrace();
            ds = new java.util.ArrayList<>();
        }
        for (NhanVien nv : ds) {
            modelNhanVien.addRow(new Object[]{
                    nv.getMaNhanVien(),
                    nv.getCCCD(),
                    nv.getHoTen(),
                    nv.getNgaySinh(),    // LocalDate (DAO đã trả về LocalDate)
                    nv.getGioiTinh(),
                    nv.getDiaChi(),
                    nv.getEmail(),
                    nv.getSDT(),
                    nv.isTrangThai() ? "Đang làm" : "Nghỉ làm"
            });
        }
    }

    private void clearForm() {
        txtMaNV.setText("");
        txtCCCD.setText("");
        txtHoTen.setText("");
        dcNgaySinh.setDate(null);
        radNam.setSelected(false);
        radNu.setSelected(false);
        txtDiaChi.setText("");
        txtEmail.setText("");
        txtSoDienThoai.setText("");
        cmbTrangThai.setSelectedIndex(0);
        tblNhanVien.clearSelection();
        maNVChon = null;
    }

    private boolean kiemTraHopLe() {
        String cccd = txtCCCD.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSoDienThoai.getText().trim();

        // 1. Kiểm tra CCCD
        if (cccd.isEmpty() || cccd.length() > 12) { // Ràng buộc: Bé hơn 13 số -> 9 hoặc 12 số
            JOptionPane.showMessageDialog(this, "CCCD không hợp lệ! Vui lòng nhập CMND/CCCD (tối đa 12 số).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtCCCD.requestFocus();
            return false;
        }
        if (!cccd.matches("\\d+")) { // Chỉ chấp nhận ký tự số
            JOptionPane.showMessageDialog(this, "CCCD chỉ được chứa ký tự số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtCCCD.requestFocus();
            return false;
        }

        // 2. Kiểm tra Họ tên
        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên không được rỗng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtHoTen.requestFocus();
            return false;
        }
        // Ràng buộc: Viết hoa chữ cái đầu tiên (Sẽ xử lý format sau khi kiểm tra rỗng)

        // 3. Kiểm tra Ngày sinh (không bắt buộc, nhưng nên có)
        if (dcNgaySinh.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Ngày sinh!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 4. Kiểm tra Giới tính
        if (!radNam.isSelected() && !radNu.isSelected()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Giới tính!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // 5. Kiểm tra Số điện thoại
        if (sdt.isEmpty() || !sdt.matches("0\\d{9}")) { // Ràng buộc: 10 ký tự số, không rỗng (bắt đầu bằng 0)
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ! Phải đủ 10 ký tự số và bắt đầu bằng 0.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtSoDienThoai.requestFocus();
            return false;
        }

        // 6. Kiểm tra Email
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email không được rỗng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }
        // Ràng buộc: Không quá 30 ký tự, không chứa ký tự đặc biệt (ngoại trừ . và @)
        if (email.length() > 30 || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ! (Không quá 30 ký tự, phải theo định dạng email).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }

        // 7. Kiểm tra Địa chỉ
        if (diaChi.isEmpty() || diaChi.length() > 50) {
            JOptionPane.showMessageDialog(this, "Địa chỉ không được rỗng và không quá 50 ký tự!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtDiaChi.requestFocus();
            return false;
        }
        if (!diaChi.matches("^[\\p{L}0-9\\s,./-]+$")) {
            JOptionPane.showMessageDialog(this, "Địa chỉ chứa ký tự đặc biệt không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtDiaChi.requestFocus();
            return false;
        }

        // 8. Kiểm tra Trạng thái
        if (cmbTrangThai.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Trạng thái!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private NhanVien getNhanVienFromForm() {
        // Bỏ qua kiểm tra CCCD, Họ tên, Giới tính ở đây vì đã có kiemTraHopLe()

        // Kiểm tra hợp lệ TỔNG THỂ trước khi xử lý dữ liệu
        if (!kiemTraHopLe()) {
            return null;
        }

        String maNV = txtMaNV.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        Date d = dcNgaySinh.getDate();
        LocalDate ngaySinh = null;
        if (d != null) {
            ngaySinh = Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String gioiTinh = radNam.isSelected() ? "Nam" : "Nữ"; // Đã kiểm tra null ở kiemTraHopLe()
        String diaChi = txtDiaChi.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSoDienThoai.getText().trim();
        String trangThaiStr = Objects.requireNonNull(cmbTrangThai.getSelectedItem()).toString();
        boolean trangThai = "Đang làm".equalsIgnoreCase(trangThaiStr);


        // Nếu là thao tác Thêm, mã NV sẽ được phát sinh trong btnThemActionPerformed
        if (maNV.isEmpty()) maNV = null;

        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(maNV); // Sẽ được gán lại nếu là Thêm mới
        nv.setCCCD(cccd);
        nv.setHoTen(hoTen);
        nv.setSDT(sdt);
        nv.setEmail(email);
        nv.setDiaChi(diaChi);
        nv.setChucVu(1); // 1 = Nhân viên (mặc định)
        nv.setTrangThai(trangThai);
        nv.setNgaySinh(ngaySinh);
        nv.setNgayVaoLam(null);
        nv.setGioiTinh(gioiTinh);

        return nv;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Vien = new JButton();
        btnTaiKhoan = new JButton();
        pnlThongTin = new JPanel();
        lblMaNV = new JLabel();
        lblCCCD = new JLabel();
        lblHoTen = new JLabel();
        lblNgaySinh = new JLabel();
        lblGioiTinh = new JLabel();
        lblDiaChi = new JLabel();
        lblEmail = new JLabel();
        lblSoDienThoai = new JLabel();
        lblTrangThai = new JLabel();
        txtMaNV = new JTextField();
        dcNgaySinh = new com.toedter.calendar.JDateChooser();
        radNam = new JRadioButton();
        radNu = new JRadioButton();
        txtCCCD = new JTextField();
        txtHoTen = new JTextField();
        cmbTrangThai = new JComboBox<>();
        txtDiaChi = new JTextField();
        txtEmail = new JTextField();
        txtSoDienThoai = new JTextField();
        lblTitle = new JLabel();
        scrollNhanVien = new JScrollPane();
        tblNhanVien = new JTable();
        btnTimKiem = new JButton();
        btnXoaTrang = new JButton();
        btnThem = new JButton();
        btnCapNhat = new JButton();
        btnXuatExcel = new JButton();
        btnTaoTaiKhoan = new JButton();

        setBackground(new java.awt.Color(234, 243, 251));

        Vien.setBackground(new java.awt.Color(102, 204, 255));
        Vien.setIcon(new ImageIcon(getClass().getResource("/icon/accountant.png"))); // NOI18N
        Vien.setText("Nhân Viên");

        btnTaiKhoan.setBackground(new java.awt.Color(102, 204, 255));
        btnTaiKhoan.setIcon(new ImageIcon(getClass().getResource("/icon/profile.png"))); // NOI18N
        btnTaiKhoan.setText("Tài Khoản");
        btnTaiKhoan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTaiKhoanActionPerformed(evt);
            }
        });

        pnlThongTin.setBackground(new java.awt.Color(234, 243, 251));
        pnlThongTin.setBorder(BorderFactory.createTitledBorder("Thông tin nhân viên"));
        pnlThongTin.setMaximumSize(new java.awt.Dimension(0, 0));

        lblMaNV.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblMaNV.setText("Mã nhân viên:");

        lblCCCD.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblCCCD.setText("CCCD:\n");

        lblHoTen.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblHoTen.setText("Họ tên:");

        lblNgaySinh.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblNgaySinh.setText("Ngày sinh:");

        lblGioiTinh.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblGioiTinh.setText("Giới tính:");

        lblDiaChi.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblDiaChi.setText("Địa chỉ:");

        lblEmail.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblEmail.setText("Email:\n");

        lblSoDienThoai.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblSoDienThoai.setText("Số điện thoại:");

        lblTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        lblTrangThai.setText("Trạng thái:\n");

        radNam.setText("Nam");

        radNu.setText("Nữ");

        cmbTrangThai.setModel(new DefaultComboBoxModel<>(new String[] { " ", "Đang làm", "Nghỉ làm" }));

        GroupLayout pnlThongTinLayout = new GroupLayout(pnlThongTin);
        pnlThongTin.setLayout(pnlThongTinLayout);
        pnlThongTinLayout.setHorizontalGroup(
                pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, pnlThongTinLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(lblGioiTinh)
                                        .addGroup(pnlThongTinLayout.createSequentialGroup()
                                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(lblSoDienThoai)
                                                        .addComponent(lblTrangThai)
                                                        .addComponent(lblEmail)
                                                        .addComponent(lblDiaChi))
                                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(pnlThongTinLayout.createSequentialGroup()
                                                                .addGap(59, 59, 59)
                                                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(txtEmail, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(txtDiaChi, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE))
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addGroup(GroupLayout.Alignment.TRAILING, pnlThongTinLayout.createSequentialGroup()
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(txtSoDienThoai, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(cmbTrangThai, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)))))
                                        .addGroup(pnlThongTinLayout.createSequentialGroup()
                                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(lblCCCD)
                                                        .addComponent(lblMaNV)
                                                        .addComponent(lblHoTen)
                                                        .addComponent(lblNgaySinh))
                                                .addGap(62, 62, 62)
                                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(dcNgaySinh, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtMaNV, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtHoTen, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtCCCD, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(pnlThongTinLayout.createSequentialGroup()
                                                                .addComponent(radNam)
                                                                .addGap(52, 52, 52)
                                                                .addComponent(radNu)))))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlThongTinLayout.setVerticalGroup(
                pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(pnlThongTinLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblMaNV)
                                        .addComponent(txtMaNV, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblCCCD)
                                        .addComponent(txtCCCD, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(16, 16, 16)
                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(lblHoTen)
                                        .addComponent(txtHoTen, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(pnlThongTinLayout.createSequentialGroup()
                                                .addComponent(lblNgaySinh)
                                                .addGap(22, 22, 22)
                                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblGioiTinh)
                                                        .addComponent(radNam)
                                                        .addComponent(radNu))
                                                .addGap(18, 18, 18)
                                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblDiaChi)
                                                        .addComponent(txtDiaChi, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblEmail)
                                                        .addComponent(txtEmail, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(dcNgaySinh, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(lblSoDienThoai)
                                        .addComponent(txtSoDienThoai, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlThongTinLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(lblTrangThai)
                                        .addComponent(cmbTrangThai, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(19, Short.MAX_VALUE))
        );

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setText("Quản Lý Nhân Viên");

        tblNhanVien.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        tblNhanVien.setModel(new DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                        "Mã nhân viên", "CCCD", "Họ tên", "Ngày sinh", "Giới tính", "Địa chỉ ", "Email", "Số điện thoại", "Trạng thái"
                }
        ));
        scrollNhanVien.setViewportView(tblNhanVien);

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

        btnXuatExcel.setIcon(new ImageIcon(getClass().getResource("/icon/excel.png"))); // NOI18N
        btnXuatExcel.setText("Xuất Excel");
        btnXuatExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXuatExcelActionPerformed(evt);
            }
        });

        btnTaoTaiKhoan.setIcon(new ImageIcon(getClass().getResource("/icon/create.png"))); // NOI18N
        btnTaoTaiKhoan.setText("Tạo tài khoản");
        btnTaoTaiKhoan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTaoTaiKhoanActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addComponent(Vien, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnTaiKhoan)
                                .addGap(0, 0, Short.MAX_VALUE))
                        .addComponent(lblTitle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(scrollNhanVien, GroupLayout.DEFAULT_SIZE, 935, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                .addComponent(btnThem, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(btnTimKiem, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(btnXuatExcel, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE))
                                                .addGap(31, 31, 31)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(btnCapNhat, GroupLayout.PREFERRED_SIZE, 138, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnXoaTrang, GroupLayout.PREFERRED_SIZE, 138, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnTaoTaiKhoan))
                                                .addGap(18, 18, 18))
                                        .addComponent(pnlThongTin, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnTaiKhoan, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(Vien, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(pnlThongTin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(btnTimKiem, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnXoaTrang, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addGap(14, 14, 14)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(btnCapNhat, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnThem, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(btnTaoTaiKhoan, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnXuatExcel, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 12, Short.MAX_VALUE))
                                        .addComponent(scrollNhanVien))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnTaiKhoanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTaiKhoanActionPerformed
        // TODO add your handling code here:
        MainForm main = (MainForm) SwingUtilities.getAncestorOfClass(MainForm.class, this);
        if (main != null) {
            // Hiển thị giao diện tài khoản
            main.showForm(new Gui_TaiKhoan());
        }
    }//GEN-LAST:event_btnTaiKhoanActionPerformed

    private void btnTimKiemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimKiemActionPerformed
        String maNV = txtMaNV.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSoDienThoai.getText().trim();

        String trangThai = null;
        if (cmbTrangThai.getSelectedIndex() > 0)
            trangThai = cmbTrangThai.getSelectedItem().toString();

        String gioiTinh = null;
        if (radNam.isSelected()) gioiTinh = "Nam";
        else if (radNu.isSelected()) gioiTinh = "Nữ";

        // Lấy ngày sinh (nếu có)
        LocalDate ngaySinh = null;
        if (dcNgaySinh.getDate() != null) {
            ngaySinh = dcNgaySinh.getDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        }

        // Gọi DAO timKiem
        List<NhanVien> ds = null;
        try {
            ds = nhanVienService.timKiem(null, null, hoTen, null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            ds = new java.util.ArrayList<>();
        }

        // Cập nhật bảng
        DefaultTableModel model = (DefaultTableModel) tblNhanVien.getModel();
        model.setRowCount(0);
        for (NhanVien nv : ds) {
            model.addRow(new Object[]{
                    nv.getMaNhanVien(),
                    nv.getCCCD(),
                    nv.getHoTen(),
                    nv.getNgaySinh(),
                    nv.getGioiTinh(),
                    nv.getDiaChi(),
                    nv.getEmail(),
                    nv.getSDT(),
                    nv.isTrangThai() ? "Đang làm" : "Nghỉ làm"
            });
        }

        if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên phù hợp!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        // 1. Kiểm tra nếu đang chọn một nhân viên từ bảng (tránh thêm trùng khi đang xem chi tiết)
        if (maNVChon != null && !maNVChon.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nhân viên này đã tồn tại trong hệ thống!\nNếu bạn muốn thay đổi thông tin, hãy nhấn nút 'Cập nhật'.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        NhanVien nv = getNhanVienFromForm();
        if (nv != null) {
            // 2. Kiểm tra trùng CCCD trong cơ sở dữ liệu (Local check for compatibility)
            boolean exists = false;
            for (int i = 0; i < modelNhanVien.getRowCount(); i++) {
                if (nv.getCCCD().equals(modelNhanVien.getValueAt(i, 1).toString())) {
                    exists = true;
                    break;
                }
            }
            
            if (exists) {
                JOptionPane.showMessageDialog(this,
                        "❌ Lỗi: Số CCCD này đã tồn tại trong hệ thống!",
                        "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                txtCCCD.requestFocus();
                return;
            }

            if (nv.getNgayVaoLam() == null) {
                nv.setNgayVaoLam(LocalDate.now());
            }

            String maMoi = "";
            try {
                maMoi = nhanVienService.generateMaNhanVien(nv.getNgayVaoLam(), nv.getNgaySinh());
            } catch (Exception e) { e.printStackTrace(); }
            nv.setMaNhanVien(maMoi);

            boolean ok = false;
            try {
                ok = nhanVienService.them(nv);
            } catch (Exception e) { e.printStackTrace(); }
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "✅ Thêm nhân viên thành công!\nMã nhân viên mới: " + nv.getMaNhanVien());
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Thêm thất bại! Kiểm tra lại kết nối hoặc dữ liệu.");
            }
        }
    }

    private void btnXoaTrangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaTrangActionPerformed
        clearForm();
        loadData();
    }

    private void btnCapNhatActionPerformed(java.awt.event.ActionEvent evt) {
        NhanVien nv = getNhanVienFromForm();
        if (nv != null) {
            // 🚫 Không cho phép cập nhật mã — chỉ cập nhật nhân viên đang được chọn
            if (maNVChon == null || maNVChon.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 nhân viên trong bảng để cập nhật.");
                return;
            }

            // Luôn gán mã của nhân viên đang được chọn
            nv.setMaNhanVien(maNVChon);

            boolean ok = false;
            try {
                ok = nhanVienService.sua(nv);
            } catch (Exception e) { e.printStackTrace(); }
            if (ok) {
                JOptionPane.showMessageDialog(this, "✅ Cập nhật thông tin nhân viên thành công!");
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Cập nhật thất bại! Kiểm tra lại dữ liệu hoặc kết nối cơ sở dữ liệu.");
            }
        }

    }

    private void btnXuatExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXuatExcelActionPerformed
        JTableExporter.exportJTableToExcel(tblNhanVien);
    }

    private void btnTaoTaiKhoanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTaoTaiKhoanActionPerformed
        int row = tblNhanVien.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một nhân viên trong bảng để tạo tài khoản.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int modelRow = tblNhanVien.convertRowIndexToModel(row);
        String maNVCanTaoTK = modelNhanVien.getValueAt(modelRow, 0).toString();

        boolean tonTai = false;
        try {
            tonTai = taiKhoanService.kiemTraTonTaiTheoMaNV(maNVCanTaoTK);
        } catch (Exception e) { e.printStackTrace(); }

        if (tonTai) {
            JOptionPane.showMessageDialog(this,
                    "Nhân viên này đã có tài khoản!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        MainForm main = (MainForm) SwingUtilities.getAncestorOfClass(MainForm.class, this);
        if (main != null) {
            Gui_TaiKhoan guiTaiKhoan = new Gui_TaiKhoan(maNVCanTaoTK);
            main.showForm(guiTaiKhoan);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton Vien;
    private JButton btnCapNhat;
    private JButton btnTaiKhoan;
    private JButton btnTaoTaiKhoan;
    private JButton btnThem;
    private JButton btnTimKiem;
    private JButton btnXoaTrang;
    private JButton btnXuatExcel;
    private JComboBox<String> cmbTrangThai;
    private com.toedter.calendar.JDateChooser dcNgaySinh;
    private JLabel lblCCCD;
    private JLabel lblDiaChi;
    private JLabel lblEmail;
    private JLabel lblGioiTinh;
    private JLabel lblHoTen;
    private JLabel lblMaNV;
    private JLabel lblNgaySinh;
    private JLabel lblSoDienThoai;
    private JLabel lblTitle;
    private JLabel lblTrangThai;
    private JPanel pnlThongTin;
    private JRadioButton radNam;
    private JRadioButton radNu;
    private JScrollPane scrollNhanVien;
    private JTable tblNhanVien;
    private JTextField txtCCCD;
    private JTextField txtDiaChi;
    private JTextField txtEmail;
    private JTextField txtHoTen;
    private JTextField txtMaNV;
    private JTextField txtSoDienThoai;
    // End of variables declaration//GEN-END:variables
}