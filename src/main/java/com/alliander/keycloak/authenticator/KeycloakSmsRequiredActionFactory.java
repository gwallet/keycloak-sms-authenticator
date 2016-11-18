package com.alliander.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Created by joris on 14/11/2016.
 */
public class KeycloakSmsRequiredActionFactory implements RequiredActionFactory {

    private static Logger logger = Logger.getLogger(KeycloakSmsRequiredActionFactory.class);
    private static final KeycloakSmsRequiredAction SINGLETON = new KeycloakSmsRequiredAction();

    public RequiredActionProvider create(KeycloakSession session) {
        logger.debug("create called ...");
        return SINGLETON;
    }

    public String getId() {
        logger.debug("getId called ... returning " + KeycloakSmsRequiredAction.PROVIDER_ID);
        return KeycloakSmsRequiredAction.PROVIDER_ID;
    }

    public String getDisplayText() {
        logger.debug("getDisplayText called ...");
        return "SMS Authentication";
    }

    public void init(Config.Scope config) {
        logger.debug("init called ...");
    }

    public void postInit(KeycloakSessionFactory factory) {
        logger.debug("postInit called ...");
    }

    public void close() {
        logger.debug("getId close ...");
    }


}
