//Modify import problem past date
package MajorProject;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.io.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class TaskManager extends JFrame {
	private static final String DATA_FILE = "data.ser"; // Only for saving and loading a file whenever opening the program.
    private List<Task> tasks;
    private DefaultTableModel tableModel;
    private JComboBox<String> categoryFilter;

    public TaskManager() {
    	
        setTitle("Task Manager");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Loading back tasks that have been saved in the serialization file.
        tasks = loadTasks();

        // Table setup
        String[] columnNames = {"Category", "Task Description", "Due Date", "Priority","Completed"};
        tableModel = new DefaultTableModel(columnNames, 0) 
        {
        	public Class<?> getColumnClass(int columnIndex) 
        		{
            switch (columnIndex) //Specify data type for each column. 
            		{
                case 0: return String.class;  
                case 1: return String.class;  
                case 2: return Date.class;    
                case 3: return String.class;  
                case 4: return Boolean.class;
                default: return Object.class;
            		}
        		}
        };
        
        //Change the boolean variable "completed" in each task object to true or false according to the checkbox
        tableModel.addTableModelListener(new TableModelListener() {
           public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 4) {
                    int row = e.getFirstRow();
                    tasks.get(row).markAsComplete();
                   }
            }
        });
        
        //Custom the sorting method for the date column
    	TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
    	sorter.setComparator(2, (date1, date2) -> ((Date) date1).compareTo((Date) date2)); 
        JTable table = new JTable(tableModel) 
        {
            // Override isCellEditable to make all cells non-editable except for the "Completed" column
            public boolean isCellEditable(int row, int column) 
            {
                return column ==4;
            }
            
        };
        table.setRowSorter(sorter);
        table.setAutoCreateRowSorter(true);
        // Set custom renderer for the date column
        table.getColumnModel().getColumn(2).setCellRenderer(new DateRenderer());
        
     // Adding row to the table:
        for (Task task : tasks) {
        	 Object[] taskDetails = {task.getCat(), task.getDes(), task.getDate(), task.getPri(),task.isCompleted()};
        	 tableModel.addRow(taskDetails);
        }
        
         
        // Set up filtering button and action listener
        String[] categories = {"All", "School", "Home", "Social"};
        categoryFilter = new JComboBox<>(categories);
        categoryFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filterTasksByCategory((String) categoryFilter.getSelectedItem());
            }
        });

        // Button setup
        JButton addButton = new JButton("Add Task");
        JButton editButton = new JButton("Edit Task");
        JButton removeButton = new JButton("Remove Task");
        JButton viewCompletedButton = new JButton("View Completed Tasks");
        JButton exportButton = new JButton("Export Tasks");
        JButton importButton = new JButton("Import Tasks");
        
        //Adding action listeners to all buttons:
        
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });
        
        editButton.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    editTask(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(TaskManager.this, "Please select a task to edit.");
                }
            }
        });
        
        viewCompletedButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		filterTasksByCompletedness(tasks);
        	}
        });
        
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int confirm = JOptionPane.showConfirmDialog(
                            TaskManager.this,
                            "Are you sure you want to remove this task?",
                            "Confirmation",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        // Retrieve the selected task and store it into tobeRemoved
                        Task tobeRemoved = tasks.get(selectedRow);

                        // Remove the task from the ArrayList
                        tasks.remove(tobeRemoved);

                        // Update table with the new tasks list. 
                        updateTable();
                    }
                } else {
                    JOptionPane.showMessageDialog(TaskManager.this, "Please select a task to remove.");
                }
            }
        });
        
        exportButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		exportTasks();
        	}
        });
        
        importButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		
        		importTasks();
        	}
        });
        
        //Pop up a task details when double click on a row. 
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.rowAtPoint(evt.getPoint());
                    showTaskDetails(row,table);
                }
            }
        });
        
        // Add components to the GUI
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(viewCompletedButton);

        JPanel	northPanel = new JPanel();
        northPanel.add(new JLabel("Filter by Category:"));
        northPanel.add(categoryFilter);
        northPanel.add(exportButton);
        northPanel.add(importButton);

        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Save tasks when the application is closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveTasks();
            }
        });

        setVisible(true);
        checkNotifications(tasks);
       
    }
    
    //Supporting methods
    
    private void showTaskDetails(int rowIndex,JTable table) {
        if (rowIndex >= 0 && rowIndex < tableModel.getRowCount()) {
            Task selectedTask = tasks.get(rowIndex);

            JDialog dialog = new JDialog(this, "Task Details", true);
            dialog.setLayout(new BorderLayout());

            // Create a panel to display task details
            JPanel taskDetailsPanel = createTaskDetailsPanel(selectedTask);

            // Add panel to the dialog
            dialog.add(taskDetailsPanel, BorderLayout.CENTER);


            // Customize dialog
            dialog.setSize(450, 300);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            }}
    
    private JPanel createTaskDetailsPanel(Task task) {
        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Category:"));
        panel.add(new JLabel(task.getCat()));
        
        // Use a JTextArea for the description
        JTextArea description = new JTextArea(task.getDes());
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        //Using scroll pane to fit for long des
        JScrollPane descriptionPane = new JScrollPane(description);
        panel.add(new JLabel("Task Description:"));
        panel.add(descriptionPane);

        panel.add(new JLabel("Due Date:"));
        SimpleDateFormat fm = new SimpleDateFormat("EEE MMM dd yyyy");
        panel.add(new JLabel(fm.format(task.getDate())));
        panel.add(new JLabel("Priority:"));
        panel.add(new JLabel(task.getPri()));
        panel.add(new JLabel("Remind "+task.getReminder()+" days before the due date"));

        return panel;
    }
    
    //Saving and loading tasks each time user close and reopen the application
    private void saveTasks() {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            output.writeObject(tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private List<Task> loadTasks() {
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            return (List<Task>) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
    
    
    //Export/import tasks
    private void exportTasks() {
    	try {
    		String homePath =  System.getProperty("user.home");
    		Path filePath = Paths.get(homePath,"export_data.txt");
    		//Print the file path to console window
    		System.out.println(filePath);
    		
    		//Create a new file if there is no existing file at that location
    		if(!Files.exists(filePath)) {
    			Files.createFile(filePath);
    		}
    		BufferedWriter bufwri = new BufferedWriter(new FileWriter(filePath.toString()));
    		for(Task task: tasks) {
    			bufwri.write(task.toString(task.getReminder()));
    			bufwri.newLine();
    			}
    		bufwri.close();
    		
    	}catch (IOException e) {
    		e.printStackTrace();
    	}
    	catch(SecurityException e) {
    		e.printStackTrace();
    	}
    }
    
    
    private void importTasks() {
    	List<Task> importTasksList = new ArrayList<>();

        // Prompt the user for the file directory
        String path = JOptionPane.showInputDialog(this, "Enter the file path:");

        if (!path.isEmpty()) {
        	//Remove quotations characters at the beginning and the end of the file path.
        	path = path.trim().replaceAll("^\"|\"$", "");
            try {
                Path filePath = Paths.get(path);
                BufferedReader bufread = new BufferedReader(new FileReader(filePath.toString()));
                String line;
                
                while ((line = bufread.readLine()) != null) {
                    Task task = Task.fromString(line);
                    if (task != null) {
                    	importTasksList.add(task);
                    }
                }
                int count = 0;
                for(Task task:importTasksList) {
                	count++;
                	if(task.getDate().before(new Date())) {
                		JOptionPane.showMessageDialog(this, "Line " +count+" contains date in the past. Please modify the data before importing again.");
                		importTasksList.clear();
                		break;
                	}
                }
                bufread.close();

                tasks.addAll(importTasksList);
                updateTable();
                if(importTasksList.isEmpty()) {
                	JOptionPane.showMessageDialog(this, "Data has not been imported");
                }
                else {
                	JOptionPane.showMessageDialog(this, "Data imported.");}
                
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Invalid file path: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // User canceled or entered an empty string
            JOptionPane.showMessageDialog(this, "Invalid input", "Error", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    
    //Button listeners:
    private void addTask() {
        // Create a dropdown menu for task categories
        String[] catOptions = {"School", "Home", "Social"};
        JComboBox<String> categoryComboBox = new JComboBox<>(catOptions);

        // Create input fields for other task details
        JTextField descriptionfield = new JTextField();
        JTextField dueDatefield = new JTextField();
        JTextField remindDay = new JTextField();
        

        // Create a dropdown menu for priority levels
        String[] priorityOptions = {"High", "Medium", "Low"};
        JComboBox<String> priorityComboBox = new JComboBox<>(priorityOptions);

        //Panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryComboBox);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionfield);
        inputPanel.add(new JLabel("Due Date (yyyy-MM-dd):"));
        inputPanel.add(dueDatefield);
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(priorityComboBox);
        JPanel remindPanel = new JPanel(new GridLayout(0,3));
        remindPanel.add(new JLabel("Remind me:"));
        remindPanel.add(remindDay);
        remindPanel.add(new JLabel("days before the due date"));
        inputPanel.add(remindPanel);

        int choice = JOptionPane.showConfirmDialog(
                this, inputPanel, "Add Task", JOptionPane.OK_CANCEL_OPTION);

        // If the user choose OK, create the task:
        if (choice == JOptionPane.OK_OPTION) {
            String category = (String) categoryComboBox.getSelectedItem();     
            String description;
            if(descriptionfield.getText().isBlank()) {
            	JOptionPane.showMessageDialog(this, "Description cannot be blank");
            	addTask();
            	return;
            }
            else {
            	description = descriptionfield.getText();}
            String dueDate = dueDatefield.getText();
            String priority = (String) priorityComboBox.getSelectedItem();
            int remind;
            if(remindDay.getText().isEmpty()) {
            	 remind = 3;
            }
            else {
            	 remind = Integer.parseInt(remindDay.getText());
            }
            
            
            Date dD;
            try {
                dD = new SimpleDateFormat("yyyy-MM-dd").parse(dueDate);

                // Check if the selected date is in the future
                if (dD.before(new Date())) {
                    JOptionPane.showMessageDialog(this, "Please choose a future date for the task.");
                    addTask();
                    return;
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid date format.");
                addTask();
                return;
            }

            Task task = null;
            switch (category.toLowerCase()) {
                case "school":
                    task = new SchoolTask(description, new SimpleDateFormat("yyyy-MM-dd").format(dD), priority,remind);
                    break;
                case "home":
                    task = new HomeTask(description, new SimpleDateFormat("yyyy-MM-dd").format(dD), priority,remind);
                    break;
                case "social":
                    task = new SocialTask(description, new SimpleDateFormat("yyyy-MM-dd").format(dD), priority,remind);
                    break;
                    }
            
            tasks.add(task);
            updateTable();
        }
    }

    private void editTask(int rowIndex) {
        // Retrieve task details from the selected row
        String description = (String) tableModel.getValueAt(rowIndex, 1);
        Date dD = (Date) tableModel.getValueAt(rowIndex, 2);
        // Display current data for description and due date. 
        String[] categoryOptions = {"Home","School","Social"};
        JComboBox<String> categoryComboBox = new JComboBox<>(categoryOptions);
        JTextField descriptionField = new JTextField(description);
        JTextField dueDateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(dD));
        String[] priorityOptions = {"High", "Medium", "Low"};
        JComboBox<String> priorityComboBox = new JComboBox<>(priorityOptions);
        JTextField remindDayField = new JTextField();
        remindDayField.setSize(5,5);
        JPanel remindPanel = new JPanel(new GridLayout(0,3));
        remindPanel.add(new JLabel("Remind me: "));
        remindPanel.add(remindDayField);
        remindPanel.add(new JLabel("days before the due date"));

        //Create a panel and add components
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryComboBox);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionField);
        inputPanel.add(new JLabel("Due Date (yyyy-MM-dd):"));
        inputPanel.add(dueDateField);
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(priorityComboBox);
        inputPanel.add(remindPanel);
        
        int choice = JOptionPane.showConfirmDialog(
                this, inputPanel, "Edit Task", JOptionPane.OK_CANCEL_OPTION);

        // Retrieve the new variable and create a new/updated task
        if (choice == JOptionPane.OK_OPTION) {
        	String newCat = (String)categoryComboBox.getSelectedItem(); 
        	int newRemindDay;
        	if(remindDayField.getText().isEmpty()) {
        		newRemindDay = 3;
        	}
        	else {
        		newRemindDay = Integer.parseInt(remindDayField.getText());
        	}
        	
            String newDes;
            if(descriptionField.getText().isBlank()) {
            	JOptionPane.showMessageDialog(this, "Description cannot be blank");
            	editTask(rowIndex);
            	return;
            }
            else {
            	newDes = descriptionField.getText();}
            String newDueDate = dueDateField.getText();
            String newPri = (String)priorityComboBox.getSelectedItem();

            Date newdD;
            try {
                newdD = new SimpleDateFormat("yyyy-MM-dd").parse(newDueDate);

                // Check if the selected date is in the future
                if (newdD.before(new Date())) {
                    JOptionPane.showMessageDialog(this, "Please choose a future date for the task.");
                    editTask(rowIndex);
                    return;
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid date format.");
                editTask(rowIndex);
                return;
            }

            // Update the task
            Task updatedTask;
            switch (newCat.toLowerCase()) {
            case "school":
                updatedTask = new SchoolTask(newDes, new SimpleDateFormat("yyyy-MM-dd").format(newdD), newPri,newRemindDay);
                break;
            case "home":
                updatedTask = new HomeTask(newDes, new SimpleDateFormat("yyyy-MM-dd").format(newdD), newPri,newRemindDay);
                break;
            case "social":
                updatedTask = new SocialTask(newDes, new SimpleDateFormat("yyyy-MM-dd").format(newdD), newPri,newRemindDay);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Task was not edited.");
                return;
        }
            tasks.set(rowIndex, updatedTask);

            // Update the table
            updateTable();
        }
    }
    
    //Filtering tasks that have been marked as completed
    private void filterTasksByCompletedness(List<Task> tasks) {
    	ArrayList<Task> completedTasks = new ArrayList<>();
    	for(Task task:tasks) {
    		if(task.isCompleted()) {
    			completedTasks.add(task);
    		}
    	}
    	updateTable(completedTasks);
    		
    	
    }

    // Method to filter tasks by category and update the table
    private void filterTasksByCategory(String category) {
        if (category.equals("All")) {
            updateTable();
        } else {
            List<Task> filteredTasks = new ArrayList<>();
            for (Task task : tasks) {
                if (task.getCat().equalsIgnoreCase(category)) {
                    filteredTasks.add(task);
                }
            }
            updateTable(filteredTasks);
        }
    }
    
    
    //Show a popup notification whenever user run the application, showing if there are tasks that due within the next 3 days
    private void checkNotifications(List<Task> tasks) {
    	int count = 0;
    	JTextArea taskDesc = new JTextArea();
    	for(Task task:tasks) {
    		if(compareDate(task.getDate(),task.getReminder())) {
    			
    			taskDesc.append(task.toString()+"\n");
    			count++;
    		}
    	}
    	if(count==0) {
    		taskDesc.setText("No tasks within the next 3 days");
    	}
    	
    	JDialog upcomingNotifications = new JDialog(this,"Upcoming Tasks",true);
    	upcomingNotifications.setSize(900,200 );
    	JPanel panel = new JPanel(new BorderLayout());
    	taskDesc.setEditable(false);
    	panel.add(taskDesc,BorderLayout.CENTER);
    	upcomingNotifications.add(panel);
    	upcomingNotifications.setVisible(true);
    	
    	}
    //Supporting method for the above
    private boolean compareDate(Date a,int x) {
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DAY_OF_YEAR, x);  
    	Date xdayfromnow = cal.getTime();
    	if(xdayfromnow.compareTo(a)>0)
    		return true;
    	else
    		return false;
       	}

    // Update table with the current tasks list
    private void updateTable() {
        updateTable(tasks);
    }

    // Update table with specified parameters (filtered tasks) 
    private void updateTable(List<Task> tasks) {
        // Sort tasks by due date
        Collections.sort(tasks, Comparator.comparing(task -> task.getDate()));

        // Clear the rows
        tableModel.setRowCount(0);

        // Add tasks to the table based on the given tasks list in the parameter
        for (Task task : tasks) {
            Object[] taskDetails = {task.getCat(), task.getDes(), task.getDate(), task.getPri(),task.isCompleted()};
            tableModel.addRow(taskDetails);
        }
    }
    
    public static void main(String[] args) {
       new TaskManager();
    }
}