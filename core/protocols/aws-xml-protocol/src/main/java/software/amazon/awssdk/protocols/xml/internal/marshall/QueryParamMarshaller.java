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

package software.amazon.awssdk.protocols.xml.internal.marshall;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.core.SdkField;
import software.amazon.awssdk.core.protocol.MarshallLocation;
import software.amazon.awssdk.core.traits.ListTrait;
import software.amazon.awssdk.core.traits.MapTrait;
import software.amazon.awssdk.core.traits.RequiredTrait;
import software.amazon.awssdk.core.traits.TraitType;
import software.amazon.awssdk.protocols.core.ValueToStringConverter;

@SdkInternalApi
public final class QueryParamMarshaller {

    public static final XmlMarshaller<String> STRING = new SimpleQueryParamMarshaller<>(ValueToStringConverter.FROM_STRING);

    public static final XmlMarshaller<Integer> INTEGER = new SimpleQueryParamMarshaller<>(ValueToStringConverter.FROM_INTEGER);

    public static final XmlMarshaller<Long> LONG = new SimpleQueryParamMarshaller<>(ValueToStringConverter.FROM_LONG);

    public static final XmlMarshaller<Short> SHORT = new SimpleQueryParamMarshaller<>(ValueToStringConverter.FROM_SHORT);

    public static final XmlMarshaller<Double> DOUBLE = new SimpleQueryParamMarshaller<>(ValueToStringConverter.FROM_DOUBLE);

    public static final XmlMarshaller<Float> FLOAT = new SimpleQueryParamMarshaller<>(ValueToStringConverter.FROM_FLOAT);

    public static final XmlMarshaller<Boolean> BOOLEAN = new SimpleQueryParamMarshaller<>(ValueToStringConverter.FROM_BOOLEAN);

    public static final XmlMarshaller<Instant> INSTANT =
        new SimpleQueryParamMarshaller<>(XmlProtocolMarshaller.INSTANT_VALUE_TO_STRING);

    public static final XmlMarshaller<List<?>> LIST = (list, context, paramName, sdkField) -> {
        if (list == null || list.isEmpty()) {
            return;
        }

        for (Object member : list) {
            context.marshall(MarshallLocation.QUERY_PARAM, member, paramName, null);
        }
    };

    public static final XmlMarshaller<Map<String, ?>> MAP = (map, context, paramName, sdkField) -> {
        if (map == null || map.isEmpty()) {
            return;
        }

        MapTrait mapTrait = sdkField.getRequiredTrait(MapTrait.class, TraitType.MAP_TRAIT);
        SdkField valueField = mapTrait.valueFieldInfo();

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (valueField.containsTrait(ListTrait.class, TraitType.LIST_TRAIT)) {
                ((List<?>) entry.getValue()).forEach(val -> {
                    context.marshallerRegistry().getMarshaller(MarshallLocation.QUERY_PARAM, val)
                           .marshall(val, context, entry.getKey(), null);
                });
            } else {
                SimpleQueryParamMarshaller valueMarshaller = (SimpleQueryParamMarshaller)
                    context.marshallerRegistry().getMarshaller(MarshallLocation.QUERY_PARAM, entry.getValue());

                context.request().putRawQueryParameter(entry.getKey(), valueMarshaller.convert(entry.getValue(), null));
            }
        }
    };

    public static final XmlMarshaller<Void> NULL = (val, context, paramName, sdkField) -> {
        if (Objects.nonNull(sdkField) && sdkField.containsTrait(RequiredTrait.class, TraitType.REQUIRED_TRAIT)) {
            throw new IllegalArgumentException(String.format("Parameter '%s' must not be null", paramName));
        }
    };

    private QueryParamMarshaller() {
    }

    private static class SimpleQueryParamMarshaller<T> implements XmlMarshaller<T> {

        private final ValueToStringConverter.ValueToString<T> converter;

        private SimpleQueryParamMarshaller(ValueToStringConverter.ValueToString<T> converter) {
            this.converter = converter;
        }

        @Override
        public void marshall(T val, XmlMarshallerContext context, String paramName, SdkField<T> sdkField) {
            context.request().appendRawQueryParameter(paramName, converter.convert(val, sdkField));
        }

        public String convert(T val, SdkField<T> sdkField) {
            return converter.convert(val, sdkField);
        }
    }
}
