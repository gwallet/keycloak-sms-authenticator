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
public class KeycloakSmsRequiredMobileNumberFactory implements RequiredActionFactory {

    private static Logger logger = Logger.getLogger(KeycloakSmsRequiredMobileNumberFactory.class);
    private static final KeycloakSmsRequiredMobileNumber SINGLETON = new KeycloakSmsRequiredMobileNumber();

    public RequiredActionProvider create(KeycloakSession session) {
        logger.debug("create called ...");
        return SINGLETON;
    }

    public String getId() {
        logger.debug("getId called ... returning " + KeycloakSmsRequiredMobileNumber.PROVIDER_ID);
        return KeycloakSmsRequiredMobileNumber.PROVIDER_ID;
    }

    public String getDisplayText() {
        logger.debug("getDisplayText called ...");
        return "SMS Auth. mobile nr check";
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
