/*
 * Copyright 2009-2013 the original author or authors.
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

/**
 * @author Andres Almiray
 */
class HessianGriffonPlugin {
    // the plugin version
    String version = '1.0.0'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.2.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [lombok: '0.4']
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-hessian-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Hessian client & libraries'

    String description = '''
The Hessian plugin adds a remoting client that uses the [Hessian/Burlap][1] protocols.
It is compatible with [Grails' Remoting plugin 1.3][2].

Usage
-----

The plugin will inject the following dynamic methods:

 * `<R> R withHessian(Map<String, Object> params, Closure<R> stmts)` - executes stmts using a HessianClient
 * `<R> R withBurlap(Map<String, Object> params, Closure<R> stmts)` - executes stmts using a BurlapClient
 * `<R> R withHessian(Map<String, Object> params, CallableWithArgs<R> stmts)` - executes stmts using a HessianClient
 * `<R> R withBurlap(Map<String, Object> params, CallableWithArgs<R> stmts)` - executes stmts using a BurlapClient

Where params may contain

| Property | Type            | Required |
| -------- | --------------- | -------- |
| url      | String          | yes      |
| service  | Class or String | yes      |
| id       | String          |          |

All dynamic methods will create a new client when invoked unless you define an
`id:` attribute. When this attribute is supplied the client will be stored in
a cache managed by the `HessianProvider` that handled the call.

These methods are also accessible to any component through the singleton
`griffon.plugins.hessian.HessianEnhancer`. You can inject these methods to
non-artifacts via metaclasses. Simply grab hold of a particular metaclass and
call `HessianEnhancer.enhance(metaClassInstance)`.

Configuration
-------------

### HessianAware AST Transformation

The preferred way to mark a class for method injection is by annotating it with
`@griffon.plugins.hessian.HessianAware`. This transformation injects the
`griffon.plugins.hessian.HessianContributionHandler` interface and default behavior
that fulfills the contract.

### Dynamic Method Injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.hessian.injectInto = ['controller', 'service']

Dynamic method injection will be skipped for classes implementing
`griffon.plugins.hessian.HessianContributionHandler`.

### Example

This example relies on [Grails][3] as the service provider. Follow these steps
to configure the service on the Grails side:

1. Download a copy of [Grails][4] and install it.
2. Create a new Grails application. We'll pick 'exporter' as the application name.

        grails create-app exporter

3. Change into the application's directory. Install the remoting plugin.

        grails install-plugin remoting

4. Create the following interface in `src/groovy/exporter/Calculator.groovy`.
   This interface will be used on the Griffon side too.

        package exporter 
        interface Calculator {
            double add(double a, double b)
        }
    
5. Create an implementation of the `Calculator` interface as a service

        grails create-service calculator
    
6. Paste the following code in `grails-app/services/exporter/CalculatorService.groovy`

        package exporter
        class CalculatorService implements Calculator {
            boolean transactional = false
            static expose = ['hessian']
        
            double add(double a, double b){
                println "add($a, $b)" // good old println() for quick debugging
                return a + b
            }
        }

7. Run the application

        grails run-app

Now we're ready to build the Griffon application

1. Create a new Griffon application. We'll pick `calculator` as the application
   name

        griffon create-app calculator

2. Install the hessian plugin

        griffon install-plugin hessian

3. Fix the view script to look like this

        package calculator
        application(title: 'Hessian Plugin Example',
          pack: true,
          locationByPlatform: true,
          iconImage: imageIcon('/griffon-icon-48x48.png').image,
          iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                       imageIcon('/griffon-icon-32x32.png').image,
                       imageIcon('/griffon-icon-16x16.png').image]) {
            gridLayout(cols: 2, rows: 4)
            label('Num1:')
            textField(columns: 20, text: bind(target: model, targetProperty: 'num1'))
            label('Num2:')
            textField(columns: 20, text: bind(target: model, targetProperty: 'num2'))
            label('Result:')
            label(text: bind{model.result})
            button(calculateAction, enabled: bind{model.enabled})
        }

4. Let's add required properties to the model

        package calculator
        @Bindable
        class CalculatorModel {
            String num1
            String num2
            String result
            boolean enabled = true
        }

5. Now for the controller code. Notice that there is minimal error handling in
   place. If the user types something that is not a number the client will
   surely break, but the code is sufficient for now.

        package calculator
        import hessian.rest.ContentType
        @griffon.plugins.hessian.HessianAware
        class CalculatorController {
            def model
        
            def calculate = { evt = null ->
                String a = model.num1
                String b = model.num2
                execInsideUIAsync { model.enabled = false }
                try {
                    def result = withHessian(url: 'http://localhost:8080/exporter/calculator', id: 'client') {
                        def response = get(path: '/add', query: [a: a, b: b], accept: ContentType.JSON)
                        response.json.result
                    }
                    execInsideUIAsync { model.result = result }
                } finally {
                    execInsideUIAsync { model.enabled = true }
                }
            }
        }

6. Locate the compiled classes from Grails; jar the calculator interface and
   place it in the lib directory of the Griffon application. Assume 
   `$grailsProject` points to the directory of the exporter application and
   `$griffonProject` points to the calculator application

        cd $grailsProject/target/classes
        jar cvf $griffonProject/lib/exporter-api.jar exporter/Calculator.class

7. Run the application

        griffon run-app

The plugin exposes a Java friendly API to make the exact same calls from Java,
or any other JVM language for that matter. Here's for example the previous code
rewritten in Java. Note the usage of @HessianWare on a Java class

        package calculator;
        import exporter.Calculator;
        import griffon.util.CallableWithArgs;
        import griffon.util.CollectionUtils;
        import java.awt.event.ActionEvent;
        import java.util.Map;
        import org.codehaus.griffon.runtime.core.AbstractGriffonController;
        @griffon.plugins.hessian.HessianAware
        public class CalculatorController extends AbstractGriffonController {
            private CalculatorModel model;
        
            public void setModel(CalculatorModel model) {
                this.model = model;
            }
        
            public void calculate(ActionEvent event) {
                final double a = Double.parseDouble(model.getNum1());
                final double b = Double.parseDouble(model.getNum2());
                enableModel(false);
                try {
                    Map<String, Object> params = CollectionUtils.<String, Object> map()
                        .e("url", "http://localhost:8080/exporter/hessian/CalculatorService")
                        .e("service", Calculator.class);
                    final Double result = withHessian(params,
                        new CallableWithArgs<Double>() {
                            public Double call(Object[] args) {
                                return ((Calculator) args[0]).add(a, b);
                            }
                        });
                    execInsideUIAsync(new Runnable() {
                        public void run() {
                            model.setResult(String.valueOf(result));
                        }
                    });
                } finally {
                    enableModel(true);
                }
            }
        
            private void enableModel(final boolean enabled) {
                execInsideUIAsync(new Runnable() {
                    public void run() {
                        model.setEnabled(enabled);
                    }
                });
            }
        }


Testing
-------

Dynamic methods will not be automatically injected during unit testing, because
addons are simply not initialized for this kind of tests. However you can use
`HessianEnhancer.enhance(metaClassInstance, hessianProviderInstance)` where
`hessianProviderInstance` is of type `griffon.plugins.hessian.HessianProvider`.
The contract for this interface looks like this

    public interface HessianProvider {
        <R> R withBurlap(Map<String, Object> params, Closure<R> closure);
        <R> R withHessian(Map<String, Object> params, Closure<R> closure);
        <R> R withBurlap(Map<String, Object> params, CallableWithArgs<R> callable);
        <R> R withHessian(Map<String, Object> params, CallableWithArgs<R> callable);
    }

It's up to you define how these methods need to be implemented for your tests.
For example, here's an implementation that never fails regardless of the
arguments it receives

    class MyHessianProvider implements HessianProvider {
        public <R> R withBurlap(Map<String, Object> params, Closure<R> closure) { null }
        public <R> R withHessian(Map<String, Object> params, Closure<R> closure) { null }
        public <R> R withBurlap(Map<String, Object> params, CallableWithArgs<R> callable) { null }
        public <R> R withHessian(Map<String, Object> params, CallableWithArgs<R> callable) { null }
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            HessianEnhancer.enhance(service.metaClass, new MyHessianProvider())
            // exercise service methods
        }
    }

On the other hand, if the service is annotated with `@HessianAware` then usage
of `HessianEnhancer` should be avoided at all costs. Simply set
`hessianProviderInstance` on the service instance directly, like so, first the
service definition

    @griffon.plugins.hessian.HessianAware
    class MyService {
        def serviceMethod() { ... }
    }

Next is the test

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            service.hessianProvider = new MyHessianProvider()
            // exercise service methods
        }
    }

Tool Support
------------

### DSL Descriptors

This plugin provides DSL descriptors for Intellij IDEA and Eclipse (provided
you have the Groovy Eclipse plugin installed). These descriptors are found
inside the `griffon-hessian-compile-x.y.z.jar`, with locations

 * dsdl/hessian.dsld
 * gdsl/hessian.gdsl

### Lombok Support

Rewriting Java AST in a similar fashion to Groovy AST transformations is
possible thanks to the [lombok][5] plugin.

#### JavaC

Support for this compiler is provided out-of-the-box by the command line tools.
There's no additional configuration required.

#### Eclipse

Follow the steps found in the [Lombok][5] plugin for setting up Eclipse up to
number 5.

 6. Go to the path where the `lombok.jar` was copied. This path is either found
    inside the Eclipse installation directory or in your local settings. Copy
    the following file from the project's working directory

         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/hessian-<version>/dist/griffon-hessian-compile-<version>.jar .

 6. Edit the launch script for Eclipse and tweak the boothclasspath entry so
    that includes the file you just copied

        -Xbootclasspath/a:lombok.jar:lombok-pg-<version>.jar:\
        griffon-lombok-compile-<version>.jar:griffon-hessian-compile-<version>.jar

 7. Launch Eclipse once more. Eclipse should be able to provide content assist
    for Java classes annotated with `@HessianAware`.

#### NetBeans

Follow the instructions found in [Annotation Processors Support in the NetBeans
IDE, Part I: Using Project Lombok][6]. You may need to specify
`lombok.core.AnnotationProcessor` in the list of Annotation Processors.

NetBeans should be able to provide code suggestions on Java classes annotated
with `@HessianAware`.

#### Intellij IDEA

Follow the steps found in the [Lombok][5] plugin for setting up Intellij IDEA
up to number 5.

 6. Copy `griffon-hessian-compile-<version>.jar` to the `lib` directory

         $ pwd
           $USER_HOME/Library/Application Support/IntelliJIdea11/lombok-plugin
         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/hessian-<version>/dist/griffon-hessian-compile-<version>.jar lib

 7. Launch IntelliJ IDEA once more. Code completion should work now for Java
    classes annotated with `@HessianAware`.


[1]: http://hessian.caucho.com/
[2]: http://grails.org/plugin/remoting
[3]: http://grails.org
[4]: http://grails.org/Download
[5]: /plugin/lombok
[6]: http://netbeans.org/kb/docs/java/annotations-lombok.html
'''
}
