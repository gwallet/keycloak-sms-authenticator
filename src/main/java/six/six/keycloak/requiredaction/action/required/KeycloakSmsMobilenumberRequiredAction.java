package six.six.keycloak.requiredaction.action.required;

import org.jboss.logging.Logger;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;
import six.six.keycloak.KeycloakSmsConstants;
import six.six.keycloak.authenticator.KeycloakSmsAuthenticatorUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

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

        List<String> mobileNumberCreds = user.getAttribute(KeycloakSmsConstants.ATTR_MOBILE);

        String mobileNumber = null;

        if (mobileNumberCreds != null && !mobileNumberCreds.isEmpty()) {
            mobileNumber = mobileNumberCreds.get(0);
        }

        if (mobileNumber != null && validateTelephoneNumber(mobileNumber, KeycloakSmsAuthenticatorUtil.getMessage(context, KeycloakSmsConstants.MSG_MOBILE_REGEXP))) {
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
        if (answer != null && answer.length() > 0 && answer.equals(answer2) && validateTelephoneNumber(answer,KeycloakSmsAuthenticatorUtil.getMessage(context, KeycloakSmsConstants.MSG_MOBILE_REGEXP))) {
            logger.debug("Valid matching mobile numbers supplied, save credential ...");
            List<String> mobileNumber = new ArrayList<String>();
            mobileNumber.add(answer);

            UserModel user = context.getUser();
            user.setAttribute(KeycloakSmsConstants.ATTR_MOBILE, mobileNumber);

            context.success();
        } else if (answer != null && answer2 !=null && !answer.equals(answer2)) {
            logger.debug("Supplied mobile number values do not match...");
            Response challenge = context.form()
                    .setError("mobile_number.no.match")
                    .createForm("sms-validation-mobile-number.ftl");
            context.challenge(challenge);
        } else {
            logger.debug("Either one of two fields wasn\'t complete, or the first contains an invalid number...");
            Response challenge = context.form()
                    .setError("mobile_number.no.valid")
                    .createForm("sms-validation-mobile-number.ftl");
            context.challenge(challenge);
        }
    }

    public void close() {
        logger.debug("close called ...");
    }
}
