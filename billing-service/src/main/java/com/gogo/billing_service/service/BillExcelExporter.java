package com.gogo.billing_service.service;

import com.gogo.base_domaine_service.constante.Constante;
import com.gogo.billing_service.model.Bill;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BillExcelExporter {
    static int comp=100;

    @Autowired
    private BillingService billingService;

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Bill> billList;

    public BillExcelExporter(List<Bill> billList) {
        this.billList = billList;
        workbook = new XSSFWorkbook();
    }

    public double getAmount(String orderRef, String status) {
        return billList.stream()
                .filter(bill -> bill.getOrderRef().equalsIgnoreCase(orderRef))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .mapToDouble(bill -> bill.getPrice() * bill.getQuantity())
                .sum();
    }

    public Bill getBill(String orderRef, String status) {
        return billList.stream()
                .filter(bill -> bill.getOrderRef().equalsIgnoreCase(orderRef))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .findAny()
                .orElse(null);
    }

    public double getDiscount(String orderRef, String status) {
        return billList.stream()
                .filter(bill -> bill.getOrderRef().equalsIgnoreCase(orderRef))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .mapToDouble(Bill::getDiscount)
                .sum();
    }


    private void writeHeaderLine() {
        sheet = workbook.createSheet("Facture");

        Row headerRow = sheet.createRow(10);

        CellStyle headerStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(12);
        font.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        setBorders(headerStyle);

        createCell(headerRow, 0, Constante.DESCRIPTION, headerStyle);
        createCell(headerRow, 1, Constante.QUANTITE, headerStyle);
        createCell(headerRow, 2, Constante.PRIX_UNITAIRE, headerStyle);
        createCell(headerRow, 3, Constante.MONTANT_HT, headerStyle);
    }


    private void createCell(Row row, int columnCount, Object value, CellStyle baseStyle) {
        Cell cell = row.createCell(columnCount);

        if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        }

        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle);
        setBorders(style);
        cell.setCellStyle(style);

        sheet.autoSizeColumn(columnCount);
    }



    int rowCount = 0;

    private void writeDataLines(String orderRef, String status) {
        Bill customerBill = this.getBill(orderRef, status);
        String name = customerBill != null ? customerBill.getCustomerName() : "";
        String telephone = customerBill != null ? customerBill.getCustomerPhone() : "";
        String customerId = customerBill != null ? customerBill.getCustomerIdEvent() : "";

        rowCount = 0;

        // Infos société & client (en haut)
        CellStyle infoStyle = workbook.createCellStyle();
        XSSFFont infoFont = workbook.createFont();
        infoFont.setFontHeight(12);
        infoFont.setBold(true);
        infoStyle.setFont(infoFont);

        Row row00 = sheet.createRow(rowCount++);
        createCell(row00, 0, Constante.NOM_COMPAGNIE, infoStyle);
        createCell(row00, 2, Constante.NOM_CLIENT, infoStyle);
        createCell(row00, 3, name, infoStyle);

        Row row01 = sheet.createRow(rowCount++);
        createCell(row01, 2, Constante.NUMERO_TELEPHONE, infoStyle);
        createCell(row01, 3, telephone, infoStyle);

        Row rowClient = sheet.createRow(rowCount++);
        createCell(rowClient, 2, Constante.NUMERO_CLIENT, infoStyle);
        createCell(rowClient, 3, customerId, infoStyle);

        rowCount++; // Ligne vide

        // FACTURE (avec fond gris clair et centré)
        CellStyle titleStyle = workbook.createCellStyle();
        XSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeight(14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(titleStyle);

        Row rowFacture = sheet.createRow(rowCount++);
        sheet.addMergedRegion(new CellRangeAddress(rowFacture.getRowNum(), rowFacture.getRowNum(), 0, 3));
        createCell(rowFacture, 0, Constante.FACTURE, titleStyle);

        rowCount++;

        // Détails facture en bas
        CellStyle labelStyle = workbook.createCellStyle();
        labelStyle.setFont(infoFont);
        setBorders(labelStyle);

        Row row11 = sheet.createRow(rowCount++);
        createCell(row11, 0, Constante.NUMERO_FACTURE, labelStyle);
        createCell(row11, 1, Constante.PREFIXE + comp++, labelStyle);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Row row12 = sheet.createRow(rowCount++);
        createCell(row12, 0, Constante.DATE_FACTURE, labelStyle);
        createCell(row12, 1, LocalDateTime.now().format(formatter), labelStyle);

        Row rowOrderRef = sheet.createRow(rowCount++);
        createCell(rowOrderRef, 0, "Référence commande", labelStyle);
        createCell(rowOrderRef, 1, orderRef, labelStyle);

        // Détails produits - commence après l'en-tête (ligne 11)
        rowCount = 11;
        CellStyle productStyle = workbook.createCellStyle();
        XSSFFont productFont = workbook.createFont();
        productFont.setFontHeight(12);
        productStyle.setFont(productFont);
        productStyle.setAlignment(HorizontalAlignment.LEFT);
        setBorders(productStyle);

        for (Bill bill : billList) {
            if (!bill.getOrderRef().equalsIgnoreCase(orderRef) || !bill.getStatus().equalsIgnoreCase(status)) {
                continue;
            }

            Row row = sheet.createRow(rowCount++);
            int column = 0;
            createCell(row, column++, bill.getProductName(), productStyle);
            createCell(row, column++, bill.getQuantity(), productStyle);
            createCell(row, column++, bill.getPrice(), productStyle);
            createCell(row, column++, bill.getPrice() * bill.getQuantity(), productStyle);
        }
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }


    private void writeTaxLine(String orderRef, String status) {
        List<Bill> filteredBills = billList.stream()
                .filter(bill -> bill.getOrderRef().equalsIgnoreCase(orderRef))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .toList();

        double subtotal = filteredBills.stream()
                .mapToDouble(b -> b.getPrice() * b.getQuantity())
                .sum();
        subtotal = Math.round(subtotal * 100.0) / 100.0;

        double totalDiscount = filteredBills.stream()
                .mapToDouble(Bill::getDiscount)
                .sum();
        totalDiscount = Math.round(totalDiscount * 100.0) / 100.0;

        double net = subtotal - totalDiscount;
        net = Math.round(net * 100.0) / 100.0;

        double totalTax = net * Constante.TAX; // 0.2 si tu as défini TAX = 0.2
        totalTax = Math.round(totalTax * 100.0) / 100.0;

        double amount = net + totalTax;
        amount = Math.round(amount * 100.0) / 100.0;

        rowCount = rowCount + 1;
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        int columnCount = 2;

        Row row = sheet.createRow(rowCount++);
        createCell(row, columnCount++, Constante.TOTAL_HT, style);
        createCell(row, columnCount++, subtotal, style);

        Row row1 = sheet.createRow(rowCount++);
        createCell(row1, columnCount - 2, Constante.REMISE, style);
        createCell(row1, columnCount - 1, totalDiscount, style);

        Row row2 = sheet.createRow(rowCount++);
        createCell(row2, columnCount - 2, Constante.TVA, style);
        createCell(row2, columnCount - 1, totalTax, style);

        Row row3 = sheet.createRow(rowCount++);
        createCell(row3, columnCount - 2, Constante.TOTAL_TTC, style);
        createCell(row3, columnCount - 1, amount, style);
    }


    public void export(HttpServletResponse response, String orderRef, String status) throws IOException {
        writeHeaderLine();
        writeDataLines(orderRef, status);
        writeTaxLine(orderRef, status);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);

        workbook.close();
        outputStream.close();
    }

}
