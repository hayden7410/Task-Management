package MajorProject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;

abstract class Task implements Serializable //specifically for saving and loading tasks.
{
	private String category;
	private String description;
	private Date dueDate;
	private String priority;
	private boolean completed = false;
	private int remindDay;
	
	public Task(String cat, String des, String dD, String prio,int remindD) {
		category  = cat;
		description = des;
		try {
			this.dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(dD);
		} catch(ParseException e) {
			e.printStackTrace();
		}
		this.priority = prio;
		remindDay = remindD;
		
		
	}
	abstract String getCat(); 
	public String getDes() {
		return description;
	}
	public Date getDate() {
		return dueDate;
	}
	public String getPri() {
		return priority;
	}
	public void markAsComplete() {
		if(completed==false) {
			completed = true;
		}
		else {
			completed = false;
		}
	}
	public int getReminder() {
		return remindDay;
	}
	public boolean isCompleted() {
		return completed;
	}
	public String toString() {
	    SimpleDateFormat fmt = new SimpleDateFormat("EEE-yyyy-MM-dd");
	    return String.format("Category: %-15s Description: %-45s Due Date: %-15s Priority: %s%nAt most %d days until the due date",
	            getCat(), getDes(), fmt.format(getDate()), getPri(), getReminder());
	}
	public String toString(int x) {
		SimpleDateFormat fmt = new SimpleDateFormat("EEE-yyyy-MM-dd");
	    return String.format("Category: %-15s Description: %-45s Due Date: %-15s Priority: %s%nRemind me  %d days before the due date",
	            getCat(), getDes(), fmt.format(getDate()), getPri(), x);
	}
    public static Task fromString(String line) {
        String[] tokens = line.split("\t");
        String category = tokens[0].trim();
        String description = tokens[1].trim();
        Date dueDate = null;
        try {
            dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(tokens[2].trim());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        String dD = fmt.format(dueDate);
        String priority = tokens[3].trim();
        int remind = Integer.parseInt(tokens[4].trim());
        if ("Social".equalsIgnoreCase(category)) {
            return new SocialTask(description, dD, priority,remind);
        } else if ("Home".equalsIgnoreCase(category)) {
            return new HomeTask(description, dD, priority,remind);
        } else if ("School".equalsIgnoreCase(category)) {
            return new SchoolTask(description, dD, priority,remind);
        }
        return null;
    }

}
