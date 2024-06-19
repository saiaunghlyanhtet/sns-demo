package com.sahh.sns_demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

@Service
public class SnsService {

    private final SnsClient snsClient;
    private final String topicArn;

    public SnsService(@Value("${aws.accessKeyId}") String accessKeyId,
            @Value("${aws.secretAccessKey}") String secretAccessKey,
            @Value("${aws.region}") String region) {
        this.snsClient = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
        this.topicArn = "arn:aws:sns:ap-northeast-1:736405529708:Lashic";
    }

    public String sendSms(String phoneNumber, String message) {
        Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
        smsAttributes.put("AWS.SNS.SMS.SenderID", MessageAttributeValue.builder()
                .stringValue("Lashic") // Replace with your sender ID
                .dataType("String")
                .build());
        smsAttributes.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                .stringValue("Transactional") // Or "Promotional"
                .dataType("String")
                .build());

        PublishRequest request = PublishRequest.builder()
                .message(message)
                .phoneNumber(phoneNumber)
                .messageAttributes(smsAttributes)
                .build();
        System.out.println("Request: " + request.toString());
        PublishResponse result = snsClient.publish(request);
        System.out.println("Publish response: " + result);
        return result.messageId();
    }

    public String subscribePhoneNumberToTopic(String phoneNumber, String message) {
        boolean alreadySubscribed = isPhoneNumberSubscribed(phoneNumber);

        if (alreadySubscribed) {
            System.out.println("Phone number is already subscribed.");
            sendSms(phoneNumber, message);
            return "Already subscribed";
        } else {
            SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                    .protocol("sms")
                    .endpoint(phoneNumber)
                    .topicArn(this.topicArn)
                    .build();

            SubscribeResponse subscribeResponse = snsClient.subscribe(subscribeRequest);
            System.out.println("Subscribe response: " + subscribeResponse);

            String subscriptionArn = subscribeResponse.subscriptionArn();
            if (subscriptionArn != null && !subscriptionArn.isEmpty()) {
                sendSms(phoneNumber, message);
            }

            return subscriptionArn;
        }
    }

    private boolean isPhoneNumberSubscribed(String phoneNumber) {
        String nextToken = null;

        do {
            ListSubscriptionsByTopicRequest request = ListSubscriptionsByTopicRequest.builder()
                    .topicArn(this.topicArn)
                    .nextToken(nextToken)
                    .build();

            ListSubscriptionsByTopicResponse response = snsClient.listSubscriptionsByTopic(request);

            for (Subscription subscription : response.subscriptions()) {
                if ("sms".equals(subscription.protocol()) && phoneNumber.equals(subscription.endpoint())) {
                    return true;
                }
            }

            nextToken = response.nextToken();
        } while (nextToken != null);

        return false;
    }
}
