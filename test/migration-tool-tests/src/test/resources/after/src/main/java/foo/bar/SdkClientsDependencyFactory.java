/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package foo.bar;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

public final class SdkClientsDependencyFactory {

    private SdkClientsDependencyFactory() {
    }

    public static SqsClient defaultSqsClient() {
        return SqsClient.builder().build();
    }

    public static SqsClient sqsClientWithAllSettings() {
        return SqsClient.builder()
                              .region(Region.US_WEST_2)
                              .credentialsProvider(CredentialsDependencyFactory.defaultCredentialsProviderChain())
                              .build();
    }

    public static SqsAsyncClient defaultSqsAsyncClient() {
        return SqsAsyncClient.builder().build();
    }

    public static SqsAsyncClient defaultSqsAsyncClientWithAllSettings() {
        return SqsAsyncClient.builder()
                                   .region(Region.US_WEST_2)
                                   .credentialsProvider(CredentialsDependencyFactory.defaultCredentialsProviderChain())
                                   .build();
    }
}