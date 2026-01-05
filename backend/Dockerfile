FROM quay.io/wildfly/wildfly:38.0.1.Final-jdk21

# Copy the WAR file to the standalone deployment directory
COPY target/secureteam-access.war /opt/jboss/wildfly/standalone/deployments/

# Expose HTTP and Admin ports
EXPOSE 8080 9990

# Start WildFly in standalone mode
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
