package com.gogo.billing_service.service;


import com.gogo.billing_service.Repository.BillRepository;
import com.gogo.billing_service.dto.BillResponseDto;
import com.gogo.billing_service.dto.ProductItemDto;
import com.gogo.billing_service.exception.BillNotFoundException;
import com.gogo.billing_service.model.Bill;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PdfGeneratorService {
    @Autowired
    BillRepository billRepository;

    public ByteArrayInputStream generateInvoicePdf(BillResponseDto billDto) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Entête entreprise + date
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{6f, 4f});

            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);
            left.addElement(new Paragraph("Détails de la facture", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            header.addCell(left);

            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.NO_BORDER);
            right.setHorizontalAlignment(Element.ALIGN_RIGHT);
            right.addElement(new Paragraph("Trocady Solution Inc", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            right.addElement(new Paragraph("Date: " + billDto.getBillingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            header.addCell(right);

            document.add(header);
            document.add(new Paragraph(" "));

            // Infos Client
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA);



            Paragraph p1 = new Paragraph();
            p1.add(new Chunk("ID Commande : ", labelFont));
            p1.add(new Chunk(billDto.getOrderId(), valueFont));
            document.add(p1);

            Paragraph p2 = new Paragraph();
            p2.add(new Chunk("Nom du client : ", labelFont));
            p2.add(new Chunk(billDto.getCustomerName(), valueFont));
            document.add(p2);

            Paragraph p3 = new Paragraph();
            p3.add(new Chunk("Email : ", labelFont));
            p3.add(new Chunk(billDto.getCustomerMail(), valueFont));
            document.add(p3);

            Paragraph p4 = new Paragraph();
            p4.add(new Chunk("Montant : ", labelFont));
            p4.add(new Chunk(String.format("%.2f", billDto.getAmount()), valueFont));
            document.add(p4);

            Paragraph p5 = new Paragraph();
            p5.add(new Chunk("Taxe totale : ", labelFont));
            p5.add(new Chunk(String.format("%.2f", billDto.getTotalTax()), valueFont));
            document.add(p5);

            Paragraph p6 = new Paragraph();
            p6.add(new Chunk("Remise totale : ", labelFont));
            p6.add(new Chunk(String.format("%.2f", billDto.getTotalDiscount()), valueFont));
            document.add(p6);

            Paragraph p7 = new Paragraph();
            p7.add(new Chunk("Statut : ", labelFont));
            p7.add(new Chunk(billDto.getBillStatus(), valueFont));
            document.add(p7);

            document.add(new Paragraph(" "));

            // Tableau Produits
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{3f, 5f, 1.5f, 2f, 2f, 2f});
            table.setSpacingBefore(10);

            Stream.of("ID Produit", "Nom du produit", "Quantité", "Prix", "Remise", "Taxe")
                    .forEach(headerTitle -> {
                        PdfPCell headerCell = new PdfPCell();
                        headerCell.setBackgroundColor(Color.LIGHT_GRAY);
                        headerCell.setPhrase(new Phrase(headerTitle));
                        table.addCell(headerCell);
                    });

            for (ProductItemDto item : billDto.getProducts()) {
                table.addCell(item.getProductId());
                table.addCell(item.getProductName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(String.format("%.2f", item.getPrice()));
                table.addCell(String.format("%.2f", item.getDiscount()));
                table.addCell(String.format("%.2f", item.getTax()));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public BillResponseDto getBillDetails(String orderRef) throws BillNotFoundException {
        List<Bill> bills = billRepository.findAllByOrderRef(orderRef);
        if (bills.isEmpty()) throw new BillNotFoundException("Aucune facture trouvée");

        Bill sample = bills.get(0);

        List<ProductItemDto> products = bills.stream().map(bill -> {
            ProductItemDto dto = new ProductItemDto();
            dto.setProductId(bill.getProductIdEvent());
            dto.setProductName(bill.getProductName());
            dto.setQuantity(bill.getQuantity());
            dto.setPrice(bill.getPrice());
            dto.setDiscount(bill.getDiscount());
            dto.setTax(Math.round(((bill.getPrice() * bill.getQuantity()) - bill.getDiscount()) * 0.20 * 100.0) / 100.0);
            return dto;
        }).collect(Collectors.toList());

        double amount = products.stream().mapToDouble(p -> (p.getPrice() * p.getQuantity()) - p.getDiscount() + p.getTax()).sum();
        double totalDiscount = products.stream().mapToDouble(ProductItemDto::getDiscount).sum();
        double totalTax = products.stream().mapToDouble(ProductItemDto::getTax).sum();

        return new BillResponseDto(
                sample.getOrderRef(),
                sample.getCustomerName(),
                sample.getCustomerPhone(),
                sample.getCustomerMail(),
                Math.round(amount * 100.0) / 100.0,
                Math.round(totalTax * 100.0) / 100.0,
                Math.round(totalDiscount * 100.0) / 100.0,
                sample.getStatus(),
                sample.getBillingDate(),
                products
        );
    }

}
