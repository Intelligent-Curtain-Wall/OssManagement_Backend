package intelligentcurtainwall.ossmanagement.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public String putObject(String bucketName, String objectKey, MultipartFile file) throws IOException {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, file.getInputStream());
        ossClient.putObject(putObjectRequest);
        return objectKey;
    }
}
