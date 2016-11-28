package com.alliander.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Random;

/**
 * Created by joris on 11/11/2016.
 */
public class KeycloakSmsAuthenticator implements Authenticator {

    private static Logger logger = Logger.getLogger(KeycloakSmsAuthenticator.class);

    public static final String CREDENTIAL_TYPE = "sms_validation";

    private static enum CODE_STATUS {
        VALID,
        INVALID,
        EXPIRED
    }

//    private static Map<String, SMSCode> smsCodes = new ConcurrentHashMap<String, SMSCode>();


//    public KeycloakSmsAuthenticator() {
//        Thread t = new Thread(new Runnable() {
//            public void run() {
//                while (true) {
//                    try {
//                        Thread.sleep(60 * 1000);
//                    } catch (InterruptedException ex) {
//                    }
//                    cleanUpMap();
//                }
//            }
//        });
//
//        t.setDaemon(true);
//        t.start();
//    }


    public void authenticate(AuthenticationFlowContext context) {
        logger.info("authenticate called ... context = " + context);

        String mobileNumber = SMSAuthenticatorUtil.getAttributeValue(context.getUser(), SMSAuthenticatorContstants.ATTR_MOBILE);
        if(mobileNumber != null) {
            // The mobile number is configured --> send an SMS
            AuthenticatorConfigModel config = context.getAuthenticatorConfig();

            long nrOfDigits = SMSAuthenticatorUtil.getConfigLong(config, SMSAuthenticatorContstants.CONF_PRP_SMS_CODE_LENGTH, 8L);
            logger.info("Using nrOfDigits " + nrOfDigits);


            long ttl = SMSAuthenticatorUtil.getConfigLong(config, SMSAuthenticatorContstants.CONF_PRP_SMS_CODE_TTL, 10 * 60L); // 10 minutes in s

            logger.info("Using ttl " + ttl + " (s)");

            String code = getSmsCode(nrOfDigits);

            storeSMSCode(context, code, new Date().getTime() + (ttl * 1000)); // s --> ms
            if (sendSmsCode(mobileNumber, code)) {
                Response challenge = context.form().createForm("sms-validation.ftl");
                context.challenge(challenge);
            } else {
                Response challenge =  context.form()
                        .setError("SMS could not be sent.")
                        .createForm("sms_validation_missing_mobile.ftl");
                context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
                return;
            }
        } else {
            // The mobile number is NOT configured --> complain
            Response challenge =  context.form()
                    .setError("Missing mobile number")
                    .createForm("sms_validation_missing_mobile.ftl");
            context.failureChallenge(AuthenticationFlowError.CLIENT_CREDENTIALS_SETUP_REQUIRED, challenge);
            return;
        }
    }


    public void action(AuthenticationFlowContext context) {
        logger.info("action called ... context = " + context);
        CODE_STATUS status = validateCode(context);
        Response challenge = null;
        switch (status) {
            case EXPIRED:
                challenge =  context.form()
                        .setError("badCode")
                        .createForm("sms-validation.ftl");
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
                break;

            case INVALID:
                challenge =  context.form()
                        .setError("badCode")
                        .createForm("sms-validation.ftl");
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
                break;

            case VALID:
                context.success();
                break;

        }
    }

    private void storeSMSCode(AuthenticationFlowContext context, String code, Long expiringAt) {
        UserCredentialModel credentials = new UserCredentialModel();
        credentials.setType(SMSAuthenticatorContstants.USR_CRED_MDL_SMS_CODE);
        credentials.setValue(code);
        context.getSession().users().updateCredential(context.getRealm(), context.getUser(), credentials);

        credentials.setType(SMSAuthenticatorContstants.USR_CRED_MDL_SMS_EXP_TIME);
        credentials.setValue((expiringAt).toString());
        context.getSession().users().updateCredential(context.getRealm(), context.getUser(), credentials);
    }

    protected CODE_STATUS validateCode(AuthenticationFlowContext context) {
        CODE_STATUS result = CODE_STATUS.INVALID;

        logger.info("validateCode called ... ");
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String enteredCode = formData.getFirst(SMSAuthenticatorContstants.ANSW_SMS_CODE);

        String expectedCode = SMSAuthenticatorUtil.getCredentialValue(context.getUser(), SMSAuthenticatorContstants.USR_CRED_MDL_SMS_CODE);
        String expTimeString = SMSAuthenticatorUtil.getCredentialValue(context.getUser(), SMSAuthenticatorContstants.USR_CRED_MDL_SMS_EXP_TIME);

        logger.info("Expected code = " + expectedCode + "    entered code = " + enteredCode);

        if(expectedCode != null) {
            result = enteredCode.equals(expectedCode) ? CODE_STATUS.VALID : CODE_STATUS.INVALID;
            long now = new Date().getTime();

            logger.info("Valid code expires in " + (Long.parseLong(expTimeString) - now) + " ms");
            if(result == CODE_STATUS.VALID) {
                if (Long.parseLong(expTimeString) < now) {
                    logger.info("Code is expired !!");
                    result = CODE_STATUS.EXPIRED;
                }
            }
        }
        logger.info("result : " + result);
        return result;
    }

    public boolean requiresUser() {
        logger.info("requiresUser called ... returning true");
        return true;
    }

    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.info("configuredFor called ... session=" + session + ", realm=" + realm + ", user=" + user);
        boolean result = true;
        logger.info("... returning "  +result);
        return result;
    }

    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.info("setRequiredActions called ... session=" + session + ", realm=" + realm + ", user=" + user);
    }

    public void close() {
        logger.info("close called ...");
    }


    private String getSmsCode(long nrOfDigits) {
        if(nrOfDigits < 1) {
            throw new RuntimeException("Nr of digits must be bigger than 0");
        }

        double maxValue = Math.pow(10.0, nrOfDigits - 1.0); // 10 ^ nrOfDigits - 1;
        Random r = new Random();
        long code = (long)(r.nextFloat() * maxValue);
        return Long.toString(code);
    }

    private boolean sendSmsCode(String mobileNumber, String code) {
        // Send an SMS
        if(logger.isInfoEnabled()) { logger.info("Should send an SMS"); }
        logger.info("Sending " + code + "  to mobileNumber " + mobileNumber);
        return true;
    }

}
