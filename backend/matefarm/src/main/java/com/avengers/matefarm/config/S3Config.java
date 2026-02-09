package com.avengers.matefarm.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


/* S3 Config
 *  각 도메인은 버킷을 사용할 때
 *
 *  1) S3Client 의존성을 주입하세요.
 *   private final S3Client s3Client;
 *
 *  2) 버킷은 각 도메인의 Service 레이어에 아래 내용을 추가해주시면 됩니다.
 *   @Value("cloud.aws.s3.buckets")
 *   private String S3bucket;
 *
 *   @Value 라이브러리 임포트할 떄 lombok.Value 아니니까 잘 확인하고 import 하세요 !!! 에러 뜹니다
* */
@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;


    /*  S3Client도 Aphach Tika와 동일한 이유로 전역으로 설정하고
     *  한 객체만 S3 접근할 정보에 대해 참조되도록 @Bean으로 등록
    * */
    @Bean
    public S3Client s3Client() {

        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}

