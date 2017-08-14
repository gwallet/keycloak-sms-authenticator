package six.six.keycloak.authenticator;

import com.amazonaws.services.sns.model.PublishResult;
import org.jboss.logging.Logger;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.UserModel;
import six.six.aws.snsclient.SnsNotificationService;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;

/**
 * Created by joris on 18/11/2016.
 */
public class KeycloakSmsAuthenticatorUtil {

    private static Logger logger = Logger.getLogger(KeycloakSmsAuthenticatorUtil.class);

    public static String getAttributeValue(UserModel user, String attributeName) {
        String result = null;
        List<String> values = user.getAttribute(attributeName);
        if (values != null && values.size() > 0) {
            result = values.get(0);
        }

        return result;
    }

    public static String getConfigString(AuthenticatorConfigModel config, String configName) {
        return getConfigString(config, configName, null);
    }

    public static String getConfigString(AuthenticatorConfigModel config, String configName, String defaultValue) {

        String value = defaultValue;

        if (config.getConfig() != null) {
            // Get value
            value = config.getConfig().get(configName);
        }

        return value;
    }

    public static Long getConfigLong(AuthenticatorConfigModel config, String configName) {
        return getConfigLong(config, configName, null);
    }

    public static Long getConfigLong(AuthenticatorConfigModel config, String configName, Long defaultValue) {

        Long value = defaultValue;

        if (config.getConfig() != null) {
            // Get value
            Object obj = config.getConfig().get(configName);
            try {
                value = Long.valueOf((String) obj); // s --> ms
            } catch (NumberFormatException nfe) {
                logger.error("Can not convert " + obj + " to a number.");
            }
        }

        return value;
    }

    public static String createMessage(String code, String mobileNumber, AuthenticatorConfigModel config) {
        String text = KeycloakSmsAuthenticatorUtil.getConfigString(config, KeycloakSmsAuthenticatorConstants.CONF_PRP_SMS_TEXT);
        text = text.replaceAll("%sms-code%", code);
        text = text.replaceAll("%phonenumber%", mobileNumber);

        return text;
    }

    public static boolean isNotEmpty(String s) {
        return (s != null && s.length() > 0);
    }

    public static String setDefaultCountryCodeIfZero(String mobileNumber) {
        if (mobileNumber.startsWith("07")) {
            mobileNumber = "+44" + mobileNumber.substring(1);
        }

        return mobileNumber;
    }

    static boolean sendSmsCode(String mobileNumber, String code, AuthenticatorConfigModel config) {
        // Send an SMS
        KeycloakSmsAuthenticatorUtil.logger.debug("Sending " + code + "  to mobileNumber " + mobileNumber);

        String smsUsr = getConfigString(config, KeycloakSmsAuthenticatorConstants.CONF_PRP_SMS_CLIENTTOKEN);
        String smsPwd = getConfigString(config, KeycloakSmsAuthenticatorConstants.CONF_PRP_SMS_CLIENTSECRET);

        String smsText = createMessage(code, mobileNumber, config);
        try {
            PublishResult send_result = new SnsNotificationService().send(setDefaultCountryCodeIfZero(mobileNumber), smsText, smsUsr, smsPwd);
            return true;
       } catch(Exception e) {
            //Just like pokemon
            return false;
        }
    }

    private static String getPath(String mobileNumber, URL smsURL, String smsText) throws UnsupportedEncodingException {
        String path = smsURL.getPath();
        if (smsURL.getQuery() != null && smsURL.getQuery().length() > 0) {
            path += smsURL.getQuery();
        }
        path = path.replaceFirst("\\{message\\}", URLEncoder.encode(smsText, "UTF-8"));
        path = path.replaceFirst("\\{phonenumber\\}", URLEncoder.encode(mobileNumber, "UTF-8"));
        return path;
    }

    static String getSmsCode(long nrOfDigits) {
        if (nrOfDigits < 1) {
            throw new RuntimeException("Number of digits must be bigger than 0");
        }

        double maxValue = Math.pow(10.0, nrOfDigits); // 10 ^ nrOfDigits;
        Random r = new Random();
        long code = (long) (r.nextFloat() * maxValue);
        return Long.toString(code);
    }
}
