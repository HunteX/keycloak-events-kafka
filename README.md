# Keycloak: Event Listener SPI & Publish events to Kafka

# Setup

Kafka running at port 9092 or update in Producer.java
`BOOTSTRAP_SERVER = "127.0.0.1:9092"`

## build 
`mvn clean install`

## Deploy to Keycloak instance
`kubectl cp keycloak-spi-kafka.jar keycloak/keycloak-0:/opt/jboss/keycloak/standalone/deployments/keycloak-spi-kafka.jar`