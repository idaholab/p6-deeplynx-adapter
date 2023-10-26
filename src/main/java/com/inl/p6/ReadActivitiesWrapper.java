package com.inl.p6;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.json.JSONArray;
import org.json.JSONObject;

import org.javatuples.Pair;

import com.primavera.ws.p6.activity.Activity;
import com.primavera.ws.p6.activity.ActivityFieldType;
import com.primavera.ws.p6.relationship.RelationshipFieldType;
import com.primavera.ws.p6.wbs.WBSFieldType;
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
	private final String wbsFileName = "/var/app/sqlite/import_wbs.json";

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

		P6ServiceResponse response_wbs = mapWBS(session, deeplynx, env);
		LOGGER.log(Level.INFO, "P6 Service Response_wbs: " + response_wbs.getStatus() + " : " + response_wbs.getMsg());
	}

	private JSONObject genericP6DataGetter (Object instance, Method[] methods, Boolean getProjectObjectId) {
		JSONObject datum = new JSONObject();

		try {
			for (Method method : methods) {
				Type returnType = method.getReturnType();
				String methodName = method.getName();
				if (methodName.startsWith("get")) {
					String xmlElementName = methodName.replace("get", "");
					// Invoke getter method
					Object result = method.invoke(instance);
					// handle the cases for all the Activity property data types that we plan on supporting
					// and place in json payload
					if (result == null) {
						datum.put(xmlElementName, "");
						// datum.put(xmlElementName, JSONObject.NULL);
					} else if (returnType.equals(String.class)) {
						datum.put(xmlElementName, (String) result);
					} else if (returnType.equals(Integer.class)) {
						datum.put(xmlElementName, (Integer) result);
					} else if (returnType.equals(Double.class)) {
						datum.put(xmlElementName, (Double) result);
					} else if (returnType.equals(Boolean.class)) {
						datum.put(xmlElementName, (Boolean) result);
					} else if (returnType.equals(XMLGregorianCalendar.class)) {
						String resultJAXBElement = translateDate((XMLGregorianCalendar) result);
						datum.put(xmlElementName, resultJAXBElement);
					}
					else if (returnType.equals(JAXBElement.class)) {
						// handle the JAXBElement's
						Class<?> valueType = ((JAXBElement<?>) result).getDeclaredType();
						if (((JAXBElement<?>) result).getValue() == null) {
							if (valueType == XMLGregorianCalendar.class) {
								datum.put(xmlElementName, "");
							} else if (valueType == Double.class) {
								datum.put(xmlElementName, 0);
							}
							// datum.put(xmlElementName, JSONObject.NULL); // todo: this might be better but would require change in DeepLynx
						}
						else if (valueType == Double.class) {
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
							String resultJAXBElement = translateDate(((JAXBElement<XMLGregorianCalendar>) result).getValue());
							datum.put(xmlElementName, resultJAXBElement);
						}
						// else {
						// 	LOGGER.log(Level.WARNING, xmlElementName.concat(" data not currently supported"));
						// }
					}
					// else {
					// 	LOGGER.log(Level.WARNING, xmlElementName.concat(" data not currently supported"));
					// }


					if (getProjectObjectId == true && methodName.equals("getProjectObjectId")) {
						// this saves the last projectObjectId; works since all the projectObjectId's are the same
						projectObjectId = (Integer) result;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, "genericP6DataGetter failed | " + e.toString());
		}

		return datum;
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
				JSONObject activityData = genericP6DataGetter(act, methods, saveProjectObjectId);
				activityList.put(activityData);
				String activityID = activityData.getString("Id");
				activityIDList.add(activityID);
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.log(Level.SEVERE, "mapActivities failed | " + e.toString());
			}
		}

		// UDFValues
		Pair<P6ServiceResponse, HashMap<String, JSONObject>> udfValues = mapActivityUDFValues(session, dlService, env, projectObjectId);
		P6ServiceResponse response_udfValues = udfValues.getValue0();
		LOGGER.log(Level.INFO, "P6 Service Response_udfValues: " + response_udfValues.getStatus() + " : " + response_udfValues.getMsg());
		HashMap<String, JSONObject> udfHashmap = udfValues.getValue1();

		// ActivityCodeAssignments
		Pair<P6ServiceResponse, HashMap<String, JSONObject>> activityCodeData = mapActivityCodeAssignments(session, dlService, env);
		P6ServiceResponse response_codes = activityCodeData.getValue0();
		LOGGER.log(Level.INFO, "P6 Service Response_codes: " + response_codes.getStatus() + " : " + response_codes.getMsg());
		HashMap<String, JSONObject> codeHashmap = activityCodeData.getValue1();


		// loop through all activity objects and add UDF and ActivityCode data
		for (Object object : activityList) {
		  JSONObject activityObject = (JSONObject) object;
			int ObjectId = activityObject.getInt("ObjectId");
			String ObjectIdString = Integer.toString(ObjectId);
			// UDFValues
			JSONObject udfObject = udfHashmap.get(ObjectIdString);
			if (udfObject != null) {
				// loop to iterate through the keys in the source JSONObject, and for each key, we use the put() method to add the key-value pair to the target JSONObject
				for (String key : udfObject.keySet()) {
						// replace " " and ":" characters with "_"
						String safeKey = key.replaceAll("[\\s:#]", "_");
						// get rid of multiple consecutive "_"
						String betterKey = safeKey.replaceAll("_+", "_");
            activityObject.put(betterKey, udfObject.get(key));
        }
			}
			// ActivityCodeAssignments
			JSONObject codeObject = codeHashmap.get(ObjectIdString);
			if (codeObject != null) {
				// loop to iterate through the keys in the source JSONObject, and for each key, we use the put() method to add the key-value pair to the target JSONObject
				for (String key : codeObject.keySet()) {
						// replace " " and ":" characters with "_"
						String safeKey = key.replaceAll("[\\s:#]", "_");
						// get rid of multiple consecutive "_"
						String betterKey = safeKey.replaceAll("_+", "_");
            activityObject.put(betterKey, codeObject.get(key));
        }
			}
    }

		// write json to file
		writeJSONFile(activityList, fileName);
		// import
		dlService.createManualImport(activityList.toString());

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
		dlService.createManualImport(relationshipList.toString());

		// Check for errors and create response.
		P6ServiceResponse response = useP6ServiceMessage(errors);

		return response;
	}

	private P6ServiceResponse mapWBS(P6ServiceSession session, DeepLynxService dlService, Environment env) {

		List<WBSFieldType> fields = new ArrayList<WBSFieldType>();
		fields.add(WBSFieldType.CODE);
		fields.add(WBSFieldType.NAME);
		fields.add(WBSFieldType.OBJECT_ID);
		fields.add(WBSFieldType.PROJECT_ID);
		fields.add(WBSFieldType.PROJECT_OBJECT_ID);
		fields.add(WBSFieldType.OBS_NAME);
		fields.add(WBSFieldType.OBS_OBJECT_ID);
		fields.add(WBSFieldType.PARENT_OBJECT_ID);
		fields.add(WBSFieldType.SEQUENCE_NUMBER);

		JSONArray projectWBSList = new JSONArray();

		for (com.primavera.ws.p6.wbs.WBS wbs : getWBS(session, env.getProjectID(), fields)) {
			JSONObject projectWBS = new JSONObject();
			projectWBS.put("Code", wbs.getCode());
			projectWBS.put("Name", wbs.getName());
			projectWBS.put("ObjectId", wbs.getObjectId());
			projectWBS.put("ProjectId", wbs.getProjectId());
			projectWBS.put("ProjectObjectId", wbs.getProjectObjectId());
			projectWBS.put("OBSName", wbs.getOBSName());
			projectWBS.put("OBSObjectId", wbs.getOBSObjectId());
			projectWBS.put("ParentObjectId", wbs.getParentObjectId().getValue());
			projectWBS.put("SequenceNumber", wbs.getSequenceNumber());
			projectWBSList.put(projectWBS);
		}

		writeJSONFile(projectWBSList, wbsFileName);
		dlService.createManualImport(relationshipList.toString());

		// Check for errors and create response.
		P6ServiceResponse response = useP6ServiceMessage(errors);

		return response;
	}

	public Pair<P6ServiceResponse, HashMap<String, JSONObject>> mapActivityCodeAssignments(P6ServiceSession session, DeepLynxService dlService, Environment env) {
		List<ActivityCodeAssignmentFieldType> fields = new ArrayList<ActivityCodeAssignmentFieldType>();
		for (ActivityCodeAssignmentFieldType fieldType : ActivityCodeAssignmentFieldType.values()) {
	    fields.add(fieldType);
		}

		// hashmap of activity object id's and the corresponding activityCodeAssignments
		HashMap<String, JSONObject> hashMap = new HashMap<>();
		JSONObject activityCodeAssignments;
		JSONArray codeAssignmentList = new JSONArray();
		for (ActivityCodeAssignment code : getActivityCodeAssignments(session, env.getProjectID(), fields)) {
			Method[] methods = ActivityCodeAssignment.class.getMethods();
			Boolean saveProjectObjectId = false;
			try {
				JSONObject codeValueData = genericP6DataGetter(code, methods, saveProjectObjectId);
				codeAssignmentList.put(codeValueData);

				int activityObjectId = codeValueData.getInt("ActivityObjectId");
				String activityId = Integer.toString(activityObjectId);
				String activityCodeTypeName = codeValueData.getString("ActivityCodeTypeName");
				String activityCodeValue = codeValueData.getString("ActivityCodeValue");

				// attempt to get JSONObject of ActivityCodeAssignments for given activityId
				activityCodeAssignments = hashMap.get(activityId);
				// if null, create the JSONObject and place in hashmap
				if (activityCodeAssignments == null) {
					activityCodeAssignments = new JSONObject();
					hashMap.put(activityId, activityCodeAssignments);
				}

				activityCodeAssignments.put("ActivityCodeName_".concat(activityCodeTypeName), activityCodeValue);

			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.log(Level.SEVERE, "mapActivityActivityCodeAssignments failed | " + e.toString());
			}
		}

		// Check for errors and create response.
		P6ServiceResponse response = useP6ServiceMessage(errors);

		return Pair.with(response, hashMap);
	}

	public Pair<P6ServiceResponse, HashMap<String, JSONObject>> mapActivityUDFValues(P6ServiceSession session, DeepLynxService dlService, Environment env, int projectObjectId) {
		List<UDFValueFieldType> fields = new ArrayList<UDFValueFieldType>();
		for (UDFValueFieldType fieldType : UDFValueFieldType.values()) {
	    fields.add(fieldType);
		}

		// hashmap of activity object id's and the corresponding activityUDFValues
		HashMap<String, JSONObject> hashMap = new HashMap<>();
		JSONObject activityUDFValues;
		JSONArray udfValueList = new JSONArray();
		for (UDFValue udf : getUDFValues(session, projectObjectId, fields)) {
			Method[] methods = UDFValue.class.getMethods();
			Boolean saveProjectObjectId = false;
			try {
				JSONObject udfValueData = genericP6DataGetter(udf, methods, saveProjectObjectId);
				udfValueList.put(udfValueData);

				int foreignObjectId = udfValueData.getInt("ForeignObjectId");
				String activityId = Integer.toString(foreignObjectId);
				String udfTypeTitle = udfValueData.getString("UDFTypeTitle");
				String typeSubject = udfValueData.getString("UDFTypeSubjectArea");
				String udfDataType = udfValueData.getString("UDFTypeDataType");

				// attempt to get JSONObject of UDFValues for given activityId
				activityUDFValues = hashMap.get(activityId);
				// if null, create the JSONObject and place in hashmap
				if (activityUDFValues == null) {
					activityUDFValues = new JSONObject();
					hashMap.put(activityId, activityUDFValues);
				}

				// todo: currently supporting Text and Integer UDFTypeDataType's
				if (typeSubject.equals("Activity")) {
					if (udfDataType.equals("Text")) {
						String value = udfValueData.getString("Text");
						activityUDFValues.put("UDFValueName_".concat(udfTypeTitle), value);
					} else if (udfDataType.equals("Integer")) {
						int value = udfValueData.getInt("Integer");
						activityUDFValues.put("UDFValueName_".concat(udfTypeTitle), value);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.log(Level.SEVERE, "mapActivityUDFValues failed | " + e.toString());
			}
		}

		// Check for errors and create response.
		P6ServiceResponse response = useP6ServiceMessage(errors);

		return Pair.with(response, hashMap);
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

	public static  String translateDate(XMLGregorianCalendar date){
		String isoTimestamp = null;
		try {
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
			isoTimestamp = isoFormat.format(date.toGregorianCalendar().getTime());
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"translateDate failed: " + e.toString(), e);
		}
		return isoTimestamp;
	}

}
