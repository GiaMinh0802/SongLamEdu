package com.songlam.edu.service;

import com.songlam.edu.entity.Transaction;
import com.songlam.edu.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelReportService {

    private final TransactionRepository transactionRepository;

    public byte[] generateCashBookReport(String type, int year, Integer month, Integer quarter) throws IOException {
        LocalDate startDate;
        LocalDate endDate;
        String periodTitle;

        switch (type) {
            case "month":
                startDate = LocalDate.of(year, month, 1);
                endDate = startDate.plusMonths(1).minusDays(1);
                periodTitle = "Tháng " + month + " năm " + year;
                break;
            case "quarter":
                int startMonth = (quarter - 1) * 3 + 1;
                startDate = LocalDate.of(year, startMonth, 1);
                endDate = startDate.plusMonths(3).minusDays(1);
                periodTitle = "Quý " + quarter + " năm " + year;
                break;
            case "year":
            default:
                startDate = LocalDate.of(year, 1, 1);
                endDate = LocalDate.of(year, 12, 31);
                periodTitle = "Năm " + year;
                break;
        }

        List<Transaction> transactions = transactionRepository
                .findByDateOfRecordedBetweenOrderByDateOfRecordedAsc(startDate, endDate);

        return createExcelReport(transactions, periodTitle);
    }

    private byte[] createExcelReport(List<Transaction> transactions, String periodTitle) throws IOException {

        InputStream templateStream = new ClassPathResource("templates/sample/So_quy_tien_mat.xlsx").getInputStream();
        Workbook workbook = new XSSFWorkbook(templateStream);
        Sheet sheet = workbook.getSheetAt(0);

        Row row2 = sheet.getRow(1);
        if (row2 != null) {
            Cell periodCell = row2.getCell(0);
            if (periodCell != null) {
                periodCell.setCellValue(periodTitle);
            }
        }

        Row templateRow = sheet.getRow(6);
        CellStyle templateStyleH = null;
        Font originalFont = null;
        if (templateRow != null && templateRow.getCell(7) != null) {
            templateStyleH = templateRow.getCell(7).getCellStyle();
            if (templateStyleH.getFontIndex() >= 0) {
                originalFont = workbook.getFontAt(templateStyleH.getFontIndex());
            }
        }

        for (int i = 8; i >= 6; i--) {
            Row row = sheet.getRow(i);
            if (row != null) {
                sheet.removeRow(row);
            }
        }

        if (sheet.getLastRowNum() >= 9) {
            sheet.shiftRows(9, sheet.getLastRowNum(), -3);
        }

        DataFormat dataFormat = workbook.createDataFormat();

        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);

        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.cloneStyleFrom(borderStyle);
        numberStyle.setDataFormat(dataFormat.getFormat("#,##0"));

        Font blackFont = workbook.createFont();
        if (originalFont != null) {
            blackFont.setFontName(originalFont.getFontName());
            blackFont.setFontHeightInPoints(originalFont.getFontHeightInPoints());
            blackFont.setBold(false);
            blackFont.setItalic(originalFont.getItalic());
        }
        blackFont.setColor(IndexedColors.BLACK.getIndex());

        Font redFont = workbook.createFont();
        if (originalFont != null) {
            redFont.setFontName(originalFont.getFontName());
            redFont.setFontHeightInPoints(originalFont.getFontHeightInPoints());
            redFont.setBold(false);
            redFont.setItalic(originalFont.getItalic());
        }
        redFont.setColor(IndexedColors.RED.getIndex());

        Font blackBoldFont = workbook.createFont();
        if (originalFont != null) {
            blackBoldFont.setFontName(originalFont.getFontName());
            blackBoldFont.setFontHeightInPoints(originalFont.getFontHeightInPoints());
            blackBoldFont.setItalic(originalFont.getItalic());
        }
        blackBoldFont.setBold(true);
        blackBoldFont.setColor(IndexedColors.BLACK.getIndex());

        Font redBoldFont = workbook.createFont();
        if (originalFont != null) {
            redBoldFont.setFontName(originalFont.getFontName());
            redBoldFont.setFontHeightInPoints(originalFont.getFontHeightInPoints());
            redBoldFont.setItalic(originalFont.getItalic());
        }
        redBoldFont.setBold(true);
        redBoldFont.setColor(IndexedColors.RED.getIndex());

        String positiveFormat = "#,##0_)";
        String negativeFormat = "(#,##0)";

        CellStyle positiveBalanceStyle = workbook.createCellStyle();
        if (templateStyleH != null) {
            positiveBalanceStyle.cloneStyleFrom(templateStyleH);
        } else {
            positiveBalanceStyle.cloneStyleFrom(borderStyle);
        }
        positiveBalanceStyle.setDataFormat(dataFormat.getFormat(positiveFormat));
        positiveBalanceStyle.setFont(blackFont);

        CellStyle negativeBalanceStyle = workbook.createCellStyle();
        if (templateStyleH != null) {
            negativeBalanceStyle.cloneStyleFrom(templateStyleH);
        } else {
            negativeBalanceStyle.cloneStyleFrom(borderStyle);
        }
        negativeBalanceStyle.setDataFormat(dataFormat.getFormat(negativeFormat));
        negativeBalanceStyle.setFont(redFont);

        CellStyle positiveBoldBalanceStyle = workbook.createCellStyle();
        if (templateStyleH != null) {
            positiveBoldBalanceStyle.cloneStyleFrom(templateStyleH);
        } else {
            positiveBoldBalanceStyle.cloneStyleFrom(borderStyle);
        }
        positiveBoldBalanceStyle.setDataFormat(dataFormat.getFormat(positiveFormat));
        positiveBoldBalanceStyle.setFont(blackBoldFont);

        CellStyle negativeBoldBalanceStyle = workbook.createCellStyle();
        if (templateStyleH != null) {
            negativeBoldBalanceStyle.cloneStyleFrom(templateStyleH);
        } else {
            negativeBoldBalanceStyle.cloneStyleFrom(borderStyle);
        }
        negativeBoldBalanceStyle.setDataFormat(dataFormat.getFormat(negativeFormat));
        negativeBoldBalanceStyle.setFont(redBoldFont);

        BigDecimal runningBalance = BigDecimal.ZERO;
        BigDecimal totalThu = BigDecimal.ZERO;
        BigDecimal totalChi = BigDecimal.ZERO;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        int dataStartRow = 6;

        if (transactions.size() > 0 && sheet.getLastRowNum() >= dataStartRow) {
            sheet.shiftRows(dataStartRow, sheet.getLastRowNum(), transactions.size());
        }

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            int rowIndex = dataStartRow + i;
            Row row = sheet.createRow(rowIndex);

            String transactionNumber = t.getTransactionNumber();
            boolean isThu = t.getType().equals("PT");
            boolean isChi = t.getType().equals("PC");

            // Column A (0): dateOfRecorded
            Cell cellA = getOrCreateCell(row, 0);
            cellA.setCellValue(t.getDateOfRecorded().format(dateFormatter));
            cellA.setCellStyle(borderStyle);

            // Column B (1): dateOfDocument
            Cell cellB = getOrCreateCell(row, 1);
            cellB.setCellValue(t.getDateOfDocument().format(dateFormatter));
            cellB.setCellStyle(borderStyle);

            // Column C (2): transactionNumber
            Cell cellC = getOrCreateCell(row, 2);
            if (isThu) {
                cellC.setCellValue(transactionNumber);
            }
            cellC.setCellStyle(borderStyle);

            // Column D (3): transactionNumber
            Cell cellD = getOrCreateCell(row, 3);
            if (isChi) {
                cellD.setCellValue(transactionNumber);
            }
            cellD.setCellStyle(borderStyle);

            // Column E (4): reason
            Cell cellE = getOrCreateCell(row, 4);
            cellE.setCellValue(t.getReason());
            cellE.setCellStyle(borderStyle);

            // Column F (5): amount
            Cell cellF = getOrCreateCell(row, 5);
            if (isThu && t.getAmount() != null) {
                cellF.setCellValue(t.getAmount().doubleValue());
                totalThu = totalThu.add(t.getAmount());
                runningBalance = runningBalance.add(t.getAmount());
            }
            cellF.setCellStyle(numberStyle);

            // Column G (6): amount
            Cell cellG = getOrCreateCell(row, 6);
            if (isChi && t.getAmount() != null) {
                cellG.setCellValue(t.getAmount().doubleValue());
                totalChi = totalChi.add(t.getAmount());
                runningBalance = runningBalance.subtract(t.getAmount());
            }
            cellG.setCellStyle(numberStyle);

            // Column H (7):
            Cell cellH = getOrCreateCell(row, 7);
            if (runningBalance.compareTo(BigDecimal.ZERO) >= 0) {
                cellH.setCellValue(runningBalance.doubleValue());
                cellH.setCellStyle(positiveBalanceStyle);
            } else {
                cellH.setCellValue(runningBalance.abs().doubleValue());
                cellH.setCellStyle(negativeBalanceStyle);
            }

            // Column I (8): note
            Cell cellI = getOrCreateCell(row, 8);
            if (t.getNote() != null) {
                cellI.setCellValue(t.getNote());
            }
            cellI.setCellStyle(borderStyle);

            // Column J (9):
            Cell cellJ = getOrCreateCell(row, 9);
            if (isThu) {
                if (t.getStudent() != null && t.getStudent().getPerson() != null) {
                    cellJ.setCellValue(t.getStudent().getPerson().getFullName());
                }
            }
            if (isChi) {
                if (t.getReceiverName() != null) {
                    cellJ.setCellValue(t.getReceiverName());
                }
            }
            cellJ.setCellStyle(borderStyle);

            // Column K (10):
            Cell cellK = getOrCreateCell(row, 10);
            if (isThu) {
                cellK.setCellValue(t.getBranch().getName());
            }
            cellK.setCellStyle(borderStyle);
        }

        int totalRowIndex = dataStartRow + transactions.size();
        Row rowTotal = sheet.getRow(totalRowIndex);
        if (rowTotal != null) {
            Cell cellF = getOrCreateCell(rowTotal, 5);
            cellF.setCellValue(totalThu.doubleValue());

            Cell cellG = getOrCreateCell(rowTotal, 6);
            cellG.setCellValue(totalChi.doubleValue());

            Cell cellH = getOrCreateCell(rowTotal, 7);
            if (runningBalance.compareTo(BigDecimal.ZERO) >= 0) {
                cellH.setCellValue(runningBalance.doubleValue());
                cellH.setCellStyle(positiveBoldBalanceStyle);
            } else {
                cellH.setCellValue(runningBalance.abs().doubleValue());
                cellH.setCellStyle(negativeBoldBalanceStyle);
            }
        }

        int summaryRowIndex = dataStartRow + transactions.size() + 1;
        Row rowSummary = sheet.getRow(summaryRowIndex);
        if (rowSummary != null) {
            Cell cellF = getOrCreateCell(rowSummary, 5);
            cellF.setCellValue(totalThu.doubleValue());

            Cell cellG = getOrCreateCell(rowSummary, 6);
            cellG.setCellValue(totalChi.doubleValue());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        templateStream.close();

        return outputStream.toByteArray();
    }

    private Cell getOrCreateCell(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        return cell;
    }
}