package model;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.print.*;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.kernel.pdf.PdfDocument;
import java.io.File;

import org.json.JSONObject;

public class Printer {

    private JTextPane printLayout;
    private final String motelName;
    private final int percentTax = 19;
    private final String motelDirection;
    private DateTimeFormatter hourFormatter;
    private DateTimeFormatter dateFormatter;
    private NumberFormat numberFormat;
    private StyledDocument document;
    private final String PDF_SAVE_PATH = FileManager.PATH + "\\receiptPrints";

    public Printer() {
        printLayout = new JTextPane();
        motelName = "";
        motelDirection = "";
        hourFormatter = new DateTimeFormatterBuilder()
                .appendPattern("hh:mm").appendLiteral(' ')
                .appendText(ChronoField.AMPM_OF_DAY, Map.of(0L, "AM", 1L, "PM"))
                .toFormatter();
        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", new Locale("es", "ES"));
        numberFormat = NumberFormat.getNumberInstance(Locale.US);
        initializeStyles();
        File preparePDFRoute = new File(PDF_SAVE_PATH);
        preparePDFRoute.mkdirs();
    }

    private void initializeStyles() {
        document = printLayout.getStyledDocument();

        String fontFamily = "Calibri";  // Font family to be used for all styles

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

        //BOLD VARIANTS
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
    }

    private String spaces(int n) {
        return " ".repeat(n);
    }

    public void printRoomTimeSell(JSONObject roomTimeSell, int consecutiveTransaction, boolean justPDF) {
        // Clear the print layout
        printLayout.setText("");

        String roomString = roomTimeSell.getString("roomString");
        long price = roomTimeSell.getLong("price");
        long priceWithTax = (long) (price * ((100.0 - percentTax) / 100));
        long taxPrice = (long) (price * (percentTax / 100.0));
        int service = roomTimeSell.getInt("service");

        ZonedDateTime fullDateHourService = ZonedDateTime.parse(roomTimeSell.getString("startStatus"));

        String hourService = fullDateHourService.format(hourFormatter);
        String dateService = fullDateHourService.format(dateFormatter);

        document = printLayout.getStyledDocument();

        try {
            document.insertString(document.getLength(), spaces(2) + "MOTEL \n", document.getStyle("HeaderStyle"));
            document.insertString(document.getLength(), spaces(1) + motelName + "\n", document.getStyle("LargeStyle"));
            document.insertString(document.getLength(), "___________________________________\n", document.getStyle("SecondLastStyleBold"));
            document.insertString(document.getLength(), spaces(2) + "FACTURA DE VENTA No. ", document.getStyle("DefaultStyle"));
            document.insertString(document.getLength(), spaces(2) + consecutiveTransaction, document.getStyle("DefaultStyleBold"));
            document.insertString(document.getLength(), spaces(2) + "\n", document.getStyle("DefaultStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(3) + "Habitación No:", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), roomString, document.getStyle("TransactionStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));

            document.insertString(document.getLength(), spaces(3) + "Hora Entrada: " + hourService + "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(4) + "Servicio: " + service + " Horas" + "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(10) + "Valor:\t" + numberFormat.format(priceWithTax) + "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(6) + "IVA 19%:\t" + numberFormat.format(taxPrice) + "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(4) + "Pago Total:\t", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " " + numberFormat.format(price), document.getStyle("TransactionStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), "\n \n", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), "___________________________________\n", document.getStyle("SecondLastStyleBold"));

            document.insertString(document.getLength(), "NO OLVIDE SUS PERTENENCIAS", document.getStyle("SecondLastStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("DefaultStyle"));

            document.insertString(document.getLength(), spaces(6) + motelDirection + "\n", document.getStyle("FooterStyleBold"));
            document.insertString(document.getLength(), "___________________________________\n", document.getStyle("SecondLastStyleBold"));
            document.insertString(document.getLength(), spaces(10) + dateService + "\n", document.getStyle("SecondLastStyle"));

            //Save of a PDF beforehand
            saveAsPDF("roomBooked", fullDateHourService, consecutiveTransaction);

            // Printing process
            if (!justPDF) {
                PrinterJob job = PrinterJob.getPrinterJob();
                PageFormat pf = job.defaultPage();
                Paper paper = pf.getPaper();
                paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
                pf.setPaper(paper);
                job.setPrintable(printLayout.getPrintable(null, null), pf);

                // Print without showing the print dialog
                job.print();
            }

        } catch (BadLocationException | PrinterException ex) {
            Logger.getLogger(Printer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void printItemSold(JSONObject transaction, int consecutiveTransaction, boolean justPDF) {
        // Clear the print layout
        printLayout.setText("");
        String roomString = transaction.getString("roomSoldTo");
        ZonedDateTime fullDateHourService = ZonedDateTime.parse(transaction.getString("changeDate"));

        String hourService = fullDateHourService.format(hourFormatter);
        String dateService = fullDateHourService.format(dateFormatter);

        JSONArray registerArray = transaction.getJSONArray("register");
        long totalPrice = 0;

        //Start of the formatting for the page
        document = printLayout.getStyledDocument();
        try {
            document.insertString(document.getLength(), spaces(2) + "MOTEL \n", document.getStyle("HeaderStyle"));
            document.insertString(document.getLength(), spaces(1) + motelName + "\n", document.getStyle("LargeStyle"));
            document.insertString(document.getLength(), "___________________________________\n", document.getStyle("SecondLastStyleBold"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(2) + "VENTA A LA HABITACIÓN: ", document.getStyle("DefaultStyle"));
            document.insertString(document.getLength(), roomString, document.getStyle("TransactionStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("TransactionStyle"));

            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(2) + "FACTURA DE VENTA No. ", document.getStyle("DefaultStyle"));
            document.insertString(document.getLength(), spaces(2) + consecutiveTransaction, document.getStyle("DefaultStyleBold"));
            document.insertString(document.getLength(), spaces(2) + "\n", document.getStyle("DefaultStyle"));
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(3) + "HORA VENTA: " + hourService + "\n", document.getStyle("DefaultStyle"));
            document.insertString(document.getLength(), " \n\n\n", document.getStyle("FillerStyle"));

            for (int i = 0; i < registerArray.length(); i++) {
                JSONObject item = registerArray.getJSONObject(i);

                int quantity = item.getInt("quantity");
                String name = item.getString("itemName");
                long price = item.getLong("price");
                totalPrice += price;
                document.insertString(document.getLength(), spaces(2) + String.valueOf(quantity) + spaces(3) + name + "\t" + numberFormat.format(price) + " \n", document.getStyle("TransactionStyle"));
                document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            }
            document.insertString(document.getLength(), spaces(4) + "\n\nPago Total:\t", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " " + numberFormat.format(totalPrice), document.getStyle("TransactionStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("TransactionStyle"));

            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
            document.insertString(document.getLength(), spaces(6) + motelDirection + "\n", document.getStyle("FooterStyleBold"));
            document.insertString(document.getLength(), "___________________________________\n", document.getStyle("SecondLastStyleBold"));
            document.insertString(document.getLength(), spaces(10) + dateService + "\n", document.getStyle("SecondLastStyle"));

            //Saving a pdf beforehand
            saveAsPDF("Sale", fullDateHourService, consecutiveTransaction);

            if (!justPDF) {
                PrinterJob job = PrinterJob.getPrinterJob();
                PageFormat pf = job.defaultPage();
                Paper paper = pf.getPaper();
                paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
                pf.setPaper(paper);
                job.setPrintable(printLayout.getPrintable(null, null), pf);

                // Print without showing the print dialog
                job.print();
            }
        } catch (BadLocationException | PrinterException ex) {
            Logger.getLogger(Printer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void saveAsPDF(String type, ZonedDateTime date, int consecutive) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH-mm-ss-yyyy-MM-dd");

            // Format the ZonedDateTime using the formatter
            String formattedDateTime = date.format(formatter);
            PdfWriter writer = new PdfWriter(new FileOutputStream(PDF_SAVE_PATH + "\\" + consecutive + "-" + type + formattedDateTime + ".pdf"));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            String text = printLayout.getText();
            document.add(new Paragraph(text).setTextAlignment(TextAlignment.LEFT));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void printSummarizedTurn(JSONObject summarizedTurn, boolean b) {
        printLayout.setText("");
        ZonedDateTime fullDateTurnStart = ZonedDateTime.parse(summarizedTurn.getString("turnStart"));
        ZonedDateTime FullDateTurnEnd = ZonedDateTime.parse(summarizedTurn.getString("turnEnd"));
    }

    void printDetailedTurn(JSONObject summarizedTurn, boolean b) {
        printLayout.setText("");
        ZonedDateTime fullDateTurnStart = ZonedDateTime.parse(summarizedTurn.getString("turnStart"));
        ZonedDateTime FullDateTurnEnd = ZonedDateTime.parse(summarizedTurn.getString("turnEnd"));
    }
}
