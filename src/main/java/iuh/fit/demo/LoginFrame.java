package iuh.fit.demo;

import iuh.fit.gui.Gui_Login;
import iuh.fit.utils.ClientContext;

import javax.swing.*;

/**
 * JFrame để hiển thị màn hình đăng nhập
 */
public class LoginFrame extends JFrame {
    
    public LoginFrame() {
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Hệ Thống Quản Lý Vé Tàu - Đăng Nhập");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Chỉ đóng frame này, không thoát app
        setUndecorated(true); // Bỏ viền window để đẹp hơn
        
        // Thêm Gui_Login vào frame
        Gui_Login loginPanel = new Gui_Login();
        add(loginPanel);
        
        // Pack để tự động điều chỉnh kích thước theo panel
        pack();
        
        // Căn giữa màn hình
        setLocationRelativeTo(null);
        
        // Set resizable
        setResizable(false);
    }
    
    public static void main(String[] args) {
        // Kết nối RMI Server trước
        try {
            ClientContext.init();
            System.out.println("✅ Kết nối RMI Server thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "❌ LỖI KẾT NỐI SERVER:\n" + 
                "1. Hãy chắc chắn dự án HTQLBVT (Server) đang chạy.\n" +
                "2. Kiểm tra IP trong ClientContext.java (phải là localhost hoặc IP máy server).\n\n" +
                "Chi tiết: " + e.getMessage(), 
                "Lỗi RMI", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Chạy GUI
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) { // Đổi thành giao diện Nimbus
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                java.util.logging.Logger.getLogger(LoginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

