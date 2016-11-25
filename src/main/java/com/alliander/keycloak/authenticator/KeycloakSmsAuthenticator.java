package com.alliander.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by joris on 11/11/2016.
 */
public class KeycloakSmsAuthenticator implements Authenticator {

    private static Logger logger = Logger.getLogger(KeycloakSmsAuthenticator.class);

    public static final String CREDENTIAL_TYPE = "sms_validation";

    private static Map<String, SMSCode> smsCodes = new ConcurrentHashMap<String, SMSCode>();


    public KeycloakSmsAuthenticator() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException ex) {
                    }
                    cleanUpMap();
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }


    public void authenticate(AuthenticationFlowContext context) {
        logger.info("authenticate called ... context = " + context);

        String mobileNumber = SMSAuthenticatorUtil.getAttributeValue(context.getUser(), SMSAuthenticatorContstants.ATTR_MOBILE);
        if(mobileNumber != null) {
            // The mobile number is configured --> send an SMS
            String code = getSmsCode();

            AuthenticatorConfigModel config = context.getAuthenticatorConfig();
            long ttl = 15 * 60 * 1000; // 15 minutes in ms
            if(config.getConfig() != null) {
                // GET TTL in seconds
                String ttlString = config.getConfig().get(SMSAuthenticatorContstants.CONF_PRP_SMS_CODE_TTL);
                try {
                    ttl = Long.valueOf(ttlString) * 1000;
                } catch (NumberFormatException nfe) {
                    logger.error("Can not convert " + ttlString + " to a number.");
                }
            }
            logger.info("Using ttl " + ttl + " (ms)");

            smsCodes.put(mobileNumber, new SMSCode(code, new Date().getTime() + ttl));
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
        boolean result = false;

        logger.info("validateAnswer called ... context = " + context);
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String secret = formData.getFirst(SMSAuthenticatorContstants.ANSW_SMS_CODE);

        String mobileNumber = SMSAuthenticatorUtil.getAttributeValue(context.getUser(), SMSAuthenticatorContstants.ATTR_MOBILE);

        SMSCode expectedCode = smsCodes.get(mobileNumber);

        logger.info("Expected code = " + expectedCode.getCode() + "    entered code = " + secret);
        logger.info("... returning " + secret.equals(expectedCode));

        if(expectedCode != null) {
            result = secret.equals(expectedCode.getCode());
            logger.info("result : " + result);

            logger.info("expectedCode.expirationTime : " + expectedCode.expirationTime);
            logger.info("new Date().getTime()        : " + new Date().getTime());
            if(result) {
                if (expectedCode.expirationTime < new Date().getTime()) {
                    logger.info("Code is expired !!");
                    result = false;
                }
                logger.info("Removing code for " + mobileNumber);
                smsCodes.remove(mobileNumber);
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
//        boolean result = session.users().configuredForCredentialType(CREDENTIAL_TYPE, realm, user);
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


    public void cleanUpMap() {
        logger.info("cleanUpMap called ...");
        final long expirationTime = new Date().getTime();
        for(Iterator<String> iter = smsCodes.keySet().iterator(); iter.hasNext(); ) {
            String key = iter.next();
            if(smsCodes.get(key) != null && smsCodes.get(key).getExpirationTime() < expirationTime) {
                logger.info("removing smsCode for " + key);
                smsCodes.remove(key);
            }
        }
    }


    private class SMSCode {
        private String code;
        private long expirationTime;

        public SMSCode(String code, long expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
        }
    }

}
