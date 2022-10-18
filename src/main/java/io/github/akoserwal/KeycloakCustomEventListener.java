package io.github.akoserwal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//import org.jboss.logging.Logger;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

public class KeycloakCustomEventListener implements EventListenerProvider {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Representation {
        public Representation() {
        }

        @JsonProperty("username")
        public String Username;

        @JsonProperty("firstName")
        public String FirstName;

        @JsonProperty("lastName")
        public String LastName;

        @JsonProperty("email")
        public String Email;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AddUserRoleRepresentation {
        public AddUserRoleRepresentation() {
        }

        @JsonProperty("name")
        public String Role;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RemoveUserRoleRepresentation {
        public RemoveUserRoleRepresentation() {
        }

        @JsonProperty("name")
        public String Role;
    }

    private class CreateUserData {
        public String UserId;
        public String Username;
        public String Firstname;
        public String Lastname;
        public String Email;
    }

    private class AddUserRole {
        public String UserId;
        public String Role;
    }

    private class RemoveUserRole {
        public String UserId;
        public String Role;
    }

//    private static Logger logger = Logger.getLogger(KeycloakCustomEventListener.class);

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // System.out.println("Admin Event:-" + adminEvent.getResourceType().name());
        // Producer.publishEvent(adminEvent.getOperationType().toString(), adminEvent.getAuthDetails().getUserId());

        // TODO: add config support https://github.com/athenacykes/keycloak-kafka-eventlistener
        //		KafkaEventListenerConfig kafkaConfig = new KafkaEventListenerConfig();
        //		Properties properties = kafkaConfig.getProperties();
        // String topicName = properties.getProperty(KafkaEventListenerConfig.KAFKA_CONFIG_TOPIC_NAME_ADMIN);

        ResourceType resourceType = adminEvent.getResourceType();
        OperationType operationType = adminEvent.getOperationType();

//        System.out.println("EVENT-op-type::: " + adminEvent.getOperationType());
//        System.out.println("EVENT-re-type::: " + adminEvent.getResourceTypeAsString());
//        System.out.println("EVENT-cl-id::: " + adminEvent.getAuthDetails().getClientId());
//        System.out.println("EVENT-us-id::: " + adminEvent.getAuthDetails().getUserId());
//        System.out.println("EVENT-re-path::: " + adminEvent.getResourcePath());
//        System.out.println("EVENT::: " + adminEvent.getRepresentation());

        if (resourceType == ResourceType.USER && operationType == OperationType.CREATE) {
            SendCreateUserData(adminEvent);
        }

        if (resourceType == ResourceType.CLIENT_ROLE_MAPPING && operationType == OperationType.CREATE) {
            SendAddUserRole(adminEvent);
        }

        if (resourceType == ResourceType.CLIENT_ROLE_MAPPING && operationType == OperationType.DELETE) {
            SendRemoveUserRole(adminEvent);
        }
    }

    private void SendAddUserRole(AdminEvent adminEvent) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            AddUserRoleRepresentation rep = mapper.readValue(adminEvent.getRepresentation()
                            .replace("[", "")
                            .replace("]", ""),
                    AddUserRoleRepresentation.class
            );
            AddUserRole data = new AddUserRole();

            data.UserId = adminEvent
                    .getResourcePath()
                    .substring("users/".length() - 1, adminEvent.getResourcePath().indexOf("/role"));
            data.Role = rep.Role;

            SerializeAndSend(data, "keycloak-user-add-role");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void SendRemoveUserRole(AdminEvent adminEvent) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            RemoveUserRoleRepresentation rep = mapper.readValue(
                    adminEvent.getRepresentation()
                            .replace("[", "")
                            .replace("]", ""),
                    RemoveUserRoleRepresentation.class);
            RemoveUserRole data = new RemoveUserRole();

            data.UserId = adminEvent
                    .getResourcePath()
                    .substring("users/".length() - 1, adminEvent.getResourcePath().indexOf("/role"));
            data.Role = rep.Role;

            SerializeAndSend(data, "keycloak-user-remove-role");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void SendCreateUserData(AdminEvent adminEvent) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Representation rep = mapper.readValue(adminEvent.getRepresentation(), Representation.class);
            CreateUserData data = new CreateUserData();

            data.UserId = adminEvent.getResourcePath().replace("users/", "");
            data.Username = rep.Username;
            data.Firstname = rep.FirstName;
            data.Lastname = rep.LastName;
            data.Email = rep.Email;

            SerializeAndSend(data, "keycloak-user-add");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void SerializeAndSend(Object data, String topic) {
        ObjectMapper eventMapper = new ObjectMapper();

        JsonNode eventJson = eventMapper.convertValue(data, JsonNode.class);

        Producer.publishEvent(topic, eventJson.toString());
    }

    @Override
    public void onEvent(Event event) {
        // ignore
    }

    @Override
    public void close() {
        // ignore
    }
}
