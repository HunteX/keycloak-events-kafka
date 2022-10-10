package io.github.akoserwal;

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
    private static class Representation {
        public Representation() {
        }

        private String username;
        private String firstname;
        private String lastname;
        private String email;
    }

    private class CreateUserData {
        public String UserId;
        public String Username;
        public String Firstname;
        public String Lastname;
        public String Email;
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

        if (!(resourceType == ResourceType.USER && operationType == OperationType.CREATE)) {
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Representation rep = mapper.readValue(adminEvent.getRepresentation(), Representation.class);
            CreateUserData data = new CreateUserData();

            data.UserId = adminEvent.getResourcePath().replace("users/", "");
            data.Username = rep.username;
            data.Firstname = rep.firstname;
            data.Lastname = rep.lastname;
            data.Email = rep.email;

            SerializeAndSend(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void SerializeAndSend(CreateUserData data) {
        ObjectMapper eventMapper = new ObjectMapper();

        JsonNode eventJson = eventMapper.convertValue(data, JsonNode.class);

        Producer.publishEvent("KEYCLOAK", eventJson.toString());
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
