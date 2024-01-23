package MajorProject;
public class HomeTask extends Task
{
	public HomeTask(String des, String dD, String priority,int remindDay) {
		super("Home",des,dD,priority,remindDay);
	}
	public String getCat() {
		return "Home";
	}

}
