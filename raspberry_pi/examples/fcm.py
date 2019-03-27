import firebase_admin
import firebase_admin.credentials
import firebase_admin.messaging


def send_to_topic(topic: str) -> None:
    message = firebase_admin.messaging.Message(
        data={
            'person': 'unknown',
        },
        topic=topic,
    )

    # Send a message to the devices subscribed to the provided topic.
    response = firebase_admin.messaging.send(message)
    # Response is a message ID string.
    print('Successfully sent message:', response)


if __name__ == '__main__':
    cred = firebase_admin.credentials.Certificate('../fcm.json')
    default_app = firebase_admin.initialize_app(cred)

    send_to_topic('nssl')
