package six.six.keycloak.authenticator;


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.UserModel;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeProvider;
import six.six.gateway.Gateways;
import six.six.gateway.SMSService;
import six.six.gateway.aws.snsclient.SnsNotificationService;
import six.six.gateway.lyrasms.LyraSMSService;
import six.six.keycloak.EnvSubstitutor;
import six.six.keycloak.KeycloakSmsConstants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
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

    public static Boolean getConfigBoolean(AuthenticatorConfigModel config, String configName) {
        return getConfigBoolean(config, configName, true);
    }

    public static Boolean getConfigBoolean(AuthenticatorConfigModel config, String configName, Boolean defaultValue) {

        Boolean value = defaultValue;

        if (config.getConfig() != null) {
            // Get value
            Object obj = config.getConfig().get(configName);
            try {
                value = Boolean.valueOf((String) obj); // s --> ms
            } catch (NumberFormatException nfe) {
                logger.error("Can not convert " + obj + " to a boolean.");
            }
        }

        return value;
    }

    public static String createMessage(String text,String code, String mobileNumber) {
        if(text !=null){
            text = text.replaceAll("%sms-code%", code);
            text = text.replaceAll("%phonenumber%", mobileNumber);
        }
        return text;
    }

    public static String setDefaultCountryCodeIfZero(String mobileNumber,String prefix ,String condition) {

        if (prefix!=null && condition!=null && mobileNumber.startsWith(condition)) {
            mobileNumber = prefix + mobileNumber.substring(1);
        }
        return mobileNumber;
    }

    /**
     * Check mobile number normative strcuture
     * @param mobileNumber
     * @return formatted mobile number
     */
    public static String checkMobileNumber(String mobileNumber) {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phone = phoneUtil.parse(mobileNumber, null);
            mobileNumber = phoneUtil.format(phone,
                    PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            logger.error("Invalid phone number " + mobileNumber, e);
        }

        return mobileNumber;
    }


    public static String getMessage(AuthenticationFlowContext context, String key){
        String result=null;
        try {
            ThemeProvider themeProvider = context.getSession().getProvider(ThemeProvider.class, "extending");
            Theme currentTheme = themeProvider.getTheme(context.getRealm().getLoginTheme(), Theme.Type.LOGIN);
            Locale locale = context.getSession().getContext().resolveLocale(context.getUser());
            result = currentTheme.getMessages(locale).getProperty(key);
        }catch (IOException e){
            logger.warn(key + "not found in messages");
        }
        return result;
    }

    public static String getMessage(RequiredActionContext context, String key){
        String result=null;
        try {
            ThemeProvider themeProvider = context.getSession().getProvider(ThemeProvider.class, "extending");
            Theme currentTheme = themeProvider.getTheme(context.getRealm().getLoginTheme(), Theme.Type.LOGIN);
            Locale locale = context.getSession().getContext().resolveLocale(context.getUser());
            result = currentTheme.getMessages(locale).getProperty(key);
        }catch (IOException e){
            logger.warn(key + "not found in messages");
        }
        return result;
    }


    static boolean sendSmsCode(String mobileNumber, String code, AuthenticationFlowContext context) {
        final AuthenticatorConfigModel config = context.getAuthenticatorConfig();

        // Send an SMS
        KeycloakSmsAuthenticatorUtil.logger.debug("Sending " + code + "  to mobileNumber " + mobileNumber);

        String smsUsr = EnvSubstitutor.envSubstitutor.replace(getConfigString(config, KeycloakSmsConstants.CONF_PRP_SMS_CLIENTTOKEN));
        String smsPwd = EnvSubstitutor.envSubstitutor.replace(getConfigString(config, KeycloakSmsConstants.CONF_PRP_SMS_CLIENTSECRET));
        String gateway = getConfigString(config, KeycloakSmsConstants.CONF_PRP_SMS_GATEWAY);
        String endpoint = EnvSubstitutor.envSubstitutor.replace(getConfigString(config, KeycloakSmsConstants.CONF_PRP_SMS_GATEWAY_ENDPOINT));
        boolean isProxy = getConfigBoolean(config, KeycloakSmsConstants.PROXY_ENABLED);

        String template =getMessage(context, KeycloakSmsConstants.CONF_PRP_SMS_TEXT);

        String smsText = createMessage(template,code, mobileNumber);
        boolean result;
        SMSService smsService;
        try {
            Gateways g=Gateways.valueOf(gateway);
            switch(g) {
                case LYRA_SMS:
                    smsService=new LyraSMSService(endpoint,isProxy);
                    break;
                default:
                    smsService=new SnsNotificationService();
            }

            result=smsService.send(checkMobileNumber(setDefaultCountryCodeIfZero(mobileNumber, getMessage(context, KeycloakSmsConstants.MSG_MOBILE_PREFIX_DEFAULT), getMessage(context, KeycloakSmsConstants.MSG_MOBILE_PREFIX_CONDITION))), smsText, smsUsr, smsPwd);
          return result;
       } catch(Exception e) {
            logger.error("Fail to send SMS " ,e );
            return false;
        }
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

    public static boolean validateTelephoneNumber(String telephoneNumber, String regexp ) {
        boolean result=true;
        if(regexp!=null){
            result =telephoneNumber.matches(regexp);
        }

        return result;
    }
}
