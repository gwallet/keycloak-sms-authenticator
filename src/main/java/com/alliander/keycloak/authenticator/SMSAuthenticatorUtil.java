package com.alliander.keycloak.authenticator;

import org.keycloak.models.UserModel;

import java.util.List;

/**
 * Created by joris on 18/11/2016.
 */
public class SMSAuthenticatorUtil {
    public static String getAttributeValue(UserModel user, String attributeName) {
        String result = null;
        List<String> values = user.getAttribute(attributeName);
        if(values != null && values.size() > 0) {
            result = values.get(0);
        }

        return result;
    }
}
