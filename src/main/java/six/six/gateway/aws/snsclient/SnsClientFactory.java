package six.six.gateway.aws.snsclient;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;

/**
 * Created by nickpack on 09/08/2017.
 */
public class SnsClientFactory {
    private static AmazonSNS snsClient = null;

    public static AmazonSNS getSnsClient(String clientToken, String clientSecret) {
        if (null == snsClient) {

            AWSCredentialsProvider provider = new AWSCredentialsProvider() {
                @Override
                public AWSCredentials getCredentials() {
                    return new BasicAWSCredentials(clientToken, clientSecret);
                }
                @Override
                public void refresh() {
                }
            };
            snsClient = AmazonSNSClientBuilder.standard().withRegion("eu-west-1").withCredentials(provider).build();
        }
        return snsClient;
    }
}
