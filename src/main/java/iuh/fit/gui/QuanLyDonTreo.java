package iuh.fit.gui;

import entity.DonTreoDat;
import service.IDonTreoService;
import iuh.fit.utils.ClientContext;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;

/**
 * Class static để quản lý danh sách đơn treo thông qua RMI Service
 */
public class QuanLyDonTreo {
    
    private static IDonTreoService getService() {
        return ClientContext.getDonTreoService();
    }
    
    public static void themDonTreo(DonTreoDat donTreo) {
        try {
            if (donTreo.getNgayLap() == null) donTreo.setNgayLap(java.time.LocalDateTime.now());
            if (donTreo.getGioLap() == null) donTreo.setGioLap(java.time.LocalDateTime.now());
            getService().themDonTreo(donTreo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Lỗi khi treo đơn: " + e.getMessage());
        }
    }
    
    public static List<DonTreoDat> layDanhSachDonTreo() {
        try { return getService().layDanhSachDonTreo(); }
        catch (Exception e) { return new ArrayList<>(); }
    }
    
    public static void xoaDonHetHan() {
        try { getService().xoaDonHetHan(); } catch (Exception e) {}
    }
    
    public static boolean xoaDonTreo(String maDonTreo) {
        try { return getService().xoaDonTreo(maDonTreo); } catch (Exception e) { return false; }
    }
    
    public static DonTreoDat layDonTreo(String maDonTreo) {
        try { return getService().layDonTreo(maDonTreo); } catch (Exception e) { return null; }
    }
    
    public static List<DonTreoDat> layDonTreoTheoCCCD(String cccd) {
        try { return getService().layDonTreoTheoCCCD(cccd); } catch (Exception e) { return new ArrayList<>(); }
    }
    
    public static List<DonTreoDat> layDonTreoTheoSDT(String sdt) {
        try { return getService().layDonTreoTheoSDT(sdt); } catch (Exception e) { return new ArrayList<>(); }
    }
    
    public static int demSoLuong() {
        try {
            List<DonTreoDat> list = layDanhSachDonTreo();
            return list != null ? list.size() : 0;
        } catch (Exception e) { return 0; }
    }
}
