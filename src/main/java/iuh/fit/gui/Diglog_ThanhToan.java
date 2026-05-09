package iuh.fit.gui;

import iuh.fit.utils.SessionManager;
import iuh.fit.utils.ClientContext;
import entity.*;
import service.*;

import java.awt.Frame;
import java.awt.Window;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Dialog thanh toán
 * @author PC
 */
public class Diglog_ThanhToan extends JDialog {

    private double tongTien = 0;
    private double khuyenMai = 0;
    private int soLuongVe = 0;
    private String cccd = "";
    private String hoTen = "";
    private String sdt = "";
    private String email = "";
    private NumberFormat currencyFormat;
    
    private boolean isThanhToanThanhCong = false;
    private boolean isNhapLai = false;
    private boolean isTreoDon = false;
    
    private Gui_NhapThongTinBanVe previousGui; // Để quay lại
    private entity.DonTreoDat donTreo; // Đơn treo (nếu xử lý từ đơn tạm)
    
    /**
     * Creates new form Diglog_ThanhToan
     */
    public Diglog_ThanhToan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initCustomComponents();
    }
    
    /**
     * Constructor với dữ liệu (từ flow bán vé thường)
     */
    public Diglog_ThanhToan(java.awt.Frame parent, boolean modal, 
                             String cccd, String hoTen, String sdt, String email,
                             int soLuongVe, double tongTien, double khuyenMai,
                             Gui_NhapThongTinBanVe previousGui) {
        super(parent, modal);
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
     * Constructor với dữ liệu (từ xử lý đơn tạm)
     */
    public Diglog_ThanhToan(java.awt.Frame parent, boolean modal, 
                             String cccd, String hoTen, String sdt, String email,
                             int soLuongVe, double tongTien, double khuyenMai,
                             entity.DonTreoDat donTreo) {
        super(parent, modal);
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
    
    private void initCustomComponents() {
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        setLocationRelativeTo(null);
        
        // Thêm listener cho checkbox "Nhận đủ tiền"
        chkNhanDuTien.addActionListener(e -> {
            if (chkNhanDuTien.isSelected()) {
                // Tự động điền tổng tiền (đã trừ khuyến mãi) vào ô tiền khách đưa
                double tongTienThucTe = tongTien - khuyenMai;
                txtTienKhachDua.setText(String.valueOf((long)tongTienThucTe));
            } else {
                txtTienKhachDua.setText("");
            }
        });
        
        // Thêm listener cho textfield tiền khách đưa
        txtTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { tinhTienThua(); }
            @Override
            public void removeUpdate(DocumentEvent e) { tinhTienThua(); }
            @Override
            public void changedUpdate(DocumentEvent e) { tinhTienThua(); }
        });
        
        // Thêm ActionListener cho tất cả các nút giá gợi ý
        btn500K.addActionListener(e -> themTienGoiY(500000));
        btn200K.addActionListener(e -> themTienGoiY(200000));
        btn100K.addActionListener(e -> themTienGoiY(100000));
        btn50K.addActionListener(e -> themTienGoiY(50000));
        btn20K.addActionListener(e -> themTienGoiY(20000));
        btn10K.addActionListener(e -> themTienGoiY(10000));
        btn5K.addActionListener(e -> themTienGoiY(5000));
        btn2K.addActionListener(e -> themTienGoiY(2000));
        btn1K.addActionListener(e -> themTienGoiY(1000));
    }
    
    private void loadData() {
        lblCCCDValue.setText(cccd.isEmpty() ? "(Chưa nhập)" : cccd);
        lblHoTenValue.setText(hoTen.isEmpty() ? "(Chưa nhập)" : hoTen);
        lblSDTValue.setText(sdt.isEmpty() ? "(Chưa nhập)" : sdt);
        lblSoLuongVeValue.setText(String.valueOf(soLuongVe));
        
        // Tổng tiền sau khi trừ khuyến mãi
        double tongTienThucTe = tongTien - khuyenMai;
        lblTongTienValue.setText(currencyFormat.format(tongTienThucTe) + " ₫");
        
        lblKhuyenMaiValue.setText(khuyenMai > 0 ? currencyFormat.format(khuyenMai) + " ₫" : "0 ₫");
        lblTienThuaValue.setText("0 ₫");
    }
    
    /**
     * Thêm tiền gợi ý vào tiền khách đưa
     */
    private void themTienGoiY(double soTien) {
        try {
            String currentText = txtTienKhachDua.getText().trim();
            double currentAmount = 0;
            
            if (!currentText.isEmpty()) {
                // Xóa dấu phân cách nếu có
                currentText = currentText.replaceAll("[,.]", "");
                currentAmount = Double.parseDouble(currentText);
            }
            
            double newAmount = currentAmount + soTien;
            txtTienKhachDua.setText(String.valueOf((long)newAmount));
            
        } catch (NumberFormatException e) {
            txtTienKhachDua.setText(String.valueOf((long)soTien));
        }
    }
    
    /**
     * Tính tiền thừa
     */
    private void tinhTienThua() {
        try {
            String text = txtTienKhachDua.getText().trim();
            if (text.isEmpty()) {
                lblTienThuaValue.setText("0 ₫");
                return;
            }
            
            // Xóa dấu phân cách
            text = text.replaceAll("[,.]", "");
            double tienKhachDua = Double.parseDouble(text);
            double tienThua = tienKhachDua - (tongTien - khuyenMai);
            
            if (tienThua < 0) {
                lblTienThuaValue.setText("<html><font color='red'>" + 
                                         currencyFormat.format(tienThua) + " ₫</font></html>");
            } else {
                lblTienThuaValue.setText("<html><font color='green'>" + 
                                         currencyFormat.format(tienThua) + " ₫</font></html>");
            }
            
        } catch (NumberFormatException e) {
            lblTienThuaValue.setText("0 ₫");
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

        lblTitle = new JLabel();
        lblCCCDTitle = new JLabel();
        lblHoTenTitle = new JLabel();
        jPanel1 = new JPanel();
        btn500K = new JButton();
        btn200K = new JButton();
        btn100K = new JButton();
        btn50K = new JButton();
        btn20K = new JButton();
        btn10K = new JButton();
        btn5K = new JButton();
        btn2K = new JButton();
        btn1K = new JButton();
        lblSDTTitle = new JLabel();
        lblSoLuongVeTitle = new JLabel();
        lblTongTienTitle = new JLabel();
        lblKhuyenMaiTitle = new JLabel();
        lblTienKhachDuaTitle = new JLabel();
        lblTienThuaTitle = new JLabel();
        txtTienKhachDua = new JTextField();
        btnNhapLai = new JButton();
        btnTreoDon = new JButton();
        btnThanhToan = new JButton();
        lblCCCDValue = new JLabel();
        lblHoTenValue = new JLabel();
        lblSDTValue = new JLabel();
        lblSoLuongVeValue = new JLabel();
        lblTongTienValue = new JLabel();
        lblKhuyenMaiValue = new JLabel();
        lblTienThuaValue = new JLabel();
        jButton1 = new JButton();
        chkNhanDuTien = new JCheckBox();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(234, 243, 251));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setText("Xác nhận thông tin mua vé tàu");

        lblCCCDTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblCCCDTitle.setText("CCCD:");

        lblHoTenTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblHoTenTitle.setText("Họ tên:");

        jPanel1.setBorder(BorderFactory.createTitledBorder("Giá gợi ý"));

        btn500K.setBackground(new java.awt.Color(234, 243, 251));
        btn500K.setText("500.000");

        btn200K.setBackground(new java.awt.Color(234, 243, 251));
        btn200K.setText("200.000");

        btn100K.setBackground(new java.awt.Color(234, 243, 251));
        btn100K.setText("100.000");

        btn50K.setBackground(new java.awt.Color(234, 243, 251));
        btn50K.setText("50.000");

        btn20K.setBackground(new java.awt.Color(234, 243, 251));
        btn20K.setText("20.000");

        btn10K.setBackground(new java.awt.Color(234, 243, 251));
        btn10K.setText("10.000");

        btn5K.setBackground(new java.awt.Color(234, 243, 251));
        btn5K.setText("5.000");

        btn2K.setBackground(new java.awt.Color(234, 243, 251));
        btn2K.setText("2.000");

        btn1K.setBackground(new java.awt.Color(234, 243, 251));
        btn1K.setText("1.000");

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btn500K, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                        .addComponent(btn200K, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btn100K, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btn50K, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(btn20K, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn5K, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn1K, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(btn10K, GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(btn2K, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(16, 16, 16))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btn500K, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn200K, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btn100K, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn50K, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btn20K, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn10K, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btn5K, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn2K, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(btn1K, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        lblSDTTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSDTTitle.setText("Số điện thoại:");

        lblSoLuongVeTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSoLuongVeTitle.setText("Số lượng vé:");

        lblTongTienTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTongTienTitle.setText("Tổng tiền:");

        lblKhuyenMaiTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblKhuyenMaiTitle.setText("Khuyến mãi:");

        lblTienKhachDuaTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTienKhachDuaTitle.setText("Tiền khách đưa:");

        lblTienThuaTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTienThuaTitle.setText("Tiền thừa:");

        btnNhapLai.setIcon(new ImageIcon(getClass().getResource("/icon/refresh.png"))); // NOI18N
        btnNhapLai.setText("Nhập lại");
        btnNhapLai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNhapLaiActionPerformed(evt);
            }
        });

        btnTreoDon.setIcon(new ImageIcon(getClass().getResource("/icon/bill.png"))); // NOI18N
        btnTreoDon.setText("Treo đơn");
        btnTreoDon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTreoDonActionPerformed(evt);
            }
        });

        btnThanhToan.setIcon(new ImageIcon(getClass().getResource("/icon/payment.png"))); // NOI18N
        btnThanhToan.setText("Thanh toán ");
        btnThanhToan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThanhToanActionPerformed(evt);
            }
        });

        lblCCCDValue.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblCCCDValue.setText(" ");

        lblHoTenValue.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblHoTenValue.setText(" ");

        lblSDTValue.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSDTValue.setText(" ");

        lblSoLuongVeValue.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSoLuongVeValue.setText(" ");

        lblTongTienValue.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTongTienValue.setText(" ");

        lblKhuyenMaiValue.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblKhuyenMaiValue.setText(" ");

        lblTienThuaValue.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTienThuaValue.setText(" ");

        jButton1.setIcon(new ImageIcon(getClass().getResource("/icon/arrow.png"))); // NOI18N
        jButton1.setText("Quay Lại");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        chkNhanDuTien.setText("Nhận đủ tiền");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                        .addComponent(btnNhapLai, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48)
                        .addComponent(btnTreoDon, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
                        .addGap(54, 54, 54)
                        .addComponent(btnThanhToan)
                        .addGap(48, 48, 48))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(lblCCCDTitle, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblHoTenTitle, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSDTTitle)
                            .addComponent(lblSoLuongVeTitle, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblKhuyenMaiTitle)
                            .addComponent(lblTienKhachDuaTitle, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTienThuaTitle)
                            .addComponent(lblTongTienTitle, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(txtTienKhachDua, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblTongTienValue, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblSoLuongVeValue, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblSDTValue, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblHoTenValue, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblCCCDValue, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblTienThuaValue, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(chkNhanDuTien)))
                            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblKhuyenMaiValue, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCCCDTitle, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCCCDValue))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lblHoTenTitle)
                            .addComponent(lblHoTenValue, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSDTTitle)
                            .addComponent(lblSDTValue))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSoLuongVeTitle)
                            .addComponent(lblSoLuongVeValue))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(lblKhuyenMaiTitle)
                            .addComponent(lblKhuyenMaiValue, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTongTienValue)
                            .addComponent(lblTongTienTitle))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTienKhachDuaTitle)
                            .addComponent(txtTienKhachDua, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblTienThuaTitle))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(chkNhanDuTien)
                                .addGap(18, 18, 18)
                                .addComponent(lblTienThuaValue)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnThanhToan, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTreoDon, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNhapLai, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNhapLaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNhapLaiActionPerformed
        txtTienKhachDua.setText("");
        lblTienThuaValue.setText("0 ₫");
        txtTienKhachDua.requestFocus();
    }//GEN-LAST:event_btnNhapLaiActionPerformed

    private void btnTreoDonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTreoDonActionPerformed
        if (previousGui != null) {
            javax.swing.table.TableModel model = previousGui.getModelThongTinVe();
            for (int i = 0; i < model.getRowCount(); i++) {
                String doiTuong = model.getValueAt(i, 2) != null ? model.getValueAt(i, 2).toString().trim() : "";
                if (doiTuong.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn Đối tượng cho vé ở dòng " + (i + 1) + " trước khi treo đơn!",
                        "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }
        
        DonTreoDat donTreo = new DonTreoDat();
        donTreo.setCccdNguoiDat(cccd);
        donTreo.setHoTenNguoiDat(hoTen);
        donTreo.setSdtNguoiDat(sdt);
        donTreo.setEmailNguoiDat(email);
        donTreo.setSoLuongVe(soLuongVe);
        donTreo.setTongTien(tongTien);
        donTreo.setNgayLap(java.time.LocalDateTime.now());
        donTreo.setGioLap(java.time.LocalDateTime.now());
        
        if (previousGui != null) {
            try {
                LichTrinh lt = previousGui.getPreviousGuiBanVe().getLichTrinhDangChon();
                if (lt != null) {
                    donTreo.setGaDi(lt.getGaDi() != null ? lt.getGaDi().getTenGa() : "");
                    donTreo.setGaDen(lt.getGaDen() != null ? lt.getGaDen().getTenGa() : "");
                    donTreo.setLichTrinh(lt);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (previousGui != null) {
            javax.swing.table.TableModel model = previousGui.getModelThongTinVe();
            List<ChoNgoi> danhSachChoNgoi = previousGui.getDanhSachChoNgoi();
            List<LichTrinh> danhSachLichTrinh = previousGui.getDanhSachLichTrinh();
            
            for (int i = 0; i < model.getRowCount(); i++) {
                DonTreoDat.ThongTinVeTam veTam = new DonTreoDat.ThongTinVeTam();
                veTam.setSoGiayTo(model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString() : "");
                veTam.setHoTen(model.getValueAt(i, 1) != null ? model.getValueAt(i, 1).toString() : "");
                veTam.setDoiTuong(model.getValueAt(i, 2) != null ? model.getValueAt(i, 2).toString() : "");
                veTam.setThongTinCho(model.getValueAt(i, 3) != null ? model.getValueAt(i, 3).toString() : "");
                
                if (danhSachChoNgoi != null && i < danhSachChoNgoi.size()) {
                    veTam.setChoNgoi(danhSachChoNgoi.get(i));
                }
                if (danhSachLichTrinh != null && i < danhSachLichTrinh.size()) {
                    veTam.setLichTrinh(danhSachLichTrinh.get(i));
                }
                
                veTam.setGiaVe(parseDouble(model.getValueAt(i, 4) != null ? model.getValueAt(i, 4).toString() : "0"));
                veTam.setGiamGia(parseDouble(model.getValueAt(i, 5) != null ? model.getValueAt(i, 5).toString() : "0"));
                veTam.setThanhTien(parseDouble(model.getValueAt(i, 6) != null ? model.getValueAt(i, 6).toString() : "0"));
                
                if (previousGui.getPreviousGuiBanVe() != null) {
                    ChoNgoi cn = veTam.getChoNgoi();
                    if (cn != null) {
                        veTam.setGaDi(previousGui.getPreviousGuiBanVe().getMapGheGaDi().get(cn));
                        veTam.setGaDen(previousGui.getPreviousGuiBanVe().getMapGheGaDen().get(cn));
                    }
                }
                donTreo.themVe(veTam);
            }
        }
        
        QuanLyDonTreo.themDonTreo(donTreo);
        
        if (previousGui != null) {
            for (DonTreoDat.ThongTinVeTam veTam : donTreo.getDanhSachVe()) {
                if (veTam.getChoNgoi() != null) {
                    QuanLyGheGiuCho.themGheGiuCho(veTam.getChoNgoi().getMaChoNgoi(), donTreo.getMaDonTreo(), 
                                                 veTam.getLichTrinh() != null ? veTam.getLichTrinh().getMaLichTrinh() : null,
                                                 veTam.getGaDi() != null ? veTam.getGaDi().getMaGa() : null,
                                                 veTam.getGaDen() != null ? veTam.getGaDen().getMaGa() : null);
                }
            }
        }
        
        isTreoDon = true;
        if (previousGui != null && previousGui.getPreviousGuiBanVe() != null) {
            previousGui.getPreviousGuiBanVe().reloadSoDoGhe();
            previousGui.getPreviousGuiBanVe().capNhatSoLuongDonTreo();
        }
        dispose();
    }//GEN-LAST:event_btnTreoDonActionPerformed

    private double parseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        str = str.replaceAll("[^0-9]", "");
        try { return Double.parseDouble(str); } catch (Exception e) { return 0; }
    }

    private void btnThanhToanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThanhToanActionPerformed
        String tienKhachDuaText = txtTienKhachDua.getText().trim();
        if (tienKhachDuaText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền khách đưa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            double tienKhachDua = Double.parseDouble(tienKhachDuaText.replaceAll("[,.]", ""));
            double tongThanhToan = tongTien - khuyenMai;
            
            if (tienKhachDua < tongThanhToan) {
                JOptionPane.showMessageDialog(this, "Số tiền khách đưa không đủ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (previousGui != null) {
                javax.swing.table.TableModel model = previousGui.getModelThongTinVe();
                for (int i = 0; i < model.getRowCount(); i++) {
                    if (model.getValueAt(i, 2) == null || model.getValueAt(i, 2).toString().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Vui lòng chọn Đối tượng cho vé ở dòng " + (i + 1) + "!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }
            
            String maHoaDon = "HD" + System.currentTimeMillis();
            if (luuVaoDatabase(maHoaDon, cccd, hoTen, sdt, email, soLuongVe, tongTien, khuyenMai)) {
                isThanhToanThanhCong = true;
                java.awt.Frame parentFrame = (java.awt.Frame) SwingUtilities.getWindowAncestor(this);
                dispose();
                
                Dialog_In dialogIn = new Dialog_In(parentFrame, false, maHoaDon);
                dialogIn.setVisible(true);
                dialogIn.autoInHoaDonVaVe();
                
                if (previousGui != null && previousGui.getPreviousGuiBanVe() != null) {
                    previousGui.getPreviousGuiBanVe().reloadSoDoGhe();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu vào database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnThanhToanActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        isNhapLai = true;
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private boolean luuVaoDatabase(String maHoaDon, String cccd, String hoTen, String sdt, String email, 
                                     int soLuongVe, double tongTien, double khuyenMai) {
        try {
            IKhachHangService khService = ClientContext.getKhachHangService();
            IHoaDonService hdService = ClientContext.getHoaDonService();
            IVeService veService = ClientContext.getVeService();
            ILoaiVeService lvService = ClientContext.getLoaiVeService();
            
            KhachHang kh = khService.findByCCCD(cccd);
            if (kh == null) {
                kh = new KhachHang();
                kh.setMaKH("KH" + System.currentTimeMillis());
                kh.setCCCD(cccd);
                kh.setHoTen(hoTen);
                kh.setSDT(sdt);
                kh.setEmail(email);
                
                String doiTuongKH = "Người lớn";
                if (previousGui != null && previousGui.getModelThongTinVe().getRowCount() > 0) {
                    doiTuongKH = previousGui.getModelThongTinVe().getValueAt(0, 2).toString();
                } else if (donTreo != null && !donTreo.getDanhSachVe().isEmpty()) {
                    doiTuongKH = donTreo.getDanhSachVe().get(0).getDoiTuong();
                }
                kh.setDoiTuong(doiTuongKH);
                khService.them(kh);
            }
            
            HoaDon hoaDon = new HoaDon();
            hoaDon.setMaHoaDon(maHoaDon);
            NhanVien nv = new NhanVien();
            nv.setMaNhanVien(SessionManager.getInstance().getMaNhanVienDangNhap());
            hoaDon.setNhanVien(nv);
            hoaDon.setKhachHang(kh);
            hoaDon.setNgayTao(java.time.LocalDateTime.now());
            hoaDon.setGioTao(java.time.LocalDateTime.now());
            hoaDon.setTongTien(tongTien - khuyenMai);
            hoaDon.setTrangThai(true);
            
            if (previousGui != null) {
                javax.swing.table.TableModel model = previousGui.getModelThongTinVe();
                for (int i = 0; i < model.getRowCount(); i++) {
                    String tenKH = model.getValueAt(i, 1).toString();
                    String doiTuong = model.getValueAt(i, 2).toString();
                    double giaVe = parseDouble(model.getValueAt(i, 4).toString());
                    double giamGia = parseDouble(model.getValueAt(i, 5).toString());
                    
                    ChoNgoi choNgoi = previousGui.getDanhSachChoNgoi().get(i);
                    LichTrinh lichTrinhCuaVe = previousGui.getDanhSachLichTrinh().get(i);
                    LoaiVe loaiVe = lvService.findByTenLoaiVe(doiTuong);
                    
                    Ve ve = new Ve();
                    ve.setMaVe("V" + System.currentTimeMillis() + "_" + i);
                    ve.setLoaiVe(loaiVe);
                    ve.setThoiGianLenTau(lichTrinhCuaVe.getGioKhoiHanh());
                    ve.setGiaVe(giaVe);
                    ve.setKhachHang(kh);
                    ve.setChoNgoi(choNgoi);
                    ve.setLichTrinh(lichTrinhCuaVe);
                    ve.setTrangThai(true);
                    ve.setTenKhachHang(tenKH);
                    ve.setSoCCCD(model.getValueAt(i, 0).toString());
                    
                    if (previousGui.getPreviousGuiBanVe() != null) {
                        ve.setGaDi(previousGui.getPreviousGuiBanVe().getMapGheGaDi().get(choNgoi));
                        ve.setGaDen(previousGui.getPreviousGuiBanVe().getMapGheGaDen().get(choNgoi));
                    }
                    
                    veService.insert(ve);
                    
                    ChiTietHoaDon cthd = new ChiTietHoaDon();
                    cthd.setVe(ve);
                    cthd.setSoLuong(1);
                    cthd.setGiaVe(giaVe);
                    cthd.setMucGiam(giamGia);
                    hoaDon.themChiTiet(cthd);
                }
            } else if (donTreo != null) {
                for (int i = 0; i < donTreo.getDanhSachVe().size(); i++) {
                    DonTreoDat.ThongTinVeTam veTam = donTreo.getDanhSachVe().get(i);
                    LoaiVe loaiVe = lvService.findByTenLoaiVe(veTam.getDoiTuong());
                    
                    Ve ve = new Ve();
                    ve.setMaVe("V" + System.currentTimeMillis() + "_" + i);
                    ve.setLoaiVe(loaiVe);
                    ve.setThoiGianLenTau(veTam.getLichTrinh().getGioKhoiHanh());
                    ve.setGiaVe(veTam.getGiaVe());
                    ve.setKhachHang(kh);
                    ve.setChoNgoi(veTam.getChoNgoi());
                    ve.setLichTrinh(veTam.getLichTrinh());
                    ve.setTrangThai(true);
                    ve.setTenKhachHang(veTam.getHoTen());
                    ve.setSoCCCD(veTam.getSoGiayTo());
                    ve.setGaDi(veTam.getGaDi());
                    ve.setGaDen(veTam.getGaDen());
                    
                    veService.insert(ve);
                    
                    ChiTietHoaDon cthd = new ChiTietHoaDon();
                    cthd.setVe(ve);
                    cthd.setSoLuong(1);
                    cthd.setGiaVe(veTam.getGiaVe());
                    cthd.setMucGiam(veTam.getGiamGia());
                    hoaDon.themChiTiet(cthd);
                }
            }
            
            return hdService.them(hoaDon);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isThanhToanThanhCong() { return isThanhToanThanhCong; }
    public boolean isNhapLai() { return isNhapLai; }
    public boolean isTreoDon() { return isTreoDon; }

    private JButton btn100K;
    private JButton btn10K;
    private JButton btn1K;
    private JButton btn200K;
    private JButton btn20K;
    private JButton btn2K;
    private JButton btn500K;
    private JButton btn50K;
    private JButton btn5K;
    private JButton btn500K_1; // Extra if needed
    private JButton btnNhapLai;
    private JButton btnThanhToan;
    private JButton btnTreoDon;
    private JCheckBox chkNhanDuTien;
    private JButton jButton1;
    private JPanel jPanel1;
    private JLabel lblCCCDTitle;
    private JLabel lblCCCDValue;
    private JLabel lblHoTenTitle;
    private JLabel lblHoTenValue;
    private JLabel lblKhuyenMaiTitle;
    private JLabel lblKhuyenMaiValue;
    private JLabel lblSDTTitle;
    private JLabel lblSDTValue;
    private JLabel lblSoLuongVeValue;
    private JLabel lblSoLuongVeTitle;
    private JLabel lblTienKhachDuaTitle;
    private JLabel lblTienThuaTitle;
    private JLabel lblTienThuaValue;
    private JLabel lblTitle;
    private JLabel lblTongTienTitle;
    private JLabel lblTongTienValue;
    private JTextField txtTienKhachDua;
}
