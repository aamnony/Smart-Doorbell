import boto3


rekognition = boto3.client('rekognition')
colId = 'SmartDoorbell'

# Create collection:
# c = rekognition.create_collection(CollectionId=colId)

# Add face:
# with open(r"obama.jpg", 'rb') as imgf:
    # img = imgf.read()
# indr = rekognition.index_faces(CollectionId=colId, Image={'Bytes': img}, ExternalImageId='Obama', MaxFaces=1,)


# Search face:
with open('not_obama.jpg', 'rb') as imgf:
    img = imgf.read()
inds = rekognition.search_faces_by_image(CollectionId=colId, Image={'Bytes': img}, MaxFaces=1)
# print(inds['FaceMatches'][0]['Face']['ExternalImageId'])
print(inds)
