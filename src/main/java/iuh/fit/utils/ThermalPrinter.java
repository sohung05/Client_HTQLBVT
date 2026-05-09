package iuh.fit.utils;

import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.Ve;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.*;
import java.awt.print.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Tiện ích in ấn ra máy in nhiệt (K58)
 */
public class ThermalPrinter {

    private HoaDon hoaDon;
    private List<ChiTietHoaDon> chiTietList;
    private boolean isAutoPrint = false; // ⚡ Mặc định là in thủ công
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ThermalPrinter(HoaDon hoaDon, List<ChiTietHoaDon> chiTietList) {
        this.hoaDon = hoaDon;
        this.chiTietList = chiTietList;
    }

    public void setAutoPrint(boolean autoPrint) {
        this.isAutoPrint = autoPrint;
    }

    /**
     * In hóa đơn ra máy in nhiệt (có dialog để chọn máy in)
     */
    public boolean printInvoice() {
        try {
            // Lấy máy in mặc định (thường là Xprinter/K58)
            PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();

            if (defaultPrintService == null) {
                System.err.println("❌ Không tìm thấy máy in mặc định!");
                return false;
            }

            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPrintService(defaultPrintService);

            // Thiết lập khổ giấy K58 (58mm)
            PageFormat pageFormat = printerJob.defaultPage();
            Paper paper = pageFormat.getPaper();

            // 58mm quy đổi ra points (1 point = 1/72 inch, 1 inch = 25.4mm)
            double width = 58 * 72 / 25.4;
            double height = 1000; // Chiều dài tự do
            paper.setSize(width, height);
            paper.setImageableArea(0, 0, width, height);

            pageFormat.setPaper(paper);

            // Thiết lập nội dung in
            printerJob.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) return NO_SUCH_PAGE;

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    int y = 15;
                    int lineHeight = 12;
                    int x = 5;

                    // Font chữ
                    Font boldFont = new Font("Monospaced", Font.BOLD, 10);
                    Font normalFont = new Font("Monospaced", Font.PLAIN, 8);
                    Font smallFont = new Font("Monospaced", Font.PLAIN, 7);

                    // ========================
                    // 1. TIÊU ĐỀ
                    // ========================
                    g2d.setFont(boldFont);
                    drawCenteredText(g2d, "ĐƯỜNG SẮT VIỆT NAM", y, (int) pageFormat.getImageableWidth());
                    y += lineHeight;
                    drawCenteredText(g2d, "HÓA ĐƠN BÁN VÉ", y, (int) pageFormat.getImageableWidth());
                    y += lineHeight + 5;

                    // ========================
                    // 2. THÔNG TIN CHUNG
                    // ========================
                    g2d.setFont(normalFont);
                    g2d.drawString("Mã HĐ: " + hoaDon.getMaHoaDon(), x, y);
                    y += lineHeight;
                    g2d.drawString("Ngày: " + hoaDon.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), x, y);
                    y += lineHeight;
                    g2d.drawString("Giờ: " + hoaDon.getGioTao().format(DateTimeFormatter.ofPattern("HH:mm:ss")), x, y);
                    y += lineHeight;
                    g2d.drawString("NV: " + (hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getHoTen() : "N/A"), x, y);
                    y += lineHeight + 5;

                    // ========================
                    // 3. THÔNG TIN KHÁCH HÀNG
                    // ========================
                    g2d.drawString("KH: " + (hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getHoTen() : "Khách vãng lai"), x, y);
                    y += lineHeight;
                    g2d.drawString("SĐT: " + (hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getSDT() : "N/A"), x, y);
                    y += lineHeight + 5;

                    // ========================
                    // 4. KẺ ĐƯỜNG PHÂN CÁCH
                    // ========================
                    drawDashedLine(g2d, x, y, (int) pageFormat.getImageableWidth() - 10);
                    y += 10;

                    // ========================
                    // 5. TIÊU ĐỀ BẢNG
                    // ========================
                    g2d.setFont(boldFont);
                    // Format: Loại (8) | Chặng (12) | SL (2) | Thành tiền (10)
                    String header = String.format("%-7s %-4s %-5s %-1s %10s", "Loại", "Đi", "Đến", "SL", "T.Tiền");
                    g2d.drawString(header, x, y);
                    y += lineHeight;
                    drawDashedLine(g2d, x, y, (int) pageFormat.getImageableWidth() - 10);
                    y += 10;

                    // ========================
                    // 6. CHI TIẾT VÉ
                    // ========================
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 7));

                    for (ChiTietHoaDon ct : chiTietList) {
                        Ve ve = ct.getVe();
                        if (ve == null) continue;

                        String loaiVe = ve.getLoaiVe() != null ? getLoaiVeAbbreviation(ve.getLoaiVe().getTenLoaiVe()) : "N/A";
                        String gaDi = "??";
                        String gaDen = "??";

                        if (ve.getGaDi() != null) {
                            gaDi = getGaAbbreviation(ve.getGaDi().getTenGa());
                        } else if (ve.getLichTrinh() != null && ve.getLichTrinh().getGaDi() != null) {
                            gaDi = getGaAbbreviation(ve.getLichTrinh().getGaDi().getTenGa());
                        }

                        if (ve.getGaDen() != null) {
                            gaDen = getGaAbbreviation(ve.getGaDen().getTenGa());
                        } else if (ve.getLichTrinh() != null && ve.getLichTrinh().getGaDen() != null) {
                            gaDen = getGaAbbreviation(ve.getLichTrinh().getGaDen().getTenGa());
                        }

                        int soLuong = ct.getSoLuong();
                        double giaVe = ct.getGiaVe();
                        double thanhTien = giaVe * soLuong;

                        String tienStr = formatMoneyFull(Math.abs(thanhTien));
                        String row = String.format("%-7s %-4s %-5s %-1s %10s", loaiVe, gaDi, gaDen, soLuong, tienStr);
                        g2d.drawString(row, x, y);
                        y += lineHeight;
                    }

                    y += 5;
                    drawDashedLine(g2d, x, y, (int) pageFormat.getImageableWidth() - 10);
                    y += 10;

                    // ========================
                    // 7. TỔNG CỘNG
                    // ========================
                    g2d.setFont(boldFont);
                    g2d.drawString("TỔNG TIỀN: " + formatMoneyFull(hoaDon.getTongTien()) + "đ", x, y);
                    y += lineHeight + 5;

                    // ========================
                    // 8. LỜI CHÀO
                    // ========================
                    g2d.setFont(smallFont);
                    drawCenteredText(g2d, "Cảm ơn quý khách!", y, (int) pageFormat.getImageableWidth());
                    y += lineHeight;
                    drawCenteredText(g2d, "Hẹn gặp lại!", y, (int) pageFormat.getImageableWidth());

                    return PAGE_EXISTS;
                }
            }, pageFormat);

            printerJob.print();
            
            // ⚡ LƯU LỊCH SỬ IN (CHẠY NGẦM)
            savePrintHistory(chiTietList);
            
            return true;

        } catch (PrinterException e) {
            System.err.println("❌ Lỗi in hóa đơn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String formatMoneyFull(double amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    private void drawCenteredText(Graphics2D g2d, String text, int y, int pageWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (pageWidth - textWidth) / 2;
        g2d.drawString(text, x, y);
    }

    private void drawDashedLine(Graphics2D g2d, int x, int y, int width) {
        int dashWidth = 3;
        int gapWidth = 2;
        int currentX = x;

        while (currentX < x + width) {
            int endX = Math.min(currentX + dashWidth, x + width);
            g2d.drawLine(currentX, y, endX, y);
            currentX += dashWidth + gapWidth;
        }
    }

    private String getGaAbbreviation(String tenGa) {
        if (tenGa == null || tenGa.isEmpty()) return "??";
        String[] words = tenGa.trim().split("\\s+");
        if (words.length >= 2) {
            return (words[0].substring(0, 1) + words[1].substring(0, 1)).toUpperCase();
        }
        return tenGa.substring(0, Math.min(tenGa.length(), 2)).toUpperCase();
    }

    private String getLoaiVeAbbreviation(String tenLoaiVe) {
        if (tenLoaiVe == null) return "N/A";
        String normalized = tenLoaiVe.toLowerCase()
                .replaceAll("à|á|ả|ã|ạ|ă|ằ|ắ|ẳ|ẵ|ặ|â|ầ|ấ|ẩ|ẫ|ậ", "a")
                .replaceAll("è|é|ẻ|ẽ|ẹ|ê|ề|ế|ể|ễ|ệ", "e")
                .replaceAll("ì|í|ỉ|ĩ|ị", "i")
                .replaceAll("ò|ó|ỏ|õ|ọ|ô|ồ|ố|ổ|ỗ|ộ|ơ|ờ|ớ|ở|ỡ|ợ", "o")
                .replaceAll("ù|ú|ủ|ũ|ụ|ư|ừ|ứ|ử|ữ|ự", "u")
                .replaceAll("ỳ|ý|ỷ|ỹ|ỵ", "y")
                .replaceAll("đ", "d");

        if (normalized.contains("sinh vien")) return "SV";
        if (normalized.contains("nguoi lon")) return "NL";
        if (normalized.contains("tre em")) return "TE";
        if (normalized.contains("nguoi cao tuoi")) return "NCT";
        
        String[] words = tenLoaiVe.trim().split("\\s+");
        StringBuilder abbr = new StringBuilder();
        for (int i = 0; i < Math.min(words.length, 3); i++) {
            if (!words[i].isEmpty()) abbr.append(words[i].charAt(0));
        }
        return abbr.toString().toUpperCase();
    }

    /**
     * In vé đơn lẻ
     */
    public static boolean printTicket(Ve ve) {
        return printTicket(ve, false);
    }

    public static boolean printTicket(Ve ve, boolean isAuto) {
        try {
            PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultPrintService == null) return false;

            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPrintService(defaultPrintService);

            PageFormat pageFormat = printerJob.defaultPage();
            Paper paper = pageFormat.getPaper();
            double width = 58 * 72 / 25.4;
            double height = 400;
            paper.setSize(width, height);
            paper.setImageableArea(0, 0, width, height);
            pageFormat.setPaper(paper);

            printerJob.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) return NO_SUCH_PAGE;

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    int y = 10;
                    int lineHeight = 15;
                    int x = 5;

                    Font boldFont = new Font("SansSerif", Font.BOLD, 9);
                    Font normalFont = new Font("SansSerif", Font.PLAIN, 8);
                    Font largeFont = new Font("SansSerif", Font.BOLD, 11);
                    Font smallFont = new Font("SansSerif", Font.PLAIN, 6);

                    g2d.setFont(boldFont);
                    drawCenteredTextStatic(g2d, "CÔNG TY CỔ PHẦN VẬN TẢI", y, (int) pageFormat.getImageableWidth());
                    y += lineHeight;
                    drawCenteredTextStatic(g2d, "ĐƯỜNG SẮT HKTA", y, (int) pageFormat.getImageableWidth());
                    y += lineHeight;

                    g2d.setFont(normalFont);
                    drawCenteredTextStatic(g2d, "THẺ LÊN TÀU HỎA/BOARDING PASS", y, (int) pageFormat.getImageableWidth());
                    y += lineHeight + 3;

                    g2d.drawString("MãVé/TicketID:" + ve.getMaVe(), x, y);
                    y += lineHeight + 2;

                    String gaDiText = ve.getGaDi() != null ? ve.getGaDi().getTenGa() : (ve.getLichTrinh() != null ? ve.getLichTrinh().getGaDi().getTenGa() : "");
                    String gaDenText = ve.getGaDen() != null ? ve.getGaDen().getTenGa() : (ve.getLichTrinh() != null ? ve.getLichTrinh().getGaDen().getTenGa() : "");

                    g2d.setFont(smallFont);
                    g2d.drawString("Ga Đi", x + 10, y);
                    g2d.drawString("Ga đến", x + 90, y);
                    y += 10;

                    g2d.setFont(largeFont);
                    g2d.drawString(gaDiText, x + 5, y);
                    g2d.drawString(gaDenText, x + 80, y);
                    y += lineHeight;

                    g2d.setFont(normalFont);
                    if (ve.getLichTrinh() != null && ve.getLichTrinh().getChuyenTau() != null) {
                        g2d.drawString("Tàu/Train: " + ve.getLichTrinh().getChuyenTau().getSoHieuTau(), x, y);
                        y += lineHeight;
                    }

                    if (ve.getLichTrinh() != null && ve.getLichTrinh().getGioKhoiHanh() != null) {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        g2d.drawString("Khởi hành: " + ve.getLichTrinh().getGioKhoiHanh().format(dtf), x, y);
                        y += lineHeight;
                    }

                    if (ve.getChoNgoi() != null) {
                        String toa = ve.getChoNgoi().getToa() != null ? String.valueOf(ve.getChoNgoi().getToa().getSoToa()) : "";
                        g2d.drawString("Toa: " + toa + " | Chỗ: " + ve.getChoNgoi().getViTri(), x, y);
                        y += lineHeight;
                    }

                    g2d.drawString("Khách: " + (ve.getTenKhachHang() != null ? ve.getTenKhachHang() : ""), x, y);
                    y += lineHeight;
                    
                    g2d.setFont(boldFont);
                    g2d.drawString("Giá: " + String.format("%,.0f VND", ve.getGiaVe()), x, y);

                    return PAGE_EXISTS;
                }
            }, pageFormat);

            printerJob.print();
            savePrintHistory(ve, isAuto ? "In sau bán (Tự động)" : "In vé đơn lẻ", isAuto);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void savePrintHistory(List<ChiTietHoaDon> list) {
        if (list == null || list.isEmpty()) return;
        new Thread(() -> {
            dao.LichSuInVe_DAO dao = new dao.LichSuInVe_DAO();
            entity.NhanVien nv = utils.SessionManager.getInstance().getNhanVienDangNhap();
            for (ChiTietHoaDon ct : list) {
                if (ct.getVe() != null) {
                    entity.LichSuInVe ls = entity.LichSuInVe.builder()
                            .ve(ct.getVe())
                            .nhanVien(nv)
                            .thoiGianIn(java.time.LocalDateTime.now())
                            .loaiIn(isAutoPrint ? "In sau bán (Tự động)" : "In hóa đơn")
                            .ghiChu(isAutoPrint ? "Hệ thống tự động in sau khi thanh toán" : "In từ quản lý")
                            .build();
                    dao.insert(ls);
                }
            }
        }).start();
    }

    private static void savePrintHistory(entity.Ve ve, String loaiIn, boolean isAuto) {
        if (ve == null) return;
        new Thread(() -> {
            dao.LichSuInVe_DAO dao = new dao.LichSuInVe_DAO();
            entity.NhanVien nv = utils.SessionManager.getInstance().getNhanVienDangNhap();
            entity.LichSuInVe ls = entity.LichSuInVe.builder()
                    .ve(ve)
                    .nhanVien(nv)
                    .thoiGianIn(java.time.LocalDateTime.now())
                    .loaiIn(loaiIn)
                    .ghiChu(isAuto ? "Hệ thống tự động in sau khi thanh toán" : "In lẻ từ quản lý")
                    .build();
            dao.insert(ls);
        }).start();
    }

    private static void drawCenteredTextStatic(Graphics2D g2d, String text, int y, int pageWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (pageWidth - textWidth) / 2;
        g2d.drawString(text, x, y);
    }
}
