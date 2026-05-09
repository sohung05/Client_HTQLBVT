package iuh.fit.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class JTableExporter {

    public static void openFile(String file) throws IOException {
        File path = new File(file);
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(path);
        } else {
            JOptionPane.showMessageDialog(null, "Không thể mở file tự động. Vui lòng mở thủ công:\n" + file);
        }
    }

    public static void exportJTableToExcel(JTable table) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn đường dẫn lưu file Excel");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userChoice = fileChooser.showSaveDialog(null);
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".xlsx")) {
                    filePath += ".xlsx";
                }

                TableModel model = table.getModel();
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("DanhSach");

                // Header style
                CellStyle headerStyle = createStyleForHeader(sheet);
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < model.getColumnCount(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(model.getColumnName(i));
                    cell.setCellStyle(headerStyle);
                }

                // Data rows
                for (int i = 0; i < model.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object value = model.getValueAt(i, j);
                        Cell cell = row.createCell(j);
                        cell.setCellValue(value == null ? "" : value.toString());
                    }
                }

                // Auto-size all columns
                for (int i = 0; i < model.getColumnCount(); i++) {
                    sheet.autoSizeColumn(i);
                }

                // Save to file
                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                }
                workbook.close();

                JOptionPane.showMessageDialog(null,
                        "Xuất Excel thành công!\n" + filePath,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

                openFile(filePath);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        "Lỗi khi ghi file Excel:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static CellStyle createStyleForHeader(Sheet sheet) {
        org.apache.poi.ss.usermodel.Font font = sheet.getWorkbook().createFont();
        font.setFontName("Times New Roman");
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        return cellStyle;
    }
}