package six.six.keycloak.authenticator;

import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

/**
 * Created by nickpack on 09/08/2017.
 */
public class KeycloakSmsAuthenticatorCredentialProviderFactory implements CredentialProviderFactory<KeycloakSmsAuthenticatorCredentialProvider> {
    @Override
    public String getId() {
        return "smsCode";
    }

    @Override
    public CredentialProvider create(KeycloakSession session) {
        return new KeycloakSmsAuthenticatorCredentialProvider(session);
    }
}
