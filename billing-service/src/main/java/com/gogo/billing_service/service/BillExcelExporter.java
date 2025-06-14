package com.gogo.billing_service.service;

import com.gogo.base_domaine_service.constante.Constante;
import com.gogo.billing_service.model.Bill;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BillExcelExporter {

    static int comp = 100;

    @Autowired
    private BillingService billingService;

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<Bill> billList;
    private int rowCount;

    public BillExcelExporter(List<Bill> billList) {
        this.billList = billList;
        workbook = new XSSFWorkbook();
    }

    public double getAmount(String customerIdEvent, String status) {
        return billList.stream()
                .filter(bill -> bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .mapToDouble(bill -> bill.getPrice() * bill.getQuantity())
                .sum();
    }

    public double getDiscount(String customerIdEvent, String status) {
        return billList.stream()
                .filter(bill -> bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .mapToDouble(Bill::getDiscount)
                .sum();
    }

    public Bill getBill(String customerIdEvent, String status) {
        return billList.stream()
                .filter(bill -> bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .findFirst()
                .orElse(null);
    }

    private CellStyle createStyledCell(boolean isHeader, boolean isTitle, short bgColor) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);

        XSSFFont font = workbook.createFont();
        if (isTitle) {
            font.setFontHeight(16);
            font.setBold(true);
        } else if (isHeader) {
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
        }
        style.setFont(font);

        if (isHeader || isTitle) {
            style.setFillForegroundColor(bgColor);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(HorizontalAlignment.CENTER);
        }

        return style;
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);

        if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else {
            cell.setCellValue(String.valueOf(value));
        }

        cell.setCellStyle(style);
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Bills");
        Row headerRow = sheet.createRow(10);

        CellStyle headerStyle = createStyledCell(true, false, IndexedColors.DARK_BLUE.getIndex());

        createCell(headerRow, 0, Constante.DESCRIPTION, headerStyle);
        createCell(headerRow, 1, Constante.QUANTITE, headerStyle);
        createCell(headerRow, 2, Constante.PRIX_UNITAIRE, headerStyle);
        createCell(headerRow, 3, Constante.MONTANT_HT, headerStyle);
    }

    private void writeDataLines(String customerIdEvent, String status) {
        Bill customerBill = getBill(customerIdEvent, status);
        String name = customerBill != null ? customerBill.getCustomerName() : "N/A";
        String phone = customerBill != null ? customerBill.getCustomerPhone() : "N/A";

        rowCount = 0;
        CellStyle titleStyle = createStyledCell(false, true, IndexedColors.WHITE.getIndex());
        CellStyle infoStyle = createStyledCell(false, false, IndexedColors.WHITE.getIndex());

        Row row0 = sheet.createRow(rowCount++);
        createCell(row0, 0, Constante.NOM_COMPAGNIE, titleStyle);
        createCell(row0, 2, Constante.NOM_CLIENT, titleStyle);
        createCell(row0, 3, name, infoStyle);

        Row row1 = sheet.createRow(rowCount++);
        createCell(row1, 2, Constante.NUMERO_TELEPHONE, titleStyle);
        createCell(row1, 3, phone, infoStyle);

        rowCount++; // espace

        Row row2 = sheet.createRow(rowCount++);
        createCell(row2, 1, Constante.FACTURE, titleStyle);

        Row row3 = sheet.createRow(rowCount++);
        createCell(row3, 0, Constante.NUMERO_FACTURE, infoStyle);
        createCell(row3, 1, Constante.PREFIXE + comp++, infoStyle);

        Row row4 = sheet.createRow(rowCount++);
        createCell(row4, 0, Constante.DATE_FACTURE, infoStyle);
        createCell(row4, 1, LocalDateTime.now().toString(), infoStyle);

        Row row5 = sheet.createRow(rowCount++);
        createCell(row5, 0, Constante.NUMERO_CLIENT, infoStyle);
        createCell(row5, 1, customerIdEvent, infoStyle);

        rowCount = 11;
        CellStyle rowStyle = createStyledCell(false, false, IndexedColors.WHITE.getIndex());

        for (Bill bill : billList) {
            if (!bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent) ||
                    !bill.getStatus().equalsIgnoreCase(status)) continue;

            Row row = sheet.createRow(rowCount++);
            int col = 0;
            createCell(row, col++, bill.getProductName(), rowStyle);
            createCell(row, col++, bill.getQuantity(), rowStyle);
            createCell(row, col++, bill.getPrice(), rowStyle);
            createCell(row, col++, bill.getPrice() * bill.getQuantity(), rowStyle);
        }
    }

    private void writeTaxLine(String customerIdEvent, String status) {
        double amount = getAmount(customerIdEvent, status);
        double discount = getDiscount(customerIdEvent, status);
        double tax = amount * Constante.TAX;
        double totalTTC = amount - discount + tax;

        CellStyle style = createStyledCell(false, true, IndexedColors.LIGHT_YELLOW.getIndex());

        int col = 2;

        Row row1 = sheet.createRow(rowCount++);
        createCell(row1, col, Constante.TOTAL_HT, style);
        createCell(row1, col + 1, amount, style);

        Row row2 = sheet.createRow(rowCount++);
        createCell(row2, col, Constante.REMISE, style);
        createCell(row2, col + 1, discount, style);

        Row row3 = sheet.createRow(rowCount++);
        createCell(row3, col, Constante.TVA, style);
        createCell(row3, col + 1, tax, style);

        Row row4 = sheet.createRow(rowCount++);
        createCell(row4, col, Constante.TOTAL_TTC, style);
        createCell(row4, col + 1, totalTTC, style);
    }

    public void export(HttpServletResponse response, String customerIdEvent, String status) throws IOException {
        writeHeaderLine();
        writeDataLines(customerIdEvent, status);
        writeTaxLine(customerIdEvent, status);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
