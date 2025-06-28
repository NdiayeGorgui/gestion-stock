package com.gogo.billing_service.controller;

import com.gogo.billing_service.dto.BillResponseDto;
import com.gogo.billing_service.exception.BillNotFoundException;
import com.gogo.billing_service.model.Bill;
import com.gogo.billing_service.service.BillExcelExporter;
import com.gogo.billing_service.service.BillingService;
import com.gogo.billing_service.service.PdfGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class BillController {
    @Autowired
    private BillingService billingService;
    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Operation(
            summary = "get Bill REST API",
            description = "get Bill by id REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    @GetMapping("/bills/{orderRef}")
    public BillResponseDto getOneBill(@PathVariable("orderRef") String orderRef) {
        return billingService.findBillWithDetails(orderRef);
    }


   /* @GetMapping("/bills/{id}")
    public Bill getBill(@PathVariable("id") Long id) throws BillNotFoundException {
        Optional<Bill> bill = Optional.ofNullable(billingService.getBill(id));
        if (bill.isPresent()) {
            return bill.get();
        }
            throw new BillNotFoundException("Bill not available with id: " + id );
    }*/

    @Operation(
            summary = "get Bills REST API",
            description = "get Bill by id REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")
    @GetMapping("/bills")
    public List<BillResponseDto> getBills() {
        return billingService.getAllBillsWithProducts();
    }


    @Operation(
            summary = "get Bills REST API",
            description = "get Bills by customerIdEvent and status REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/bills/{customerIdEvent}/{status}")
    public List<Bill> getCustomerBillsStatus(@PathVariable("customerIdEvent") String customerIdEvent,@PathVariable("status") String status) throws BillNotFoundException {
        List<Bill> bills=billingService.getBills(customerIdEvent);
        if(bills.isEmpty()){
            throw new BillNotFoundException("Customer not available with id: "+customerIdEvent);
        }
        return billingService.billList(customerIdEvent,status);
    }
    @Operation(
            summary = "get and print Bill REST API",
            description = "get and print excel file Bill by customerIdEvent and status REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/bills/export/{orderRef}/{status}")
    public void exportToExcel(HttpServletResponse response,
                              @PathVariable("orderRef") String orderRef,
                              @PathVariable("status") String status) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=bill_" + orderRef + "_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Bill> billList = billingService.getBillsByOrderRefAndStatus(orderRef, status); // méthode à créer

        if (!billList.isEmpty()) {
            BillExcelExporter exporter = new BillExcelExporter(billList);
            exporter.export(response, orderRef, status);
        } else {
            throw new RuntimeException("Aucune facture trouvée pour la commande : " + orderRef);
        }
    }
    @GetMapping("/bills/pdf/{orderRef}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable("orderRef") String orderRef) throws BillNotFoundException {
        // On récupère le DTO complet avec les produits, montants, etc.
        BillResponseDto billDto = pdfGeneratorService.getBillDetails(orderRef);

        // On génère le PDF à partir du DTO
        ByteArrayInputStream pdf = pdfGeneratorService.generateInvoicePdf(billDto);

        // Préparation des headers de la réponse HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=facture_" + orderRef + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf.readAllBytes());
    }



   /* @Operation(
            summary = "get Bills REST API",
            description = "get Bills REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/bills")
    public List<Bill> getAllBills(){
        return billingService.getBills();
    }*/

    @Operation(
            summary = "get Bills REST API",
            description = "get Bills by orderIdEvent REST API from Bill object")
    @ApiResponse(responseCode = "200",
            description = "Http status 200 ")

    @GetMapping("/bills/bill/{orderIdEvent}")
    public Bill getBill(@PathVariable ("orderIdEvent") String orderIdEvent){
        return billingService.findFirstByOrderIdEvent(orderIdEvent);
    }
}
