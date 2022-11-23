package gov.inl.de.evms;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.GregorianCalendar;

public class PlannedActivity {

	double actualDuration;
	double remainingDuration;
	double plannedDuration;
	double completedDuration;
	String name;
	String completionStatus;
	String activityId;
	String projectId;
	String wbsCode;
	String wbsName;
	String wbsPath;
	int wbsObjectId;
	Date actualStartDate;
	Date actualFinishDate;
	Date projectedStartDate;
	Date projectedFinishDate;
	Date modifiedDate;

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public double getActualDuration() {
		return actualDuration;
	}

	public void setActualDuration(double actualDuration) {
		this.actualDuration = actualDuration;
	}

	public double getRemainingDuration() {
		return remainingDuration;
	}

	public void setRemainingDuration(double remainingDuration) {
		this.remainingDuration = remainingDuration;
	}

	public double getPlannedDuration() {
		return plannedDuration;
	}

	public void setPlannedDuration(double plannedDuration) {
		this.plannedDuration = plannedDuration;
	}

	public double getCompletedDuration() {
		return completedDuration;
	}

	public void setCompletedDuration(double completedDuration) {
		this.completedDuration = completedDuration;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getWBSCode() {
		return wbsCode;
	}

	public void setWBSCode(String wbsCode) {
		this.wbsCode = wbsCode;
	}

	public String getWBSName() {
		return wbsName;
	}

	public void setWBSName(String wbsName) {
		this.wbsName = wbsName;
	}

	public String getWBSPath() {
		return wbsPath;
	}

	public void setWBSPath(String wbsPath) {
		this.wbsPath = wbsPath;
	}

	public String getCompletionStatus() {
		return completionStatus;
	}

	public void setCompletionStatus(String completionStatus) {
		this.completionStatus = completionStatus;
	}

	public Date getActualStartDate() {
		return actualStartDate;
	}

	public void setActualStartDate(Date actualStartDate) {
		this.actualStartDate = actualStartDate;
	}

	public Date getActualFinishDate() {
		return actualFinishDate;
	}

	public void setActualFinishDate(Date actualFinishDate) {
		this.actualFinishDate = actualFinishDate;
	}

	public Date getProjectedStartDate() {
		return projectedStartDate;
	}

	public void setProjectedStartDate(Date projectedStartDate) {
		this.projectedStartDate = projectedStartDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getProjectedFinishDate() {
		return projectedFinishDate;
	}

	public void setProjectedFinishDate(Date projectedFinishDate) {
		this.projectedFinishDate = projectedFinishDate;
	}

	public static  Date translateDate(javax.xml.datatype.XMLGregorianCalendar date){
	        return date == null ? null : date.toGregorianCalendar().getTime();
	}

	public static Date translateDate(String dateString){
        GregorianCalendar calendar = (GregorianCalendar)GregorianCalendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat();

        try {
            if (dateString != null){
                if (dateString.matches("^(0[1-9]|1[012])/(0[1-9]|[12][0-9]|3[01])/(19|20)\\d\\d$")){
                    sdf = new SimpleDateFormat("MM/dd/yyyy");
                }
                else if (dateString.matches("^(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])-(19|20)\\d\\d$")){
                    sdf = new SimpleDateFormat("MM-dd-yyyy");
                }
                else if (dateString.matches("^(19|20)\\d\\d(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$")){
                    sdf = new SimpleDateFormat("yyyyMMdd");
                }
                else if (dateString.matches("^(19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$")){
                    sdf = new SimpleDateFormat("yyyy-MM-dd");
                }

                calendar.setTime(sdf.parse(dateString));
            }
        } catch (ParseException e) {
             e.printStackTrace();
        }
        return calendar.getTime();
    }

	public String toString() {
		return "PlannedActivity [name=" + name + ", actualStartDate=" + actualStartDate + ", actualFinishDate="
				+ actualFinishDate + ", projectedStartDate=" + projectedStartDate + ", projectedFinishDate="
				+ projectedFinishDate + "]";
	}
}
