package model.modelManagers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.RoomStatus;
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
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Generates Excel (XLSX) turn reports using Apache POI.
 *
 * <p>Each report contains three sheets:
 * <ol>
 *   <li><b>Resumen Turno</b> — detailed activity list with financial summary</li>
 *   <li><b>Detalle Completo</b> — activities grouped by type (rooms, sales, etc.)</li>
 *   <li><b>Detalle Turno</b> — printable format with activity listing and totals</li>
 * </ol>
 *
 * <p>Reports are saved to the {@code reports/} directory with filenames following
 * the pattern {@code Turno_<number>_<timestamp>.xlsx}.
 *
 * <p><b>Note:</b> Report generation is currently disabled (the body of
 * {@link #generateReport(TurnDetails)} is commented out).
 */
public class TurnReportGenerator {

    private static final String REPORT_PATH = FileManager.PATH + File.separator + "reports";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd - hh:mm a");
    private static final DateTimeFormatter DETAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    private static final String[] SHEET1_HEADERS = {
        "#", "Habitacion", "Tiempo", "Accion", "Valor", "Trans. Consecutiva", "Dev."
    };

    /**
     * Generates and saves an Excel report for the given turn.
     *
     * <p><b>Currently disabled</b> — the implementation body is commented out.
     *
     * @param turnDetails the turn data to include in the report
     */
    public static void generateReport(TurnDetails turnDetails) {
        /*new File(REPORT_PATH).mkdirs();

        String fileName = buildFileName(turnDetails);
        String filePath = REPORT_PATH + File.separator + fileName;

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);

            createSheet1(workbook, turnDetails, headerStyle, numberStyle, boldStyle);
            createSheet2(workbook, turnDetails, headerStyle, numberStyle, boldStyle);
            createSheet3(workbook, turnDetails, headerStyle, numberStyle, boldStyle);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            System.out.println("Turn report saved: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to generate turn report: " + e.getMessage());
        }
*/
    }

    /**
     * Builds the file name for the report.
     *
     * @param turnDetails the turn data (used for turn number and end time)
     * @return a file name of the form {@code Turno_<number>_<endTime>.xlsx}
     */
    private static String buildFileName(TurnDetails turnDetails) {
        long turnNumber = turnDetails.getTurnNumber();
        ZonedDateTime endTime = turnDetails.getTurnEnd() != null
                ? turnDetails.getTurnEnd()
                : ZonedDateTime.now();
        return "Turno_" + turnNumber + "_" + endTime.format(FILE_DATE_FORMATTER) + ".xlsx";
    }

    // ========== Sheet 1: Resumen Turno ==========

    /**
     * Creates the "Resumen Turno" sheet with a detailed chronological activity
     * listing and a financial summary panel.
     */
    private static void createSheet1(Workbook workbook, TurnDetails turnDetails,
                                      CellStyle headerStyle, CellStyle numberStyle, CellStyle boldStyle) {
        Sheet sheet = workbook.createSheet("Resumen Turno");

        int rowIdx = 0;

        rowIdx = writeTurnInfoHeader(sheet, rowIdx, turnDetails, boldStyle);
        rowIdx++;

        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < SHEET1_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(SHEET1_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        int dataStartRow = rowIdx;
        int rowNum = 1;
        for (TurnActivity activity : turnDetails.getActivities()) {
            int rowsAdded = addActivityToSheet1(sheet, rowIdx, activity, rowNum, numberStyle);
            rowIdx += rowsAdded;
            rowNum += rowsAdded;
        }

        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(1, 4500);
        sheet.setColumnWidth(2, 7500);
        sheet.setColumnWidth(3, 13000);
        sheet.setColumnWidth(4, 5000);
        sheet.setColumnWidth(5, 5000);
        sheet.setColumnWidth(6, 2000);

        int summaryRow = writeSummaryPanel(sheet, 4, 9, turnDetails, boldStyle, numberStyle);
        summaryRow = writeSummarizedItems(sheet, summaryRow + 1, 9, turnDetails, headerStyle, numberStyle, boldStyle);
        sheet.setColumnWidth(9, 8000);
        sheet.setColumnWidth(10, 5000);
        sheet.setColumnWidth(11, 5000);

        sheet.createFreezePane(0, dataStartRow);
    }

    /**
     * Writes the turn number, start time, and end time header rows.
     *
     * @return the next available row index
     */
    private static int writeTurnInfoHeader(Sheet sheet, int rowIdx, TurnDetails turnDetails, CellStyle boldStyle) {
        Row infoRow = sheet.createRow(rowIdx++);
        Cell infoCell = infoRow.createCell(0);
        infoCell.setCellValue("TURNO #" + turnDetails.getTurnNumber());
        infoCell.setCellStyle(boldStyle);

        Row startRow = sheet.createRow(rowIdx++);
        startRow.createCell(0).setCellValue("Inicio:");
        startRow.createCell(1).setCellValue(turnDetails.getTurnStart() != null
                ? turnDetails.getTurnStart().format(DETAIL_DATE_FORMATTER) : "N/A");

        Row endRow = sheet.createRow(rowIdx++);
        endRow.createCell(0).setCellValue("Fin:");
        endRow.createCell(1).setCellValue(turnDetails.getTurnEnd() != null
                ? turnDetails.getTurnEnd().format(DETAIL_DATE_FORMATTER) : "No finalizado");

        return rowIdx;
    }

    /**
     * Adds a single activity as one or more rows in Sheet 1.
     *
     * @return the number of rows added (multiple for multi-item sales)
     */
    private static int addActivityToSheet1(Sheet sheet, int rowIdx, TurnActivity activity, int rowNum,
                                            CellStyle numberStyle) {
        return switch (activity) {
            case SaleActivity s -> {
                int count = 0;
                for (SaleItem item : s.items()) {
                    Row row = sheet.createRow(rowIdx + count);
                    row.createCell(0).setCellValue(rowNum + count);
                    row.createCell(1).setCellValue(s.roomSoldTo());
                    row.createCell(2).setCellValue(s.changeDate().format(DATE_FORMATTER));
                    row.createCell(3).setCellValue(item.quantity() + " de " + item.itemName());
                    setNumberCell(row, 4, item.price(), numberStyle);
                    setNumberCell(row, 5, s.consecutiveTrans(), numberStyle);
                    row.createCell(6).setCellValue(item.refunded() ? "Si" : "");
                    count++;
                }
                yield count;
            }
            case RoomBookingActivity r -> {
                if (!r.isOccupied()) yield 0;
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(r.roomString());
                row.createCell(2).setCellValue(r.changeDate().format(DATE_FORMATTER));
                row.createCell(3).setCellValue("Alquiler " + r.getEffectiveService());
                setNumberCell(row, 4, r.price(), numberStyle);
                setNumberCell(row, 5, r.consecutiveTrans(), numberStyle);
                row.createCell(6).setCellValue(r.refunded() ? "Si" : "");
                yield 1;
            }
            case RoomSwapActivity s -> {
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(s.originalRoom());
                row.createCell(2).setCellValue(s.changeDate().format(DATE_FORMATTER));
                row.createCell(3).setCellValue("Cambio de habitacion a: " + s.swappedRoom());
                yield 1;
            }
            case RefundActivity r -> {
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(r.refundRoom());
                row.createCell(2).setCellValue(r.changeDate().format(DATE_FORMATTER));
                String action = r.refundType() == RefundType.SALE_REFUND
                        ? "Reembolso " + r.quantity() + " de " + r.itemName()
                        : "Reembolso habitacion " + r.refundRoom();
                row.createCell(3).setCellValue(action);
                setNumberCell(row, 4, r.price(), numberStyle);
                setNumberCell(row, 5, r.consecutiveTrans(), numberStyle);
                yield 1;
            }
            case SpendingActivity s -> {
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue("N/A");
                row.createCell(2).setCellValue(s.changeDate().format(DATE_FORMATTER));
                row.createCell(3).setCellValue("Gasto: " + s.description());
                setNumberCell(row, 4, s.value(), numberStyle);
                setNumberCell(row, 5, s.consecutiveTrans(), numberStyle);
                yield 1;
            }
            case ExtraChangeActivity e -> {
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue("N/A");
                row.createCell(2).setCellValue(e.changeDate().format(DATE_FORMATTER));
                String label = e.extraType() == ExtraChangeType.BANK_TRANSFER
                        ? "Transferencia: " : "Deposito: ";
                row.createCell(3).setCellValue(label + e.description());
                setNumberCell(row, 4, e.value(), numberStyle);
                setNumberCell(row, 5, e.consecutiveTrans(), numberStyle);
                yield 1;
            }
        };
    }

    /**
     * Writes the financial summary (rooms, products, sales, refunds, etc.) at
     * the given column position.
     *
     * @return the next available row index after the summary
     */
    private static int writeSummaryPanel(Sheet sheet, int startRow, int col, TurnDetails turnDetails,
                                           CellStyle boldStyle, CellStyle numberStyle) {
        Object[][] entries = {
            {"HABITACIONES:", turnDetails.getTotalRooms()},
            {"PRODUCTOS:", turnDetails.getTotalItems()},
            {"TOTAL VENTAS:", turnDetails.getTotalSales()},
            {"REEMBOLSO:", turnDetails.getTotalRefunds()},
            {"GASTOS:", turnDetails.getTotalSpending()},
            {"TOTAL TURNO:", turnDetails.getTotalTurn()},
            {"TOTAL TRANSFERENCIA:", turnDetails.getTotalBankTransfers()},
            {"TOTAL DEPOSITO:", turnDetails.getTotalDeposits()},
            {"TOTAL NETO:", turnDetails.getTotalNet()},
        };

        for (int i = 0; i < entries.length; i++) {
            Row r = sheet.getRow(startRow + i);
            if (r == null) r = sheet.createRow(startRow + i);
            Cell labelCell = r.createCell(col);
            labelCell.setCellValue((String) entries[i][0]);
            labelCell.setCellStyle(boldStyle);
            setNumberCell(r, col + 1, (Long) entries[i][1], numberStyle);
        }
        return startRow + entries.length;
    }

    /**
     * Writes a per-concept summary (rooms, items, refunds, extra changes) below
     * the financial summary.
     *
     * @return the next available row index
     */
    private static int writeSummarizedItems(Sheet sheet, int startRow, int col, TurnDetails turnDetails,
                                             CellStyle headerStyle, CellStyle numberStyle, CellStyle boldStyle) {
        List<TurnSummaryItemData> items = turnDetails.getSummaryItems();
        if (items.isEmpty()) return startRow;

        int rowIdx = startRow;
        Row sectionRow = sheet.getRow(rowIdx);
        if (sectionRow == null) sectionRow = sheet.createRow(rowIdx);
        Cell sectionCell = sectionRow.createCell(col);
        sectionCell.setCellValue("RESUMEN POR CONCEPTO");
        sectionCell.setCellStyle(boldStyle);
        rowIdx++;

        String[] summaryHeaders = {"Cant.", "Concepto", "Precio"};
        Row headerRow = sheet.getRow(rowIdx);
        if (headerRow == null) headerRow = sheet.createRow(rowIdx);
        for (int i = 0; i < summaryHeaders.length; i++) {
            Cell cell = headerRow.createCell(col + i);
            cell.setCellValue(summaryHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        rowIdx++;

        for (TurnSummaryItemData si : items) {
            Row r = sheet.getRow(rowIdx);
            if (r == null) r = sheet.createRow(rowIdx);
            setNumberCell(r, col, si.quantity(), numberStyle);
            r.createCell(col + 1).setCellValue(si.displayConcept());
            setNumberCell(r, col + 2, si.price(), numberStyle);
            rowIdx++;
        }
        return rowIdx;
    }

    // ========== Sheet 2: Detalle Completo ==========

    /**
     * Creates the "Detalle Completo" sheet with activities grouped by type
     * (rooms, sales, swaps, refunds, spending, extra changes).
     */
    private static void createSheet2(Workbook workbook, TurnDetails turnDetails,
                                      CellStyle headerStyle, CellStyle numberStyle, CellStyle boldStyle) {
        Sheet sheet = workbook.createSheet("Detalle Completo");

        int rowIdx = 0;
        Row infoRow = sheet.createRow(rowIdx++);
        Cell infoCell = infoRow.createCell(0);
        infoCell.setCellValue("DETALLE COMPLETO - TURNO #" + turnDetails.getTurnNumber());
        infoCell.setCellStyle(boldStyle);
        rowIdx++;

        List<TurnActivity> activities = turnDetails.getActivities();

        rowIdx = writeRoomDetailSection(sheet, rowIdx, activities, headerStyle, numberStyle, boldStyle);
        rowIdx += 2;
        rowIdx = writeSaleDetailSection(sheet, rowIdx, activities, headerStyle, numberStyle, boldStyle);
        rowIdx += 2;
        rowIdx = writeSwapDetailSection(sheet, rowIdx, activities, headerStyle, numberStyle, boldStyle);
        rowIdx += 2;
        rowIdx = writeRefundDetailSection(sheet, rowIdx, activities, headerStyle, numberStyle, boldStyle);
        rowIdx += 2;
        rowIdx = writeSpendingDetailSection(sheet, rowIdx, activities, headerStyle, numberStyle, boldStyle);
        rowIdx += 2;
        rowIdx = writeExtraChangeDetailSection(sheet, rowIdx, activities, headerStyle, numberStyle, boldStyle);

        sheet.setColumnWidth(0, 7500);
        sheet.setColumnWidth(1, 4500);
        sheet.setColumnWidth(2, 2500);
        sheet.setColumnWidth(3, 2500);
        sheet.setColumnWidth(4, 3500);
        sheet.setColumnWidth(5, 6500);
        sheet.setColumnWidth(6, 6500);
        sheet.setColumnWidth(7, 5000);
        sheet.setColumnWidth(8, 3500);
        sheet.setColumnWidth(9, 3500);
        sheet.setColumnWidth(10, 4000);
        sheet.setColumnWidth(11, 5000);
        sheet.setColumnWidth(12, 4000);
    }

    // ========== Sheet 3: Detalle Turno (detailed print format) ==========

    /**
     * Creates the "Detalle Turno" sheet — a printable listing of all sales
     * (rooms and items) with turn totals.
     */
    private static void createSheet3(Workbook workbook, TurnDetails turnDetails,
                                      CellStyle headerStyle, CellStyle numberStyle, CellStyle boldStyle) {
        Sheet sheet = workbook.createSheet("Detalle Turno");

        int rowIdx = 0;

        rowIdx = writeTurnInfoHeader(sheet, rowIdx, turnDetails, boldStyle);
        rowIdx++;

        Row subtitleRow = sheet.createRow(rowIdx++);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue("DETALLE VENTAS TURNO");
        subtitleCell.setCellStyle(boldStyle);
        rowIdx++;

        String[] headers = {"Fecha", "Habitacion", "Concepto", "Precio"};
        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int dataStartRow = rowIdx;

        for (TurnActivity activity : turnDetails.getActivities()) {
            switch (activity) {
                case SaleActivity s -> {
                    for (SaleItem item : s.items()) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(s.changeDate().format(DATE_FORMATTER));
                        row.createCell(1).setCellValue(s.roomSoldTo());
                        row.createCell(2).setCellValue(item.itemName());
                        setNumberCell(row, 3, item.price(), numberStyle);
                    }
                }
                case RoomBookingActivity r -> {
                    if (!r.isOccupied()) break;
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(r.changeDate().format(DATE_FORMATTER));
                    row.createCell(1).setCellValue(r.roomString());
                    row.createCell(2).setCellValue("Alquiler " + r.getEffectiveService());
                    setNumberCell(row, 3, r.price(), numberStyle);
                }
                case RoomSwapActivity s -> {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(s.changeDate().format(DATE_FORMATTER));
                    row.createCell(1).setCellValue(s.originalRoom());
                    row.createCell(2).setCellValue("Cambio a: " + s.swappedRoom());
                }
                case RefundActivity r -> {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(r.changeDate().format(DATE_FORMATTER));
                    row.createCell(1).setCellValue(r.refundRoom());
                    String concept;
                    if (r.refundType() == RefundType.SALE_REFUND) {
                        concept = "Reembolso " + r.quantity() + " de " + r.itemName();
                    } else {
                        concept = "Reembolso habitacion " + r.refundRoom();
                    }
                    row.createCell(2).setCellValue(concept);
                    setNumberCell(row, 3, r.price(), numberStyle);
                }
                case SpendingActivity s -> {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(s.changeDate().format(DATE_FORMATTER));
                    row.createCell(1).setCellValue("N/A");
                    row.createCell(2).setCellValue("Gasto: " + s.description());
                    setNumberCell(row, 3, s.value(), numberStyle);
                }
                case ExtraChangeActivity e -> {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(e.changeDate().format(DATE_FORMATTER));
                    row.createCell(1).setCellValue("N/A");
                    String label = e.extraType() == ExtraChangeType.BANK_TRANSFER
                            ? "Transferencia: " : "Deposito: ";
                    row.createCell(2).setCellValue(label + e.description());
                    setNumberCell(row, 3, e.value(), numberStyle);
                }
            }
        }

        rowIdx++;
        writeDetailedTurnTotals(sheet, rowIdx, turnDetails, boldStyle, numberStyle);

        sheet.setColumnWidth(0, 7500);
        sheet.setColumnWidth(1, 4500);
        sheet.setColumnWidth(2, 13000);
        sheet.setColumnWidth(3, 5000);
        sheet.createFreezePane(0, dataStartRow);
    }

    /**
     * Writes the detailed turn totals section (rooms, products, refunds, etc.)
     * on the "Detalle Turno" sheet.
     */
    private static void writeDetailedTurnTotals(Sheet sheet, int rowIdx,
                                                 TurnDetails turnDetails,
                                                 CellStyle boldStyle, CellStyle numberStyle) {
        Object[][] entries = {
            {"HABITACIONES:", turnDetails.getTotalRooms()},
            {"PRODUCTOS:", turnDetails.getTotalItems()},
            {"TOTAL VENTAS:", turnDetails.getTotalSales()},
            {"", null},
            {"REEMBOLSOS:", turnDetails.getTotalRefunds()},
            {"GASTOS:", turnDetails.getTotalSpending()},
            {"TOTAL TURNO:", turnDetails.getTotalTurn()},
            {"", null},
            {"TRANSFERENCIAS:", turnDetails.getTotalBankTransfers()},
            {"DEPOSITOS:", turnDetails.getTotalDeposits()},
            {"TOTAL NETO:", turnDetails.getTotalNet()},
        };

        for (int i = 0; i < entries.length; i++) {
            Row r = sheet.createRow(rowIdx + i);
            String label = (String) entries[i][0];
            Cell labelCell = r.createCell(2);
            labelCell.setCellValue(label);
            if (!label.isEmpty()) {
                labelCell.setCellStyle(boldStyle);
            }
            Long value = (Long) entries[i][1];
            if (value != null) {
                setNumberCell(r, 3, value, numberStyle);
            }
        }
    }

    /**
     * Writes the rooms detail section (all statuses) on the "Detalle Completo" sheet.
     *
     * @return the next available row index
     */
    private static int writeRoomDetailSection(Sheet sheet, int rowIdx, List<TurnActivity> activities,
                                               CellStyle headerStyle, CellStyle numberStyle, CellStyle boldStyle) {
        Row sectionRow = sheet.createRow(rowIdx++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("HABITACIONES (Todos los estados)");
        sectionCell.setCellStyle(boldStyle);
        rowIdx++;

        String[] headers = {"Fecha", "Habitacion", "Torre", "Piso", "Num Hab", "Estado",
                "Hora Inicio", "Hora Fin", "Precio", "Servicio", "Extension", "Serv. Efectivo",
                "Trans. Consecutiva", "Reembolsado"};
        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (TurnActivity a : activities) {
            if (!(a instanceof RoomBookingActivity r)) continue;
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.changeDate().format(DETAIL_DATE_FORMATTER));
            row.createCell(1).setCellValue(r.roomString());
            setNumberCell(row, 2, r.towerNumber() + 1, numberStyle);
            setNumberCell(row, 3, r.floorNumber() + 1, numberStyle);
            setNumberCell(row, 4, r.roomNumber() + 1, numberStyle);
            row.createCell(5).setCellValue(statusLabel(r.roomStatus()));
            row.createCell(6).setCellValue(r.startStatus() != null ? r.startStatus().format(DETAIL_DATE_FORMATTER) : "");
            row.createCell(7).setCellValue(r.endStatus() != null ? r.endStatus().format(DETAIL_DATE_FORMATTER) : "");
            setNumberCell(row, 8, r.price(), numberStyle);
            setNumberCell(row, 9, r.service(), numberStyle);
            setNumberCell(row, 10, r.extension(), numberStyle);
            setNumberCell(row, 11, r.getEffectiveService(), numberStyle);
            setNumberCell(row, 12, r.consecutiveTrans(), numberStyle);
            row.createCell(13).setCellValue(r.refunded() ? "Si" : "");
        }
        return rowIdx;
    }

    /**
     * Writes the product sales detail section on the "Detalle Completo" sheet.
     *
     * @return the next available row index
     */
    private static int writeSaleDetailSection(Sheet sheet, int rowIdx, List<TurnActivity> activities,
                                               CellStyle headerStyle, CellStyle numberStyle, CellStyle boldStyle) {
        Row sectionRow = sheet.createRow(rowIdx++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("VENTAS DE PRODUCTOS");
        sectionCell.setCellStyle(boldStyle);
        rowIdx++;

        String[] headers = {"Fecha", "Habitacion", "Item", "Item ID", "Cantidad", "Precio",
                "Reembolsado", "Trans. Consecutiva"};
        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (TurnActivity a : activities) {
            if (!(a instanceof SaleActivity s)) continue;
            for (SaleItem item : s.items()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.changeDate().format(DETAIL_DATE_FORMATTER));
                row.createCell(1).setCellValue(s.roomSoldTo());
                row.createCell(2).setCellValue(item.itemName());
                setNumberCell(row, 3, item.itemID(), numberStyle);
                setNumberCell(row, 4, item.quantity(), numberStyle);
                setNumberCell(row, 5, item.price(), numberStyle);
                row.createCell(6).setCellValue(item.refunded() ? "Si" : "");
                setNumberCell(row, 7, s.consecutiveTrans(), numberStyle);
            }
        }
        return rowIdx;
    }

    /**
     * Writes the room swap detail section on the "Detalle Completo" sheet.
     *
     * @return the next available row index
     */
    private static int writeSwapDetailSection(Sheet sheet, int rowIdx, List<TurnActivity> activities,
                                               CellStyle headerStyle, CellStyle numberStyle, CellStyle boldStyle) {
        Row sectionRow = sheet.createRow(rowIdx++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("CAMBIOS DE HABITACION");
        sectionCell.setCellStyle(boldStyle);
        rowIdx++;

        String[] headers = {"Fecha", "Hab Original", "Torre O", "Piso O", "Num O",
                "Hab Nueva", "Torre N", "Piso N", "Num N"};
        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (TurnActivity a : activities) {
            if (!(a instanceof RoomSwapActivity s)) continue;
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(s.changeDate().format(DETAIL_DATE_FORMATTER));
            row.createCell(1).setCellValue(s.originalRoom());
            setNumberCell(row, 2, s.originalTowerNumber() + 1, numberStyle);
            setNumberCell(row, 3, s.originalFloorNumber() + 1, numberStyle);
            setNumberCell(row, 4, s.originalRoomNumber() + 1, numberStyle);
            row.createCell(5).setCellValue(s.swappedRoom());
            setNumberCell(row, 6, s.swappedTowerNumber() + 1, numberStyle);
            setNumberCell(row, 7, s.swappedFloorNumber() + 1, numberStyle);
            setNumberCell(row, 8, s.swappedRoomNumber() + 1, numberStyle);
        }
        return rowIdx;
    }

    /**
     * Writes the refunds detail section on the "Detalle Completo" sheet.
     *
     * @return the next available row index
     */
    private static int writeRefundDetailSection(Sheet sheet, int rowIdx, List<TurnActivity> activities,
                                                 CellStyle headerStyle, CellStyle numberStyle, CellStyle boldStyle) {
        Row sectionRow = sheet.createRow(rowIdx++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("REEMBOLSOS");
        sectionCell.setCellStyle(boldStyle);
        rowIdx++;

        String[] headers = {"Fecha", "Tipo Reembolso", "Habitacion", "Precio", "Item ID",
                "Cantidad", "Item", "Servicio", "Trans. Consecutiva", "Trans. Original"};
        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (TurnActivity a : activities) {
            if (!(a instanceof RefundActivity r)) continue;
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.changeDate().format(DETAIL_DATE_FORMATTER));
            row.createCell(1).setCellValue(r.refundType() == RefundType.SALE_REFUND ? "Venta" : "Habitacion");
            row.createCell(2).setCellValue(r.refundRoom());
            setNumberCell(row, 3, r.price(), numberStyle);
            setNumberCell(row, 4, r.itemID(), numberStyle);
            setNumberCell(row, 5, r.quantity(), numberStyle);
            row.createCell(6).setCellValue(r.itemName() != null ? r.itemName() : "");
            setNumberCell(row, 7, r.refundService(), numberStyle);
            setNumberCell(row, 8, r.consecutiveTrans(), numberStyle);
            setNumberCell(row, 9, r.refundConsecutiveTrans(), numberStyle);
        }
        return rowIdx;
    }

    private static int writeSpendingDetailSection(Sheet sheet, int rowIdx, List<TurnActivity> activities,
                                                   CellStyle headerStyle, CellStyle numberStyle, CellStyle boldStyle) {
        Row sectionRow = sheet.createRow(rowIdx++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("GASTOS");
        sectionCell.setCellStyle(boldStyle);
        rowIdx++;

        String[] headers = {"Fecha", "Descripcion", "Valor", "Trans. Consecutiva"};
        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (TurnActivity a : activities) {
            if (!(a instanceof SpendingActivity s)) continue;
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(s.changeDate().format(DETAIL_DATE_FORMATTER));
            row.createCell(1).setCellValue(s.description());
            setNumberCell(row, 2, s.value(), numberStyle);
            setNumberCell(row, 3, s.consecutiveTrans(), numberStyle);
        }
        return rowIdx;
    }

    /**
     * Writes the transfers and deposits detail section on the "Detalle Completo" sheet.
     *
     * @return the next available row index
     */
    private static int writeExtraChangeDetailSection(Sheet sheet, int rowIdx, List<TurnActivity> activities,
                                                      CellStyle headerStyle, CellStyle numberStyle, CellStyle boldStyle) {
        Row sectionRow = sheet.createRow(rowIdx++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("TRANSFERENCIAS Y DEPOSITOS");
        sectionCell.setCellStyle(boldStyle);
        rowIdx++;

        String[] headers = {"Fecha", "Tipo", "Descripcion", "Valor", "Trans. Consecutiva"};
        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (TurnActivity a : activities) {
            if (!(a instanceof ExtraChangeActivity e)) continue;
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(e.changeDate().format(DETAIL_DATE_FORMATTER));
            row.createCell(1).setCellValue(e.extraType() == ExtraChangeType.BANK_TRANSFER
                    ? "Transferencia" : "Deposito");
            row.createCell(2).setCellValue(e.description());
            setNumberCell(row, 3, e.value(), numberStyle);
            setNumberCell(row, 4, e.consecutiveTrans(), numberStyle);
        }
        return rowIdx;
    }

    // ========== Helpers ==========

    /**
     * Sets a numeric cell with right-aligned number formatting.
     */
    private static void setNumberCell(Row row, int col, long value, CellStyle numberStyle) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(numberStyle);
    }

    /**
     * Formats a long value as a locale-formatted number string.
     *
     * @param value the raw numeric value
     * @return a formatted string (e.g. "1,234,567")
     */
    private static String formatCurrency(long value) {
        return String.format("%,d", value);
    }

    /**
     * Converts a {@link RoomStatus} to its Spanish display label.
     *
     * @param status the room status enum
     * @return {@code "Libre"}, {@code "Aseo"}, or {@code "Ocupado"}
     */
    private static String statusLabel(RoomStatus status) {
        return switch (status) {
            case FREE -> "Libre";
            case CLEANING -> "Aseo";
            case OCCUPIED -> "Ocupado";
        };
    }

    /**
     * Creates a bold, grey-background cell style for table headers.
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Creates a right-aligned, comma-formatted number cell style.
     */
    private static CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    /**
     * Creates a bold-font-only cell style for labels and section titles.
     */
    private static CellStyle createBoldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
