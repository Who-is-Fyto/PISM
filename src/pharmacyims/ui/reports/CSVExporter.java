package pharmacyims.ui.reports;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

// Utility for exporting JTable datasets to CSV files.
public class CSVExporter {

    public static boolean exportTableToCSV(Component parent, JTable table, String defaultBaseName) {
        if (table == null || table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent, "The current report has no data records to export.", "Empty Report", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String suggestedName = defaultBaseName + "_" + timestamp + ".csv";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report to CSV");
        fileChooser.setSelectedFile(new File(suggestedName));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Spreadsheet (*.csv)", "csv"));

        int choice = fileChooser.showSaveDialog(parent);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File file = fileChooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }

        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    parent,
                    "File '" + file.getName() + "' already exists.\nDo you want to overwrite it?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            TableModel model = table.getModel();
            int colCount = table.getColumnCount();
            int rowCount = table.getRowCount();

            // Write CSV headers
            for (int col = 0; col < colCount; col++) {
                String header = table.getColumnName(col);
                writer.write(escapeCSV(header));
                if (col < colCount - 1) writer.write(",");
            }
            writer.newLine();

            // Write data rows
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < colCount; col++) {
                    Object val = table.getValueAt(row, col);
                    String text = val != null ? val.toString() : "";
                    writer.write(escapeCSV(text));
                    if (col < colCount - 1) writer.write(",");
                }
                writer.newLine();
            }

            writer.flush();
            JOptionPane.showMessageDialog(
                    parent,
                    "Successfully exported " + rowCount + " records to:\n" + file.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return true;

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Failed to export CSV: " + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "\"\"";
        boolean mustQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (mustQuote) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
