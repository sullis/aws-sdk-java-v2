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

package software.amazon.awssdk.core.traits;

import software.amazon.awssdk.annotations.SdkProtectedApi;

/**
 * Trait that indicates a member is the 'payload' member.
 */
@SdkProtectedApi
public final class PayloadTrait implements Trait {

    private PayloadTrait() {
    }

    public static PayloadTrait create() {
        return new PayloadTrait();
    }

    @Override
    public TraitType type() {
        return TraitType.PAYLOAD_TRAIT;
    }
}
