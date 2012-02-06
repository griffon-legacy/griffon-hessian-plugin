/*
 * Copyright 2009-2012 the original author or authors.
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

import griffon.util.CallableWithArgs
import java.util.concurrent.ConcurrentHashMap

import java.lang.reflect.InvocationTargetException

/**
 * @author Andres Almiray
 */
@Singleton
class HessianConnector implements HessianProvider {
    private final Map PROXIES = new ConcurrentHashMap()
    private static final Class[] CTOR_ARGS1 = [String, Class] as Class[]
    private static final Class[] CTOR_ARGS2 = [String, String] as Class[]

    Object withHessian(Map params, Closure closure) {
        doWithProxy(HessianProxy, params, closure)
    }

    public <T> T withHessian(Map params, CallableWithArgs<T> callable) {
        doWithProxy(HessianProxy, params, callable)
    }

    Object withBurlap(Map params, Closure closure) {
        doWithProxy(BurlapProxy, params, closure)
    }

    public <T> T withBurlap(Map params, CallableWithArgs<T> callable) {
        doWithProxy(BurlapProxy, params, callable)
    }
    
   // ======================================================

    private Object doWithProxy(Class klass, Map params, Closure closure) {
        def proxy = configureProxy(klass, params)

        if (closure) {
            closure.delegate = proxy
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            return closure()
        }
        return null
    }

    private <T> T doWithProxy(Class klass, Map params, CallableWithArgs<T> callable) {
        def proxy = configureProxy(klass, params)

        if (callable) {
            callable.args = [proxy] as Object[]
            return callable.run()
        }
        return null
    }

    private configureProxy(Class klass, Map params) {
        def proxy = null
        if (params.id) {
            String id = params.remove('id').toString()
            proxy = PROXIES[id]
            if (proxy == null) {
                proxy = makeProxy(klass, params)
                PROXIES[id] = proxy 
            }
        } else {
            proxy = makeProxy(klass, params)
        }
        
        proxy
    }

    private makeProxy(Class klass, Map params) {
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
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to create ${(klass == HessianProxy? 'hessian' : 'burlap')} proxy, reason: $e", e)
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to create ${(klass == HessianProxy? 'hessian' : 'burlap')} proxy, reason: $e", e)
        }
    }
}
