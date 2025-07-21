package com.example.demo.util;

import com.example.demo.model.CreditCard;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Utility class for reading credit card data from Excel files.
 */
public class ExcelReader {

    /**
     * Reads credit card data from an Excel file path.
     *
     * @param filePath Path to the Excel file
     * @return List of CreditCard objects parsed from the file
     */
    public static List<CreditCard> readCreditCardsFromExcel(String filePath) {
        try (FileInputStream file = new FileInputStream(filePath)) {
            return readCreditCardsFromExcel(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file from path: " + filePath, e);
        }
    }

    /**
     * Reads credit card data from an Excel InputStream.
     *
     * @param inputStream InputStream of the Excel file
     * @return List of CreditCard objects parsed from the stream
     */
    public static List<CreditCard> readCreditCardsFromExcel(InputStream inputStream) {
        List<CreditCard> creditCards = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                creditCards.add(createCreditCardFromRow(row));
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file from input stream", e);
        }

        return creditCards;
    }

    /**
     * Converts a single row into a CreditCard object.
     */
    private static CreditCard createCreditCardFromRow(Row row) {
        return new CreditCard(
                getCellValue(row.getCell(0)), // Card Title
                getCellValue(row.getCell(1)), // Card Images
                getCellValue(row.getCell(2)), // Annual Fees
                getCellValue(row.getCell(3)), // Purchase Interest Rate
                getCellValue(row.getCell(4)), // Cash Interest Rate
                getCellValue(row.getCell(5)), // Product Value Prop
                getCellValue(row.getCell(6)), // Product Benefits
                getCellValue(row.getCell(7)), // Bank Name
                getCellValue(row.getCell(8))  // Card Link
        );
    }

    /**
     * Extracts a string value from a cell.
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return getFormulaCellValue(cell);
            default:
                return "";
        }
    }

    /**
     * Handles formula cell values.
     */
    private static String getFormulaCellValue(Cell cell) {
        try {
            return String.valueOf(cell.getNumericCellValue());
        } catch (IllegalStateException e) {
            return cell.getStringCellValue().trim();
        }
    }
}
