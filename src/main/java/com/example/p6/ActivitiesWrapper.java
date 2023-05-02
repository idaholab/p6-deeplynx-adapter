package com.example.p6;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;

import com.primavera.ws.p6.activity.ActivityFieldType;
import com.primavera.ws.p6.activity.ActivityPortType;
import com.primavera.ws.p6.activity.ActivityService;

import com.primavera.ws.p6.relationship.RelationshipFieldType;
import com.primavera.ws.p6.relationship.RelationshipPortType;
import com.primavera.ws.p6.relationship.RelationshipService;

import com.primavera.ws.p6.activitycode.*;
import com.primavera.ws.p6.activitycodetype.*;
import com.primavera.ws.p6.activitycodeassignment.*;
import com.primavera.ws.p6.udfvalue.*;
import com.primavera.ws.p6.udftype.*;

public class ActivitiesWrapper {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected ArrayList<P6ServiceMessage> errors = new ArrayList<P6ServiceMessage>();
    protected Map<String,Integer> foundActivityCodes = new HashMap<String,Integer>();

    public ActivitiesWrapper() {
    }

	protected List<com.primavera.ws.p6.activity.Activity> getActivities(P6ServiceSession session, String projectID, List<ActivityFieldType> returnParams){
        String url = session.getP6url() + "ActivityService?wsdl";
        URL wsdlURL = null;

        try {
            wsdlURL = new URL(url);
        } catch (MalformedURLException e) {
            addError(P6ServiceMessage.MessageType.APPLICATION, "Error creating URL for ActivityService.  URL: " + url + " \nMalformedURLException thrown: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "getActivities wsdlURL failed: " + e.toString());
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "getActivities wsdlURL failed: " + e.toString());
        }

        ActivityService service = new ActivityService(wsdlURL);
        ActivityPortType servicePort = service.getActivityPort();
        session.setUserNameToken((BindingProvider)servicePort);

        List<com.primavera.ws.p6.activity.Activity> results = null;

        try {
            results = servicePort.readActivities(returnParams,"ProjectId='" + projectID + "'", null);
        } catch (com.primavera.ws.p6.activity.IntegrationFault e) {
            LOGGER.log(Level.SEVERE, "getActivities failed: " + e.toString());
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "getActivities failed: " + e.toString());
        }

        return results;
    }

  protected List<com.primavera.ws.p6.relationship.Relationship> getRelationships(P6ServiceSession session, int projectObjectID, List<RelationshipFieldType> returnParams){
        String url = session.getP6url() + "RelationshipService?wsdl";
        URL wsdlURL = null;

        try {
            wsdlURL = new URL(url);
        } catch (MalformedURLException e) {
            addError(P6ServiceMessage.MessageType.APPLICATION, "Error creating URL for RelationshipService.  URL: " + url + " \nMalformedURLException thrown: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "getRelationships wsdlURL failed: " + e.toString());
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "getRelationships wsdlURL failed: " + e.toString());
        }

        RelationshipService service = new RelationshipService(wsdlURL);
        RelationshipPortType servicePort = service.getRelationshipPort();
        session.setUserNameToken((BindingProvider)servicePort);

        List<com.primavera.ws.p6.relationship.Relationship> results = null;

        try {
            results = servicePort.readRelationships(returnParams,"SuccessorProjectObjectId='" + projectObjectID + "'", null);
        } catch (com.primavera.ws.p6.relationship.IntegrationFault e) {
            LOGGER.log(Level.SEVERE, "getRelationships failed: " + e.toString());
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "getRelationships failed: " + e.toString());
        }

        return results;
      }

      public List<ActivityCodeAssignment> getActivityCodeAssignments(P6ServiceSession session, String projectID, List<ActivityCodeAssignmentFieldType> returnParams){
        String url = session.getP6url() + "ActivityCodeAssignmentService?wsdl";
        URL wsdlURL = null;

        try {
            wsdlURL = new URL(url);
        } catch (MalformedURLException e) {
            addError(P6ServiceMessage.MessageType.APPLICATION, "Error creating URL for ActivityService.  URL: " + url + " \nMalformedURLException thrown: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "getActivityCodeAssignments wsdlURL failed: " + e.toString());
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "getActivityCodeAssignments wsdlURL failed: " + e.toString());
        }

        ActivityCodeAssignmentService acservice = new ActivityCodeAssignmentService(wsdlURL);
        ActivityCodeAssignmentPortType acservicePort = acservice.getActivityCodeAssignmentPort();
        session.setUserNameToken((BindingProvider)acservicePort);

        List<ActivityCodeAssignment> results = null;
        try {
            results = acservicePort.readActivityCodeAssignments(returnParams,"ProjectId='" + projectID + "'", null);
        } catch (com.primavera.ws.p6.activitycodeassignment.IntegrationFault e) {
            LOGGER.log(Level.SEVERE, "getActivityCodeAssignments failed: " + e.toString());
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "getActivityCodeAssignments failed: " + e.toString());
        }

        return results;
    }

    public List<UDFValue> getUDFValues(P6ServiceSession session, int projectObjectID, List<UDFValueFieldType> returnParams){
      String url = session.getP6url() + "UDFValueService?wsdl";
      URL wsdlURL = null;

      try {
          wsdlURL = new URL(url);
      } catch (MalformedURLException e) {
          addError(P6ServiceMessage.MessageType.APPLICATION, "Error creating URL for UDFValueService. URL: " + url + " \nMalformedURLException thrown: " + e.getMessage());
          LOGGER.log(Level.SEVERE, "getUDFValues wsdlURL failed: " + e.toString());
      } catch(Exception e) {
          LOGGER.log(Level.SEVERE, "getUDFValues wsdlURL failed: " + e.toString());
      }

      UDFValueService udfService = new UDFValueService(wsdlURL);
      UDFValuePortType udfServicePort = udfService.getUDFValuePort();
      session.setUserNameToken((BindingProvider)udfServicePort);

      List<UDFValue> results = null;
      try {
          results = udfServicePort.readUDFValues(returnParams,"ProjectObjectId='" + projectObjectID + "' AND UDFTypeSubjectArea='Activity'", null);
      } catch (com.primavera.ws.p6.udfvalue.IntegrationFault e) {
          LOGGER.log(Level.SEVERE, "getUDFValues failed: " + e.toString());
      } catch(Exception e) {
          LOGGER.log(Level.SEVERE, "getUDFValues failed: " + e.toString());
      }

      return results;
  }


    protected void addError(P6ServiceMessage.MessageType type, String messageText){
        if(errors == null){
            errors = new ArrayList<P6ServiceMessage>();
        }
        setLog(messageText);
        P6ServiceMessage message = new P6ServiceMessage();
        message.setType(type);
        message.setMessage(messageText);
        message.setDateTime(new Date());
        errors.add(message);
    }

    protected void setLog(String log) {
        LOGGER.log(Level.SEVERE, "P6ServiceMessage: " + log);
    }
}
