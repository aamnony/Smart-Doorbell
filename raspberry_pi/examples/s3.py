import boto3
import botocore


def upload(bucket_name: str, local_path: str, image_name: str) -> None:
    s3 = boto3.client('s3')
    s3.upload_file(local_path, bucket_name, image_name)


def download(bucket_name: str, local_path: str, image_name: str) -> None:
    s3 = boto3.resource('s3')
    s3.Bucket(bucket_name).download_file(image_name, local_path)


if __name__ == '__main__':
    bucket_name = 'smartdoorbellfaces'
    local_path = 'oryx_technion'
    image_name = 'recognized/oryx_technion'

    download(bucket_name, local_path, image_name)
