package six.six.aws.snsclient;

import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nickpack on 09/08/2017.
 */
public class SnsNotificationService {

    public PublishResult send(String phoneNumber, String message, String clientToken, String clientSecret) {
        Map<String, MessageAttributeValue> smsAttributes = new HashMap<String, MessageAttributeValue>();
        smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
                .withStringValue("HomeOffice")
                .withDataType("String"));

        return SnsClientFactory.getSnsClient(clientToken, clientSecret).publish(new PublishRequest()
                .withMessage(message)
                .withPhoneNumber(phoneNumber)
                .withMessageAttributes(smsAttributes));
    }
}
