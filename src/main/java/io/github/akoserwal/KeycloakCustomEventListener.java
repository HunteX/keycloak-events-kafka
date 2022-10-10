package io.github.akoserwal;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//import org.jboss.logging.Logger;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

public class KeycloakCustomEventListener implements EventListenerProvider {
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

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.convertValue(adminEvent.getRepresentation().replace("\\", ""), JsonNode.class);

        CreateUserData data = new CreateUserData();

        data.UserId = adminEvent.getResourcePath().replace("users/", "");
        data.Username = node.get("username").toString();
        data.Firstname = node.get("firstName").toString();
        data.Lastname = node.get("lastName").toString();
        data.Email = node.get("email").toString();

        SerializeAndSend(data);
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
