package six.six.keycloak.requiredaction;

import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

/**
 * Created by nickpack on 15/08/2017.
 */
public class KeycloakSmsMobilenumberCredentialProviderFactory implements CredentialProviderFactory<KeycloakSmsMobilenumberCredentialProvider> {
    @Override
    public String getId() {
        return "mobile_number";
    }

    @Override
    public CredentialProvider create(KeycloakSession session) {
        return new KeycloakSmsMobilenumberCredentialProvider(session);
    }

}
