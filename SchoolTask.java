package MajorProject;
public class SchoolTask extends Task
{

	public SchoolTask(String des, String dD, String prio,int remindDay) {
		super("School", des, dD, prio,remindDay);
		// TODO Auto-generated constructor stub
	}
	public String getCat() {
		return "School";
	}


}
