package iuh.fit.gui.menu.component;

import iuh.fit.gui.menu.event.EventMenu;
import iuh.fit.gui.menu.event.EventMenuSelected;
import iuh.fit.gui.menu.event.EventShowPopupMenu;
import iuh.fit.gui.menu.model.ModelMenu;
import iuh.fit.gui.menu.swing.MenuAnimation;
import iuh.fit.gui.menu.swing.MenuItem;
import iuh.fit.gui.menu.swing.scrollbar.ScrollBarCustom;

import java.awt.Component;
import javax.swing.ImageIcon;
import net.miginfocom.swing.MigLayout;

public class Menu extends javax.swing.JPanel {

    public boolean isShowMenu() {
        return showMenu;
    }

    public void addEvent(EventMenuSelected event) {
        this.event = event;
    }

    public void setEnableMenu(boolean enableMenu) {
        this.enableMenu = enableMenu;
    }

    public void setShowMenu(boolean showMenu) {
        this.showMenu = showMenu;
    }

    public void addEventShowPopup(EventShowPopupMenu eventShowPopup) {
        this.eventShowPopup = eventShowPopup;
    }

    private final MigLayout layout;
    private EventMenuSelected event;
    private EventShowPopupMenu eventShowPopup;
    private boolean enableMenu = true;
    private boolean showMenu = true;

    public Menu() {
        initComponents();
        setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setVerticalScrollBar(new ScrollBarCustom());
        layout = new MigLayout("wrap, fillx, insets 0", "[fill]", "[]50[]");
        panel.setLayout(layout);
    }

    public void initMenuItem() {
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/dashboard.png")), "Dashboard"));
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/trainticket.png")), "Vé", "Bán Vé", "Trả Vé"));
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/customer.png")), "Khách Hàng"));
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/employee.png")), "Nhân Viên"));
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/sale.png")), "Khuyến Mãi", "Khuyến mãi theo hóa đơn", "Khuyến mãi theo đối tượng"));
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/thongke.png")), "Thống Kê", "Doanh Thu", "Lượt Vé"));
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/help.png")), "Trợ Giúp"));
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/out.png")), "Đăng Xuất"));

    }
    
    /**
     * Khởi tạo menu dựa trên chức vụ
     * @param chucVu 0 = Quản lý (full quyền), 1 = Nhân viên (hạn chế)
     */
    public void initMenuItemByRole(int chucVu) {
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/dashboard.png")), "Dashboard"));
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/trainticket.png")), "Vé", "Bán Vé", "Trả Vé"));
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/customer.png")), "Khách Hàng"));
        
        // Chỉ Quản lý (chucVu = 0) mới thấy menu Nhân Viên và Khuyến Mãi
        if (chucVu == 0) {
            addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/employee.png")), "Nhân Viên"));
            addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/sale.png")), "Khuyến Mãi", "Khuyến mãi theo hóa đơn", "Khuyến mãi theo đối tượng"));
        }
        
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/thongke.png")), "Thống Kê", "Doanh Thu", "Lượt Vé"));
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/help.png")), "Trợ Giúp"));
        addMenu(new ModelMenu(new ImageIcon(getClass().getResource("/icon/out.png")), "Đăng Xuất"));
    }

    private void addMenu(ModelMenu menu) {
        panel.add(new MenuItem(menu, getEventMenu(), event, panel.getComponentCount()), "h 40!");
    }

    private EventMenu getEventMenu() {
        return new EventMenu() {
            @Override
            public boolean menuPressed(Component com, boolean open) {
                if (enableMenu) {
                    if (isShowMenu()) {
                        if (open) {
                            new MenuAnimation(layout, com).openMenu();
                        } else {
                            new MenuAnimation(layout, com).closeMenu();
                        }
                        return true;
                    } else {
                        eventShowPopup.showPopup(com);
                    }
                }
                return false;
            }
        };
    }

    public void hideallMenu() {
        for (Component com : panel.getComponents()) {
            MenuItem item = (MenuItem) com;
            if (item.isOpen()) {
                new MenuAnimation(layout, com, 500).closeMenu();
                item.setOpen(false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sp = new javax.swing.JScrollPane();
        panel = new javax.swing.JPanel();
        profile1 = new Profile();

        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setViewportBorder(null);

        panel.setOpaque(false);

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 312, Short.MAX_VALUE)
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 523, Short.MAX_VALUE)
        );

        sp.setViewportView(panel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sp, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
            .addComponent(profile1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(profile1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sp, javax.swing.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // Gradient xanh pastel nhẹ
        java.awt.GradientPaint gp = new java.awt.GradientPaint(
                0, 0, new java.awt.Color(200, 230, 255),   // xanh trắng nhạt ở trên
                0, getHeight(), new java.awt.Color(140, 190, 255) // xanh sáng ở dưới
        );

        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel panel;
    private Profile profile1;
    private javax.swing.JScrollPane sp;
    // End of variables declaration//GEN-END:variables
}

