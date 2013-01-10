/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.hessian

/**
 * @author Andres Almiray
 */
@Singleton
class HessianConnector {
    private static final Class[] CTOR_ARGS1 = [String, Class] as Class[]
    private static final Class[] CTOR_ARGS2 = [String, String] as Class[]

    public createClient(Class klass, Map params) {
        def url = params.remove('url')
        if (!url) {
            throw new IllegalArgumentException("Failed to create ${(klass == HessianProxy? 'hessian' : 'burlap')} proxy, url: parameter is null or invalid.")
        }

        def ctorArgs = CTOR_ARGS1
        def serviceClass = params.remove('service')
        if (!serviceClass) {
            throw new IllegalArgumentException("Failed to create ${(klass == HessianProxy? 'hessian' : 'burlap')} proxy, service: parameter is null or invalid.")
        }
        if (!(serviceClass instanceof Class)) {
            serviceClass = serviceClass.toString()
            ctorArgs = CTOR_ARGS2
        }

        try {
            return klass.getDeclaredConstructor(ctorArgs).newInstance(url, serviceClass)
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create ${(klass == HessianProxy? 'hessian' : 'burlap')} proxy, reason: $e", e)
        }
    }
}
