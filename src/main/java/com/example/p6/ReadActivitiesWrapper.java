package com.example.p6;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import com.primavera.ws.p6.activity.ActivityFieldType;
import com.primavera.ws.p6.relationship.RelationshipFieldType;
import com.primavera.ws.p6.activitycode.ActivityCodeFieldType;
import com.primavera.ws.p6.activitycodeassignment.ActivityCodeAssignment;
import com.primavera.ws.p6.activitycodeassignment.ActivityCodeAssignmentFieldType;
import com.primavera.ws.p6.authentication.AuthenticationService;
import com.primavera.ws.p6.authentication.AuthenticationServicePortType;
import com.primavera.ws.p6.authentication.IntegrationFault;

public class ReadActivitiesWrapper extends ActivitiesWrapper {

	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	Date lastCheckDate;
	Date relsLastCheckDate;
	Date codesLastCheckDate;
	DeepLynxService dlService;
	P6ServiceSession session;
	private final String fileName = "import.json";
	private final String relsFileName = "import_rels.json";
	private final String codesAssignmentsFileName = "import_code_assignments.json";
	int projectObjectId;

	public Date getLastCheckDate() {
		return lastCheckDate;
	}

	public void setLastCheckDate(Date lastCheckDate) {
		this.lastCheckDate = lastCheckDate;
	}

	public Date getRelsLastCheckDate() {
		return relsLastCheckDate;
	}

	public void setRelsLastCheckDate(Date relsLastCheckDate) {
		this.relsLastCheckDate = relsLastCheckDate;
	}

	public Date getCodesLastCheckDate() {
		return codesLastCheckDate;
	}

	public void setCodesLastCheckDate(Date codesLastCheckDate) {
		this.codesLastCheckDate = codesLastCheckDate;
	}

	public ReadActivitiesWrapper() {
		this.lastCheckDate = new Date(0);
		this.relsLastCheckDate = new Date(0);
		this.codesLastCheckDate = new Date(0);
	}

	public P6ServiceResponse mapActivities(Environment env, int databaseInstance) {
		dlService = new DeepLynxService(env);
		P6ServiceResponse response = new P6ServiceResponse();

		dlService.authenticate();

		boolean containerExists = dlService.checkContainer();
		if (!containerExists) {
			response.setStatus("FAILURE");
			response.setMsg("FAILURE. Container does not exist!");
			return response;
		}

		Date dataSourceDate = dlService.checkDataSource();
		if (dataSourceDate == null) {
			response.setStatus("FAILURE");
			response.setMsg("FAILURE. Data source not found or could not be created!");
			return response;
		}
		this.setLastCheckDate(dataSourceDate);

		// todo: may need to do something to close this session
		session = new P6ServiceSession(env.getUserName(), env.getPassword(), databaseInstance, env.getP6URL());
		List<ActivityFieldType> fields = new ArrayList<ActivityFieldType>();

		// Must specify which fields you desire to retrieve
		fields.add(ActivityFieldType.NAME);
		fields.add(ActivityFieldType.START_DATE);
		fields.add(ActivityFieldType.OBJECT_ID);
		fields.add(ActivityFieldType.PLANNED_START_DATE);
		fields.add(ActivityFieldType.ACTUAL_START_DATE);
		fields.add(ActivityFieldType.PLANNED_FINISH_DATE);
		fields.add(ActivityFieldType.ACTUAL_FINISH_DATE);
		fields.add(ActivityFieldType.FINISH_DATE);
		fields.add(ActivityFieldType.STATUS);
		fields.add(ActivityFieldType.ID);
		fields.add(ActivityFieldType.PROJECT_ID);
		fields.add(ActivityFieldType.PROJECT_OBJECT_ID);
		fields.add(ActivityFieldType.WBS_CODE);
		fields.add(ActivityFieldType.WBS_NAME);
		fields.add(ActivityFieldType.WBS_PATH);
		fields.add(ActivityFieldType.ACTUAL_DURATION);
		fields.add(ActivityFieldType.REMAINING_DURATION);
		fields.add(ActivityFieldType.PLANNED_DURATION);
		fields.add(ActivityFieldType.AT_COMPLETION_DURATION);
		fields.add(ActivityFieldType.LAST_UPDATE_DATE);
		fields.add(ActivityFieldType.LAST_UPDATE_USER);
		fields.add(ActivityFieldType.CREATE_DATE);
		fields.add(ActivityFieldType.CREATE_USER);
		fields.add(ActivityFieldType.DATA_DATE);

		try {
			String p6url = env.getP6URL() + "AuthenticationService?wsdl";

			AuthenticationService service = new AuthenticationService(new URL(p6url));
			AuthenticationServicePortType servicePort = service.getAuthenticationServiceSOAP12PortHttp();
			session.setUserNameToken((BindingProvider) servicePort);
			servicePort.login(env.getUserName(), env.getPassword(), databaseInstance);
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException. StackTrace: ");
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		} catch (IntegrationFault e) {
			System.out.println("IntegrationFault. StackTrace: ");
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}

		JSONArray activityList = new JSONArray();

		for (com.primavera.ws.p6.activity.Activity act : getActivities(session, env.getProjectID(), fields)) {
			PlannedActivity myActivity = new PlannedActivity();
			myActivity.setActualStartDate(PlannedActivity.translateDate(act.getActualStartDate().getValue()));
			myActivity.setActualFinishDate(PlannedActivity.translateDate(act.getActualFinishDate().getValue()));
			myActivity.setProjectedFinishDate(PlannedActivity.translateDate(act.getFinishDate()));
			myActivity.setProjectedStartDate(PlannedActivity.translateDate(act.getStartDate()));
			myActivity.setName(act.getName());
			myActivity.setCompletionStatus(act.getStatus());
			myActivity.setActivityId(act.getId());
			myActivity.setProjectId(act.getProjectId());
			myActivity.setWBSCode(act.getWBSCode());
			myActivity.setWBSName(act.getWBSName());
			myActivity.setWBSPath(act.getWBSPath());
			myActivity.setActualDuration(act.getActualDuration().getValue());
			myActivity.setCompletedDuration(act.getAtCompletionDuration());
			myActivity.setPlannedDuration(act.getPlannedDuration());
			myActivity.setRemainingDuration(act.getRemainingDuration().getValue());
			myActivity.setModifiedDate(PlannedActivity.translateDate(act.getLastUpdateDate().getValue()));

			// need the projectObjectId to filter with in ActivitiesWrapper.getRelationships
			projectObjectId = act.getProjectObjectId();

			// Check if modified date is greater than last check date and if so send to Deep
			// Lynx
			if (myActivity.getModifiedDate().compareTo(this.getLastCheckDate()) > 0) {
				JSONObject activity = new JSONObject();
				activity.put("Id", myActivity.getActivityId());
				activity.put("ProjectId", myActivity.getProjectId());
				activity.put("WBSCode", myActivity.getWBSCode());
				activity.put("WBSName", myActivity.getWBSName());
				activity.put("WBSPath", myActivity.getWBSPath());
				activity.put("Name", myActivity.getName());
				activity.put("CompletionStatus", myActivity.getCompletionStatus());
				activity.put("ProjectedStartDate", myActivity.getProjectedStartDate());
				activity.put("ProjectedFinishDate", myActivity.getProjectedFinishDate());
				activity.put("ActualStartDate", myActivity.getActualStartDate());
				activity.put("ActualFinishDate", myActivity.getActualFinishDate());
				activity.put("PlannedDuration", myActivity.getPlannedDuration());
				activity.put("ActualDuration", myActivity.getActualDuration());
				activity.put("RemainingDuration", myActivity.getRemainingDuration());
				activity.put("CompletedDuration", myActivity.getCompletedDuration());
				// activity.put("LastCheckDate", this.getLastCheckDate()); // not P6 data
				activity.put("LastUpdateDate", myActivity.getModifiedDate());
				activityList.put(activity);
			}
		}

		// Update lastCheckDate
		Date lastCheckDate = new Date();
		this.setLastCheckDate(lastCheckDate);

		if (activityList.length() > 0) {
			this.writeJSONFile(activityList, fileName);
			File importFile = new File(fileName);
			dlService.createManualImport(importFile);
		}

		// Check for errors and create response.
		boolean failure = false, warning = false;
		StringBuffer msg = new StringBuffer("");

		for (P6ServiceMessage message : errors) {
			if (message.getType() == P6ServiceMessage.MessageType.APPLICATION) {
				failure = true;
			} else {
				warning = true;
			}

			msg.append(message.getType().toString() + " Error: <br/>");
			msg.append(message.getMessage() + "<br/><br/>");
		}

		if (failure) {
			response.setStatus("FAILURE");
		} else if (warning) {
			response.setStatus("WARNING");
		} else {
			response.setStatus("SUCCESS");
		}

		response.setMsg(msg.toString());

		return response;
	}

	// todo: mapRelationships must be called after mapActivities so that it can use
	// the same DL service and P6 session; not ideal but better than keeping them
	// totally independent and repeating services
	public P6ServiceResponse mapRelationships() {
		P6ServiceResponse response = new P6ServiceResponse();

		List<RelationshipFieldType> fields = new ArrayList<RelationshipFieldType>();

		// Must specify which fields you desire to retrieve
		fields.add(RelationshipFieldType.PREDECESSOR_ACTIVITY_ID);
		fields.add(RelationshipFieldType.PREDECESSOR_PROJECT_ID);
		fields.add(RelationshipFieldType.SUCCESSOR_ACTIVITY_ID);
		fields.add(RelationshipFieldType.SUCCESSOR_PROJECT_ID);
		fields.add(RelationshipFieldType.LAST_UPDATE_DATE);

		JSONArray relationshipList = new JSONArray();

		for (com.primavera.ws.p6.relationship.Relationship rel : getRelationships(session, projectObjectId, fields)) {
			PlannedRelationship myRelationship = new PlannedRelationship();
			myRelationship.setPredecessorActivityId(rel.getPredecessorActivityId());
			myRelationship.setPredecessorProjectId(rel.getPredecessorProjectId());
			myRelationship.setSuccessorActivityId(rel.getSuccessorActivityId());
			myRelationship.setSuccessorProjectId(rel.getSuccessorProjectId());
			myRelationship.setModifiedDate(PlannedActivity.translateDate(rel.getLastUpdateDate().getValue()));

			// Check if modified date is greater than last check date and if so send to Deep
			// Lynx
			if (myRelationship.getModifiedDate().compareTo(this.getRelsLastCheckDate()) > 0) {
				JSONObject relationship = new JSONObject();
				relationship.put("PredecessorActivityId", myRelationship.getPredecessorActivityId());
				relationship.put("PredecessorProjectId", myRelationship.getPredecessorProjectId());
				relationship.put("SuccessorActivityId", myRelationship.getSuccessorActivityId());
				relationship.put("SuccessorProjectId", myRelationship.getSuccessorProjectId());
				relationship.put("LastUpdateDate", myRelationship.getModifiedDate());
				relationshipList.put(relationship);
			}
		}

		// Update relsLastCheckDate
		Date lastCheckDate = new Date();
		this.setRelsLastCheckDate(lastCheckDate);

		if (relationshipList.length() > 0) {
			this.writeJSONFile(relationshipList, relsFileName);
			File importFile = new File(relsFileName);
			dlService.createManualImport(importFile);
		}

		// Check for errors and create response.
		boolean failure = false, warning = false;
		StringBuffer msg = new StringBuffer("");

		for (P6ServiceMessage message : errors) {
			if (message.getType() == P6ServiceMessage.MessageType.APPLICATION) {
				failure = true;
			} else {
				warning = true;
			}

			msg.append(message.getType().toString() + " Error: <br/>");
			msg.append(message.getMessage() + "<br/><br/>");
		}

		if (failure) {
			response.setStatus("FAILURE");
		} else if (warning) {
			response.setStatus("WARNING");
		} else {
			response.setStatus("SUCCESS");
		}

		response.setMsg(msg.toString());

		return response;
	}

	public P6ServiceResponse mapActivityCodeAssignments(Environment env) {
		P6ServiceResponse response = new P6ServiceResponse();

		List<ActivityCodeAssignmentFieldType> fields = new ArrayList<ActivityCodeAssignmentFieldType>();
		fields.add(ActivityCodeAssignmentFieldType.ACTIVITY_CODE_DESCRIPTION);
		fields.add(ActivityCodeAssignmentFieldType.ACTIVITY_CODE_TYPE_NAME);
		fields.add(ActivityCodeAssignmentFieldType.ACTIVITY_CODE_VALUE);
		fields.add(ActivityCodeAssignmentFieldType.ACTIVITY_ID);
		fields.add(ActivityCodeAssignmentFieldType.ACTIVITY_NAME);
		fields.add(ActivityCodeAssignmentFieldType.PROJECT_ID);
		fields.add(ActivityCodeAssignmentFieldType.ACTIVITY_CODE_OBJECT_ID);
		fields.add(ActivityCodeAssignmentFieldType.LAST_UPDATE_DATE);

		JSONArray activityCodeAssignmentList = new JSONArray();

		for (ActivityCodeAssignment code : getActivityCodeAssignments(session, env.getProjectID(), fields)) {
			PlannedActivityCode myActivityCode = new PlannedActivityCode();
			myActivityCode.setActivityCodeDescription(code.getActivityCodeDescription());
			myActivityCode.setActivityCodeTypeName(code.getActivityCodeTypeName());
			myActivityCode.setActivityCodeValue(code.getActivityCodeValue());
			myActivityCode.setActivityId(code.getActivityId());
			myActivityCode.setActivityName(code.getActivityName());
			myActivityCode.setProjectId(code.getProjectId());
			myActivityCode.setActivityCodeObjectId(code.getActivityCodeObjectId());
			myActivityCode.setModifiedDate(PlannedActivity.translateDate(code.getLastUpdateDate().getValue()));

			if (myActivityCode.getModifiedDate().compareTo(this.getCodesLastCheckDate()) > 0) {
				JSONObject activityCodeAssignment = new JSONObject();
				activityCodeAssignment.put("ActivityCodeDescription", code.getActivityCodeDescription());
				activityCodeAssignment.put("ActivityCodeTypeName", code.getActivityCodeTypeName());
				activityCodeAssignment.put("ActivityCodeValue", code.getActivityCodeValue());
				activityCodeAssignment.put("ActivityId", code.getActivityId());
				activityCodeAssignment.put("ActivityName", code.getActivityName());
				activityCodeAssignment.put("ProjectId", code.getProjectId());
				activityCodeAssignment.put("ActivityCodeObjectId", code.getActivityCodeObjectId());
				activityCodeAssignmentList.put(activityCodeAssignment);
			}

		}

		// Update codesLastCheckDate
		Date lastCheckDate = new Date();
		this.setCodesLastCheckDate(lastCheckDate);

		if (activityCodeAssignmentList.length() > 0) {
			this.writeJSONFile(activityCodeAssignmentList, codesAssignmentsFileName);
			File importFile = new File(codesAssignmentsFileName);
			dlService.createManualImport(importFile);
		}

		// Check for errors and create response.
		boolean failure = false, warning = false;
		StringBuffer msg = new StringBuffer("");

		for (P6ServiceMessage message : errors) {
			if (message.getType() == P6ServiceMessage.MessageType.APPLICATION) {
				failure = true;
			} else {
				warning = true;
			}

			msg.append(message.getType().toString() + " Error: <br/>");
			msg.append(message.getMessage() + "<br/><br/>");
		}

		if (failure) {
			response.setStatus("FAILURE");
		} else if (warning) {
			response.setStatus("WARNING");
		} else {
			response.setStatus("SUCCESS");
		}

		response.setMsg(msg.toString());

		return response;
	}

	public void writeJSONFile(JSONArray jsonList, String nameFile) {
		try (FileWriter file = new FileWriter(nameFile)) {

			file.write(jsonList.toString());
			file.flush();
			LOGGER.log(Level.INFO, "File successfully written");

		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}

}
