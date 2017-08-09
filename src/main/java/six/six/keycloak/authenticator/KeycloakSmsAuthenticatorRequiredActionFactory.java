package six.six.keycloak.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Created by nickpack on 09/08/2017.
 */
public class KeycloakSmsAuthenticatorRequiredActionFactory implements RequiredActionFactory {

    private static final KeycloakSmsAuthenticatorRequiredAction SINGLETON = new KeycloakSmsAuthenticatorRequiredAction();

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return SINGLETON;
    }


    @Override
    public String getId() {
        return KeycloakSmsAuthenticatorRequiredAction.PROVIDER_ID;
    }

    @Override
    public String getDisplayText() {
        return "SMS Code";
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }
}
