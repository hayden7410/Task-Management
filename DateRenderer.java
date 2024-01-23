package MajorProject;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
class DateRenderer extends DefaultTableCellRenderer {
        private SimpleDateFormat sdf;

        public DateRenderer() {
            super();
            sdf = new SimpleDateFormat("EEE MMM dd yyyy");
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Date) //Of Type Date
            {
                value = sdf.format((Date) value);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

