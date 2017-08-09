package six.six.keycloak.authenticator;

import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserCredentialModel;

import javax.ws.rs.core.Response;

/**
 * Created by nickpack on 09/08/2017.
 */
public class KeycloakSmsAuthenticatorRequiredAction  implements RequiredActionProvider {

    public static final String PROVIDER_ID = "smsCode";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {

    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        Response challenge = context.form().createForm("sms-validation.ftl");
        context.challenge(challenge);

    }

    @Override
    public void processAction(RequiredActionContext context) {
        String answer = (context.getHttpRequest().getDecodedFormParameters().getFirst("smsCode"));
        UserCredentialModel input = new UserCredentialModel();
        input.setType(KeycloakSmsAuthenticatorConstants.ANSW_SMS_CODE);
        input.setValue(answer);
        context.getSession().userCredentialManager().updateCredential(context.getRealm(), context.getUser(), input);
        context.success();
    }

    @Override
    public void close() {

    }
}
