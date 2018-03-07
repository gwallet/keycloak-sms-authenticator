package six.six.gateway.aws.snsclient;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;

/**
 * Created by nickpack on 09/08/2017.
 */
public class SnsClientFactory {
    private static AmazonSNSClient snsClient = null;

    public static AmazonSNSClient getSnsClient(String clientToken, String clientSecret) {
        if (null == snsClient) {
            BasicAWSCredentials CREDENTIALS = new BasicAWSCredentials(clientToken, clientSecret);
            snsClient = new AmazonSNSClient(CREDENTIALS).withRegion(Region.getRegion(Regions.EU_WEST_1));
        }
        return snsClient;
    }
}
