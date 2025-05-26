package com.thrillpool;

import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.utils.AttributeMap;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        DynamoDbAsyncClientBuilder builder = DynamoDbAsyncClient
                .builder()
                .endpointOverride(URI.create("http://localhost:8001"))
                .region(Region.EU_CENTRAL_1);

        SdkAsyncHttpClient underlyingClient = NettyNioAsyncHttpClient.builder()
                .buildWithDefaults(AttributeMap.builder()
                        .put(SdkHttpConfigurationOption.CONNECTION_ACQUIRE_TIMEOUT, Duration.ofMillis(2500))
                        .put(SdkHttpConfigurationOption.CONNECTION_TIMEOUT, Duration.ofMillis(4000))
                        .build()
                );
        builder.httpClient(underlyingClient);

        long startTime = System.currentTimeMillis();

        try (DynamoDbAsyncClient client = builder.build()) {
            try {
                CompletableFuture<ListTablesResponse> futureResult = client.listTables();
                ListTablesResponse result = futureResult.join();
                System.out.println(result);
            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println(System.currentTimeMillis() - startTime);
            try {
                CompletableFuture<ListTablesResponse> futureResult = client.listTables();
                ListTablesResponse result = futureResult.join();
                System.out.println(result);
            } catch (Exception e) {
                System.out.println(e);
            }
            System.out.println(System.currentTimeMillis() - startTime);
        }
    }
}
