package de.kmj.robots.controlApp;

import de.kmj.robots.messaging.StatusMessage;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * GUI component for displaying the StatusMessages received from the connected
 * RobotEngine.
 * 
 * @see de.kmj.robots.messaging.StatusMessage
 * @author Kathrin Janowski
 */
public class StatusDisplayPanel extends JPanel implements ActionListener{
    
    private final JTable mStatusTable;
    private final DefaultTableModel mStatusModel;
    private final GregorianCalendar mCalendar;
    private final JButton mClearButton;
    
    public StatusDisplayPanel()
    {
        super(new BorderLayout());
        
        setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED),
                                   "status messages"));
        
        String[] columnNames = new String[]{
            "time", "task ID", "status", "details"
        };
        mStatusModel = new DefaultTableModel(columnNames, 0);
        mStatusTable = new JTable(mStatusModel);
        
        JScrollPane scrollPane = new JScrollPane(mStatusTable);
        add(scrollPane, BorderLayout.CENTER);
        
        mClearButton = new JButton("clear status messages");
        mClearButton.setActionCommand("clear");
        mClearButton.addActionListener(this);
        add(mClearButton, BorderLayout.SOUTH);
        
        mCalendar = new GregorianCalendar(TimeZone.getTimeZone("CET"));
    }
    
    public void showStatus(StatusMessage status)
    {
        Date now = new Date();
        mCalendar.setTime(now);
        
        //get the timestamp
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND);
        int millis = mCalendar.get(Calendar.MILLISECOND);

        String timestamp = String.format("%02d:%02d:%02d.%03d",
                hour, minute, second, millis);

        String detailText = "";
        
        TreeMap<String, String> details = status.getStatusDetails();
        if(!details.isEmpty())
        {
            StringBuilder detailBuilder = new StringBuilder();
            int count = details.size();
            
            for(Entry<String, String> detail: details.entrySet())
            {
                detailBuilder.append(detail.getKey());
                detailBuilder.append("=\"");
                detailBuilder.append(detail.getValue());
                detailBuilder.append("\"");
                
                if(count>1) detailBuilder.append("\n");
                count--;
            }
            detailText = detailBuilder.toString();
        }
        
        
        mStatusModel.addRow(new String[]{
            timestamp, status.getTaskID(), status.getStatus(), detailText
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("clear"))
        {
            while(mStatusModel.getRowCount()>0)
                mStatusModel.removeRow(0);
        }
    }
}
