package com.example.p6;

import java.util.Date;

public class PlannedActivityCode {

	String activityCodeDescription;
	String activityCodeTypeName;
	String activityCodeValue;
	String activityId;
	String activityName;
	String projectId;
	int activityCodeObjectId;
  Date modifiedDate;

  public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getActivityCodeDescription() {
		return activityCodeDescription;
	}

	public void setActivityCodeDescription(String activityCodeDescription) {
		this.activityCodeDescription = activityCodeDescription;
	}

  public String getActivityCodeTypeName() {
		return activityCodeTypeName;
	}

	public void setActivityCodeTypeName(String activityCodeTypeName) {
		this.activityCodeTypeName = activityCodeTypeName;
	}

  public String getActivityCodeValue() {
		return activityCodeValue;
	}

	public void setActivityCodeValue(String activityCodeValue) {
		this.activityCodeValue = activityCodeValue;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

  public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public int getActivityCodeObjectId() {
		return activityCodeObjectId;
	}

	public void setActivityCodeObjectId(int activityCodeObjectId) {
		this.activityCodeObjectId = activityCodeObjectId;
	}


}
