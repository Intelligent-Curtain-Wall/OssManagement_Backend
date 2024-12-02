package intelligentcurtainwall.ossmanagement.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class OssService {

    private final OSS ossClient;

    @Autowired
    public OssService(OSS ossClient) {
        this.ossClient = ossClient;
    }

    public byte[] getObject(String bucketName, String objectKey) {
        OSSObject ossObject = ossClient.getObject(new GetObjectRequest(bucketName, objectKey));
        InputStream inputStream = ossObject.getObjectContent();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while (true) {
            try {
                if ((length = inputStream.read(buffer)) == -1) {
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
