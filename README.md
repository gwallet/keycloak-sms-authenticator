# keycloak-sms-authenticator


KEYCLOAK_HOME/bin/
/Users/joris/tools/keycloak-2.2.1.Final/bin/jboss-cli.sh --command="module add --name=com.alliander.keycloak.authenticator.sms-authenticator --resources=target/keycloak-sms-authenticator.jar --dependencies=org.keycloak.keycloak-core,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-services,org.jboss.resteasy.resteasy-jaxrs,javax.ws.rs.api"



cp target/keycloak-sms-authenticator.jar /Users/joris/tools/keycloak-2.2.1.Final/providers/



cp sms-validation.ftl /Users/joris/tools/keycloak-2.2.1.Final/themes/base/login/
cp sms_validtion_config.ftl /Users/joris/tools/keycloak-2.2.1.Final/themes/base/login/

cp sms_validtion_missing_mobile.ftl /Users/joris/tools/keycloak-2.2.1.Final/themes/base/login/
