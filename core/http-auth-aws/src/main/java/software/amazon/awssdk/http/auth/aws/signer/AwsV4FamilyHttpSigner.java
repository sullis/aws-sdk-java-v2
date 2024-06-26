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

package software.amazon.awssdk.http.auth.aws.signer;

import java.time.Duration;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.checksums.spi.ChecksumAlgorithm;
import software.amazon.awssdk.http.auth.spi.signer.HttpSigner;
import software.amazon.awssdk.http.auth.spi.signer.SignerProperty;
import software.amazon.awssdk.identity.spi.Identity;

/**
 * An interface shared by {@link AwsV4HttpSigner} and {@link AwsV4aHttpSigner} for defining signer properties that are common
 * across both signers.
 */
@SdkPublicApi
public interface AwsV4FamilyHttpSigner<T extends Identity> extends HttpSigner<T> {
    /**
     * The name of the AWS service. This property is required. This value can be found in the service documentation or
     * on the service client itself (e.g. {@code S3Client.SERVICE_NAME}).
     */
    SignerProperty<String> SERVICE_SIGNING_NAME =
        SignerProperty.create(AwsV4FamilyHttpSigner.class, "ServiceSigningName");

    /**
     * A boolean to indicate whether to double url-encode the resource path when constructing the canonical request. This property
     * defaults to true.
     * <p>
     * Note: S3 requires this value to be set to 'false' to prevent signature mismatch errors for certain paths.
     */
    SignerProperty<Boolean> DOUBLE_URL_ENCODE =
        SignerProperty.create(AwsV4FamilyHttpSigner.class, "DoubleUrlEncode");

    /**
     * A boolean to indicate whether the resource path should be "normalized" according to RFC3986 when constructing the canonical
     * request. This property defaults to true.
     * <p>
     * Note: S3 requires this value to be set to 'false' to prevent signature mismatch errors for certain paths.
     */
    SignerProperty<Boolean> NORMALIZE_PATH =
        SignerProperty.create(AwsV4FamilyHttpSigner.class, "NormalizePath");

    /**
     * The location where auth-related data is inserted, as a result of signing. This property defaults to HEADER.
     */
    SignerProperty<AuthLocation> AUTH_LOCATION =
        SignerProperty.create(AwsV4FamilyHttpSigner.class, "AuthLocation");

    /**
     * The duration for the request to be valid. This property defaults to null. This can be set to presign the request for
     * later use. The maximum allowed value for this property is 7 days. This is only supported when AuthLocation=QUERY.
     */
    SignerProperty<Duration> EXPIRATION_DURATION =
        SignerProperty.create(AwsV4FamilyHttpSigner.class, "ExpirationDuration");

    /**
     * Whether to indicate that a payload is signed or not. This property defaults to true. This can be set false to disable
     * payload signing.
     * <p>
     * When this value is true and {@link #CHUNK_ENCODING_ENABLED} is false, the whole payload must be read to generate
     * the payload signature. For very large payloads, this could impact memory usage and call latency. Some services
     * support this value being disabled, especially over HTTPS where SSL provides some of its own protections against
     * payload tampering.
     */
    SignerProperty<Boolean> PAYLOAD_SIGNING_ENABLED =
        SignerProperty.create(AwsV4FamilyHttpSigner.class, "PayloadSigningEnabled");

    /**
     * Whether to indicate that a payload is chunk-encoded or not. This property defaults to false. This can be set true to
     * enable the `aws-chunked` content-encoding.
     * <p>
     * Only some services support this value being set to true, but for those services it can prevent the need to read
     * the whole payload before writing when {@link #PAYLOAD_SIGNING_ENABLED} is true.
     */
    SignerProperty<Boolean> CHUNK_ENCODING_ENABLED =
        SignerProperty.create(AwsV4FamilyHttpSigner.class, "ChunkEncodingEnabled");

    /**
     * The algorithm to use for calculating a "flexible" checksum. This property is optional.
     */
    SignerProperty<ChecksumAlgorithm> CHECKSUM_ALGORITHM =
        SignerProperty.create(AwsV4FamilyHttpSigner.class, "ChecksumAlgorithm");

    /**
     * This enum represents where auth-related data is inserted, as a result of signing.
     */
    enum AuthLocation {
        /**
         * Indicates auth-related data is inserted in HTTP headers.
         */
        HEADER,

        /**
         * Indicates auth-related data is inserted in HTTP query-parameters.
         */
        QUERY_STRING
    }
}
