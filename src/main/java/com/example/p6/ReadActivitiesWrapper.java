package com.example.p6;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.BindingProvider;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.json.JSONArray;
import org.json.JSONObject;

import org.javatuples.Pair;

import com.primavera.ws.p6.activity.Activity;
import com.primavera.ws.p6.activity.ActivityFieldType;
import com.primavera.ws.p6.relationship.RelationshipFieldType;
import com.primavera.ws.p6.activitycode.ActivityCodeFieldType;
import com.primavera.ws.p6.activitycodeassignment.ActivityCodeAssignment;
import com.primavera.ws.p6.activitycodeassignment.ActivityCodeAssignmentFieldType;
import com.primavera.ws.p6.udfvalue.UDFValue;
import com.primavera.ws.p6.udfvalue.UDFValueFieldType;
import com.primavera.ws.p6.authentication.AuthenticationService;
import com.primavera.ws.p6.authentication.AuthenticationServicePortType;
import com.primavera.ws.p6.authentication.IntegrationFault;

public class ReadActivitiesWrapper extends ActivitiesWrapper {

	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private final String fileName = "/var/app/sqlite/import.json";
	private final String relsFileName = "/var/app/sqlite/import_rels.json";
	private final String codesAssignmentsFileName = "/var/app/sqlite/import_code_assignments.json";
	private final String udfValuesFileName = "/var/app/sqlite/import_udf_values.json";

	// todo: could make getting this a little better
	private Integer projectObjectId;

	public void importP6Data(Environment env, int databaseInstance){
		DeepLynxService deeplynx = new DeepLynxService(env);
		deeplynx.authenticate();

		P6ServiceSession session = new P6ServiceSession(env.getUserName(), env.getPassword(), databaseInstance, env.getP6URL());
		try {
			String p6url = env.getP6URL() + "AuthenticationService?wsdl";

			AuthenticationService auth = new AuthenticationService(new URL(p6url));
			AuthenticationServicePortType servicePort = auth.getAuthenticationServiceSOAP12PortHttp();
			session.setUserNameToken((BindingProvider) servicePort);
			servicePort.login(env.getUserName(), env.getPassword(), databaseInstance);
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, "P6 AuthenticationService failed with p6url " + env.getP6URL() + " and username " + env.getUserName(), e);
		} catch (IntegrationFault e) {
			LOGGER.log(Level.SEVERE, "P6 AuthenticationService failed with p6url " + env.getP6URL() + " and username " + env.getUserName(), e);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "P6 AuthenticationService failed with p6url " + env.getP6URL() + " and username " + env.getUserName(), e);
		}

		P6ServiceResponse response = mapActivities(session, deeplynx, env);
		LOGGER.log(Level.INFO, "P6 Service Response: " + response.getStatus() + " : " + response.getMsg());

		P6ServiceResponse response_rels = mapRelationships(session, deeplynx, env, projectObjectId);
		LOGGER.log(Level.INFO, "P6 Service Response_rels: " + response_rels.getStatus() + " : " + response_rels.getMsg());

		P6ServiceResponse response_codes = mapActivityCodeAssignments(session, deeplynx, env);
		LOGGER.log(Level.INFO, "P6 Service Response_codes: " + response_codes.getStatus() + " : " + response_codes.getMsg());

		// could filter by projectObjectId or activityObjectIdList; projectObjectId makes more sense at the moment
		P6ServiceResponse response_udfValues = mapActivityUDFValues(session, deeplynx, env, projectObjectId);
		LOGGER.log(Level.INFO, "P6 Service Response_udfValues: " + response_udfValues.getStatus() + " : " + response_udfValues.getMsg());

		// todo: may need to use CalendarService to get work hours per day (and maybe duration units..)
		// CalendarService calendarService = service.getCalendarService();
		// Project project = new Project();
		// project.setId(projectId);
		// Calendar calendar = calendarService.readProjectCalendar(project, null);
		// int workHoursPerDay = calendar.getHoursPerDay();
		// System.out.println("The work hours per day for the project are " + workHoursPerDay + " hours.");
	}

	// todo: look into using web services to get other necessary info like various units (duration, cost, etc.)
	// todo: check if there are differences between what the user sees in P6 and the names in the Activity Class - may need to use a service to translate or find/write a doc
	// todo: possibly make use of getClass() - could make a generic mapClassInstances() method

	private Pair<JSONObject, String> genericP6DataGetter (Object instance, Method[] methods, Boolean getProjectObjectId) {
		JSONObject datum = new JSONObject();
		String datumId = new String();

		try {
			for (Method method : methods) {
				String methodName = method.getName();
				if (methodName.startsWith("get")) {
					String xmlElementName = methodName.replace("get", "");
					// Invoke getter method
					Object result = method.invoke(instance);

					// handle the cases for all the Activity property data types that we plan on supporting
					// and place in json payload
					if (result instanceof String) {
						datum.put(xmlElementName, (String) result);
					} else if (result instanceof Integer) {
						datum.put(xmlElementName, (Integer) result);
					} else if (result instanceof Double) {
						datum.put(xmlElementName, (Double) result);
					} else if (result instanceof Boolean) {
						// todo: need some test data for Boolean
						datum.put(xmlElementName, (Boolean) result);
					} else if (result instanceof XMLGregorianCalendar) {
						Date resultJAXBElement = translateDate((XMLGregorianCalendar) result);
					} else if (result == null) {
							// todo: see how many transformations this generates with and without this
							// todo: this could be a user parameter
							datum.put(xmlElementName, "");
					} else if (result instanceof JAXBElement) {
						// handle the JAXBElement's
						Class<?> valueType = ((JAXBElement<?>) result).getDeclaredType();
						if (valueType == Double.class) {
							Double resultJAXBElement = ((JAXBElement<Double>) result).getValue();
							datum.put(xmlElementName, resultJAXBElement);
						} else if (valueType == String.class) {
							String resultJAXBElement = ((JAXBElement<String>) result).getValue();
							datum.put(xmlElementName, resultJAXBElement);
						} else if (valueType == Integer.class) {
							Integer resultJAXBElement = ((JAXBElement<Integer>) result).getValue();
							datum.put(xmlElementName, resultJAXBElement);
						} else if (valueType == Boolean.class) {
							Boolean resultJAXBElement = ((JAXBElement<Boolean>) result).getValue();
							datum.put(xmlElementName, resultJAXBElement);
						} else if (valueType == XMLGregorianCalendar.class) {
							Date resultJAXBElement = translateDate(((JAXBElement<XMLGregorianCalendar>) result).getValue());
							datum.put(xmlElementName, resultJAXBElement);
						} else {
							System.out.println("JAXBElement: ".concat(methodName));
						}
					}
					// else {
					// 	System.out.println(methodName); // getClass
					// }

					// get Ids for later use
					if (methodName == "getId") {
						datumId = (String) result;
					}

					if (getProjectObjectId == true && methodName == "getProjectObjectId") {
						// this saves the last projectObjectId; works since all the projectObjectId's are the same
						projectObjectId = (Integer) result;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, "genericP6DataGetter failed | " + e.toString());
		}

		return Pair.with(datum, datumId);
	}


	private P6ServiceResponse mapActivities(P6ServiceSession session, DeepLynxService dlService, Environment env) {

		List<ActivityFieldType> fields = new ArrayList<>();
		for (ActivityFieldType fieldType : ActivityFieldType.values()) {
	    fields.add(fieldType);
		}

		JSONArray activityList = new JSONArray();
		List<String> activityIDList = new ArrayList<String>();
		for (Activity act : getActivities(session, env.getProjectID(), fields)) {
			Method[] methods = Activity.class.getMethods();
			Boolean saveProjectObjectId = true;

			try {
				Pair<JSONObject, String> activityData = genericP6DataGetter(act, methods, saveProjectObjectId);
				activityList.put(activityData.getValue0());
				activityIDList.add(activityData.getValue1());
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.log(Level.SEVERE, "mapActivities failed | " + e.toString());
			}
		}

		// write json to file and import
		writeJSONFile(activityList, fileName);
		File importFile = new File(fileName);
		dlService.createManualImport(importFile);
		// delete DL nodes that no longer exist in P6
		dlService.deleteNodes(activityIDList, "Activity", "Id");

		// Check for errors and create response.
		P6ServiceResponse response = useP6ServiceMessage(errors);

		return response;
	}

	private P6ServiceResponse mapRelationships(P6ServiceSession session, DeepLynxService dlService, Environment env, int projectObjectId) {
		List<RelationshipFieldType> fields = new ArrayList<RelationshipFieldType>();
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
			relationship.put("LastUpdateDate", translateDate(rel.getLastUpdateDate().getValue()));
			relationshipList.put(relationship);
		}

		writeJSONFile(relationshipList, relsFileName);
		File importFile = new File(relsFileName);
		dlService.createManualImport(importFile);

		// Check for errors and create response.
		P6ServiceResponse response = useP6ServiceMessage(errors);

		return response;
	}

	public P6ServiceResponse mapActivityCodeAssignments(P6ServiceSession session, DeepLynxService dlService, Environment env) {
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
		List<String> activityCodeIDList = new ArrayList<String>();
		for (ActivityCodeAssignment code : getActivityCodeAssignments(session, env.getProjectID(), fields)) {
			// P6 doesn't give unique activity code assignment ids, but a given activity code can only be assigned to a given activty once
			// unique id for DL typemapping
			String activityCodeAssignmentId = code.getActivityId() + code.getActivityCodeObjectId();
			activityCodeIDList.add(activityCodeAssignmentId);

			JSONObject activityCodeAssignment = new JSONObject();
			activityCodeAssignment.put("ActivityCodeDescription", code.getActivityCodeDescription());
			activityCodeAssignment.put("ActivityCodeTypeName", code.getActivityCodeTypeName());
			activityCodeAssignment.put("ActivityCodeValue", code.getActivityCodeValue());
			activityCodeAssignment.put("ActivityId", code.getActivityId());
			activityCodeAssignment.put("ActivityName", code.getActivityName());
			activityCodeAssignment.put("ProjectId", code.getProjectId());
			activityCodeAssignment.put("ActivityCodeObjectId", code.getActivityCodeObjectId());
			activityCodeAssignment.put("ActivityCodeAssignmentId", activityCodeAssignmentId);
			activityCodeAssignment.put("LastUpdateDate", translateDate(code.getLastUpdateDate().getValue()));

			activityCodeAssignmentList.put(activityCodeAssignment);
		}

		writeJSONFile(activityCodeAssignmentList, codesAssignmentsFileName);
		File importFile = new File(codesAssignmentsFileName);
		dlService.createManualImport(importFile);
		// delete DL nodes that no longer exist in P6
		dlService.deleteNodes(activityCodeIDList, "ActivityCode", "ActivityCodeAssignmentId");

		// Check for errors and create response.
		P6ServiceResponse response = useP6ServiceMessage(errors);

		return response;
	}

	public P6ServiceResponse mapActivityUDFValues(P6ServiceSession session, DeepLynxService dlService, Environment env, int projectObjectId) {
		List<UDFValueFieldType> fields = new ArrayList<UDFValueFieldType>();
		for (UDFValueFieldType fieldType : UDFValueFieldType.values()) {
	    fields.add(fieldType);
		}

		JSONArray udfValueList = new JSONArray();
		List<String> udfValueIDList = new ArrayList<String>();
		for (UDFValue udf : getUDFValues(session, projectObjectId, fields)) {
			Method[] methods = UDFValue.class.getMethods();
			Boolean saveProjectObjectId = false;

			try {
				// todo: refactor udfValueId part
				String foreignObjectId = Integer.toString(udf.getForeignObjectId());
				String udfTypeObjectId = Integer.toString(udf.getUDFTypeObjectId());
				String udfValueId = foreignObjectId + udfTypeObjectId;
				udfValueIDList.add(udfValueId);
				// udfValue.put("UDFValueId", udfValueId);

				Pair<JSONObject, String> udfValueData = genericP6DataGetter(udf, methods, saveProjectObjectId);
				udfValueList.put(udfValueData.getValue0());
				udfValueIDList.add(udfValueData.getValue1());

			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.log(Level.SEVERE, "mapActivityUDFValues failed | " + e.toString());
			}
		}

		writeJSONFile(udfValueList, udfValuesFileName);
		File importFile = new File(udfValuesFileName);
		dlService.createManualImport(importFile);
		// delete DL nodes that no longer exist in P6
		dlService.deleteNodes(udfValueIDList, "UDFValue", "UDFValueId");

		// Check for errors and create response.
		P6ServiceResponse response = useP6ServiceMessage(errors);

		return response;
	}

	// errors variable comes from ActivitiesWrapper
	private P6ServiceResponse useP6ServiceMessage(ArrayList<P6ServiceMessage> errors) {
	  P6ServiceResponse response = new P6ServiceResponse();
	  try {
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

	  } catch (Exception e) {
	    LOGGER.log(Level.SEVERE,"useP6ServiceMessage failed: " + e.toString(), e);
	  }

	  return response;
	}

	private void writeJSONFile(JSONArray jsonList, String nameFile) {
		try (FileWriter file = new FileWriter(nameFile)) {

			file.write(jsonList.toString());
			file.flush();
			LOGGER.log(Level.INFO, "File successfully written");

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,"writeJSONFile failed: " + e.toString(), e);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"writeJSONFile failed: " + e.toString(), e);
		}
	}

	public static  Date translateDate(XMLGregorianCalendar date){
	        return date == null ? null : date.toGregorianCalendar().getTime();
	}

	// public static Date translateDate(String dateString){
  //       GregorianCalendar calendar = (GregorianCalendar)GregorianCalendar.getInstance();
  //       SimpleDateFormat sdf = new SimpleDateFormat();
	//
  //       try {
  //           if (dateString != null){
  //               if (dateString.matches("^(0[1-9]|1[012])/(0[1-9]|[12][0-9]|3[01])/(19|20)\\d\\d$")){
  //                   sdf = new SimpleDateFormat("MM/dd/yyyy");
  //               }
  //               else if (dateString.matches("^(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])-(19|20)\\d\\d$")){
  //                   sdf = new SimpleDateFormat("MM-dd-yyyy");
  //               }
  //               else if (dateString.matches("^(19|20)\\d\\d(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$")){
  //                   sdf = new SimpleDateFormat("yyyyMMdd");
  //               }
  //               else if (dateString.matches("^(19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$")){
  //                   sdf = new SimpleDateFormat("yyyy-MM-dd");
  //               }
	//
  //               calendar.setTime(sdf.parse(dateString));
  //           }
  //       } catch (ParseException e) {
	// 					LOGGER.log(Level.SEVERE,"translateDate failed: " + e.toString(), e);
  //       } catch (Exception e) {
	// 					LOGGER.log(Level.SEVERE,"translateDate failed: " + e.toString(), e);
  //       }
  //       return calendar.getTime();
  //   }

}
