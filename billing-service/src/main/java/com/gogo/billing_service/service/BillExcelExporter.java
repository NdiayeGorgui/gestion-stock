package com.gogo.billing_service.service;

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
import java.util.List;

import static org.apache.poi.ss.util.CellUtil.createCell;

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

    public  double getAmount(String customerIdEvent,String status){
        List<Bill> customerBills=this.billList;
        return customerBills.stream()
                .filter(bill -> bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .map(bill -> (bill.getPrice()*bill.getQuantity()))
                .mapToDouble(i->i).sum();
    }

    public Bill getBill(String customerIdEvent,String status){
        List<Bill> customerBills=this.billList;
        return customerBills.stream()
                .filter(bill -> bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .findAny().orElse(null);
    }



    public  double getDiscount(String customerIdEvent,String status){
        List<Bill> customerBills=this.billList;
        return customerBills.stream()
                .filter(bill -> bill.getCustomerIdEvent().equalsIgnoreCase(customerIdEvent))
                .filter(bill -> bill.getStatus().equalsIgnoreCase(status))
                .map(Bill::getDiscount)
                .mapToDouble(i->i).sum();
    }


    private void writeHeaderLine() {
        sheet = workbook.createSheet("Bills");

        Row row = sheet.createRow(10);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        //style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.index);

        //sheet.addMergedRegion(new CellRangeAddress(1,1,1,4)); //fusionnera de B2 à E2
        createCell(row, 0, "Description", style);
        createCell(row, 1, "Quantité", style);
        createCell(row, 2, "Prix Unitaire", style);
        createCell(row, 3, "Montant HT", style);


    }



    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Double) {
            cell.setCellValue((Double) value);
        }
        else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        }
        else  if (value instanceof String){
            cell.setCellValue((String) value);
        }
        style.setBorderTop(BorderStyle.valueOf((short) 1)); // single line border
        style.setBorderBottom(BorderStyle.valueOf((short) 1)); // single line border
        style.setBorderLeft(BorderStyle.valueOf((short) 1)); // single line border
        style.setBorderRight(BorderStyle.valueOf((short) 1)); // single line border

        cell.setCellStyle(style);
    }

    int rowCount = 0;

    private void writeDataLines(String customerIdEvent,String status) {

        Bill customerBill=this.getBill(customerIdEvent,status);
        String name=null;
        String telephone=null;
        if(customerBill!=null){
             name=customerBill.getCustomerName();
             telephone=customerBill.getCustomerPhone();
        }

        rowCount = 0;
        int columnCount00 = 0;

        Row row00 = sheet.createRow(rowCount++);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);


        /*CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.valueOf((short) 1)); // single line border
        cellStyle.setBorderBottom(BorderStyle.valueOf((short) 1)); // single line border
        //add many others here
        cell1.setCellStyle(cellStyle); //apply that style to the cell*/

        createCell(row00, columnCount00, "Gorgui Solution Inc", style);
        createCell(row00, columnCount00+2, "Nom Client:", style);
        createCell(row00, columnCount00+3, name, style);
        rowCount=1;
        Row row01 = sheet.createRow(rowCount++);
        createCell(row01, 2, "N° Telephone:", style);
        createCell(row01, 3, telephone, style);
        rowCount = 3;
        int columnCount1 = 1;

        Row row0 = sheet.createRow(rowCount++);
        createCell(row0, columnCount1++, "FACTURE", style);



        int columnCount2 = 0;
        Row row11 = sheet.createRow(rowCount++);

        createCell(row11, columnCount2++, "N° Facture", style);
        createCell(row11, columnCount2++, "FA"+comp++, style);

        Row row12 = sheet.createRow(rowCount++);
        createCell(row12, columnCount2-2, "Date Facture", style);
        createCell(row12, columnCount2-1, LocalDateTime.now().toString(), style);

        Row row13 = sheet.createRow(rowCount++);
        createCell(row13, columnCount2-2, "N° Client", style);
        createCell(row13, columnCount2-1, customerIdEvent, style);

        rowCount = 11;
        for (Bill bill : billList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, bill.getProductName(), style);
            createCell(row, columnCount++, bill.getQuantity(), style);
            createCell(row, columnCount++, bill.getPrice(), style);
            createCell(row, columnCount++, bill.getPrice()*bill.getQuantity(), style);

        }

    }

    private void writeTaxLine(String customerIdEvent,String status) {
        double amount= this.getAmount(customerIdEvent,status);
        double discount=this.getDiscount(customerIdEvent,status);
        rowCount=rowCount+1;
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

            int columnCount = 2;
            Row row = sheet.createRow(rowCount++);

            createCell(row, columnCount++, "Total HT", style);
            createCell(row, columnCount++, amount, style);

            Row row1 = sheet.createRow(rowCount++);
            createCell(row1, columnCount-2, "Remise", style);
            createCell(row1, columnCount-1, discount, style);

            Row row2 = sheet.createRow(rowCount++);
            createCell(row2, columnCount-2, "TVA 20%", style);
            createCell(row2, columnCount-1, amount*0.2, style);

            Row row3 = sheet.createRow(rowCount++);
            createCell(row3, columnCount-2, "Total TTC", style);
            createCell(row3, columnCount-1, (0.2*amount+(amount-discount)), style);


    }

    public void export(HttpServletResponse response,String customerIdEvent,String status) throws IOException {
        writeHeaderLine();
        writeDataLines(customerIdEvent,status);
        writeTaxLine(customerIdEvent,status);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();

    }


}
