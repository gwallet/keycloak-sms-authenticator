package six.six.gateway.lyrasms;

import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import six.six.gateway.SMSService;
import six.six.keycloak.KeycloakSmsConstants;

import java.util.Optional;

/**
 * LyraSMS Service implementation
 */
public class LyraSMSService implements SMSService {

    private static Logger logger = Logger.getLogger(LyraSMSService.class);


    final private static String OPTION = "01100";
    final private static String ACTION = "add";
    final private static String FORWARD = "smsIsSent";
    final private static String DEADLINE = "null";

    private String url;
    private LyraSMSRestService remoteService;

    public LyraSMSService(String url, Boolean proxyOn) {
        this.url = url;
        this.remoteService = buildClient(url, proxyOn);
    }

    private static LyraSMSRestService buildClient(String uri, Boolean proxyOn) {
        String portTemp = Optional.ofNullable(System.getProperty("http." + KeycloakSmsConstants.PROXY_PORT))
                .filter(s -> s != null && !s.isEmpty()).orElse(System.getProperty("https." + KeycloakSmsConstants.PROXY_PORT));

        final String host = Optional.ofNullable(System.getProperty("http." + KeycloakSmsConstants.PROXY_HOST))
                .filter(s -> s != null && !s.isEmpty()).orElse(System.getProperty("https." + KeycloakSmsConstants.PROXY_HOST));
        final int port = portTemp != null ? Integer.valueOf(portTemp) : 8080;
        final String scheme = System.getProperty("http." + KeycloakSmsConstants.PROXY_HOST) != null ? "http" : "https";

        ResteasyClientBuilder builder = new ResteasyClientBuilder();

        if (proxyOn) {
            builder.defaultProxy(host, port, scheme);
        }

        ResteasyClient client = builder.disableTrustManager().build();
        ResteasyWebTarget target = client.target(uri);

        return target
                .proxyBuilder(LyraSMSRestService.class)
                .classloader(LyraSMSRestService.class.getClassLoader())
                .build();

    }

    public boolean send(String phoneNumber, String message, String login, String pw) {
        boolean result;
        if (phoneNumber != null) {
            //Support only this format 3367...
            phoneNumber = phoneNumber.replace("+", "");
        }

        String resultM = this.remoteService.send(login, pw, phoneNumber, message, OPTION,DEADLINE,null,ACTION,FORWARD,null,null);
        result = resultM.indexOf("status=0") > -1;

        if (!result) {
            logger.error("Fail to send SMS by LyraSMS: " + resultM );
        }
        return result;
    }
}
