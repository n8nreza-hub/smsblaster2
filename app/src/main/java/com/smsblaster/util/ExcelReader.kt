package com.smsblaster.util

import android.content.Context
import android.net.Uri
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream

object ExcelReader {

    /**
     * خواندن شماره‌ها از ستون B (ستون دوم، ایندکس 1)
     * ردیف اول ممکن است هدر باشد
     */
    fun readPhoneNumbers(context: Context, uri: Uri): List<String> {
        val phoneNumbers = mutableListOf<String>()

        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return emptyList()

            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            val totalRows = sheet.lastRowNum

            for (rowIndex in 0..totalRows) {
                val row = sheet.getRow(rowIndex) ?: continue

                // ستون B = ایندکس 1
                val cell = row.getCell(1) ?: continue

                val phone = when (cell.cellType) {
                    org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                        cell.numericCellValue.toLong().toString()
                    }
                    org.apache.poi.ss.usermodel.CellType.STRING -> {
                        cell.stringCellValue.trim()
                    }
                    else -> continue
                }

                // بررسی اینکه هدر نباشد
                if (rowIndex == 0 && !isValidPhone(phone)) continue

                val cleaned = cleanPhone(phone)
                if (isValidPhone(cleaned)) {
                    phoneNumbers.add(cleaned)
                }
            }

            workbook.close()
            inputStream.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return phoneNumbers.distinct() // حذف تکراری‌ها
    }

    /**
     * پاکسازی شماره تلفن
     */
    private fun cleanPhone(phone: String): String {
        var cleaned = phone.trim()
            .replace(" ", "")
            .replace("-", "")
            .replace("+98", "0")

        // اگر با 98 شروع شود
        if (cleaned.startsWith("98") && cleaned.length == 12) {
            cleaned = "0" + cleaned.substring(2)
        }

        return cleaned
    }

    /**
     * اعتبارسنجی شماره ایرانی
     */
    fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.replace(" ", "").replace("-", "")
        return cleaned.matches(Regex("^09[0-9]{9}$")) ||
               cleaned.matches(Regex("^9[0-9]{9}$"))
    }
}
