package six.six.keycloak;

/**
 * Created by joris on 18/11/2016.
 */
public class KeycloakSmsConstants {
    public static final String ATTR_MOBILE = "mobile_number";
    public static final String ANSW_SMS_CODE = "smsCode";

    public static final String CONF_PRP_SMS_CODE_TTL = "sms-auth.code.ttl";
    public static final String CONF_PRP_SMS_CODE_LENGTH = "sms-auth.code.length";
    public static final String CONF_PRP_SMS_TEXT = "sms-auth.msg.text";

    // Gateway
    public static final String CONF_PRP_SMS_GATEWAY = "sms-auth.sms.gateway";
    public static final String CONF_PRP_SMS_GATEWAY_ENDPOINT = "sms-auth.sms.gateway.endpoint";

    // User/Credential
    public static final String CONF_PRP_SMS_CLIENTTOKEN = "sms-auth.sms.clienttoken";
    public static final String CONF_PRP_SMS_CLIENTSECRET = "sms-auth.sms.clientsecret";

    // User credentials (used to persist the sent sms code + expiration time cluster wide)
    public static final String USR_CRED_MDL_SMS_CODE = "sms-auth.code";
    public static final String USR_CRED_MDL_SMS_EXP_TIME = "sms-auth.exp-time";

    // Messages
    public static final String MSG_MOBILE_REGEXP = "mobile_number.regexp.validation";
    public static final String MSG_MOBILE_PREFIX_DEFAULT = "mobile_number.prefix.default";
    public static final String MSG_MOBILE_PREFIX_CONDITION = "mobile_number.prefix.condition";
    /*
    mobile_number.prefix.default=+44
    mobile_number.prefix.condition=07
    mobile_number.regexp.validation=^(?:(?:\\(?(?:0(?:0|11)\\)?[\\s-]?\\(?|\\+)44\\)?[\\s-]?(?:\\(?0\\)?[\\s-]?)?)|(?:\\(?0))(?:(?:\\d{5}\\)?[\\s-]?\\d{4,5})|(?:\\d{4}\\)?[\\s-]?(?:\\d{5}|\\d{3}[\\s-]?\\d{3}))|(?:\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{3,4})|(?:\\d{2}\\)?[\\s-]?\\d{4}[\\s-]?\\d{4}))(?:[\\s-]?(?:x|ext\\.?|\\#)\\d{3,4})?$
     */

    // Proxy
    public static final String PROXY_ENABLED = "proxy_enabled";
    public static final String PROXY_HOST= "proxyHost";
    public static final String PROXY_PORT= "proxyPort";
}
