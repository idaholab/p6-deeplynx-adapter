package com.example.p6;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
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

	DeepLynxService dlService;
	P6ServiceSession session;
	private final String fileName = "import.json";
	private final String relsFileName = "import_rels.json";
	private final String codesAssignmentsFileName = "import_code_assignments.json";
	int projectObjectId;

	public P6ServiceResponse mapActivities(Environment env, int databaseInstance) {
		dlService = new DeepLynxService(env);
		P6ServiceResponse response = new P6ServiceResponse();

		dlService.authenticate();

		// boolean containerExists = dlService.checkContainer();
		// if (!containerExists) {
		// 	response.setStatus("FAILURE");
		// 	response.setMsg("FAILURE. Container does not exist!");
		// 	return response;
		// }

		Date dataSourceDate = dlService.checkDataSource();
		if (dataSourceDate == null) {
			response.setStatus("FAILURE");
			response.setMsg("FAILURE. Data source not found or could not be created!");
			return response;
		}

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
			// need the projectObjectId to filter with in ActivitiesWrapper.getRelationships
			// todo: this saves the last projectObjectId; only works since all the projectObjectId's are the same
			projectObjectId = act.getProjectObjectId();

			JSONObject activity = new JSONObject();
			activity.put("Id", act.getId());
			activity.put("ProjectId", act.getProjectId());
			activity.put("WBSCode", act.getWBSCode());
			activity.put("WBSName", act.getWBSName());
			activity.put("WBSPath", act.getWBSPath());
			activity.put("Name", act.getName());
			activity.put("CompletionStatus", act.getStatus());
			activity.put("ProjectedStartDate", this.translateDate(act.getStartDate()));
			activity.put("ProjectedFinishDate", this.translateDate(act.getFinishDate()));
			activity.put("ActualStartDate", act.getActualStartDate().getValue());
			activity.put("ActualFinishDate", act.getActualFinishDate().getValue());
			activity.put("PlannedDuration", act.getPlannedDuration());
			activity.put("ActualDuration", act.getActualDuration().getValue());
			activity.put("RemainingDuration", act.getRemainingDuration().getValue());
			activity.put("CompletedDuration", act.getAtCompletionDuration());
			activity.put("LastUpdateDate", this.translateDate(act.getLastUpdateDate().getValue()));
			activityList.put(activity);
		}

		// write json to file and import
		this.writeJSONFile(activityList, fileName);
		File importFile = new File(fileName);
		dlService.createManualImport(importFile);

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
			JSONObject relationship = new JSONObject();
			relationship.put("PredecessorActivityId", rel.getPredecessorActivityId());
			relationship.put("PredecessorProjectId", rel.getPredecessorProjectId());
			relationship.put("SuccessorActivityId", rel.getSuccessorActivityId());
			relationship.put("SuccessorProjectId", rel.getSuccessorProjectId());
			relationship.put("LastUpdateDate", this.translateDate(rel.getLastUpdateDate().getValue()));
			relationshipList.put(relationship);
		}

		this.writeJSONFile(relationshipList, relsFileName);
		File importFile = new File(relsFileName);
		dlService.createManualImport(importFile);

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
			JSONObject activityCodeAssignment = new JSONObject();
			activityCodeAssignment.put("ActivityCodeDescription", code.getActivityCodeDescription());
			activityCodeAssignment.put("ActivityCodeTypeName", code.getActivityCodeTypeName());
			activityCodeAssignment.put("ActivityCodeValue", code.getActivityCodeValue());
			activityCodeAssignment.put("ActivityId", code.getActivityId());
			activityCodeAssignment.put("ActivityName", code.getActivityName());
			activityCodeAssignment.put("ProjectId", code.getProjectId());
			activityCodeAssignment.put("ActivityCodeObjectId", code.getActivityCodeObjectId());
			activityCodeAssignment.put("LastUpdateDate", this.translateDate(code.getLastUpdateDate().getValue()));
			activityCodeAssignmentList.put(activityCodeAssignment);

		}

		this.writeJSONFile(activityCodeAssignmentList, codesAssignmentsFileName);
		File importFile = new File(codesAssignmentsFileName);
		dlService.createManualImport(importFile);

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

}
