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

import java.util.concurrent.ConcurrentHashMap

/**
 * @author Andres Almiray
 */
class HessianClientHolder {
    private static final HessianClientHolder INSTANCE

    static {
        INSTANCE = new HessianClientHolder()
    }

    static HessianClientHolder getInstance() {
        INSTANCE
    }

    private HessianClientHolder() {}

    private final Map<String, BurlapProxy> BURLAP = new ConcurrentHashMap<String, BurlapProxy>()
    private final Map<String, HessianProxy> HESSIAN = new ConcurrentHashMap<String, HessianProxy>()

    String[] getBurlapProxyIds() {
        List<String> ids = []
        ids.addAll(BURLAP.keySet())
        ids.toArray(new String[ids.size()])
    }

    BurlapProxy getBurlapProxy(String id) {
        BURLAP[id]
    }

    void setBurlapProxy(String id, BurlapProxy client) {
        BURLAP[id] = client
    }

    String[] getHessianProxyIds() {
        List<String> ids = []
        ids.addAll(HESSIAN.keySet())
        ids.toArray(new String[ids.size()])
    }

    HessianProxy getHessianProxy(String id) {
        HESSIAN[id]
    }

    void setHessianProxy(String id, HessianProxy client) {
        HESSIAN[id] = client
    }

    // ======================================================

    BurlapProxy fetchBurlapProxy(Map<String, Object> params) {
        (BurlapProxy) fetchClient(BURLAP, BurlapProxy, params)
    }

    HessianProxy fetchHessianProxy(Map<String, Object> params) {
        (HessianProxy) fetchClient(HESSIAN, HessianProxy, params)
    }

    private fetchClient(Map clientStore, Class klass, Map<String, Object> params) {
        def client = clientStore[(params.id).toString()]
        if (client == null) {
            String id = params.id ? params.remove('id').toString() : '<EMPTY>'
            client = HessianConnector.instance.createClient(klass, params)
            if (id != '<EMPTY>') clientStore[id] = client
        }
        client
    }
}
