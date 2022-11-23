package com.example.evms;

import java.util.Date;

public class PlannedRelationship {

	String predecessorActivityId;
	String predecessorProjectId;
  String successorActivityId;
	String successorProjectId;
  Date modifiedDate;

  public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getPredecessorActivityId() {
		return predecessorActivityId;
	}

	public void setPredecessorActivityId(String predecessorActivityId) {
		this.predecessorActivityId = predecessorActivityId;
	}

  public String getPredecessorProjectId() {
		return predecessorProjectId;
	}

	public void setPredecessorProjectId(String predecessorProjectId) {
		this.predecessorProjectId = predecessorProjectId;
	}

  public String getSuccessorActivityId() {
		return successorActivityId;
	}

	public void setSuccessorActivityId(String successorActivityId) {
		this.successorActivityId = successorActivityId;
	}

  public String getSuccessorProjectId() {
		return successorProjectId;
	}

	public void setSuccessorProjectId(String successorProjectId) {
		this.successorProjectId = successorProjectId;
	}


}
