package com.mycompany.proton.util;

import javafx.scene.control.TableView;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utilitário para exportação de TableViews para CSV e Excel.
 */
public class Exportador {

    /**
     * Exporta o conteúdo de uma TableView genérica para um arquivo CSV.
     */
    public static void exportarParaCSV(TableView<?> tabela, String caminho) throws IOException {
        try (FileWriter writer = new FileWriter(caminho)) {
            int totalColunas = tabela.getColumns().size();
            
            // 1. Gera o Cabeçalho
            for (int col = 0; col < totalColunas; col++) {
                writer.append(escaparCSV(tabela.getColumns().get(col).getText()));
                if (col < totalColunas - 1) {
                    writer.append(';');
                }
            }
            writer.append('\n');

            // 2. Gera as Linhas de Dados (usando rowIndex para evitar erro de Generics)
            int totalLinhas = tabela.getItems().size();
            for (int rowIndex = 0; rowIndex < totalLinhas; rowIndex++) {
                for (int colIndex = 0; colIndex < totalColunas; colIndex++) {
                    
                    // Extrai o dado da célula baseando-se no número da linha
                    Object value = tabela.getColumns().get(colIndex).getCellData(rowIndex);
                    String texto = (value != null) ? value.toString() : "";
                    
                    writer.append(escaparCSV(texto));
                    if (colIndex < totalColunas - 1) {
                        writer.append(';');
                    }
                }
                writer.append('\n');
            }
        }
    }

    /**
     * Escapa um valor para uso seguro em CSV (evita que textos com ";" quebrem o Excel).
     */
    private static String escaparCSV(String valor) {
        if (valor == null) return "";
        if (valor.contains(";") || valor.contains("\n") || valor.contains("\"")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    /**
     * Exporta o conteúdo de uma TableView genérica para um arquivo Excel (.xlsx).
     */
    public static void exportarParaExcel(TableView<?> tabela, String caminho) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Dados Exportados");
            int totalColunas = tabela.getColumns().size();
            int rowNum = 0;

            // 1. Gera a Linha de Cabeçalho
            Row headerRow = sheet.createRow(rowNum++);
            for (int col = 0; col < totalColunas; col++) {
                headerRow.createCell(col).setCellValue(tabela.getColumns().get(col).getText());
            }

            // 2. Gera as Linhas de Dados (usando rowIndex para evitar erro de Generics)
            int totalLinhas = tabela.getItems().size();
            for (int rowIndex = 0; rowIndex < totalLinhas; rowIndex++) {
                Row row = sheet.createRow(rowNum++);
                for (int colIndex = 0; colIndex < totalColunas; colIndex++) {
                    
                    // Extrai o dado da célula baseando-se no número da linha
                    Object value = tabela.getColumns().get(colIndex).getCellData(rowIndex);
                    row.createCell(colIndex).setCellValue(value != null ? value.toString() : "");
                }
            }

            // 3. Ajusta a largura de cada coluna ao conteúdo
            for (int col = 0; col < totalColunas; col++) {
                sheet.autoSizeColumn(col);
            }

            // 4. Salva o arquivo fisicamente
            try (FileOutputStream fileOut = new FileOutputStream(caminho)) {
                workbook.write(fileOut);
            }
        }
    }
}