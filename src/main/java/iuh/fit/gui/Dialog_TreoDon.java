/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package iuh.fit.gui;

import entity.DonTreoDat;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dialog để hiển thị và xử lý các đơn treo
 * @author PC
 */
public class Dialog_TreoDon extends javax.swing.JDialog {

    private boolean isConfirmed = false;
    private DonTreoDat donDuocChon = null;
    private DefaultTableModel modelTable;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private javax.swing.Timer updateTimer; // Timer để update thời gian còn lại
    private boolean isSearching = false; // Flag đánh dấu đang trong chế độ tìm kiếm
    
    private Gui_BanVe guiBanVe = null;
    
    /**
     * Creates new form Dialog_TreoDon
     */
    public Dialog_TreoDon(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(parent);
        initCustomComponents();
    }
    
    public Dialog_TreoDon(java.awt.Frame parent, boolean modal, Gui_BanVe guiBanVe) {
        super(parent, modal);
        this.guiBanVe = guiBanVe;
        initComponents();
        setLocationRelativeTo(parent);
        initCustomComponents();
    }
    
    private void initCustomComponents() {
        // Khởi tạo model cho table với cột ẩn Mã đơn treo
        modelTable = new DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Mã đơn", "CCCD", "Họ tên", "Số điện thoại", "Ngày lập", "Giờ lập", "Số lượng vé", "Thời gian còn"
            }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblThongTin.setModel(modelTable);
        
        // Ẩn cột Mã đơn treo (index 0)
        tblThongTin.getColumnModel().getColumn(0).setMinWidth(0);
        tblThongTin.getColumnModel().getColumn(0).setMaxWidth(0);
        tblThongTin.getColumnModel().getColumn(0).setPreferredWidth(0);
        
        // Load tất cả đơn treo khi mở dialog
        loadTatCaDonTreo();
        
        // Tạo Timer để update thời gian còn lại mỗi giây
        updateTimer = new javax.swing.Timer(1000, e -> updateThoiGianConLai());
        updateTimer.start();
        
        // Stop timer khi đóng dialog
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (updateTimer != null) {
                    updateTimer.stop();
                }
            }
        });
    }
    
    /**
     * Load tất cả đơn treo vào table
     */
    private void loadTatCaDonTreo() {
        isSearching = false; // Reset trạng thái tìm kiếm
        modelTable.setRowCount(0);
        List<DonTreoDat> danhSach = QuanLyDonTreo.layDanhSachDonTreo();
        
        for (DonTreoDat don : danhSach) {
            modelTable.addRow(new Object[]{
                don.getMaDonTreo(), // Cột ẩn
                don.getCccdNguoiDat() != null ? don.getCccdNguoiDat() : "",
                don.getHoTenNguoiDat() != null ? don.getHoTenNguoiDat() : "",
                don.getSdtNguoiDat() != null ? don.getSdtNguoiDat() : "",
                don.getNgayLap() != null ? don.getNgayLap().format(dateFormatter) : "",
                don.getGioLap() != null ? don.getGioLap().format(timeFormatter) : "",
                don.getSoLuongVe(),
                don.getThoiGianConLaiFormatted() // Thêm cột thời gian còn lại
            });
        }
    }
    
    /**
     * Update thời gian còn lại cho tất cả các dòng
     */
    private void updateThoiGianConLai() {
        // Nếu không tìm kiếm, kiểm tra xem có cần reload do thay đổi số lượng đơn không
        if (!isSearching) {
            List<DonTreoDat> danhSachHienTai = QuanLyDonTreo.layDanhSachDonTreo();
            if (modelTable.getRowCount() != danhSachHienTai.size()) {
                loadTatCaDonTreo();
                return;
            }
        }
        
        // Cập nhật thời gian cho từng dòng đang hiển thị dựa trên Mã đơn
        List<Integer> rowsToRemove = new java.util.ArrayList<>();
        
        for (int i = 0; i < modelTable.getRowCount(); i++) {
            Object objMaDon = modelTable.getValueAt(i, 0);
            if (objMaDon == null) continue;
            
            String maDon = objMaDon.toString();
            DonTreoDat don = QuanLyDonTreo.layDonTreo(maDon);
            
            if (don != null && don.conTrongThoiHan()) {
                // Cập nhật thời gian còn lại (index 7)
                modelTable.setValueAt(don.getThoiGianConLaiFormatted(), i, 7);
            } else {
                // ⚡ GIẢI PHÓNG TRÊN DATA: Đơn đã hết hạn hoặc bị xóa trên Server
                // Lấy danh sách mã ghế để giải phóng RAM Remote
                List<String> maGheList = new java.util.ArrayList<>();
                if (don != null && don.getDanhSachVe() != null) {
                    for (DonTreoDat.ThongTinVeTam ve : don.getDanhSachVe()) {
                        if (ve.getChoNgoi() != null) {
                            maGheList.add(ve.getChoNgoi().getMaChoNgoi());
                        }
                    }
                }
                
                // Gọi xoaDonTreo trên server (Xử lý trên Data)
                QuanLyDonTreo.xoaDonTreo(maDon);
                
                // Giải phóng triệt để trên RAM (Xử lý hiển thị)
                QuanLyGheGiuCho.xoaTatCaGheCuaDonTreo(maDon, maGheList);
                
                // ⚡ REFRESH SƠ ĐỒ GHẾ NGAY LẬP TỨC
                if (guiBanVe != null) {
                    guiBanVe.reloadSoDoGhe();
                }
                
                // Đánh dấu để xóa khỏi bảng
                rowsToRemove.add(i);
            }
        }
        
        // Xóa các dòng hết hạn (duyệt ngược từ dưới lên để không sai index)
        for (int i = rowsToRemove.size() - 1; i >= 0; i--) {
            modelTable.removeRow(rowsToRemove.get(i));
        }
    }
    
    /**
     * Getter để kiểm tra xem có chọn đơn để xử lý không
     */
    public boolean isConfirmed() {
        return isConfirmed;
    }
    
    /**
     * Setter để đánh dấu đã xử lý đơn
     */
    public void setConfirmed(boolean confirmed) {
        this.isConfirmed = confirmed;
    }
    
    /**
     * Lấy đơn được chọn
     */
    public DonTreoDat getDonDuocChon() {
        return donDuocChon;
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tblThongTin = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnTim = new javax.swing.JButton();
        btnXuLi = new javax.swing.JButton();
        btnHuyDon = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("Thông Tin Đơn Tạm");

        tblThongTin.setModel(new DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "CCCD", "Họ tên", "Số điện thoại", "Ngày lập", "Giờ lập", "Số lượng vé", "Thời gian còn"
            }
        ));
        jScrollPane1.setViewportView(tblThongTin);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Thông tin đơn đặt"));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("CCCD:");

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Số điện thoai:");

        btnTim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/TimKiem.png"))); // NOI18N
        btnTim.setText("Tìm");
        btnTim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTimActionPerformed(evt);
            }
        });

        btnXuLi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/bill1.png"))); // NOI18N
        btnXuLi.setText("Xử lí");
        btnXuLi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXuLiActionPerformed(evt);
            }
        });

        btnHuyDon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/multiply.png"))); // NOI18N
        btnHuyDon.setText("Hủy Đơn");
        btnHuyDon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHuyDonActionPerformed(evt);
            }
        });

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
                .addGap(84, 84, 84)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(71, 71, 71)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(105, 105, 105))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnTim)
                        .addGap(63, 63, 63)
                        .addComponent(btnXuLi, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(49, 49, 49)
                        .addComponent(btnHuyDon, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(124, 124, 124))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnXuLi, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTim, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHuyDon, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 727, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void btnTimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimActionPerformed
        // Tìm kiếm theo CCCD hoặc SĐT
        String cccd = jTextField1.getText().trim();
        String sdt = jTextField2.getText().trim();
        
        if (cccd.isEmpty() && sdt.isEmpty()) {
            // Nếu cả 2 đều trống → Load tất cả
            loadTatCaDonTreo();
            return;
        }
        
        isSearching = true; // Đánh dấu đang tìm kiếm
        
        modelTable.setRowCount(0);
        List<DonTreoDat> ketQua = new java.util.ArrayList<>();
        
        // Tìm theo CCCD
        if (!cccd.isEmpty()) {
            ketQua.addAll(QuanLyDonTreo.layDonTreoTheoCCCD(cccd));
        }
        
        // Tìm theo SĐT
        if (!sdt.isEmpty()) {
            List<DonTreoDat> ketQuaSDT = QuanLyDonTreo.layDonTreoTheoSDT(sdt);
            // Thêm vào nếu chưa có trong kết quả
            for (DonTreoDat don : ketQuaSDT) {
                if (!ketQua.contains(don)) {
                    ketQua.add(don);
                }
            }
        }
        
        // Hiển thị kết quả
        for (DonTreoDat don : ketQua) {
            modelTable.addRow(new Object[]{
                don.getMaDonTreo(),
                don.getCccdNguoiDat() != null ? don.getCccdNguoiDat() : "",
                don.getHoTenNguoiDat() != null ? don.getHoTenNguoiDat() : "",
                don.getSdtNguoiDat() != null ? don.getSdtNguoiDat() : "",
                don.getNgayLap() != null ? don.getNgayLap().format(dateFormatter) : "",
                don.getGioLap() != null ? don.getGioLap().format(timeFormatter) : "",
                don.getSoLuongVe(),
                don.getThoiGianConLaiFormatted()
            });
        }
        
        if (ketQua.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy đơn treo nào!",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnTimActionPerformed

    private void btnXuLiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXuLiActionPerformed
        // Xử lí đơn được chọn → Mở Dialog Thanh Toán
        int selectedRow = tblThongTin.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn đơn cần xử lý!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Lấy mã đơn từ cột ẩn (index 0)
        String maDonChon = modelTable.getValueAt(selectedRow, 0).toString();
        
        // Tìm đơn trong danh sách
        DonTreoDat donCanXuLy = QuanLyDonTreo.layDonTreo(maDonChon);
        
        if (donCanXuLy == null) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy đơn!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // ⚡ GIA HẠN ghế giữ chỗ thêm 15 phút (tránh hết hạn trong lúc thanh toán)
        QuanLyGheGiuCho.giaHanGheCuaDonTreo(donCanXuLy.getMaDonTreo());
        
        // Ẩn Dialog Treo Đơn tạm thời (KHÔNG dispose ngay)
        this.setVisible(false);
        
        // Mở Dialog Thanh Toán với dữ liệu từ đơn treo
        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        Diglog_ThanhToan dialogThanhToan = new Diglog_ThanhToan(
            parentFrame, true,
            donCanXuLy.getCccdNguoiDat(),
            donCanXuLy.getHoTenNguoiDat(),
            donCanXuLy.getSdtNguoiDat(),
            donCanXuLy.getEmailNguoiDat(),
            donCanXuLy.getSoLuongVe(),
            donCanXuLy.getTongTien(),
            0, // Khuyến mãi tạm thời = 0
            donCanXuLy // ⚡ Truyền đơn treo để lấy thông tin vé
        );
        
        dialogThanhToan.setVisible(true);
        
        // Sau khi thanh toán thành công → Xóa đơn treo và ghế giữ chỗ
        if (dialogThanhToan.isThanhToanThanhCong()) {
            // Xóa ghế giữ chỗ
            QuanLyGheGiuCho.xoaTatCaGheCuaDonTreo(donCanXuLy.getMaDonTreo());
            
            // Xóa đơn treo
            QuanLyDonTreo.xoaDonTreo(donCanXuLy.getMaDonTreo());
            
            // ⚡ Reload sơ đồ ghế đã xử lý trong Diglog_ThanhToan → Không cần làm lại ở đây
            
            // Đóng Dialog Treo Đơn SAU KHI đã xử lý xong
            this.dispose();
        } else {
            // Nếu không thanh toán (nhập lại, treo đơn, v.v.) → Hiện lại dialog
            loadTatCaDonTreo(); // Reload danh sách
            this.setVisible(true);
        }
    }//GEN-LAST:event_btnXuLiActionPerformed

    private void btnHuyDonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHuyDonActionPerformed
        // Hủy đơn treo
        int selectedRow = tblThongTin.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn đơn cần hủy!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Lấy mã đơn từ cột ẩn (index 0)
        String maDonChon = modelTable.getValueAt(selectedRow, 0).toString();
        String hoTen = modelTable.getValueAt(selectedRow, 2).toString(); // Index 2 là Họ tên sau khi thêm mã đơn
        
        // Xác nhận hủy
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn hủy đơn của:\n" +
            "Họ tên: " + hoTen + "\n" +
            "Mã đơn: " + maDonChon + "?",
            "Xác nhận hủy",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // ⚡ LẤY THÔNG TIN GHẾ TỪ DATA TRƯỚC:
            // ⚡ Lấy lại đơn đầy đủ từ Server để đảm bảo có danh sách vé
            DonTreoDat don = QuanLyDonTreo.layDonTreo(maDonChon);
            List<String> maGheList = new java.util.ArrayList<>();
            
            if (don != null && don.getDanhSachVe() != null) {
                for (DonTreoDat.ThongTinVeTam ve : don.getDanhSachVe()) {
                    if (ve.getChoNgoi() != null) {
                        maGheList.add(ve.getChoNgoi().getMaChoNgoi());
                    }
                }
            }
            
            System.out.println("🗑️ Hủy đơn " + maDonChon + " - Giải phóng " + maGheList.size() + " ghế");
            
            // Gọi lệnh xóa đơn treo trên Server (Server sẽ xóa cứng trong Database)
            boolean thanhCong = QuanLyDonTreo.xoaDonTreo(maDonChon);
            
            if (thanhCong) {
                System.out.println("✅ Đã hủy đơn " + maDonChon + " trên Server. Tiến hành giải phóng " + maGheList.size() + " ghế.");
                
                // ✅ GIẢI PHÓNG TRÊN RAM CLIENT: Xóa khỏi danh sách giữ chỗ local và remote
                QuanLyGheGiuCho.xoaTatCaGheCuaDonTreo(maDonChon, maGheList);
                
                // Reload table trong dialog
                loadTatCaDonTreo();
                
                // ⚡ REFRESH SƠ ĐỒ GHẾ NGAY LẬP TỨC TRÊN GUI BÁN VÉ
                if (guiBanVe != null) {
                    guiBanVe.reloadSoDoGhe();
                }
                
                JOptionPane.showMessageDialog(this,
                    "Hủy đơn thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Lỗi: Không thể hủy đơn trên hệ thống!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnHuyDonActionPerformed

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
            java.util.logging.Logger.getLogger(Dialog_TreoDon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dialog_TreoDon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dialog_TreoDon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dialog_TreoDon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Dialog_TreoDon dialog = new Dialog_TreoDon(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnHuyDon;
    private javax.swing.JButton btnTim;
    private javax.swing.JButton btnXuLi;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTable tblThongTin;
    // End of variables declaration//GEN-END:variables
}
