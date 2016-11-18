# keycloak-sms-authenticator

To install the SMS Authenticator one has to:

* Add the jar to the Keycloak server:
`cp target/keycloak-sms-authenticator.jar _KEYCLOAK_HOME_/providers/`

* Add two templates to the Keycloak server:
`cp templates/sms-validation.ftl _KEYCLOAK_HOME_/themes/base/login/`
`cp templates/sms_validtion_missing_mobile.ftl _KEYCLOAK_HOME_/themes/base/login/`


Configure your REALM to use the SMS Authentication.
First create a new REALM (or select a previously created REALM).

Under Authentication > Flows:
* Copy 'Browse' flow to 'Browser with SMS' flow
* Click on 'Actions > Add execution on the 'Browser with SMS Forms' line and add the 'SMS Authentication'
* Set 'SMS Authentication' to 'REQUIRED'

Under Authentication > Bindings:
* Select 'Browser with SMS' as the 'Browser Flow' for the REALM.

Under Authentication > Required Actions:
* Click on Register and select 'SMS Authentication' to add the Required Action to the REALM.
* Make sure that for the 'SMS Authentication' both the 'Enabled' and 'Default Action' check boes are checked.