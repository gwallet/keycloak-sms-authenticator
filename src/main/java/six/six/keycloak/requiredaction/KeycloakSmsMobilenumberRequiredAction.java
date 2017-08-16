package six.six.keycloak.requiredaction;

import org.jboss.logging.Logger;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import six.six.keycloak.authenticator.KeycloakSmsAuthenticatorConstants;
import six.six.keycloak.authenticator.KeycloakSmsAuthenticatorUtil;

import javax.ws.rs.core.Response;

import static six.six.keycloak.authenticator.KeycloakSmsAuthenticatorUtil.validateTelephoneNumber;

/**
 * Created by nickpack on 15/08/2017.
 */
public class KeycloakSmsMobilenumberRequiredAction implements RequiredActionProvider {
    private static Logger logger = Logger.getLogger(KeycloakSmsMobilenumberRequiredAction.class);
    public static final String PROVIDER_ID = "sms_auth_check_mobile";

    public void evaluateTriggers(RequiredActionContext context) {
        logger.debug("evaluateTriggers called ...");
    }

    public void requiredActionChallenge(RequiredActionContext context) {
        logger.debug("requiredActionChallenge called ...");

        UserModel user = context.getUser();
        String mobileNumber = KeycloakSmsAuthenticatorUtil.getAttributeValue(user, KeycloakSmsAuthenticatorConstants.ATTR_MOBILE);

        if (mobileNumber != null && validateTelephoneNumber(mobileNumber)) {
            // Mobile number is configured
            context.ignore();
        } else {
            // Mobile number is not configured or is invalid
            Response challenge = context.form().createForm("sms-validation-mobile-number.ftl");
            context.challenge(challenge);
        }
    }

    public void processAction(RequiredActionContext context) {
        logger.debug("processAction called ...");

        String answer = (context.getHttpRequest().getDecodedFormParameters().getFirst("mobile_number"));
        String answer2 = (context.getHttpRequest().getDecodedFormParameters().getFirst("mobile_number_confirm"));
        if (answer != null && answer.length() > 0 && answer.equals(answer2) && validateTelephoneNumber(answer)) {
            UserCredentialModel input = new UserCredentialModel();
            input.setType(KeycloakSmsAuthenticatorConstants.ATTR_MOBILE);
            input.setValue(answer);
            context.getSession().userCredentialManager().updateCredential(context.getRealm(), context.getUser(), input);
            context.success();
        } else {
            context.failure();
        }
    }

    public void close() {
        logger.debug("close called ...");
    }
}
