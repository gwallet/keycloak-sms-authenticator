package six.six.keycloak.requiredaction.credential;

import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

/**
 * Mobile Number Internal management
 * Created by nickpack on 15/08/2017.
 */
public class KeycloakSmsMobilenumberCredentialProviderFactory implements CredentialProviderFactory<six.six.keycloak.requiredaction.credential.KeycloakSmsMobilenumberCredentialProvider> {
    @Override
    public String getId() {
        return "mobile_number";
    }

    @Override
    public CredentialProvider create(KeycloakSession session) {
        return new six.six.keycloak.requiredaction.credential.KeycloakSmsMobilenumberCredentialProvider(session);
    }

}
