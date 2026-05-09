package iuh.fit.demo;

import entity.NhanVien;
import iuh.fit.gui.menu.component.Header;
import iuh.fit.gui.menu.component.Menu;
import iuh.fit.gui.menu.event.EventMenuSelected;
import iuh.fit.gui.menu.event.EventShowPopupMenu;
import iuh.fit.gui.menu.form.MainForm;
import iuh.fit.gui.menu.swing.MenuItem;
import iuh.fit.gui.menu.swing.PopupMenu;
import iuh.fit.gui.menu.swing.icon.GoogleMaterialDesignIcons;
import iuh.fit.gui.menu.swing.icon.IconFontSwing;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.TimingTargetAdapter;
import iuh.fit.utils.ClientContext;
import iuh.fit.utils.SessionManager;
import iuh.fit.gui.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Main extends javax.swing.JFrame {

    private MigLayout layout;
    private Menu menu;
    private Header header;
    private MainForm main;
    private Animator animator;
    private int currentMenuIndex = 0;
    private int currentSubMenuIndex = -1;
    private Gui_NhapThongTinHanhTrinh.ThongTinHanhTrinh lastThongTinHanhTrinh;

    public Main() {
        initComponents();
        init();
        setExtendedState(getExtendedState() | javax.swing.JFrame.MAXIMIZED_BOTH);
        NhanVien nv = SessionManager.getInstance().getNhanVienDangNhap();

        // Truyền vào header
        if (header != null && nv != null) {
            header.setNhanVien(nv);
        }
    }

    private void init() {
        layout = new MigLayout("fill", "0[]0[100%, fill]0", "0[fill, top]0");
        bg.setLayout(layout);
        menu = new Menu();
        header = new Header();
        main = new MainForm();
        
        // Lấy chức vụ từ session
        NhanVien nv = SessionManager.getInstance().getNhanVienDangNhap();
        int chucVu = (nv != null) ? nv.getChucVu() : 0; // Mặc định 0 = Quản lý
        
        // DEBUG: Hiển thị thông tin phân quyền
        if (nv != null) {
            System.out.println("👤 Đăng nhập: " + nv.getHoTen() + " | Chức vụ: " + chucVu + " (" + (chucVu == 0 ? "Quản lý" : "Nhân viên") + ")");
        } else {
            System.out.println("⚠️ Chưa login, dùng quyền mặc định: Quản lý");
        }
        
        // ✅ QUAN TRỌNG: Phải SET EVENT TRƯỚC khi init menu!
        menu.addEvent(new EventMenuSelected() {
            @Override
            public void menuSelected(int menuIndex, int subMenuIndex) {
                currentMenuIndex = menuIndex;
                currentSubMenuIndex = subMenuIndex;
                showSelectedForm(menuIndex, subMenuIndex);
            }
        });
        
        // ✅ QUAN TRỌNG: Init menu SAU khi đã set event!
        menu.initMenuItemByRole(chucVu);
        
        menu.addEventShowPopup(new EventShowPopupMenu() {
            @Override
            public void showPopup(Component com) {
                MenuItem item = (MenuItem) com;
                PopupMenu popup = new PopupMenu(Main.this, item.getIndex(), item.getEventSelected(), item.getMenu().getSubMenu());
                int x = Main.this.getX() + 52;
                int y = Main.this.getY() + com.getY() + 86;
                popup.setLocation(x, y);
                popup.setVisible(true);
            }
        });
        // menu.initMenuItem(); // ❌ BỎ: Đã init menu theo role ở trên rồi, không cần init lại
        bg.add(menu, "w 230!, spany 2");    // Span Y 2cell
        bg.add(header, "h 50!, wrap");
        bg.add(main, "w 100%, h 100%");
        TimingTarget target = new TimingTargetAdapter() {
            @Override
            public void timingEvent(float fraction) {
                double width;
                if (menu.isShowMenu()) {
                    width = 60 + (170 * (1f - fraction));
                } else {
                    width = 60 + (170 * fraction);
                }
                layout.setComponentConstraints(menu, "w " + width + "!, spany2");
                menu.revalidate();
            }

            @Override
            public void end() {
                menu.setShowMenu(!menu.isShowMenu());
                menu.setEnableMenu(true);
            }

        };
        animator = new Animator(500, target);
        animator.setResolution(0);
        header.addMenuEvent(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (!animator.isRunning()) {
                    animator.start();
                }
                menu.setEnableMenu(false);
                if (menu.isShowMenu()) {
                    menu.hideallMenu();
                }
            }
        });
        header.addReloadEvent(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("🔄 Reloading current form - Menu: " + currentMenuIndex + " | SubMenu: " + currentSubMenuIndex);
                showSelectedForm(currentMenuIndex, currentSubMenuIndex);
            }
        });
        //  Init google icon font
        IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
        //  Start with this form
        main.showForm(new Gui_Dashboard());
    }

    private void showSelectedForm(int menuIndex, int subMenuIndex) {
        NhanVien nv = SessionManager.getInstance().getNhanVienDangNhap();
        int chucVu = (nv != null) ? nv.getChucVu() : 0;

        System.out.println("📍 Showing Form - Index: " + menuIndex + " | SubMenu: " + subMenuIndex + " | ChucVu: " + chucVu);

        // Nếu là Nhân viên (chucVu = 1), điều chỉnh menu index
        int adjustedMenuIndex = menuIndex;
        if (chucVu == 1 && menuIndex >= 3) {
            adjustedMenuIndex = menuIndex + 2;
        }

        switch (adjustedMenuIndex) {
            case 0: // Dashboard
                main.showForm(new Gui_Dashboard());
                break;
            case 1: // Vé
                switch (subMenuIndex) {
                    case 0: // Bán Vé
                        // Nếu đang ở Gui_BanVe thì nạp lại chính trang đó với info cũ
                        if (main.getCurrentForm() instanceof Gui_BanVe && lastThongTinHanhTrinh != null) {
                            main.showForm(new Gui_BanVe(lastThongTinHanhTrinh));
                        } else {
                            // Nếu không thì mới hiện trang nhập thông tin
                            Gui_NhapThongTinHanhTrinh guiNhapThongTin = new Gui_NhapThongTinHanhTrinh();
                            guiNhapThongTin.setCallback(info -> {
                                lastThongTinHanhTrinh = info;
                                Gui_BanVe guiBanVe = new Gui_BanVe(info);
                                main.showForm(guiBanVe);
                            });
                            main.showForm(guiNhapThongTin);
                        }
                        break;
                    case 1: // Trả Vé
                        main.showForm(new Gui_TraVe());
                        break;
                    case 2: // Đổi Vé
                        main.showForm(new Gui_DoiVe());
                        break;
                }
                break;
            case 2: // Khách Hàng
                main.showForm(new Gui_KhachHang());
                break;
            case 3: // Nhân Viên
                main.showForm(new Gui_NhanVien());
                break;
            case 4: // Khuyến Mãi
                switch (subMenuIndex) {
                    case 0: // KM Hóa Đơn
                        main.showForm(new Gui_KhuyenMaiHoaDon());
                        break;
                    case 1: // KM Đối Tượng
                        main.showForm(new Gui_KhuyenMaiDoiTuong());
                        break;
                }
                break;
            case 5: // Thống Kê
                switch (subMenuIndex) {
                    case 0: // Doanh Thu
                        main.showForm(new Gui_ThongKeDoanhThu());
                        break;
                    case 1: // Lượt Vé
                        main.showForm(new Gui_ThongKeLuotVe());
                        break;
                }
                break;
            case 6: // Trợ Giúp
                try {
                    java.io.File file = new java.io.File("src/main/resources/TroGiup/index.html");
                    if (file.exists()) {
                        Desktop.getDesktop().browse(file.toURI());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 7: // Đăng Xuất
                utils.SessionManager.getInstance().logout();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                });
                javax.swing.SwingUtilities.getWindowAncestor(main).dispose();
                break;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bg = new javax.swing.JLayeredPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(false);

        bg.setBackground(new Color(245, 245, 245));
        bg.setOpaque(true);

        javax.swing.GroupLayout bgLayout = new javax.swing.GroupLayout(bg);
        bg.setLayout(bgLayout);
        bgLayout.setHorizontalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1366, Short.MAX_VALUE)
        );
        bgLayout.setVerticalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 783, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bg)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        // ✅ Kết nối RMI Server trước khi chạy GUI
        try {
            ClientContext.init();
            System.out.println("✅ Kết nối RMI Server thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Không thể kết nối đến RMI Server!\n" + e.getMessage(),
                "Lỗi kết nối", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        
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
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Thay vì mở Main, chúng ta mở LoginFrame
                new LoginFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane bg;
    // End of variables declaration//GEN-END:variables
}
