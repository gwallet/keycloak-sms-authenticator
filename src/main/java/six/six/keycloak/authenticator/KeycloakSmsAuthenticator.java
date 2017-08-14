package six.six.keycloak.authenticator;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Created by joris on 11/11/2016.
 */
public class KeycloakSmsAuthenticator implements Authenticator {

    private static Logger logger = Logger.getLogger(KeycloakSmsAuthenticator.class);

    public static final String CREDENTIAL_TYPE = "sms_validation";

    private enum CODE_STATUS {
        VALID,
        INVALID,
        EXPIRED
    }


    public void authenticate(AuthenticationFlowContext context) {
        logger.debug("authenticate called ... context = " + context);

        AuthenticatorConfigModel config = context.getAuthenticatorConfig();

        String mobileNumberAttribute = KeycloakSmsAuthenticatorUtil.getConfigString(config, KeycloakSmsAuthenticatorConstants.CONF_PRP_USR_ATTR_MOBILE);
        if (mobileNumberAttribute == null) {
            logger.error("Mobile number attribute is not configured for the SMS Authenticator.");
            Response challenge = context.form()
                    .setError("Mobile number can not be determined.")
                    .createForm("sms-validation-error.ftl");
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }

        String mobileNumber = KeycloakSmsAuthenticatorUtil.getAttributeValue(context.getUser(), mobileNumberAttribute);
        if (mobileNumber != null) {
            // The mobile number is configured --> send an SMS
            long nrOfDigits = KeycloakSmsAuthenticatorUtil.getConfigLong(config, KeycloakSmsAuthenticatorConstants.CONF_PRP_SMS_CODE_LENGTH, 8L);
            logger.debug("Using nrOfDigits " + nrOfDigits);


            long ttl = KeycloakSmsAuthenticatorUtil.getConfigLong(config, KeycloakSmsAuthenticatorConstants.CONF_PRP_SMS_CODE_TTL, 10 * 60L); // 10 minutes in s

            logger.debug("Using ttl " + ttl + " (s)");

            String code = KeycloakSmsAuthenticatorUtil.getSmsCode(nrOfDigits);

            storeSMSCode(context, code, new Date().getTime() + (ttl * 1000)); // s --> ms
            if (KeycloakSmsAuthenticatorUtil.sendSmsCode(mobileNumber, code, context.getAuthenticatorConfig())) {
                Response challenge = context.form().createForm("sms-validation.ftl");
                context.challenge(challenge);
            } else {
                Response challenge = context.form()
                        .setError("SMS could not be sent.")
                        .createForm("sms-validation-error.ftl");
                context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            }
        } else {
            // The mobile number is NOT configured --> complain
            Response challenge = context.form()
                    .setError("Missing mobile number")
                    .createForm("sms-validation-error.ftl");
            context.failureChallenge(AuthenticationFlowError.CLIENT_CREDENTIALS_SETUP_REQUIRED, challenge);
        }
    }


    public void action(AuthenticationFlowContext context) {
        logger.debug("action called ... context = " + context);
        CODE_STATUS status = validateCode(context);
        Response challenge = null;
        switch (status) {
            case EXPIRED:
                challenge = context.form()
                        .setError("code is expired")
                        .createForm("sms-validation.ftl");
                context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, challenge);
                break;

            case INVALID:
                if (context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.OPTIONAL ||
                        context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.ALTERNATIVE) {
                    logger.debug("Calling context.attempted()");
                    context.attempted();
                } else if (context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.REQUIRED) {
                    challenge = context.form()
                            .setError((String) context.getSession().getAttribute(KeycloakSmsAuthenticatorConstants.USR_CRED_MDL_SMS_CODE))
                            .createForm("sms-validation.ftl");
                    context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
                } else {
                    // Something strange happened
                    logger.warn("Undefined execution ...");
                }
                break;

            case VALID:
                context.success();
                break;

        }
    }

    // Store the code + expiration time in a UserCredential. Keycloak will persist these in the DB.
    // When the code is validated on another node (in a clustered environment) the other nodes have access to it's values too.
    private void storeSMSCode(AuthenticationFlowContext context, String code, Long expiringAt) {
        UserCredentialModel credentials = new UserCredentialModel();
        credentials.setType(KeycloakSmsAuthenticatorConstants.USR_CRED_MDL_SMS_CODE);
        credentials.setValue(code);

        context.getSession().userCredentialManager().updateCredential(context.getRealm(), context.getUser(), credentials);

        credentials.setType(KeycloakSmsAuthenticatorConstants.USR_CRED_MDL_SMS_EXP_TIME);
        credentials.setValue((expiringAt).toString());
        context.getSession().userCredentialManager().updateCredential(context.getRealm(), context.getUser(), credentials);
    }


    protected CODE_STATUS validateCode(AuthenticationFlowContext context) {
        CODE_STATUS result = CODE_STATUS.INVALID;

        logger.debug("validateCode called ... ");
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String enteredCode = formData.getFirst(KeycloakSmsAuthenticatorConstants.ANSW_SMS_CODE);
        KeycloakSession session = context.getSession();

        List codeCreds = session.userCredentialManager().getStoredCredentialsByType(context.getRealm(), context.getUser(), KeycloakSmsAuthenticatorConstants.USR_CRED_MDL_SMS_CODE);
        /*List timeCreds = session.userCredentialManager().getStoredCredentialsByType(context.getRealm(), context.getUser(), KeycloakSmsAuthenticatorConstants.USR_CRED_MDL_SMS_EXP_TIME);*/

        CredentialModel expectedCode = (CredentialModel) codeCreds.get(0);
        /*CredentialModel expTimeString = (CredentialModel) timeCreds.get(0);*/

        logger.debug("Expected code = " + expectedCode + "    entered code = " + enteredCode);

        if (expectedCode != null) {
            result = enteredCode.equals(expectedCode.getValue()) ? CODE_STATUS.VALID : CODE_STATUS.INVALID;
            /*long now = new Date().getTime();

            logger.debug("Valid code expires in " + (Long.parseLong(expTimeString.getValue()) - now) + " ms");
            if (result == CODE_STATUS.VALID) {
                if (Long.parseLong(expTimeString.getValue()) < now) {
                    logger.debug("Code is expired !!");
                    result = CODE_STATUS.EXPIRED;
                }
            }*/
        }
        logger.debug("result : " + result);
        return result;
    }

    public boolean requiresUser() {
        logger.debug("requiresUser called ... returning true");
        return true;
    }

    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.debug("configuredFor called ... session=" + session + ", realm=" + realm + ", user=" + user);
        return true;
    }

    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.debug("setRequiredActions called ... session=" + session + ", realm=" + realm + ", user=" + user);
    }

    public void close() {
        logger.debug("close called ...");
    }


    private CredentialsProvider getCredentialsProvider(String smsUsr, String smsPwd, String proxyUsr, String proxyPwd, URL smsURL, URL proxyURL) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();

        // If defined, add BASIC Authentication parameters
        if (KeycloakSmsAuthenticatorUtil.isNotEmpty(smsUsr) && KeycloakSmsAuthenticatorUtil.isNotEmpty(smsPwd)) {
            credsProvider.setCredentials(
                    new AuthScope(smsURL.getHost(), smsURL.getPort()),
                    new UsernamePasswordCredentials(smsUsr, smsPwd));

        }

        // If defined, add Proxy Authentication parameters
        if (KeycloakSmsAuthenticatorUtil.isNotEmpty(proxyUsr) && KeycloakSmsAuthenticatorUtil.isNotEmpty(proxyPwd)) {
            credsProvider.setCredentials(
                    new AuthScope(proxyURL.getHost(), proxyURL.getPort()),
                    new UsernamePasswordCredentials(proxyUsr, proxyPwd));

        }
        return credsProvider;
    }

}
