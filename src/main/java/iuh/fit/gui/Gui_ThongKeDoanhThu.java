package iuh.fit.gui;

import com.toedter.calendar.JDateChooser;
import iuh.fit.utils.ClientContext;
import service.IThongKeService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;

import entity.HoaDon;
import entity.ChiTietHoaDon;

public class Gui_ThongKeDoanhThu extends JPanel {

    private JLabel lblDoanhThu, lblSoHoaDon, lblHoaDonTra, lblSoVeTra;
    private JTable table;
    private DefaultTableModel tableModel;
    private DefaultCategoryDataset dataset;
    private DefaultPieDataset pieDataset;
    private IThongKeService thongKeService;
    private JComboBox<String> cbThang, cbNam;
    private JFreeChart barChart;

    public Gui_ThongKeDoanhThu() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        thongKeService = ClientContext.getThongKeService();

        // =================== PANEL LỌC ĐƯA LÊN TRÊN CÙNG ===================
        add(createFilterPanel(), BorderLayout.NORTH);

        // =================== PANEL CHỨA 4 CARD + BIỂU ĐỒ + BẢNG ===================
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        centerPanel.add(createStatCardsPanel(), BorderLayout.NORTH);
        centerPanel.add(createStatsPanel(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        
        // Load dữ liệu mặc định: tháng và năm hiện tại
        LocalDate now = LocalDate.now();
        loadData(now.getMonthValue(), now.getYear());
    }

    // =================== 4 CARD THỐNG KÊ (MÀU TRẮNG - CHỮ XÁM) ===================
    private JPanel createStatCardsPanel() {
        JPanel panelStats = new JPanel(new GridLayout(1, 4, 20, 10));
        panelStats.setBorder(new EmptyBorder(10, 20, 10, 20));
        panelStats.setBackground(Color.WHITE);

        Color textGray = new Color(60, 60, 60); // Xám đậm

        lblDoanhThu = createStatCard("Tổng doanh thu trong tháng", "0 ₫",
                Color.WHITE, textGray);
        lblSoHoaDon = createStatCard("Số hóa đơn trong tháng", "0",
                Color.WHITE, textGray);
        lblHoaDonTra = createStatCard("Số hóa đơn trả", "0",
                Color.WHITE, textGray);
        lblSoVeTra = createStatCard("Số vé trả", "0",
                Color.WHITE, textGray);

        panelStats.add(lblDoanhThu.getParent());
        panelStats.add(lblSoHoaDon.getParent());
        panelStats.add(lblHoaDonTra.getParent());
        panelStats.add(lblSoVeTra.getParent());

        return panelStats;
    }

    private JLabel createStatCard(String title, String value, Color bg, Color textColor) {

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(200, 70));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setForeground(textColor);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.BOLD, 24));
        lblValue.setForeground(textColor);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(lblValue, BorderLayout.CENTER);

        return lblValue;
    }

    // =================== PANEL LỌC (TRÊN ĐẦU) ===================
    private JPanel createFilterPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(BorderFactory.createTitledBorder("Lọc"));
        p.setBackground(Color.WHITE);

        String[] months = {"01","02","03","04","05","06","07","08","09","10","11","12"};
        cbThang = new JComboBox<>(months);
        cbThang.setSelectedIndex(LocalDate.now().getMonthValue() - 1); // Tháng hiện tại

        String[] years = new String[51];
        for (int i = 0; i <= 50; i++) years[i] = String.valueOf(2020 + i);
        cbNam = new JComboBox<>(years);
        cbNam.setSelectedItem(String.valueOf(LocalDate.now().getYear())); // Năm hiện tại

        JButton btnLoc = new JButton("Lọc");
        
        // Sự kiện khi nhấn nút Lọc
        btnLoc.addActionListener(e -> {
            int thang = cbThang.getSelectedIndex() + 1;
            int nam = Integer.parseInt((String) cbNam.getSelectedItem());
            loadData(thang, nam);
        });

        p.add(new JLabel("Tháng:"));
        p.add(cbThang);
        p.add(new JLabel("Năm:"));
        p.add(cbNam);
        p.add(btnLoc);

        return p;
    }

    // =================== BIỂU ĐỒ + BẢNG ===================
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(Color.WHITE);

        // ===== BIỂU ĐỒ =====
        dataset = new DefaultCategoryDataset();

        for (int day = 1; day <= 31; day++) {
            dataset.addValue(0, "Doanh thu", String.valueOf(day));
        }

        barChart = ChartFactory.createBarChart(
                "Doanh thu theo ngày",
                "Ngày",
                "Doanh thu",
                dataset
        );

         // Nền tổng thể chart
        barChart.setBackgroundPaint(Color.WHITE);

         // Lấy plot để chỉnh nền vùng hiển thị cột
        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);      // nền vùng cột
        plot.setRangeGridlinePaint(Color.GRAY); // màu lưới ngang

        ChartPanel chartPanel = new ChartPanel(barChart);

        JPanel chartWrapper = new JPanel(new BorderLayout());
        chartWrapper.setPreferredSize(new Dimension(1000, 350));
        chartWrapper.add(chartPanel, BorderLayout.CENTER);

        statsPanel.add(chartWrapper, BorderLayout.NORTH);


        // ===== BẢNG =====
        String[] cols = {"Mã hóa đơn", "Mã nhân viên", "Mã khách hàng",
                "Ngày tạo", "Giờ tạo", "Tổng tiền"};

        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1000, 200));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Thông tin hóa đơn"));

        statsPanel.add(scrollPane, BorderLayout.CENTER);

        return statsPanel;
    }


    // =================== CẬP NHẬT BẢNG ===================
    private void capNhatBang(List<HoaDon> ds) {
        tableModel.setRowCount(0);

        for (HoaDon hd : ds) {
            double tongTien = hd.getDanhSachChiTiet().stream()
                    .mapToDouble(ChiTietHoaDon::tinhThanhTien)
                    .sum();

            tableModel.addRow(new Object[]{
                    hd.getMaHoaDon(),
                    hd.getNhanVien().getMaNhanVien(),
                    hd.getKhachHang().getMaKH(),
                    hd.getNgayTao(),
                    hd.getGioTao(),
                    String.format("%,.0f", tongTien)
            });
        }
    }

    // =================== CẬP NHẬT BIỂU ĐỒ ===================
    private void capNhatBieuDo(List<HoaDon> ds) {
        for (int i = 1; i <= 31; i++)
            dataset.setValue(0, "Doanh thu", String.valueOf(i));

        for (HoaDon hd : ds) {
            if (hd.getNgayTao() == null) continue;

            int day = hd.getNgayTao().getDayOfMonth();

            double sum = hd.getDanhSachChiTiet().stream()
                    .filter(ct -> ct.getMucGiam() >= 0) // loại vé trả
                    .mapToDouble(ChiTietHoaDon::tinhThanhTien)
                    .sum();


            double old = dataset.getValue("Doanh thu", String.valueOf(day)).doubleValue();
            dataset.setValue(old + sum, "Doanh thu", String.valueOf(day));
        }

    }
    
    // =================== LOAD DATA ===================
    private void loadData(int thang, int nam) {
        // Cập nhật tiêu đề biểu đồ
        barChart.setTitle("Doanh thu theo ngày - Tháng " + thang + "/" + nam);
        
        // Lấy dữ liệu từ service
        List<HoaDon> dsHoaDon = new java.util.ArrayList<>();
        double tongDoanhThu = 0;
        try {
            dsHoaDon = thongKeService.loadHoaDonTheoThangNam(thang, nam);
            tongDoanhThu = thongKeService.getTongDoanhThu(thang, nam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int tongSoHoaDon = dsHoaDon.size();
        
        // ⚡ TÍNH SỐ VÉ TRẢ: Đếm vé có trangThai = 0 (đã trả) trong các hóa đơn của tháng này
        int soVeTra = 0;
        int soHoaDonCoVeTra = 0; // Số hóa đơn có ít nhất 1 vé bị trả
        
        for (HoaDon hd : dsHoaDon) {
            int soVeTraTrongHoaDon = 0;
            for (ChiTietHoaDon cthd : hd.getDanhSachChiTiet()) {
                // Kiểm tra xem vé đã bị trả chưa (trangThai = 0)
                entity.Ve ve = cthd.getVe();
                if (ve != null && !ve.isTrangThai()) { // trangThai = false/0 = đã trả
                    soVeTra++;
                    soVeTraTrongHoaDon++;
                }
            }
            if (soVeTraTrongHoaDon > 0) {
                soHoaDonCoVeTra++;
            }
        }
        
        // Cập nhật 4 card
        lblDoanhThu.setText(String.format("%,.0f ₫", tongDoanhThu));
        lblSoHoaDon.setText(String.valueOf(tongSoHoaDon));
        lblHoaDonTra.setText(String.valueOf(soHoaDonCoVeTra)); // Số hóa đơn có vé bị trả
        lblSoVeTra.setText(String.valueOf(soVeTra)); // Tổng số vé bị trả
        
        // Cập nhật bảng
        capNhatBang(dsHoaDon);
        
        // Cập nhật biểu đồ
        capNhatBieuDo(dsHoaDon);
    }
}
