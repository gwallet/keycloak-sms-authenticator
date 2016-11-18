package com.alliander.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by joris on 11/11/2016.
 */
public class KeycloakSmsAuthenticator implements Authenticator {

    private static Logger logger = Logger.getLogger(KeycloakSmsAuthenticator.class);

    public static final String CREDENTIAL_TYPE = "sms_validation";

    private static Map<String, String> smsCodes = new ConcurrentHashMap<String, String>();


    public void authenticate(AuthenticationFlowContext context) {
        logger.info("authenticate called ... context = " + context);

        String mobileNumber = SMSAuthenticatorUtil.getAttributeValue(context.getUser(), SMSAuthenticatorContstants.ATTR_MOBILE);
        if(mobileNumber != null) {
            // The mobile number is configured --> send an SMS

            String code = getSmsCode();
            smsCodes.put(mobileNumber, code);
            if (sendSmsCode(mobileNumber, code)) {

                Response challenge = context.form().createForm("sms-validation.ftl");
                context.challenge(challenge);
            }
        } else {
            // The mobile number is NOT configured --> complain

            Response challenge =  context.form()
                    .setError("Missing mobile number")
                    .createForm("sms_validtion_missing_mobile.ftl");
            context.failureChallenge(AuthenticationFlowError.CLIENT_CREDENTIALS_SETUP_REQUIRED, challenge);
            return;
        }
    }


    public void action(AuthenticationFlowContext context) {
        logger.info("action called ... context = " + context);
        boolean validated = validateAnswer(context);
        if (!validated) {
            Response challenge =  context.form()
                    .setError("badCode")
                    .createForm("sms-validation.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }
        context.success();
    }


    protected boolean validateAnswer(AuthenticationFlowContext context) {
        logger.info("validateAnswer called ... context = " + context);
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String secret = formData.getFirst(SMSAuthenticatorContstants.ANSW_SMS_CODE);

        String mobileNumber = SMSAuthenticatorUtil.getAttributeValue(context.getUser(), SMSAuthenticatorContstants.ATTR_MOBILE);

        String expectedCode = smsCodes.get(mobileNumber);
        logger.info("Expected code = " + expectedCode + "    entered code = " + secret);
        logger.info("... returning " + secret.equals(expectedCode));

        return secret.equals(expectedCode);
    }

    public boolean requiresUser() {
        logger.info("requiresUser called ... returning true");
        return true;
    }

    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.info("configuredFor called ... session=" + session + ", realm=" + realm + ", user=" + user);
//        boolean result = true;
        boolean result = session.users().configuredForCredentialType(CREDENTIAL_TYPE, realm, user);
        logger.info("... returning "  +result);
        return result;
    }

    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.info("setRequiredActions called ... session=" + session + ", realm=" + realm + ", user=" + user);
    }

    public void close() {
        logger.info("close called ...");
    }


    private String getSmsCode() {
        Random r = new Random ();
        return Long.toString (r.nextLong (), 36).replaceAll("-", "");
    }

    private boolean sendSmsCode(String mobileNumber, String code) {
        // Send an SMS
        if(logger.isInfoEnabled()) { logger.info("Should send an SMS"); }
        logger.info("Sending " + code + "  to mobileNumber " + mobileNumber);
        return true;
    }

}
