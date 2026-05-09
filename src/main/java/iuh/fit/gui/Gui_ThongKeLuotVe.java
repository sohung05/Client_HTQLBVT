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

import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;

public class Gui_ThongKeLuotVe extends JPanel {

    // CARD THỐNG KÊ

    private JLabel lblKhach, lblSoVe, lblChuyen;
    private JTable table;
    private DefaultTableModel tableModel;


    // ComboBox lọc
    private JComboBox<String> cbThang, cbNam;
    private JButton btnLoc;
    
    // service và datasets
    private IThongKeService thongKeService;
    private DefaultCategoryDataset barDataset;
    private DefaultPieDataset pieDataset;
    private JFreeChart barChart;
    private JFreeChart pieChart;

    public Gui_ThongKeLuotVe() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        thongKeService = ClientContext.getThongKeService();

        add(createFilterPanel(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        centerPanel.add(createObjectStatsPanel(), BorderLayout.CENTER);
        centerPanel.add(createDailyChartPanel(), BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
        
        // Load dữ liệu mặc định: tháng và năm hiện tại
        LocalDate now = LocalDate.now();
        loadData(now.getMonthValue(), now.getYear());
    }

    private JPanel createStatCardsPanel() {
        JPanel panelStats = new JPanel();
        panelStats.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelStats.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelStats.setBackground(Color.WHITE);

        Color textGray = new Color(60, 60, 60);

        lblKhach = createStatCard("Tổng lượng khách", "0", Color.WHITE, textGray);
        lblSoVe = createStatCard("Tổng số vé đã đặt", "0", Color.WHITE, textGray);
        lblChuyen = createStatCard("Tổng số chuyến", "0", Color.WHITE, textGray);

        panelStats.add(lblKhach.getParent());
        panelStats.add(lblSoVe.getParent());
        panelStats.add(lblChuyen.getParent());

        return panelStats;
    }

    private JLabel createStatCard(String title, String value, Color bg, Color textColor) {


        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setPreferredSize(new Dimension(170, 70));


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

    // =================== PANEL LỌC ===================
    private JPanel createFilterPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(BorderFactory.createTitledBorder("Lọc"));
        p.setBackground(Color.WHITE);

        String[] months = {"01","02","03","04","05","06","07","08","09","10","11","12"};
        cbThang = new JComboBox<>(months);
        cbThang.setSelectedIndex(LocalDate.now().getMonthValue() - 1); // Tháng hiện tại

        String[] years = new String[11];
        for (int i = 0; i <= 10; i++) years[i] = String.valueOf(2020 + i);
        cbNam = new JComboBox<>(years);
        cbNam.setSelectedItem(String.valueOf(LocalDate.now().getYear())); // Năm hiện tại

        btnLoc = new JButton("Lọc");
        
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

    private JPanel createObjectStatsPanel() {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // ======================= CỘT TRÁI =======================
        JPanel leftColumn = new JPanel(new BorderLayout());
        leftColumn.setBackground(Color.WHITE);
        leftColumn.setPreferredSize(new Dimension(550, 300));

        // 3 CARD
        JPanel cards = createStatCardsPanel();
        leftColumn.add(cards, BorderLayout.NORTH);

        // Tạo bảng
        String[] cols = {"Chuyến", "Số lượt đi"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);

// Tạo JScrollPane và đặt border với tiêu đề
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(550, 230));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Chuyến đi nhiều nhất trong tháng"));
// Thêm vào panel
        leftColumn.add(scrollPane, BorderLayout.CENTER);


        // ======================= CỘT PHẢI =======================
        JPanel rightColumn = new JPanel(new BorderLayout());
        rightColumn.setBackground(Color.WHITE);
        rightColumn.setPreferredSize(new Dimension(420, 300));

        // Pie chart
        pieDataset = new DefaultPieDataset();
        pieChart = ChartFactory.createPieChart(
                "Tỉ lệ khách hàng theo đối tượng",
                pieDataset,
                true, false, false
        );

        org.jfree.chart.plot.PiePlot piePlot = (org.jfree.chart.plot.PiePlot) pieChart.getPlot();
        piePlot.setBackgroundPaint(Color.WHITE); // nền vùng hiển thị Pie

        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setBackground(Color.WHITE);   // nền toàn bộ chart panel
        rightColumn.add(chartPanel, BorderLayout.CENTER);

        // ======================= THÊM VÀO MAIN =======================
        mainPanel.add(leftColumn, BorderLayout.WEST);
        mainPanel.add(rightColumn, BorderLayout.CENTER);

        return mainPanel;
    }


    // =================== BAR CHART THEO NGÀY ===================
    private JPanel createDailyChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        barDataset = new DefaultCategoryDataset();

        // Thêm 31 ngày vào trục X với giá trị 0 tạm thời
        for (int day = 1; day <= 31; day++) {
            barDataset.addValue(0, "Số lượt vé", String.valueOf(day));
        }

        barChart = ChartFactory.createBarChart(
                "Thống kê lượt vé theo ngày",
                "Ngày",
                "Số lượt vé",
                barDataset
        );
        barChart.setBackgroundPaint(Color.WHITE);

        // Lấy plot để chỉnh nền vùng hiển thị cột
        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);      // nền vùng cột
        plot.setRangeGridlinePaint(Color.GRAY);
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(1000, 300));

        panel.add(chartPanel, BorderLayout.CENTER);
        return panel;
    }
    
    // =================== LOAD DATA ===================
    private void loadData(int thang, int nam) {
        // Cập nhật tiêu đề biểu đồ
        barChart.setTitle("Thống kê lượt vé theo ngày - Tháng " + thang + "/" + nam);
        pieChart.setTitle("Tỉ lệ khách hàng theo đối tượng - Tháng " + thang + "/" + nam);
        
        // Lấy dữ liệu từ service
        try {
            int tongLuongKhach = thongKeService.getTongLuotKhach(thang, nam);
            int tongSoVe = thongKeService.getTongSoVe(thang, nam);
            int tongSoTuyen = thongKeService.getTongSoTuyen(thang, nam);
            
            // Cập nhật 3 card
            lblKhach.setText(String.valueOf(tongLuongKhach));
            lblSoVe.setText(String.valueOf(tongSoVe));
            lblChuyen.setText(String.valueOf(tongSoTuyen));
            
            // Cập nhật bảng "Chuyến đi nhiều nhất"
            tableModel.setRowCount(0);
            java.util.List<Object[]> tuyenNhieuNhat = thongKeService.getTuyenNhieuNhatTrongThang(thang, nam);
            for (Object[] row : tuyenNhieuNhat) {
                tableModel.addRow(new Object[]{
                    row[0],  // Tên tuyến
                    row[1]   // Số lượt
                });
            }
            
            // Cập nhật Pie Chart "Tỉ lệ khách hàng theo đối tượng"
            pieDataset.clear();
            Map<String, Integer> tyLeDoiTuong = thongKeService.getTiLeKhachHangTheoDoiTuong(thang, nam);
            for (Map.Entry<String, Integer> entry : tyLeDoiTuong.entrySet()) {
                String tenDoiTuong = entry.getKey() != null ? entry.getKey().toString() : "Không xác định";
                pieDataset.setValue(tenDoiTuong, entry.getValue());
            }
            
            // Cập nhật Bar Chart "Số lượt vé theo ngày"
            // Reset tất cả các ngày về 0
            for (int day = 1; day <= 31; day++) {
                barDataset.setValue(0, "Số lượt vé", String.valueOf(day));
            }
            
            // Load dữ liệu thực từ service
            Map<Integer, Integer> soVeTheoNgay = thongKeService.getSoVeTheoNgay(thang, nam);
            for (Map.Entry<Integer, Integer> entry : soVeTheoNgay.entrySet()) {
                int ngay = entry.getKey();
                int soVe = entry.getValue();
                barDataset.setValue(soVe, "Số lượt vé", String.valueOf(ngay));
            }
            
            System.out.println("✅ Đã load dữ liệu Thống kê lượt vé tháng " + thang + "/" + nam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
