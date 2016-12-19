package com.alliander.keycloak.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import javax.ws.rs.HttpMethod;
import java.util.ArrayList;
import java.util.List;

import static com.alliander.keycloak.authenticator.SMSAuthenticatorContstants.AUTH_METHOD_BASIC;
import static com.alliander.keycloak.authenticator.SMSAuthenticatorContstants.AUTH_METHOD_INMESSAGE;


/**
 * Created by joris on 11/11/2016.
 */
public class KeycloakSmsAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    public static final String PROVIDER_ID = "sms-authentication";

    private static Logger logger = Logger.getLogger(KeycloakSmsAuthenticatorFactory.class);
    private static final KeycloakSmsAuthenticator SINGLETON = new KeycloakSmsAuthenticator();


    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.OPTIONAL,
            AuthenticationExecutionModel.Requirement.DISABLED};

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;

        // Mobile number attribute
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_USR_ATTR_MOBILE);
        property.setLabel("Mobile number attribute");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("The attribute in which the mobile number of a user is stored.");
        configProperties.add(property);

        // SMS Code
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_CODE_TTL);
        property.setLabel("SMS code time to live");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("The validity of the sent code in seconds.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_CODE_LENGTH);
        property.setLabel("Length of the SMS code");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Length of the SMS code.");
        configProperties.add(property);

        // SMS Text
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_TEXT);
        property.setLabel("Template of text to send to the user");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Use %sms-code% as placeholder for the generated SMS code. Use %user% and %password% as placeholder when 'In message' authentication is used.");
        configProperties.add(property);

        // SMS Gateway
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_METHOD);
        property.setLabel("HTTP method");
        property.setHelpText("");
        List<String> methods = new ArrayList(2);
        methods.add(HttpMethod.GET);
        methods.add(HttpMethod.POST);
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setDefaultValue(methods);
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_URL);
        property.setLabel("URL of SMS gateway");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Use {message} as a placeholder for the message and {phonenumber} as a placeholder for the mobile number when the SMS text is to be passed as a URL parameter.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_CONTENT_TYPE);
        property.setLabel("Content type");
        property.setHelpText("");
        List<String> types = new ArrayList(2);
        types.add("application/json");
        types.add("application/xml");
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setDefaultValue(types);
        configProperties.add(property);

        // SMS Authentication
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_AUTHTYPE);
        property.setLabel("Authentication method");
        property.setHelpText("");
        types = new ArrayList(2);
        types.add(AUTH_METHOD_BASIC);
        types.add(AUTH_METHOD_INMESSAGE);
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setDefaultValue(types);
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_USERNAME);
        property.setLabel("Username to authenticate towards the SMS Gateway");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_SMS_PASSWORD);
        property.setLabel("Password to authenticate towards the SMS Gateway");
        property.setType(ProviderConfigProperty.PASSWORD);
        property.setHelpText("");
        configProperties.add(property);




        // HTTP Proxy
        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_PROXY_URL);
        property.setLabel("URL of HTTP proxy to use when calling the SMS gateway");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Emtpy when no proxy is needed");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_PROXY_USERNAME);
        property.setLabel("Username to authenticate towards the HTTP proxy");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(SMSAuthenticatorContstants.CONF_PRP_PROXY_PASSWORD);
        property.setLabel("Password to authenticate towards the HTTP proxy");
        property.setType(ProviderConfigProperty.PASSWORD);
        property.setHelpText("");
        configProperties.add(property);

    }

    public String getId() {
        logger.debug("getId called ... returning " + PROVIDER_ID);
        return PROVIDER_ID;
    }

    public Authenticator create(KeycloakSession session) {
        logger.debug("create called ... returning " + SINGLETON);
        return SINGLETON;
    }


    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        logger.debug("getRequirementChoices called ... returning " + REQUIREMENT_CHOICES);
        return REQUIREMENT_CHOICES;
    }

    public boolean isUserSetupAllowed() {
        logger.debug("isUserSetupAllowed called ... returning true");
        return true;
    }

    public boolean isConfigurable() {
        boolean result = true;
        logger.debug("isConfigurable called ... returning " + result);
        return result;
    }

    public String getHelpText() {
        logger.debug("getHelpText called ...");
        return "Validates an OTP sent by SMS.";
    }

    public String getDisplayType() {
        String result = "SMS Authentication";
        logger.debug("getDisplayType called ... returning " + result);
        return result;
    }

    public String getReferenceCategory() {
        logger.debug("getReferenceCategory called ... returning sms-auth-code");
        return "sms-auth-code";
    }

    public List<ProviderConfigProperty> getConfigProperties() {
        logger.debug("getConfigProperties called ... returning " + configProperties);
        return configProperties;
    }

    public void init(Config.Scope config) {
        logger.debug("init called ... config.scope = " + config);
    }

    public void postInit(KeycloakSessionFactory factory) {
        logger.debug("postInit called ... factory = " + factory);
    }

    public void close() {
        logger.debug("close called ...");
    }
}
