package six.six.keycloak.requiredaction;

import org.jboss.logging.Logger;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;

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

        List<String> mobileNumberCreds = user.getAttribute("mobile_number");

        String mobileNumber = null;

        if (mobileNumberCreds != null && !mobileNumberCreds.isEmpty()) {
            mobileNumber = mobileNumberCreds.get(0);
        }

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
            logger.debug("Valid matching mobile numbers supplied, save credential ...");
            List<String> mobileNumber = new ArrayList<String>();
            mobileNumber.add(answer);

            UserModel user = context.getUser();
            user.setAttribute("mobile_number", mobileNumber);

            context.success();
        } else if (answer != null && answer2 !=null && !answer.equals(answer2)) {
            logger.debug("Supplied mobile number values do not match...");
            Response challenge = context.form()
                    .setError("Entered mobile numbers do not match.")
                    .createForm("sms-validation-mobile-number.ftl");
            context.challenge(challenge);
        } else {
            logger.debug("Either one of two fields wasnt complete, or the first contains an invalid number...");
            Response challenge = context.form()
                    .setError("Please enter a valid UK telephone number.")
                    .createForm("sms-validation-mobile-number.ftl");
            context.challenge(challenge);
        }
    }

    public void close() {
        logger.debug("close called ...");
    }
}
