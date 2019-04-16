import time

import AWSIoTPythonSDK
import AWSIoTPythonSDK.MQTTLib


ENDPOINT = 'a18gejfspwq80u.iot.us-east-1.amazonaws.com'


class C:
    def __init__(self):
        self.mqtt_client = AWSIoTPythonSDK.MQTTLib.AWSIoTMQTTClient("myClientID")
        self.mqtt_client.disableMetricsCollection()
        self.mqtt_client.configureEndpoint(ENDPOINT, 8883)
        self.mqtt_client.configureCredentials('root-CA.crt', 'ab6bcc17f5-private.pem.key', 'ab6bcc17f5-certificate.pem.crt')
        
        self.mqtt_client.configureOfflinePublishQueueing(-1)  # Infinite offline Publish queueing
        self.mqtt_client.configureDrainingFrequency(2)  # Draining: 2 Hz
        self.mqtt_client.configureConnectDisconnectTimeout(10)  # 10 sec
        self.mqtt_client.configureMQTTOperationTimeout(5)  # 5 sec
    
    def on_message_received(self, client, userdata, message) -> None:
        print('<' + str(message.topic) + '>', str(message.payload.decode('utf-8')))
        time.sleep(1)
        print('toc')
    
    def run(self):
        self.mqtt_client.connect()
        self.mqtt_client.subscribe('myTopic', 1, self.on_message_received)
        while True:
            print('tic')
            time.sleep(5)
        # mqtt_client.publish("myTopic", "myPayload", 0)
        # mqtt_client.unsubscribe("myTopic")
        # mqtt_client.disconnect()

if __name__ == '__main__':
    C().run()

    