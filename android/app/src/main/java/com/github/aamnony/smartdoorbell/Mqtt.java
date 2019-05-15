package com.github.aamnony.smartdoorbell;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttMessageDeliveryCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;

import java.security.KeyStore;
import java.util.UUID;

public class Mqtt {
    private static final String TAG = "Mqtt";
    public static final String ACTION_UNLOCK_DOOR = "open";
    public static final String ACTION_STREAM = "stream";

    private  AWSIotMqttManager mqttManager;
    private AWSIotClient mIotAndroidClient;
    private String keystorePath;
    private String keystoreName;
    private String keystorePassword;
    private String certificateId;
    private KeyStore clientKeyStore = null;
    private static Mqtt singleton = null;

    private Mqtt(Context context) {
        Region region = Region.getRegion(AppHelper.IOT_REGION);
        String clientId = UUID.randomUUID().toString();

        mqttManager = new AWSIotMqttManager(clientId, AppHelper.CUSTOMER_SPECIFIC_ENDPOINT);

        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mqttManager.setKeepAlive(10);

        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("my/lwt/topic", "Android client lost connection", AWSIotMqttQos.QOS0);
        mqttManager.setMqttLastWillAndTestament(lwt);

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(), // context
                AppHelper.IDENTITIES_POOL_ID, // Identity Pool ID
                AppHelper.IOT_REGION // Region
        );
        // IoT Client (for creation of certificate if needed)
        mIotAndroidClient = new AWSIotClient(credentialsProvider);
        mIotAndroidClient.setRegion(region);

        keystorePath = context.getApplicationContext().getFilesDir().getPath();
        keystoreName = AppHelper.KEYSTORE_NAME;
        keystorePassword = AppHelper.KEYSTORE_PASSWORD;
        certificateId = AppHelper.CERTIFICATE_ID;

        // To load cert/key from keystore on filesystem
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath,
                        keystoreName, keystorePassword)) {
                    Log.i(TAG, "Certificate " + certificateId + " found in keystore - using for MQTT.");
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                            keystorePath, keystoreName, keystorePassword);
                } else {
                    Log.i(TAG, "Key/cert " + certificateId + " not found in keystore.");
                }
            } else {
                Log.i(TAG, "Keystore " + keystorePath + "/" + keystoreName + " not found.");
            }
        } catch (Exception e) {
            Log.e(TAG, "An error occurred retrieving cert/key from keystore.", e);
        }

        if (clientKeyStore == null) {
            Log.i(TAG, "Cert/key was not found in keystore - creating new key and certificate.");

            new Thread(() -> {
                try {
                    // Create a new private key and certificate. This call
                    // creates both on the server and returns them to the
                    // device.
                    CreateKeysAndCertificateRequest createKeysAndCertificateRequest = new CreateKeysAndCertificateRequest();
                    createKeysAndCertificateRequest.setSetAsActive(true);
                    final CreateKeysAndCertificateResult createKeysAndCertificateResult;
                    createKeysAndCertificateResult = mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
                    Log.i(TAG, "Cert ID: " + createKeysAndCertificateResult.getCertificateId() + " created.");

                    // store in keystore for use in MQTT client
                    // saved as alias "default" so a new certificate isn't
                    // generated each run of this application
                    AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId,
                            createKeysAndCertificateResult.getCertificatePem(),
                            createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                            keystorePath, keystoreName, keystorePassword);

                    // load keystore from file into memory to pass on
                    // connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                            keystorePath, keystoreName, keystorePassword);

                    Log.i(TAG, clientKeyStore.toString());
                    // Attach a policy to the newly created certificate.
                    // This flow assumes the policy was already created in
                    // AWS IoT and we are now just attaching it to the
                    // certificate.
                    AttachPrincipalPolicyRequest policyAttachRequest = new AttachPrincipalPolicyRequest();
                    policyAttachRequest.setPolicyName(AppHelper.AWS_IOT_POLICY_NAME);
                    policyAttachRequest.setPrincipal(createKeysAndCertificateResult.getCertificateArn());
                    mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

                } catch (Exception e) {
                    Log.e(TAG, "Exception occurred when generating new private key and certificate.", e);
                }
            }).start();
        }
    }

    public static Mqtt get(Context context) {
        if (singleton == null) {
            singleton = new Mqtt(context);
        }
        return singleton;
    }

    public void sendMessage(String topic, String command, String... args) {
        if (args.length > 0 && command.equals(ACTION_UNLOCK_DOOR)) {
            command += " " + args[0] + " " + args[1];
        }
        String sendCommand = command;
        try {
//                mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() { // Doesn't work, stuck on reconnecting
            mqttManager.connect(clientKeyStore, (status, throwable) -> {
                Log.d(TAG, "Status = " + String.valueOf(status));
                if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                    mqttManager.publishString(sendCommand, topic, AWSIotMqttQos.QOS0, (status1, userData) -> {
                        switch (status1) {
                            case Success:
//                                try {
//                                    mqttManager.disconnect();
//                                } catch (Exception e) {
//                                    Log.e(TAG, "Disconnect error.", e);
//                                }
                                break;
                            case Fail:
                                Log.e(TAG, "Publish error.");
                                break;
                        }
                    }, null);
                }
            });

        } catch (final Exception e) {
            Log.e(TAG, "Connection error.", e);
        }
    }

}
