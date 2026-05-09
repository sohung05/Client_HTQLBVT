package iuh.fit.gui;

import entity.GheGiuCho;
import service.IDonTreoService;
import iuh.fit.utils.ClientContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Quản lý danh sách ghế đang được giữ chỗ (15 phút)
 */
public class QuanLyGheGiuCho {
    private static List<GheGiuCho> danhSachGheGiuCho = new ArrayList<>();
    private static List<String> danhSachMaGheDangTreoRemote = new ArrayList<>(); 
    private static Timer timer = new Timer(true);
    
    public static void refreshDanhSachGheTreo(String maLichTrinh, String maGaDi, String maGaDen) {
        if (maLichTrinh == null || maGaDi == null || maGaDen == null) return;
        try {
            danhSachMaGheDangTreoRemote = ClientContext.getDonTreoService().layDanhSachMaGheDangTreo(maLichTrinh, maGaDi, maGaDen);
        } catch (Exception e) {
            System.err.println("Lỗi refresh ghế treo: " + e.getMessage());
        }
    }
    
    public static void themGheGiuCho(String maChoNgoi, String maDonTreo, String maLichTrinh, String maGaDi, String maGaDen) {
        GheGiuCho gheGiuCho = new GheGiuCho(maChoNgoi, maDonTreo, maLichTrinh, maGaDi, maGaDen);
        danhSachGheGiuCho.add(gheGiuCho);
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                xoaGheGiuCho(maChoNgoi, maLichTrinh);
            }
        }, 15 * 60 * 1000); 
    }
    
    public static boolean kiemTraGheDangGiuCho(String maChoNgoi, String maLichTrinh) {
        xoaGheHetHan();
        boolean dangGiuChoLocal = false;
        if (maLichTrinh == null) {
            dangGiuChoLocal = danhSachGheGiuCho.stream()
                .anyMatch(ghe -> ghe.getMaChoNgoi().equals(maChoNgoi) && ghe.conTrongThoiGianGiuCho());
        } else {
            dangGiuChoLocal = danhSachGheGiuCho.stream()
                .anyMatch(ghe -> ghe.getMaChoNgoi().equals(maChoNgoi) 
                    && (ghe.getMaLichTrinh() == null || ghe.getMaLichTrinh().equals(maLichTrinh))
                    && ghe.conTrongThoiGianGiuCho());
        }
        if (dangGiuChoLocal) return true;
        return danhSachMaGheDangTreoRemote != null && danhSachMaGheDangTreoRemote.contains(maChoNgoi);
    }
    
    public static void xoaGheGiuCho(String maChoNgoi, String maLichTrinh) {
        if (maLichTrinh == null) {
            danhSachGheGiuCho.removeIf(ghe -> ghe.getMaChoNgoi().equals(maChoNgoi));
        } else {
            danhSachGheGiuCho.removeIf(ghe -> ghe.getMaChoNgoi().equals(maChoNgoi) 
                && (ghe.getMaLichTrinh() == null || ghe.getMaLichTrinh().equals(maLichTrinh)));
        }
    }
    
    public static void xoaTatCaGheCuaDonTreo(String maDonTreo) {
        xoaTatCaGheCuaDonTreo(maDonTreo, null);
    }
    
    public static void xoaTatCaGheCuaDonTreo(String maDonTreo, List<String> maGheBosung) {
        if (maDonTreo == null) return;
        List<String> maGheCanXoa = danhSachGheGiuCho.stream()
                .filter(ghe -> maDonTreo.equals(ghe.getMaDonTreo()))
                .map(GheGiuCho::getMaChoNgoi)
                .collect(Collectors.toCollection(ArrayList::new));
        if (maGheBosung != null) {
            for (String ma : maGheBosung) {
                if (ma != null && !maGheCanXoa.contains(ma)) maGheCanXoa.add(ma);
            }
        }
        danhSachGheGiuCho.removeIf(ghe -> maDonTreo.equals(ghe.getMaDonTreo()));
        if (danhSachMaGheDangTreoRemote != null) danhSachMaGheDangTreoRemote.removeAll(maGheCanXoa);
    }
    
    public static void xoaGheHetHan() {
        danhSachGheGiuCho.removeIf(ghe -> !ghe.conTrongThoiGianGiuCho());
    }

    public static List<String> getDanhSachMaGheDangTreoRemote() {
        return danhSachMaGheDangTreoRemote;
    }

    public static void giaHanGheCuaDonTreo(String maDonTreo) {
        if (maDonTreo == null) return;
        for (GheGiuCho ghe : danhSachGheGiuCho) {
            if (maDonTreo.equals(ghe.getMaDonTreo())) {
                ghe.giaHanThoiGian(15);
            }
        }
    }
}
