package model.modelManagers;

import model.modelManagers.FileManager;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
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
import model.dto.TurnSummaryItemData;
import model.turn.ExtraChangeActivity;
import model.turn.ExtraChangeType;
import model.turn.RefundActivity;
import model.turn.RefundType;
import model.turn.RoomBookingActivity;
import model.turn.RoomSwapActivity;
import model.turn.SaleActivity;
import model.turn.SaleItem;
import model.turn.SpendingActivity;
import model.turn.TurnActivity;
import model.turn.TurnDetails;

public class Printer {

    private static final Logger LOGGER = Logger.getLogger(Printer.class.getName());

    private static final DateTimeFormatter TURN_DATE_SECTION_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss");
    private static final DateTimeFormatter DETAILED_TURN_DATE_FORMATTER
            = DateTimeFormatter.ofPattern("MM/dd-HH:mm");

    private final JTextPane printLayout;
    private String motelName;
    private String motelAddress;
    private String motelID;
    private final DateTimeFormatter hourFormatter;
    private final DateTimeFormatter dateFormatter;
    private final NumberFormat numberFormat;
    private StyledDocument document;
    private final String PDF_SAVE_PATH = FileManager.PATH + File.separator + "receiptPrints";
    private PrintService printerService;

    public Printer() {
        System.out.println("Printer initialized");
        printLayout = new JTextPane();
        hourFormatter = new DateTimeFormatterBuilder()
                .appendPattern("hh:mm").appendLiteral(' ')
                .appendText(ChronoField.AMPM_OF_DAY, Map.of(0L, "AM", 1L, "PM"))
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
        String lowerName = printerName.toLowerCase();
        PrintService service = null;
        PrintService[] services = PrinterJob.lookupPrintServices();
        for (int index = 0; service == null && index < services.length; index++) {
            if (services[index].getName().toLowerCase().contains(lowerName)) {
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

        addStyle("HeaderStyle", 10, false, fontFamily);
        addStyle("LargeStyle", 19, false, fontFamily);
        addStyle("DefaultStyle", 10, false, fontFamily);
        addStyle("TransactionStyle", 9, false, fontFamily);
        addStyle("FooterStyle", 8, false, fontFamily);
        addStyle("SecondLastStyle", 7, false, fontFamily);

        addStyle("HeaderStyleBold", 10, true, fontFamily);
        addStyle("LargeStyleBold", 19, true, fontFamily);
        addStyle("DefaultStyleBold", 10, true, fontFamily);
        addStyle("TransactionStyleBold", 9, true, fontFamily);
        addStyle("FooterStyleBold", 8, true, fontFamily);
        addStyle("SecondLastStyleBold", 7, true, fontFamily);

        addStyle("FillerStyle", 1, false, fontFamily);

        Style centeredStyle = document.addStyle("CenteredStyle", null);
        StyleConstants.setAlignment(centeredStyle, StyleConstants.ALIGN_CENTER);
    }

    private void addStyle(String name, int fontSize, boolean bold, String fontFamily) {
        Style style = document.addStyle(name, null);
        StyleConstants.setFontSize(style, fontSize);
        StyleConstants.setFontFamily(style, fontFamily);
        if (bold) {
            StyleConstants.setBold(style, true);
        }
    }

    private void resetDocument() {
        printLayout.setText("");
        document = printLayout.getStyledDocument();
    }

    private void printFillerLines(int count) throws BadLocationException {
        for (int i = 0; i < count; i++) {
            document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
        }
    }

    // ========== Document Assembly Helpers ==========

    private static String spaces(int n) {
        return " ".repeat(n);
    }

    /**
     * Prints the standard header: name + address + NIT + legal text + separator.
     */
    private void printHeader() throws BadLocationException {
        document.insertString(document.getLength(), motelName + "\n", document.getStyle("LargeStyle"));
        document.insertString(document.getLength(), spaces(3) + motelAddress + "\n", document.getStyle("FooterStyleBold"));
        document.insertString(document.getLength(), spaces(3) + motelID + "\n", document.getStyle("FooterStyleBold"));
        document.insertString(document.getLength(), spaces(2) + "PERSONA NATURAL.\n NO RESPONSABLE DE IVA\n", document.getStyle("SecondLastStyleBold"));
        printSeparator();
    }

    /**
     * Prints a turn report subtitle (e.g. "RESUMEN VENTAS TURNO") after the standard header.
     */
    private void printTurnReportSubtitle(String subtitle) throws BadLocationException {
        document.insertString(document.getLength(), spaces(1) + subtitle + "\n", document.getStyle("FooterStyleBold"));
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

    public void printRoomTimeSell(RoomBookingActivity activity, int consecutiveTransaction, boolean justPDF) {
        resetDocument();

        String roomString = activity.roomString();
        long price = activity.price();
        int service = activity.getEffectiveService();

        ZonedDateTime fullDateHourService = activity.startStatus();
        String hourService = fullDateHourService.format(hourFormatter);
        String dateService = fullDateHourService.format(dateFormatter);

        try {
            printHeader();
            printTransactionNumber(consecutiveTransaction);

            printFillerLines(1);
            document.insertString(document.getLength(), spaces(3) + "Habitaci\u00f3n No:", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), roomString, document.getStyle("TransactionStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("TransactionStyle"));
            printFillerLines(1);
            document.insertString(document.getLength(), spaces(3) + "Hora Entrada: " + hourService + "\n", document.getStyle("TransactionStyle"));
            printFillerLines(1);
            document.insertString(document.getLength(), spaces(4) + "Servicio: " + service + " Horas" + "\n", document.getStyle("TransactionStyle"));
            printFillerLines(2);
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

            printFillerLines(3);
            completePrinting("roomBooked", fullDateHourService, consecutiveTransaction, justPDF);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public void printItemSold(SaleActivity activity, int consecutiveTransaction, boolean justPDF) {
        resetDocument();

        String roomString = activity.roomSoldTo();
        ZonedDateTime fullDateHourService = activity.changeDate();
        String hourService = fullDateHourService.format(hourFormatter);
        String dateService = fullDateHourService.format(dateFormatter);
        List<SaleItem> items = activity.items();
        long totalPrice = 0;

        try {
            printHeader();

            printFillerLines(1);
            document.insertString(document.getLength(), spaces(2) + "VENTA A LA HABITACI\u00d3N: ", document.getStyle("DefaultStyle"));
            document.insertString(document.getLength(), roomString, document.getStyle("TransactionStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("TransactionStyle"));
            printFillerLines(1);

            printTransactionNumber(consecutiveTransaction);

            printFillerLines(1);
            document.insertString(document.getLength(), spaces(3) + "HORA VENTA: " + hourService + "\n", document.getStyle("DefaultStyle"));
            document.insertString(document.getLength(), " \n\n\n", document.getStyle("FillerStyle"));

            for (SaleItem item : items) {
                long quantity = item.quantity();
                String name = item.itemName();
                long price = item.price();
                totalPrice += price;
                document.insertString(document.getLength(), spaces(2) + quantity + spaces(3) + name + "\t" + numberFormat.format(price) + " \n", document.getStyle("TransactionStyle"));
                printFillerLines(1);
            }
            document.insertString(document.getLength(), spaces(4) + "\n\nPago Total:\t", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " " + numberFormat.format(totalPrice), document.getStyle("TransactionStyleBold"));
            document.insertString(document.getLength(), "\n", document.getStyle("TransactionStyle"));
            printFillerLines(1);
            document.insertString(document.getLength(), spaces(6) + motelAddress + "\n", document.getStyle("FooterStyleBold"));
            printSeparator();
            document.insertString(document.getLength(), spaces(10) + dateService + "\n", document.getStyle("SecondLastStyle"));

            printFillerLines(3);
            completePrinting("Sale", fullDateHourService, consecutiveTransaction, justPDF);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    void printSummarizedTurn(TurnDetails turnDetails, boolean justPDF) {
        printSummarizedTurnInternal(turnDetails, false, justPDF);
    }

    void printDetailedTurn(TurnDetails turnDetails, boolean justPDF) {
        printDetailedTurnInternal(turnDetails, false, justPDF);
    }

    // ========== Current-Turn Printing (mid-turn, "No finalizado") ==========

    public void printSummarizedCurrentTurn(TurnDetails turnDetails) {
        printSummarizedTurnInternal(turnDetails, true, false);
    }

    private void printSummarizedTurnInternal(TurnDetails turnDetails, boolean isCurrent, boolean justPDF) {
        resetDocument();

        ZonedDateTime fullDateTurnStart = turnDetails.getTurnStart();
        int turnNumber = (int) turnDetails.getTurnNumber();

        try {
            printHeader();
            printTurnReportSubtitle("RESUMEN VENTAS TURNO");
            printFillerLines(1);

            String startDate = fullDateTurnStart.format(TURN_DATE_SECTION_FORMATTER);
            document.insertString(document.getLength(), "Inicio turno: \n", document.getStyle("SecondLastStyleBold"));
            document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);
            document.insertString(document.getLength(), startDate + "\n", document.getStyle("DefaultStyleBold"));
            document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);
            document.insertString(document.getLength(), "Fin turno: \n", document.getStyle("SecondLastStyleBold"));
            document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);
            if (isCurrent) {
                document.insertString(document.getLength(), "No finalizado\n", document.getStyle("DefaultStyleBold"));
            } else {
                ZonedDateTime fullDateTurnEnd = turnDetails.getTurnEnd();
                String endDate = fullDateTurnEnd.format(TURN_DATE_SECTION_FORMATTER);
                document.insertString(document.getLength(), endDate + "\n", document.getStyle("DefaultStyleBold"));
            }
            document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);

            printSeparator();
            document.insertString(document.getLength(), "Cant\tConcepto\tPrecio\n", document.getStyle("TransactionStyle"));
            printSeparator();
            printFillerLines(1);

            List<TurnSummaryItemData> summaryItems = turnDetails.getSummaryItems();
            for (TurnSummaryItemData si : summaryItems) {
                String change = si.summaryType();
                if ("room".equals(change)) {
                    document.insertString(document.getLength(), spaces(3) + si.quantity() + " Alquiler " + si.service()
                            + "\t" + numberFormat.format(si.price()) + "\n", document.getStyle("TransactionStyle"));
                } else if ("item".equals(change)) {
                    document.insertString(document.getLength(), spaces(3) + si.quantity() + spaces(2) + si.name()
                            + "\t" + numberFormat.format(si.price()) + "\n", document.getStyle("TransactionStyle"));
                }
            }

            printTurnTotals(turnDetails.getTotalRooms(), turnDetails.getTotalItems(), turnDetails.getTotalSales());

            if (isCurrent) {
                printSummarizedTurnExtras(turnDetails);
                printFillerLines(3);
                saveAsPDF("summarizedTurn", fullDateTurnStart, turnNumber);
                printWithService();
            } else {
                printFillerLines(3);
                completePrinting("summarizedTurn", fullDateTurnStart, turnNumber, justPDF);
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /** Prints the refunds / spending / transfers / deposits section for summarized current turn. */
    private void printSummarizedTurnExtras(TurnDetails turnDetails) throws BadLocationException {
        printFillerLines(1);
        document.insertString(document.getLength(), "REEMBOLSOS\n", document.getStyle("FooterStyleBold"));
        printSeparator();
        document.insertString(document.getLength(), "Cant\tConcepto\tPrecio\n", document.getStyle("TransactionStyle"));
        printSeparator();
        printFillerLines(1);

        long totalRefunds = turnDetails.getTotalRefunds();
        long totalItemRefunds = turnDetails.getTotalItemRefunds();
        long totalRoomRefunds = turnDetails.getTotalRoomRefunds();
        for (TurnSummaryItemData si : turnDetails.getSummaryItems()) {
            String change = si.summaryType();
            if ("roomRefund".equals(change)) {
                document.insertString(document.getLength(), spaces(1) + si.quantity() + " Alquiler " + si.service()
                        + "\t" + numberFormat.format(si.price()) + "\n", document.getStyle("TransactionStyle"));
            } else if ("itemRefund".equals(change)) {
                document.insertString(document.getLength(), spaces(1) + si.quantity() + spaces(2) + si.name()
                        + "\t" + numberFormat.format(si.price()) + "\n", document.getStyle("TransactionStyle"));
            }
        }
        printFillerLines(1);
        printSeparator();
        printFillerLines(1);
        document.insertString(document.getLength(), spaces(2) + "Reembolsos Habitaciones: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(totalRoomRefunds) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(2);
        document.insertString(document.getLength(), spaces(4) + "Reembolsos Productos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(totalItemRefunds) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(1);
        document.insertString(document.getLength(), "Total Reembolsos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(totalRefunds) + "\n", document.getStyle("DefaultStyleBold"));
        printSeparator();
        printFillerLines(1);

        document.insertString(document.getLength(), spaces(2) + "Gastos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(turnDetails.getTotalSpending()) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(1);
        document.insertString(document.getLength(), "Total Turno: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(turnDetails.getTotalTurn()) + "\n", document.getStyle("DefaultStyleBold"));
        printSeparator();
        printFillerLines(1);
        document.insertString(document.getLength(), spaces(2) + "Transferencias: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(turnDetails.getTotalBankTransfers()) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(2);
        document.insertString(document.getLength(), spaces(2) + "Depositos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(turnDetails.getTotalDeposits()) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(1);
        document.insertString(document.getLength(), "Total Neto: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(turnDetails.getTotalNet()) + "\n", document.getStyle("DefaultStyleBold"));
    }

    public void printDetailedCurrentTurn(TurnDetails turnDetails) {
        printDetailedTurnInternal(turnDetails, true, false);
    }

    private void printDetailedTurnInternal(TurnDetails turnDetails, boolean isCurrent, boolean justPDF) {
        resetDocument();

        ZonedDateTime fullDateTurnStart = turnDetails.getTurnStart();
        int turnNumber = (int) turnDetails.getTurnNumber();

        try {
            printHeader();
            printTurnReportSubtitle("DETALLE VENTAS TURNO");
            printFillerLines(1);

            String startDate = fullDateTurnStart.format(TURN_DATE_SECTION_FORMATTER);
            document.insertString(document.getLength(), "Inicio turno: \n", document.getStyle("SecondLastStyleBold"));
            document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);
            document.insertString(document.getLength(), startDate + "\n", document.getStyle("DefaultStyleBold"));
            document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);
            document.insertString(document.getLength(), "Fin turno: \n", document.getStyle("SecondLastStyleBold"));
            document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);
            if (isCurrent) {
                document.insertString(document.getLength(), "No finalizado\n", document.getStyle("DefaultStyleBold"));
            } else {
                ZonedDateTime fullDateTurnEnd = turnDetails.getTurnEnd();
                String endDate = fullDateTurnEnd.format(TURN_DATE_SECTION_FORMATTER);
                document.insertString(document.getLength(), endDate + "\n", document.getStyle("DefaultStyleBold"));
            }
            document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);

            document.insertString(document.getLength(), "Tiempo Hab Concepto Precio\n", document.getStyle("TransactionStyleBold"));
            printSeparator();

            List<TurnActivity> activities = turnDetails.getActivities();
            for (TurnActivity activity : activities) {
                ZonedDateTime changeDate = activity.changeDate();
                String formattedDate = changeDate.format(DETAILED_TURN_DATE_FORMATTER);

                switch (activity) {
                    case SaleActivity s -> {
                        for (SaleItem item : s.items()) {
                            document.insertString(document.getLength(),
                                    spaces(1) + formattedDate + "|" + s.roomSoldTo()
                                            + "|" + item.itemName() + "|" + item.price() + "\n",
                                    document.getStyle("TransactionStyle"));
                        }
                    }
                    case RoomBookingActivity r -> {
                        if (r.isOccupied()) {
                            int displayedService = r.getEffectiveService();
                            document.insertString(document.getLength(),
                                    spaces(1) + formattedDate + "|" + r.roomString()
                                            + "|" + "Alquiler " + displayedService + "|" + r.price() + "\n",
                                    document.getStyle("TransactionStyle"));
                        }
                    }
                    case RoomSwapActivity r -> {
                        document.insertString(document.getLength(),
                                spaces(1) + formattedDate + "|" + r.originalRoom()
                                        + "|" + "Cambio a: " + r.swappedRoom() + "\n",
                                document.getStyle("TransactionStyle"));
                    }
                    case RefundActivity r -> {
                        if (isCurrent) {
                            if (r.refundType() == RefundType.SALE_REFUND) {
                                document.insertString(document.getLength(),
                                        spaces(1) + formattedDate + "|Reembolso de " + r.quantity()
                                                + " de " + r.itemName() + "|" + r.price() + "\n",
                                        document.getStyle("TransactionStyle"));
                            } else {
                                document.insertString(document.getLength(),
                                        spaces(1) + formattedDate + "|Reembolso de habitacion " + r.refundRoom()
                                                + "|" + r.price() + "\n",
                                        document.getStyle("TransactionStyle"));
                            }
                        }
                    }
                    case SpendingActivity s -> {
                        if (isCurrent) {
                            document.insertString(document.getLength(),
                                    spaces(1) + formattedDate + "|Gasto de: " + s.description()
                                            + "|" + s.value() + "\n",
                                    document.getStyle("TransactionStyle"));
                        }
                    }
                    case ExtraChangeActivity e -> {
                        if (isCurrent) {
                            String label = e.extraType() == ExtraChangeType.SAFE_DEPOSIT ? "Deposito de: " : "Transferencia de: ";
                            document.insertString(document.getLength(),
                                    spaces(1) + formattedDate + "|" + label + e.description()
                                            + "|" + e.value() + "\n",
                                    document.getStyle("TransactionStyle"));
                        }
                    }
                }
            }

            printTurnTotals(turnDetails.getTotalRooms(), turnDetails.getTotalItems(), turnDetails.getTotalSales());

            if (isCurrent) {
                printDetailedTurnExtras(turnDetails);
                printFillerLines(3);
                saveAsPDF("detailedTurnTurn", fullDateTurnStart, turnNumber);
                printWithService();
            } else {
                printFillerLines(3);
                completePrinting("detailedTurnTurn", fullDateTurnStart, turnNumber, justPDF);
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /** Prints the refunds / spending / transfers / deposits section for detailed current turn. */
    private void printDetailedTurnExtras(TurnDetails turnDetails) throws BadLocationException {
        printSeparator();
        printFillerLines(1);
        document.insertString(document.getLength(), spaces(2) + "Reembolsos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(turnDetails.getTotalRefunds()) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(2);
        document.insertString(document.getLength(), spaces(4) + "Gastos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(turnDetails.getTotalSpending()) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(1);
        document.insertString(document.getLength(), "Total Turno: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(turnDetails.getTotalTurn()) + "\n", document.getStyle("DefaultStyleBold"));
        printSeparator();
        printFillerLines(1);
        document.insertString(document.getLength(), spaces(2) + "Transferencias: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(turnDetails.getTotalBankTransfers()) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(2);
        document.insertString(document.getLength(), spaces(2) + "Depositos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(turnDetails.getTotalDeposits()) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(1);
        document.insertString(document.getLength(), "Total Neto: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), numberFormat.format(turnDetails.getTotalNet()) + "\n", document.getStyle("DefaultStyleBold"));
    }

    // ========== PDF & Print ==========

    private void saveAsPDF(String type, ZonedDateTime date, int consecutive) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String formattedDateTime = date.format(formatter);
        String filePath = PDF_SAVE_PATH + File.separator + formattedDateTime + "-No" + consecutive + "-" + type + ".pdf";
        try (FileOutputStream fos = new FileOutputStream(filePath);
             PdfDocument pdfDoc = new PdfDocument(new PdfWriter(fos));
             Document document = new Document(pdfDoc)) {
            String text = printLayout.getText();
            document.add(new Paragraph(text).setTextAlignment(TextAlignment.LEFT));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save PDF: " + filePath, e);
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
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
