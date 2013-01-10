/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lombok.core.handlers;

import lombok.core.BaseConstants;
import lombok.core.util.MethodDescriptor;

import static lombok.core.util.MethodDescriptor.args;
import static lombok.core.util.MethodDescriptor.type;
import static lombok.core.util.MethodDescriptor.typeParams;

/**
 * @author Andres Almiray
 */
public interface HessianAwareConstants extends BaseConstants {
    String HESSIAN_PROVIDER_TYPE = "griffon.plugins.hessian.HessianProvider";
    String DEFAULT_HESSIAN_PROVIDER_TYPE = "griffon.plugins.hessian.DefaultHessianProvider";
    String HESSIAN_CONTRIBUTION_HANDLER_TYPE = "griffon.plugins.hessian.HessianContributionHandler";
    String HESSIAN_PROVIDER_FIELD_NAME = "this$hessianProvider";
    String METHOD_GET_HESSIAN_PROVIDER = "getHessianProvider";
    String METHOD_SET_HESSIAN_PROVIDER = "setHessianProvider";
    String METHOD_WITH_HESSIAN = "withHessian";
    String METHOD_WITH_BURLAP = "withBurlap";
    String PROVIDER = "provider";

    MethodDescriptor[] METHODS = new MethodDescriptor[]{
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_BURLAP,
            args(
                type(JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                type(GROOVY_LANG_CLOSURE, R))
        ),
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_HESSIAN,
            args(
                type(JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                type(GROOVY_LANG_CLOSURE, R))
        ),
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_BURLAP,
            args(
                type(JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                type(GRIFFON_UTIL_CALLABLEWITHARGS, R))
        ),
        MethodDescriptor.method(
            type(R),
            typeParams(R),
            METHOD_WITH_HESSIAN,
            args(
                type(JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                type(GRIFFON_UTIL_CALLABLEWITHARGS, R))
        )
    };
}
