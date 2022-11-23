/*
 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.inl.de.p6adapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.ws.BindingProvider;

import com.primavera.ws.p6.activity.ActivityFieldType;
import com.primavera.ws.p6.activity.ActivityPortType;
import com.primavera.ws.p6.activity.ActivityService;

import com.primavera.ws.p6.relationship.RelationshipFieldType;
import com.primavera.ws.p6.relationship.RelationshipPortType;
import com.primavera.ws.p6.relationship.RelationshipService;

public class ActivitiesWrapper {

    protected ArrayList<P6ServiceMessage> errors = new ArrayList<P6ServiceMessage>();
    protected String log = "";

    public ActivitiesWrapper() {
    }

	protected List<com.primavera.ws.p6.activity.Activity> getActivity(P6ServiceSession session, String activityId, String projectID, List<ActivityFieldType> returnParams){
        String url = session.getP6url() + "ActivityService?wsdl";
        URL wsdlURL = null;

        try {
            wsdlURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            addError(P6ServiceMessage.MessageType.APPLICTION, "Error creating URL for ActivityService.  URL: " + url + " \nMalformedURLException thrown: " + e.getMessage());
        }

        ActivityService service = new ActivityService(wsdlURL);
        ActivityPortType servicePort = service.getActivityPort();
        session.setUserNameToken((BindingProvider)servicePort);

        List<com.primavera.ws.p6.activity.Activity> results = null;

        try {
            results = servicePort.readActivities(returnParams,"ProjectId='" + projectID + "' AND Id in ('"+activityId+"')", null);
        } catch (com.primavera.ws.p6.activity.IntegrationFault e) {
            e.printStackTrace();
        }

        return results;
    }

	protected List<com.primavera.ws.p6.activity.Activity> getActivities(P6ServiceSession session, String projectID, List<ActivityFieldType> returnParams){
        String url = session.getP6url() + "ActivityService?wsdl";
        URL wsdlURL = null;

        try {
            wsdlURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            addError(P6ServiceMessage.MessageType.APPLICTION, "Error creating URL for ActivityService.  URL: " + url + " \nMalformedURLException thrown: " + e.getMessage());
        }

        ActivityService service = new ActivityService(wsdlURL);
        ActivityPortType servicePort = service.getActivityPort();
        session.setUserNameToken((BindingProvider)servicePort);

        List<com.primavera.ws.p6.activity.Activity> results = null;

        try {
            results = servicePort.readActivities(returnParams,"ProjectId='" + projectID + "'", null);
        } catch (com.primavera.ws.p6.activity.IntegrationFault e) {
            e.printStackTrace();
        }

        return results;
    }

	protected boolean updateActivities(P6ServiceSession session, List<com.primavera.ws.p6.activity.Activity> activities){
        String url = session.getP6url() + "ActivityService?wsdl";
        URL wsdlURL = null;

        try {
            wsdlURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            addError(P6ServiceMessage.MessageType.APPLICTION, "Error creating URL for ActivityService.  URL: " + url + " \nMalformedURLException thrown: " + e.getMessage());
        }

        ActivityService service = new ActivityService(wsdlURL);
        ActivityPortType servicePort = service.getActivityPort();
        session.setUserNameToken((BindingProvider)servicePort);

        boolean success = false;

        try {
        	success = servicePort.updateActivities(activities);
        } catch (com.primavera.ws.p6.activity.IntegrationFault e) {
            e.printStackTrace();
        }

        return success;
    }

    protected List<com.primavera.ws.p6.relationship.Relationship> getRelationships(P6ServiceSession session, int projectObjectID, List<RelationshipFieldType> returnParams){
          String url = session.getP6url() + "RelationshipService?wsdl";
          URL wsdlURL = null;

          try {
              wsdlURL = new URL(url);
          } catch (MalformedURLException e) {
              e.printStackTrace();
              addError(P6ServiceMessage.MessageType.APPLICTION, "Error creating URL for RelationshipService.  URL: " + url + " \nMalformedURLException thrown: " + e.getMessage());
          }

          RelationshipService service = new RelationshipService(wsdlURL);
          RelationshipPortType servicePort = service.getRelationshipPort();
          session.setUserNameToken((BindingProvider)servicePort);

          List<com.primavera.ws.p6.relationship.Relationship> results = null;

          try {
              results = servicePort.readRelationships(returnParams,"SuccessorProjectObjectId='" + projectObjectID + "'", null);
          } catch (com.primavera.ws.p6.relationship.IntegrationFault e) {
              e.printStackTrace();
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
        Date date = new Date();
        this.log += date + ":  " + log + "\n";
    }

    protected String getLog() {
        return log;
    }

    protected void clearLog() {
        log = "";
    }
}
