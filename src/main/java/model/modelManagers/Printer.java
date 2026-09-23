package model.modelManagers;

import model.modelManagers.FileManager;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintService;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.pdf.PdfDocument;
import java.io.File;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import model.dto.TurnSummaryItemData;
import model.json.CurrencyConfig;
import model.print.PrintDataBuilder;
import model.print.PrintFieldRegistry;
import model.print.PrintTemplate;
import model.print.PrintTemplateStore;
import model.print.PrintTemplateType;
import model.print.TemplateRenderer;
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
import view.helpers.CurrencyFormatter;
import view.helpers.TimeFormatter;

/**
 * Handles receipt and turn-report printing, as well as PDF generation.
 *
 * <p>Uses a {@link JTextPane} for document assembly and supports printing to
 * configured {@link PrintService} instances or saving as PDF via iText.
 *
 * <p>Print outputs are also saved to the {@code receiptPrints/} directory as PDF files.
 */
public class Printer {

    private static final Logger LOGGER = Logger.getLogger(Printer.class.getName());

    /** Paper used when the printer declares no size: a standard 80 mm thermal roll. */
    private static final int DEFAULT_PAPER_WIDTH_MM = 80;
    private static final int DEFAULT_PAPER_HEIGHT_MM = 297;
    /** Widest paper still considered a thermal roll; above this the declaration is a sheet. */
    private static final int MAX_ROLL_WIDTH_MM = 120;
    private static final double POINTS_PER_MM = 72.0 / 25.4;

    private final JTextPane printLayout;
    private String motelName;
    private String motelAddress;
    private String motelID;
    private CurrencyConfig currencyConfig = CurrencyConfig.defaultConfig();
    private StyledDocument document;
    private final String PDF_SAVE_PATH = FileManager.PATH + File.separator + "receiptPrints";
    private final PrintTemplateStore templateStore = new PrintTemplateStore(Path.of(FileManager.PATH));
    private PrintService printerService;

    /**
     * Initialises the printer, sets up text styles, creates the PDF output
     * directory, and discovers the default print service.
     */
    public Printer() {
        LOGGER.fine("Printer initialized");
        printLayout = new JTextPane();
        initializeStyles();
        File preparePDFRoute = new File(PDF_SAVE_PATH);
        preparePDFRoute.mkdirs();
        printerService = PrinterJob.getPrinterJob().getPrintService();
    }

    /**
     * Currently not used, to be implemented custom currency configuration
     * @param cfg 
     */
    public void setCurrencyConfig(CurrencyConfig cfg) {
        this.currencyConfig = cfg != null ? cfg : CurrencyConfig.defaultConfig();
    }

    /**
     * Sets the motel information printed on each receipt header.
     *
     * @param name    motel name
     * @param address motel address
     * @param iD      motel tax / NIT identifier
     */
    public void setPrinterVariables(String name, String address, String iD) {
        this.motelName = name;
        this.motelAddress = address;
        this.motelID = iD;
    }

    /**
     * Returns the names of all available print services.
     *
     * @return list of printer names
     */
    public List<String> getPrinterServiceNameList() {
        PrintService[] services = PrinterJob.lookupPrintServices();
        List<String> list = new ArrayList<>();
        for (PrintService service : services) {
            list.add(service.getName());
        }
        return list;
    }

    /**
     * Selects a print service whose name contains the given string (case-insensitive).
     *
     * @param printerName substring to match against available printer names
     */
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

    /**
     * Returns the name of the currently selected printer.
     *
     * @return printer name, or {@code "N/A"} if none is selected
     */
    public String getCurrentPrinterName() {
        if (printerService == null) {
            return "N/A";
        }
        return printerService.getName();
    }

    /**
     * Returns the name of the first available print service.
     *
     * @return first printer name, or {@code null} if none are available
     */
    public String getFirstAvailablePrinterName() {
        PrintService[] services = PrinterJob.lookupPrintServices();
        if (services.length > 0) {
            return services[0].getName();
        }
        return null;
    }

    private void initializeStyles() {
        document = printLayout.getStyledDocument();
        TemplateRenderer.applyReceiptStyles(document);
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
        document.insertString(document.getLength(), CurrencyFormatter.format(totalRooms, currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
        document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
        document.insertString(document.getLength(), spaces(4) + "Productos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(totalItems, currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        document.insertString(document.getLength(), " \n", document.getStyle("FillerStyle"));
        document.insertString(document.getLength(), "Total Turno: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(totalSales, currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
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

    /**
     * Prints a room booking receipt.
     *
     * @param activity               the room booking activity data
     * @param consecutiveTransaction the invoice / transaction number
     * @param justPDF                if {@code true} only saves the PDF without physical printing
     */
    public void printRoomTimeSell(RoomBookingActivity activity, int consecutiveTransaction, boolean justPDF) {
        if (printWithTemplate(PrintTemplateType.ROOM_RECEIPT,
                PrintDataBuilder.roomReceipt(activity, consecutiveTransaction, printContext()),
                "roomBooked", activity.startStatus(), consecutiveTransaction, justPDF)) {
            return;
        }

        resetDocument();

        String roomString = activity.roomString();
        long price = activity.price();
        long serviceDuration = activity.getEffectiveServiceDuration();

        ZonedDateTime fullDateHourService = activity.startStatus();
        String hourService = fullDateHourService.format(PrintDataBuilder.HOUR_FORMATTER);
        String dateService = fullDateHourService.format(PrintDataBuilder.DATE_FORMATTER);

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
            document.insertString(document.getLength(), spaces(4) + "Servicio: " + TimeFormatter.formatDuration(serviceDuration) + "\n", document.getStyle("TransactionStyle"));
            printFillerLines(2);
            document.insertString(document.getLength(), spaces(4) + "Pago Total:\t", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " " + CurrencyFormatter.format(price, currencyConfig), document.getStyle("TransactionStyleBold"));
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

    /**
     * Prints an item sale receipt.
     *
     * @param activity               the sale activity data
     * @param consecutiveTransaction the invoice / transaction number
     * @param justPDF                if {@code true} only saves the PDF without physical printing
     */
    public void printItemSold(SaleActivity activity, int consecutiveTransaction, boolean justPDF) {
        if (printWithTemplate(PrintTemplateType.SALE_RECEIPT,
                PrintDataBuilder.saleReceipt(activity, consecutiveTransaction, printContext()),
                "Sale", activity.changeDate(), consecutiveTransaction, justPDF)) {
            return;
        }

        resetDocument();

        String roomString = activity.roomSoldTo();
        ZonedDateTime fullDateHourService = activity.changeDate();
        String hourService = fullDateHourService.format(PrintDataBuilder.HOUR_FORMATTER);
        String dateService = fullDateHourService.format(PrintDataBuilder.DATE_FORMATTER);
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
                document.insertString(document.getLength(), spaces(2) + quantity + spaces(3) + name + "\t" + CurrencyFormatter.format(price, currencyConfig) + " \n", document.getStyle("TransactionStyle"));
                printFillerLines(1);
            }
            document.insertString(document.getLength(), spaces(4) + "\n\nPago Total:\t", document.getStyle("TransactionStyle"));
            document.insertString(document.getLength(), " " + CurrencyFormatter.format(totalPrice, currencyConfig), document.getStyle("TransactionStyleBold"));
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

    /**
     * Prints a summarised turn report for the currently active (unfinished) turn.
     * Always both saves the PDF and prints physically.
     *
     * @param turnDetails the turn data to print
     */
    public void printSummarizedCurrentTurn(TurnDetails turnDetails) {
        printSummarizedTurnInternal(turnDetails, true, false);
    }

    private void printSummarizedTurnInternal(TurnDetails turnDetails, boolean isCurrent, boolean justPDF) {
        if (printWithTemplate(PrintTemplateType.TURN_SUMMARY,
                PrintDataBuilder.turnSummary(turnDetails, isCurrent, printContext()),
                "summarizedTurn", turnDetails.getTurnStart(), (int) turnDetails.getTurnNumber(), justPDF)) {
            return;
        }

        resetDocument();

        ZonedDateTime fullDateTurnStart = turnDetails.getTurnStart();
        int turnNumber = (int) turnDetails.getTurnNumber();

        try {
            printHeader();
            printTurnReportSubtitle("RESUMEN VENTAS TURNO");
            printFillerLines(1);

            String startDate = fullDateTurnStart.format(PrintDataBuilder.TURN_DATE_FORMATTER);
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
                String endDate = fullDateTurnEnd.format(PrintDataBuilder.TURN_DATE_FORMATTER);
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
                    document.insertString(document.getLength(), spaces(3) + si.quantity() + " Alquiler " + TimeFormatter.formatDuration(si.serviceDuration())
                            + "\t" + CurrencyFormatter.format(si.price(), currencyConfig) + "\n", document.getStyle("TransactionStyle"));
                } else if ("item".equals(change)) {
                    document.insertString(document.getLength(), spaces(3) + si.quantity() + spaces(2) + si.name()
                            + "\t" + CurrencyFormatter.format(si.price(), currencyConfig) + "\n", document.getStyle("TransactionStyle"));
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
                document.insertString(document.getLength(), spaces(1) + si.quantity() + " Alquiler " + TimeFormatter.formatDuration(si.serviceDuration())
                        + "\t" + CurrencyFormatter.format(si.price(), currencyConfig) + "\n", document.getStyle("TransactionStyle"));
            } else if ("itemRefund".equals(change)) {
                document.insertString(document.getLength(), spaces(1) + si.quantity() + spaces(2) + si.name()
                        + "\t" + CurrencyFormatter.format(si.price(), currencyConfig) + "\n", document.getStyle("TransactionStyle"));
            }
        }
        printFillerLines(1);
        printSeparator();
        printFillerLines(1);
        document.insertString(document.getLength(), spaces(2) + "Reembolsos Habitaciones: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(totalRoomRefunds, currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(2);
        document.insertString(document.getLength(), spaces(4) + "Reembolsos Productos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(totalItemRefunds, currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(1);
        document.insertString(document.getLength(), "Total Reembolsos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(totalRefunds, currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printSeparator();
        printFillerLines(1);

        document.insertString(document.getLength(), spaces(2) + "Gastos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(turnDetails.getTotalSpending(), currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(1);
        document.insertString(document.getLength(), "Total Turno: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(turnDetails.getTotalTurn(), currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printSeparator();
        printFillerLines(1);
        document.insertString(document.getLength(), spaces(2) + "Transferencias: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(turnDetails.getTotalBankTransfers(), currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(2);
        document.insertString(document.getLength(), spaces(2) + "Depositos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(turnDetails.getTotalDeposits(), currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(1);
        document.insertString(document.getLength(), "Total Neto: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(turnDetails.getTotalNet(), currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
    }

    public void printDetailedCurrentTurn(TurnDetails turnDetails) {
        printDetailedTurnInternal(turnDetails, true, false);
    }

    private void printDetailedTurnInternal(TurnDetails turnDetails, boolean isCurrent, boolean justPDF) {
        if (printWithTemplate(PrintTemplateType.TURN_DETAIL,
                PrintDataBuilder.turnDetail(turnDetails, isCurrent, printContext()),
                "detailedTurnTurn", turnDetails.getTurnStart(), (int) turnDetails.getTurnNumber(), justPDF)) {
            return;
        }

        resetDocument();

        ZonedDateTime fullDateTurnStart = turnDetails.getTurnStart();
        int turnNumber = (int) turnDetails.getTurnNumber();

        try {
            printHeader();
            printTurnReportSubtitle("DETALLE VENTAS TURNO");
            printFillerLines(1);

            String startDate = fullDateTurnStart.format(PrintDataBuilder.TURN_DATE_FORMATTER);
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
                String endDate = fullDateTurnEnd.format(PrintDataBuilder.TURN_DATE_FORMATTER);
                document.insertString(document.getLength(), endDate + "\n", document.getStyle("DefaultStyleBold"));
            }
            document.setParagraphAttributes(document.getLength() - 1, 1, document.getStyle("CenteredStyle"), false);

            document.insertString(document.getLength(), "Tiempo Hab Concepto Precio\n", document.getStyle("TransactionStyleBold"));
            printSeparator();

            List<TurnActivity> activities = turnDetails.getActivities();
            for (TurnActivity activity : activities) {
                ZonedDateTime changeDate = activity.changeDate();
                String formattedDate = changeDate.format(PrintDataBuilder.DETAIL_DATE_FORMATTER);

                switch (activity) {
                    case SaleActivity s -> {
                        for (SaleItem item : s.items()) {
                            document.insertString(document.getLength(),
                                    spaces(1) + formattedDate + "|" + s.roomSoldTo()
                                            + "|" + item.itemName() + "|" + CurrencyFormatter.format(item.price(), currencyConfig) + "\n",
                                    document.getStyle("TransactionStyle"));
                        }
                    }
                    case RoomBookingActivity r -> {
                        if (r.isOccupied()) {
                            long displayedService = r.getEffectiveServiceDuration();
                            document.insertString(document.getLength(),
                                    spaces(1) + formattedDate + "|" + r.roomString()
                                            + "|" + "Alquiler " + TimeFormatter.formatDuration(displayedService) + "|" + CurrencyFormatter.format(r.price(), currencyConfig) + "\n",
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
                                                + " de " + r.itemName() + "|" + CurrencyFormatter.format(r.price(), currencyConfig) + "\n",
                                        document.getStyle("TransactionStyle"));
                            } else {
                                document.insertString(document.getLength(),
                                        spaces(1) + formattedDate + "|Reembolso de habitacion " + r.refundRoom()
                                                + "|" + CurrencyFormatter.format(r.price(), currencyConfig) + "\n",
                                        document.getStyle("TransactionStyle"));
                            }
                        }
                    }
                    case SpendingActivity s -> {
                        if (isCurrent) {
                            document.insertString(document.getLength(),
                                    spaces(1) + formattedDate + "|Gasto de: " + s.description()
                                            + "|" + CurrencyFormatter.format(s.value(), currencyConfig) + "\n",
                                    document.getStyle("TransactionStyle"));
                        }
                    }
                    case ExtraChangeActivity e -> {
                        if (isCurrent) {
                            String label = e.extraType() == ExtraChangeType.SAFE_DEPOSIT ? "Deposito de: " : "Transferencia de: ";
                            document.insertString(document.getLength(),
                                    spaces(1) + formattedDate + "|" + label + e.description()
                                            + "|" + CurrencyFormatter.format(e.value(), currencyConfig) + "\n",
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
        document.insertString(document.getLength(), CurrencyFormatter.format(turnDetails.getTotalRefunds(), currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(2);
        document.insertString(document.getLength(), spaces(4) + "Gastos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(turnDetails.getTotalSpending(), currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(1);
        document.insertString(document.getLength(), "Total Turno: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(turnDetails.getTotalTurn(), currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printSeparator();
        printFillerLines(1);
        document.insertString(document.getLength(), spaces(2) + "Transferencias: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(turnDetails.getTotalBankTransfers(), currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(2);
        document.insertString(document.getLength(), spaces(2) + "Depositos: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(turnDetails.getTotalDeposits(), currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
        printFillerLines(1);
        document.insertString(document.getLength(), "Total Neto: ", document.getStyle("DefaultStyle"));
        document.insertString(document.getLength(), CurrencyFormatter.format(turnDetails.getTotalNet(), currencyConfig) + "\n", document.getStyle("DefaultStyleBold"));
    }

    // ========== Template-Based Printing ==========

    /**
     * Prints a document with the customized template the user saved for the given type.
     *
     * @param type    the template type
     * @param data    the field values to resolve, as built by {@link PrintDataBuilder}
     * @param pdfType PDF file name discriminator
     * @param date    timestamp used in the PDF file name
     * @param id      transaction identifier used in the PDF file name
     * @param justPDF if {@code true} only the PDF is saved, without physical printing
     * @return {@code true} when the template was used; {@code false} means the caller must
     *         fall back to the built-in layout
     */
    private boolean printWithTemplate(PrintTemplateType type, Map<String, String> data, String pdfType,
                                      ZonedDateTime date, int id, boolean justPDF) {
        PrintTemplate template = templateStore.load(type);
        if (template == null) {
            return false;
        }
        try {
            resetDocument();
            TemplateRenderer.render(template, data, document);
            completePrinting(pdfType, date, id, justPDF);
            return true;
        } catch (BadLocationException e) {
            LOGGER.log(Level.SEVERE, "Fallo al imprimir la plantilla " + type, e);
            return false;
        }
    }

    private PrintDataBuilder.PrintContext printContext() {
        return new PrintDataBuilder.PrintContext(motelName, motelAddress, motelID, currencyConfig);
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
        PageFormat format = pageFormat();
        if (format == null) {
            return;
        }
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(printerService);
            job.setPrintable(printLayout.getPrintable(null, null), format);
            job.print();
        } catch (PrinterException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    // ========== Paper Size ==========

    /**
     * Returns the page format the print job lays out to: the printer's declared page with
     * its imageable area stretched over the whole paper. Both the real print and the paper
     * size reported to the editor come from here, so the preview cannot drift from the print.
     *
     * @return the page format, or {@code null} when no print service is selected or it
     *         rejects the job
     */
    private PageFormat pageFormat() {
        if (printerService == null) {
            return null;
        }
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(printerService);
            PageFormat format = job.defaultPage();
            Paper paper = format.getPaper();
            paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
            format.setPaper(paper);
            return format;
        } catch (PrinterException ex) {
            LOGGER.log(Level.WARNING, "No se pudo consultar el tamaño de papel de la impresora", ex);
            return null;
        }
    }

    /**
     * Returns the paper size the configured printer declares, in points.
     *
     * <p>Drivers without a usable declaration (or no printer at all) report the default
     * 80 mm roll, flagged with {@code declared == false}.
     *
     * @return the paper size the editor preview must use
     */
    public PaperInfo paperInfo() {
        PageFormat format = pageFormat();
        if (format == null) {
            return defaultPaperInfo();
        }
        Paper paper = format.getPaper();
        return new PaperInfo(paper.getWidth(), paper.getHeight(), true, getCurrentPrinterName());
    }

    private static PaperInfo defaultPaperInfo() {
        double width = DEFAULT_PAPER_WIDTH_MM * POINTS_PER_MM;
        double height = DEFAULT_PAPER_HEIGHT_MM * POINTS_PER_MM;
        return new PaperInfo(width, height, false, null);
    }

    /**
     * Paper size used to lay out a document.
     *
     * @param widthPoints  paper width in points (1/72 inch)
     * @param heightPoints paper height in points
     * @param declared     whether the printer declared the size; {@code false} means the
     *                     80 mm roll default was used
     * @param printerName  name of the printer that declared it, or {@code null}
     */
    public record PaperInfo(double widthPoints, double heightPoints, boolean declared, String printerName) {

        /** @return the width in millimetres, rounded */
        public int widthMm() {
            return (int) Math.round(widthPoints / POINTS_PER_MM);
        }

        /** @return the height in millimetres, rounded */
        public int heightMm() {
            return (int) Math.round(heightPoints / POINTS_PER_MM);
        }

        /** @return whether the declared width looks like a thermal roll rather than a sheet */
        public boolean looksLikeRoll() {
            return widthPoints <= MAX_ROLL_WIDTH_MM * POINTS_PER_MM;
        }
    }

    // ========== Test Printing ==========

    /**
     * Prints a test copy of the given template with sample data.
     *
     * <p>Shares the rendering and the print job with real receipts, so what comes out of the
     * printer is what the template will produce. Nothing else is touched: no PDF is written,
     * no transaction, counter or room is read or modified, and the sample values come from
     * {@link PrintFieldRegistry}.
     *
     * @param template the template to print, typically the unsaved copy being edited
     * @return {@code false} when there is no printer to print on or the layout fails
     */
    public boolean printTestTemplate(PrintTemplate template) {
        if (printerService == null || template == null) {
            return false;
        }
        try {
            resetDocument();
            TemplateRenderer.render(template, PrintFieldRegistry.sampleData(template.templateType()), document);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, "Fallo al preparar la impresión de prueba", ex);
            return false;
        }
        printWithService();
        return true;
    }
}
