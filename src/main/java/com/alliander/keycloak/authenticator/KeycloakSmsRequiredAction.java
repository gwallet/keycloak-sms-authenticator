package com.alliander.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserCredentialValueModel;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by joris on 14/11/2016.
 */
public class KeycloakSmsRequiredAction implements RequiredActionProvider {

    private static Logger logger = Logger.getLogger(KeycloakSmsRequiredAction.class);
    public static final String PROVIDER_ID = "sms_auth_verify_code";

    public void evaluateTriggers(RequiredActionContext context) {
        logger.debug("evaluateTriggers called ...");
    }

    public void requiredActionChallenge(RequiredActionContext context) {
        logger.debug("requiredActionChallenge called ...");

        Response challenge = context.form().createForm("sms_validtion_config.ftl");
        context.challenge(challenge);
//        List<String> mobileNumberList = context.getUser().getAttribute("mobile");

    }

    public void processAction(RequiredActionContext context) {
        logger.debug("processAction called ...");


        String answer = (context.getHttpRequest().getDecodedFormParameters().getFirst("phonenumber"));
        if(answer != null && answer.length() > 0) {
            UserCredentialValueModel model = new UserCredentialValueModel();
            model.setValue(answer);
            model.setType(KeycloakSmsAuthenticator.CREDENTIAL_TYPE);
            context.getUser().updateCredentialDirectly(model);
            context.success();
        } else {
            context.failure();
        }
    }

    public void close() {
        logger.debug("close called ...");
    }
}
