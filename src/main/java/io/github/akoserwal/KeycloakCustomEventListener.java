package io.github.akoserwal;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;

public class KeycloakCustomEventListener implements EventListenerProvider {
    ObjectMapper eventMapper = new ObjectMapper();

    private static Logger logger = Logger.getLogger(KeycloakCustomEventListener.class);

    public KeycloakCustomEventListener() {

    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // System.out.println("Admin Event:-" + adminEvent.getResourceType().name());
        // Producer.publishEvent(adminEvent.getOperationType().toString(), adminEvent.getAuthDetails().getUserId());

        // TODO: add config support https://github.com/athenacykes/keycloak-kafka-eventlistener
        //		KafkaEventListenerConfig kafkaConfig = new KafkaEventListenerConfig();
        //		Properties properties = kafkaConfig.getProperties();
        // String topicName = properties.getProperty(KafkaEventListenerConfig.KAFKA_CONFIG_TOPIC_NAME_ADMIN);

        ObjectMapper eventMapper = new ObjectMapper();
        JsonNode eventJson = eventMapper.convertValue(adminEvent, JsonNode.class);

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
