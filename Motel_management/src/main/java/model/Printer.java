package model;

import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintService;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.kernel.pdf.PdfDocument;
import java.io.File;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class Printer {

    private final JTextPane printLayout;
    private String motelName;
    private String motelAddress;
    private String motelID;
    private final DateTimeFormatter hourFormatter;
    private final DateTimeFormatter dateFormatter;
    private final NumberFormat numberFormat;
    private StyledDocument document;
    private final String PDF_SAVE_PATH = FileManager.PATH + "\\receiptPrints";
    private PrintService printerService;

    public Printer() {
        System.out.println("FileManager initialized");
        printLayout = new JTextPane();
        hourFormatter = new DateTimeFormatterBuilder()
                .appendPattern("hh:mm").appendLiteral(' ')
                .appendText(ChronoField.AMPM_OF_DAY, new HashMap<Long, String>() {
                    {
                        put(0L, "AM");
                        put(1L, "PM");
                    }
                })
                .toFormatter();
        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", new Locale("es", "ES"));
        numberFormat = NumberFormat.getNumberInstance(Locale.US);
        initializeStyles();
        File preparePDFRoute = new File(PDF_SAVE_PATH);
        preparePDFRoute.mkdirs();
        printerService = PrinterJob.getPrinterJob().getPrintService();
    }

    public void setPrinterVariables(String name, String address, String iD) {
        this.motelName = name;
        this.motelAddress = address;
        this.motelID = iD;
    }

    public List<String> getPrinterServiceNameList() {
        PrintService[] services = PrinterJob.lookupPrintServices();
        List<String> list = new ArrayList<>();
        for (PrintService service : services) {
            list.add(service.getName());
        }
        return list;
    }

    public void setPrinterService(String printerName) {
        printerName = printerName.toLowerCase();
        PrintService service = null;
        PrintService[] services = PrinterJob.lookupPrintServices();
        for (int index = 0; service == null && index < services.length; index++) {
            if (services[index].getName().toLowerCase().contains(printerName)) {
                service = services[index];
            }
        }
        printerService = service;
    }

    public String getCurrentPrinterName() {
        if (printerService == null) {
            return "N/A";
        }
        return printerService.getName();
    }

    public String getFirstAvailablePrinterName() {
        PrintService[] services = PrinterJob.lookupPrintServices();
        if (services.length > 0) {
            return services[0].getName();
        }
        return null;
    }

    private void initializeStyles() {
        document = printLayout.getStyledDocument();
        String fontFamily = "Calibri";

        Style headerStyle = document.addStyle("HeaderStyle", null);
        StyleConstants.setFontSize(headerStyle, 12);
        StyleConstants.setFontFamily(headerStyle, fontFamily);

        Style largeStyle = document.addStyle("LargeStyle", null);
        StyleConstants.setFontSize(largeStyle, 28);
        StyleConstants.setFontFamily(largeStyle, fontFamily);

        Style defaultStyle = document.addStyle("DefaultStyle", null);
        StyleConstants.setFontSize(defaultStyle, 10);
        StyleConstants.setFontFamily(defaultStyle, fontFamily);

        Style transactionStyle = document.addStyle("TransactionStyle", null);
        StyleConstants.setFontSize(transactionStyle, 9);
        StyleConstants.setFontFamily(transactionStyle, fontFamily);

        Style footerStyle = document.addStyle("FooterStyle", null);
        StyleConstants.setFontSize(footerStyle, 8);
        StyleConstants.setFontFamily(footerStyle, fontFamily);

        Style secondLastStyle = document.addStyle("SecondLastStyle", null);
        StyleConstants.setFontSize(secondLastStyle, 7);
        StyleConstants.setFontFamily(secondLastStyle, fontFamily);

        // BOLD VARIANTS
        Style headerStyleBold = document.addStyle("HeaderStyleBold", null);
        StyleConstants.setFontSize(headerStyleBold, 12);
        StyleConstants.setBold(headerStyleBold, true);
        StyleConstants.setFontFamily(headerStyleBold, fontFamily);

        Style largeStyleBold = document.addStyle("LargeStyleBold", null);
        StyleConstants.setFontSize(largeStyleBold, 28);
        StyleConstants.setBold(largeStyleBold, true);
        StyleConstants.setFontFamily(largeStyleBold, fontFamily);

        Style defaultStyleBold = document.addStyle("DefaultStyleBold", null);
        StyleConstants.setFontSize(defaultStyleBold, 10);
        StyleConstants.setBold(defaultStyleBold, true);
        StyleConstants.setFontFamily(defaultStyleBold, fontFamily);

        Style transactionStyleBold = document.addStyle("TransactionStyleBold", null);
        StyleConstants.setFontSize(transactionStyleBold, 9);
        StyleConstants.setBold(transactionStyleBold, true);
        StyleConstants.setFontFamily(transactionStyleBold, fontFamily);

        Style footerStyleBold = document.addStyle("FooterStyleBold", null);
        StyleConstants.setFontSize(footerStyleBold, 8);
        StyleConstants.setBold(footerStyleBold, true);
        StyleConstants.setFontFamily(footerStyleBold, fontFamily);

        Style secondLastStyleBold = document.addStyle("SecondLastStyleBold", null);
        StyleConstants.setFontSize(secondLastStyleBold, 7);
        StyleConstants.setBold(secondLastStyleBold, true);
        StyleConstants.setFontFamily(secondLastStyleBold, fontFamily);

        Style fillerStyle = document.addStyle("FillerStyle", null);
        StyleConstants.setFontSize(fillerStyle, 1);
        StyleConstants.setFontFamily(fillerStyle, fontFamily);

        Style centeredStyle = document.addStyle("CenteredStyle", null);
        StyleConstants.setAlignment(centeredStyle, StyleConstants.ALIGN_CENTER);
    }

    // ========== Document Assembly Helpers ==========

    private String spaces(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Prints the standard header: MOTEL + name + address + NIT + optional subtitle + separator.
     */
    private void printHeader(String subtitle) throws BadLocationException {
        document.insertString(document.getLength(), spaces(2) + "MOTEL", document.getStyle("HeaderStyle"));
        document.insertString(document.getLength(), spaces(1) + motelName + "\n", document.getStyle("LargeStyle"));
        document.insertString(document.getLength(), spaces(6) + motelAddress + "\n", document.getStyle("FooterStyleBold"));
        document.insertString(document.getLength(), spaces(3) + motelID + "\n", document.getStyle("FooterStyleBold"));
        if (subtitle != null && !subtitle.isEmpty()) {
            document.insertString(document.getLength(), spaces(1) + subtitle + "\n", document.getStyle("FooterStyleBold"));
        }
        printSeparator();
    }

    /**
     * Prints the standard separator line.
     */
    private void printSeparator() throws BadLocationException {
        document.insertString(document.getLength(), "___________________________________\n", document.getStyle("SecondLastStyleBold"));
    }

    /**
     * Prints "FACTURA DE VENTA No. <consecutive>".
     */
    private void printTransactionNumber(int consecutiveTransaction) throws BadLocationException {
        document.insertString(document.getLength(), spaces(2) + "FACTURA DE VENTA No. ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), spaces(2) + consecutiveTransaction, document.getStyle("DefaultStyleBold"));
        document.insertString(document.getLength(), spaces(2) + "\n", document.getStyle("DefaultStyle"));
    }

    /**
     * Prints the centered start/end date section for turn reports.
     */
    private void printTurnDateSection(ZonedDateTime start, ZonedDateTime end) throws BadLocationException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss");
        String startDate = start.format(formatter);
        String endDate = end.format(formatter);

        document.insertString(document.getLength(), "Inicio turno: \n", document.getStyle("SecondLastStyleBold"));
        document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);
        document.insertString(document.getLength(), startDate + "\n", document.getStyle("DefaultStyleBold"));
        document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);
        document.insertString(document.getLength(), "Fin turno: \n", document.getStyle("SecondLastStyleBold"));
        document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);
        document.insertString(document.getLength(), endDate + "\n", document.getStyle("DefaultStyleBold"));
        document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);
    }

    /**
     * Prints the totals section (Habitaciones / Productos / Total) for turn reports.
     */
    private void printTurnTotals(long totalRooms, long totalItems, long totalSales) throws BadLocationException {
        document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
        printSeparator();
        document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
        document.insertString(document.getLength(), spaces(2) + "Habitaciones: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(totalRooms) + "\n", document.getStyle("DefaultStyleBold"));
        document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
        document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
        document.insertString(document.getLength(), spaces(4) + "Productos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(totalItems) + "\n", document.getStyle("DefaultStyleBold"));
        document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
        document.insertString(document.getLength(), "Total Turno: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(totalSales) + "\n", document.getStyle("DefaultStyleBold"));
    }

    /**
     * Saves a PDF and optionally prints to the configured printer.
     */
    private void completePrinting(String pdfType, ZonedDateTime date, int id, boolean justPDF) {
        saveAsPDF(pdfType, date, id);
        if (!justPDF) {
            printWithService();
        }
    }

    // ========== Print Methods ==========

    public void printRoomTimeSell(JSONObject roomTimeSell, int consecutiveTransaction, boolean justPDF) {
        printLayout.setText("");
        document = printLayout.getStyledDocument();

        String roomString = roomTimeSell.getString("roomString");
        long price = roomTimeSell.getLong("price");
        int service = roomTimeSell.getInt("service");

        ZonedDateTime fullDateHourService = ZonedDateTime.parse(roomTimeSell.getString("startStatus"));
        String hourService = fullDateHourService.format(hourFormatter);
        String dateService = fullDateHourService.format(dateFormatter);

        try {
            printHeader(null);
            printTransactionNumber(consecutiveTransaction);

            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(3) + "Habitaci\u00f3n No:", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), roomString, document.getStyle("TransactionStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(3) + "Hora Entrada: " + hourService + "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(4) + "Servicio: " + service + " Horas" + "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(4) + "Pago Total:\t", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " " + numberFormat.format(price), document.getStyle("TransactionStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), "\n \n", document.getStyle("TransactionStyle"));
            printSeparator();

            document.insertString(document.getLength(), "NO OLVIDE SUS PERTENENCIAS", document.getStyle("SecondLastStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("DefaultStyle"));
            document.insertString(document.getLength(), spaces(6) + motelAddress + "\n", document.getStyle("FooterStyleBold"));
            printSeparator();
            document.insertString(document.getLength(), spaces(10) + dateService + "\n", document.getStyle("SecondLastStyle"));

            completePrinting("roomBooked", fullDateHourService, consecutiveTransaction, justPDF);
        } catch (BadLocationException ex) {
            Logger.getLogger(Printer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void printItemSold(JSONObject transaction, int consecutiveTransaction, boolean justPDF) {
        printLayout.setText("");
        document = printLayout.getStyledDocument();

        String roomString = transaction.getString("roomSoldTo");
        ZonedDateTime fullDateHourService = ZonedDateTime.parse(transaction.getString("changeDate"));
        String hourService = fullDateHourService.format(hourFormatter);
        String dateService = fullDateHourService.format(dateFormatter);
        JSONArray registerArray = transaction.getJSONArray("register");
        long totalPrice = 0;

        try {
            printHeader(null);

            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(2) + "VENTA A LA HABITACI\u00d3N: ", document.getStyle("DefaultStyle"));
            document.insertString(document.getLength(), roomString, document.getStyle("TransactionStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));

            printTransactionNumber(consecutiveTransaction);

            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(3) + "HORA VENTA: " + hourService + "\n", document.getStyle("DefaultStyle"));
            document.insertString(document.getLength(), " \n\n\n", document.getStyle("FillerStyle"));

            for (int i = 0; i < registerArray.length(); i++) {
                JSONObject item = registerArray.getJSONObject(i);
                int quantity = item.getInt("quantity");
                String name = item.getString("itemName");
                long price = item.getLong("price");
                totalPrice += price;
                document.insertString(document.getLength(), spaces(2) + quantity + spaces(3) + name + "\t" + numberFormat.format(price) + " \n", document.getStyle("TransactionStyle"));
                document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            }
            document.insertString(document.getLength(), spaces(4) + "\n\nPago Total:\t", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " " + numberFormat.format(totalPrice), document.getStyle("TransactionStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(6) + motelAddress + "\n", document.getStyle("FooterStyleBold"));
            printSeparator();
            document.insertString(document.getLength(), spaces(10) + dateService + "\n", document.getStyle("SecondLastStyle"));

            completePrinting("Sale", fullDateHourService, consecutiveTransaction, justPDF);
        } catch (BadLocationException ex) {
            Logger.getLogger(Printer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void printSummarizedTurn(JSONObject summarizedTurn, boolean justPDF) {
        printLayout.setText("");
        document = printLayout.getStyledDocument();

        ZonedDateTime fullDateTurnStart = ZonedDateTime.parse(summarizedTurn.getString("turnStart"));
        ZonedDateTime FullDateTurnEnd = ZonedDateTime.parse(summarizedTurn.getString("turnEnd"));
        int turnNumber = summarizedTurn.getInt("turnNumber");

        try {
            printHeader("RESUMEN VENTAS TURNO");
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));

            printTurnDateSection(fullDateTurnStart, FullDateTurnEnd);

            printSeparator();
            document.insertString(document.getLength(), "Cant\tConcepto\tPrecio\n", document.getStyle("TransactionStyle"));
            printSeparator();
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));

            JSONArray summaryArray = summarizedTurn.getJSONArray("turnSummary");
            long totalSales = 0;
            long totalItems = 0;
            long totalRooms = 0;
            for (int i = 0; i < summaryArray.length(); i++) {
                JSONObject summaryObject = summaryArray.getJSONObject(i);
                String change = summaryObject.getString("summaryType");
                if ("room".equals(change)) {
                    int quantity = summaryObject.getInt("quantity");
                    long price = summaryObject.getLong("price");
                    int service = summaryObject.getInt("service");
                    totalSales += price;
                    totalRooms += price;
                    document.insertString(document.getLength(), spaces(3) + quantity + " Alquiler " + service + "\t" + numberFormat.format(price) + "\n", document.getStyle("TransactionStyle"));
                } else if ("item".equals(change)) {
                    int quantity = summaryObject.getInt("quantity");
                    long price = summaryObject.getLong("price");
                    String itemName = summaryObject.getString("itemName");
                    document.insertString(document.getLength(), spaces(3) + quantity + spaces(2) + itemName + "\t" + numberFormat.format(price) + "\n", document.getStyle("TransactionStyle"));
                    totalSales += price;
                    totalItems += price;
                }
            }

            printTurnTotals(totalRooms, totalItems, totalSales);
            completePrinting("summarizedTurn", fullDateTurnStart, turnNumber, justPDF);
        } catch (BadLocationException ex) {
            Logger.getLogger(Printer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void printDetailedTurn(JSONObject detailedTurn, boolean justPDF) {
        printLayout.setText("");
        document = printLayout.getStyledDocument();

        ZonedDateTime fullDateTurnStart = ZonedDateTime.parse(detailedTurn.getString("turnStart"));
        ZonedDateTime FullDateTurnEnd = ZonedDateTime.parse(detailedTurn.getString("turnEnd"));
        int turnNumber = detailedTurn.getInt("turnNumber");

        try {
            printHeader("DETALLE VENTAS TURNO");
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));

            printTurnDateSection(fullDateTurnStart, FullDateTurnEnd);

            document.insertString(document.getLength(), "Tiempo Hab Concepto Precio\n", document.getStyle("TransactionStyleBold"));
            printSeparator();

            JSONArray turnActivity = detailedTurn.getJSONArray("turnActivity");
            long totalSales = 0;
            long totalItems = 0;
            long totalRooms = 0;
            for (int i = 0; i < turnActivity.length(); i++) {
                JSONObject change = turnActivity.getJSONObject(i);
                String changeType = change.getString("changeType");
                ZonedDateTime changeDate = ZonedDateTime.parse(change.getString("changeDate"));
                DateTimeFormatter specialDateFormatter = DateTimeFormatter.ofPattern("MM/dd-HH:mm");
                String formattedDate = changeDate.format(specialDateFormatter);
                if (changeType.equals("sale")) {
                    JSONArray register = change.getJSONArray("register");
                    for (int j = 0; j < register.length(); j++) {
                        JSONObject currentItem = register.getJSONObject(j);
                        document.insertString(document.getLength(), spaces(1) + formattedDate + "|" + change.getString("roomSoldTo")
                                + "|" + currentItem.getString("itemName") + "|" + currentItem.getLong("price") + "\n", document.getStyle("TransactionStyle"));
                        totalSales += currentItem.getLong("price");
                        totalItems += currentItem.getLong("price");
                    }
                } else if (changeType.equals("room") && change.getInt("roomStatus") == 3) {
                    totalSales += change.getLong("price");
                    totalRooms += change.getLong("price");
                    if (change.getInt("servicedExtension") == 0) {
                        document.insertString(document.getLength(), spaces(1) + formattedDate + "|" + change.getString("roomString")
                                + "|" + "Alquiler " + change.getInt("service") + "|" + change.getLong("price") + "\n", document.getStyle("TransactionStyle"));
                    } else {
                        document.insertString(document.getLength(), spaces(1) + formattedDate + "|" + change.getString("roomString")
                                + "|" + "Alquiler " + change.getInt("servicedExtension") + "|" + change.getLong("price") + "\n", document.getStyle("TransactionStyle"));
                    }
                } else if (changeType.equals("roomSwap")) {
                    document.insertString(document.getLength(), spaces(1) + formattedDate + "|" + change.getString("originalRoom")
                            + "|" + "Cambio a: " + change.getString("swapedRoom") + "\n", document.getStyle("TransactionStyle"));
                }
            }

            printTurnTotals(totalRooms, totalItems, totalSales);
            completePrinting("detailedTurnTurn", fullDateTurnStart, turnNumber, justPDF);
        } catch (BadLocationException ex) {
            Logger.getLogger(Printer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // ========== PDF & Print ==========

    private void saveAsPDF(String type, ZonedDateTime date, int consecutive) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            String formattedDateTime = date.format(formatter);
            PdfWriter writer = new PdfWriter(new FileOutputStream(PDF_SAVE_PATH + "\\" + formattedDateTime + "-No" + consecutive + "-" + type + ".pdf"));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            String text = printLayout.getText();
            document.add(new Paragraph(text).setTextAlignment(TextAlignment.LEFT));
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printWithService() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(printerService);
            PageFormat pf = job.defaultPage();
            Paper paper = pf.getPaper();
            paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
            pf.setPaper(paper);
            job.setPrintable(printLayout.getPrintable(null, null), pf);
            job.print();
        } catch (PrinterException ex) {
            Logger.getLogger(Printer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
