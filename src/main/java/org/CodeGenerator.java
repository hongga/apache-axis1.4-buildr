package org;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.xml.rpc.Service;

import org.apache.log4j.Logger;

/**
 * 
 * @author oleksandr_kyetov
 * 
 */
public class CodeGenerator {

    /**
     * 
     */
    private static final Logger logger = Logger.getLogger(CodeGenerator.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            /*
             * General properties
             */
            Properties properties = Utils.getProperites();

            /*
             * Call center WSDL path
             */
            StringBuilder callCenterWSDLPath = new StringBuilder().append(properties.getProperty(ConstantsProperty.CALL_CENTER_PROTOCOL))
                    .append("://").append(properties.getProperty(ConstantsProperty.CALL_CENTER_SERVER_ADDRESS)).append(":")
                    .append(properties.getProperty(ConstantsProperty.CALL_CENTER_SERVER_PORT)).append("/")
                    .append(properties.getProperty(ConstantsProperty.CALL_CENTER_POINT)).append("/")
                    .append(properties.getProperty(ConstantsProperty.CALL_CENTER_AUTHENTICATION_WSDL)).append("?wsdl");

            /*
             * Path to generated from WSDL java sources
             */
            StringBuilder callCenterJavaSourcePath = new StringBuilder().append(System.getProperty("user.dir"))
                    .append(System.getProperty("file.separator")).append(properties.getProperty(ConstantsProperty.WSDL_SOURCE_FOLDER));

            /*
             * Path to compiled WSDL java classes
             */
            StringBuilder callCenterJavaCompilePath = new StringBuilder().append(System.getProperty("user.dir"))
                    .append(System.getProperty("file.separator")).append(properties.getProperty(ConstantsProperty.WSDL_CLASSES_FOLDER));

            /*
             * For details execute as WSDL2Code.main(new String[] { "" } ); and see usage details
             */
            WSDL2Code
                    .main(new String[] { "-o", properties.getProperty(ConstantsProperty.WSDL_SOURCE_FOLDER), callCenterWSDLPath.toString() });

            /*
             * Get folder for WSDL generated java sources
             */
            File callCenterSourceDirectory = new File(callCenterJavaSourcePath.append(System.getProperty("file.separator"))
                    .append(properties.getProperty(ConstantsProperty.WSDL_PACKAGE).replace(".", System.getProperty("file.separator")))
                    .toString());

            /*
             * Create directory for compiled classes
             */
            File callCenterCompileDirectory = new File(callCenterJavaCompilePath.toString());
            if (!callCenterCompileDirectory.exists()) {
                callCenterCompileDirectory.mkdirs();
            }

            /*
             * Get list of java sources generated from WSDL
             */
            String[] callCenterJavaSources = callCenterSourceDirectory.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".java");
                }
            });

            /*
             * Convert sources list to its full paths
             */
            for (int i = 0; i < callCenterJavaSources.length; i++) {
                callCenterJavaSources[i] = callCenterJavaSourcePath.toString() + System.getProperty("file.separator")
                        + callCenterJavaSources[i];
            }

            if (logger.isDebugEnabled()) {
                logger.debug("List of files generated from " + properties.getProperty(ConstantsProperty.CALL_CENTER_AUTHENTICATION_WSDL)
                        + " to be compiled:");
                for (String javaSource : callCenterJavaSources) {
                    logger.debug(javaSource);
                }
            }

            /*
             * Compose arguments for java compiler
             */
            List<String> callCenterArguments = new ArrayList<String>();
            // Output directory
            callCenterArguments.add("-d");
            callCenterArguments.add(callCenterCompileDirectory.toString());
            // Current class path
            callCenterArguments.add("-cp");
            callCenterArguments.add(System.getProperty("java.class.path"));
            // Enable all warnings
            callCenterArguments.add("-Xlint:all");
            // Add sources which need to be compiled
            for (String javaSource : callCenterJavaSources) {
                callCenterArguments.add(javaSource);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Call java compiler with next arguments:");
                logger.debug(callCenterArguments);
            }

            /*
             * Compile java sources
             */
            JavaCompiler callCenterJavaCompiler = ToolProvider.getSystemJavaCompiler();
            callCenterJavaCompiler.run(null, null, null, callCenterArguments.toArray(new String[0]));

            /*
             * Load AuthenticationWSBindingStub class generated from AuthenticationWS
             */
            Class<?> callCenterAuthenticationWSBindingStubClass = ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty(ConstantsProperty.WSDL_PACKAGE) + "."
                            + properties.getProperty(ConstantsProperty.AUTHENTICATION_WS_BINDING_STUB));

            /*
             * Load AuthenticationWSServiceLocator class generated from AuthenticationWS
             */
            Class<?> callCenterAuthenticationWSServiceLocatorClass = ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty(ConstantsProperty.WSDL_PACKAGE) + "."
                            + properties.getProperty(ConstantsProperty.AUTHENTICATION_WS_BINDING_SERVICE_LOCATOR));

            /*
             * Load AuthenticationResult class generated from AuthenticationWS
             */
            Class<?> callCenterAuthenticationResultClass = ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty(ConstantsProperty.WSDL_PACKAGE) + "."
                            + properties.getProperty(ConstantsProperty.AUTHENTICATION_WS_AUTHENTICATION_RESULT));

            /*
             * Load CredentialsDTO class generated from AuthenticationWS
             */
            Class<?> callCenterCredentialsDTOClass = ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty(ConstantsProperty.WSDL_PACKAGE) + "."
                            + properties.getProperty(ConstantsProperty.AUTHENTICATION_WS_CREDENTIALS_DTO));

            /*
             * Get constructor AuthenticationWSBindingStub(URL, Service)
             */
            Constructor<?> callCenterAuthenticationWSBindingStubConstructor = callCenterAuthenticationWSBindingStubClass
                    .getDeclaredConstructor(URL.class, Service.class);
            callCenterAuthenticationWSBindingStubConstructor.setAccessible(true);

            /*
             * Get constructor AuthenticationWSServiceLocator()
             */
            Constructor<?> callCenterAuthenticationWSServiceLocatorConstructor = callCenterAuthenticationWSServiceLocatorClass
                    .getDeclaredConstructor();
            callCenterAuthenticationWSServiceLocatorConstructor.setAccessible(true);

            /*
             * Get constructor AuthenticationResult()
             */
            Constructor<?> callCenterAuthenticationResultConstructor = callCenterAuthenticationResultClass.getDeclaredConstructor();
            callCenterAuthenticationResultConstructor.setAccessible(true);

            /*
             * Get constructor CredentialsDTO(String, String)
             */
            Constructor<?> callCenterCredentialsDTOConstructor = callCenterCredentialsDTOClass.getDeclaredConstructor(String.class,
                    String.class);
            callCenterCredentialsDTOConstructor.setAccessible(true);

            /*
             * Instantiate AuthenticationWSBindingStub object
             */
            Object callCenterAuthenticationWSBindingStubObject = callCenterAuthenticationWSBindingStubConstructor.newInstance(new URL(
                    callCenterWSDLPath.toString()), callCenterAuthenticationWSServiceLocatorConstructor.newInstance());

            /*
             * Get login(CredentialsDTO) method of AuthenticationWSBindingStub object
             */
            Method callCenterAuthenticationWSBindingStubObjectLoginMethod = callCenterAuthenticationWSBindingStubObject.getClass()
                    .getMethod("login", callCenterCredentialsDTOClass);

            /*
             * Invoke login(CredentialsDTO) method of AuthenticationWSBindingStub
             */
            Object callCenterAuthenticationResultObject = callCenterAuthenticationWSBindingStubObjectLoginMethod.invoke(
                    callCenterAuthenticationWSBindingStubObject,
                    callCenterCredentialsDTOConstructor.newInstance(properties.getProperty(ConstantsProperty.CALL_CENTER_LOGIN),
                            properties.getProperty(ConstantsProperty.CALL_CENTER_PASSWORD)));

            /*
             * Get isSuccessful() method of AuthenticationResultObject object
             */
            Method callCenterAuthenticationResultObjectIsSuccessfulMethod = callCenterAuthenticationResultObject.getClass().getMethod(
                    "isSuccessful");

            /*
             * Verify authentication results
             */
            if (Boolean.valueOf(String.valueOf(callCenterAuthenticationResultObjectIsSuccessfulMethod
                    .invoke(callCenterAuthenticationResultObject)))) {
                /*
                 * Get getSessionID() method of AuthenticationResultObject object
                 */
                Method callCenterAuthenticationResultObjectGetSessionIdMethod = callCenterAuthenticationResultObject.getClass().getMethod(
                        "getSessionID");

                /*
                 * Invoke getSessionID() method of AuthenticationResultObject
                 */
                String callCenterSessionId = String.valueOf(callCenterAuthenticationResultObjectGetSessionIdMethod
                        .invoke(callCenterAuthenticationResultObject));

                /*
                 * Get getWebServices() method of AuthenticationResultObject object
                 */
                Method callCenterAuthenticationResultObjectGetWebServicesMethod = callCenterAuthenticationResultObject.getClass()
                        .getMethod("getWebServices");

                /*
                 * Invoke getWebServices() method of AuthenticationResultObject
                 */
                String[] callCenterWebServices = (String[]) callCenterAuthenticationResultObjectGetWebServicesMethod
                        .invoke(callCenterAuthenticationResultObject);

                if (logger.isDebugEnabled()) {
                    for (String webService : callCenterWebServices) {
                        logger.debug("Returned WebServices:");
                        logger.debug(webService);
                    }
                }

                /*
                 * WSDL point path
                 */
                StringBuilder WSDLPath = new StringBuilder().append(properties.getProperty(ConstantsProperty.CALL_CENTER_PROTOCOL))
                        .append("://").append(properties.getProperty(ConstantsProperty.CALL_CENTER_SERVER_ADDRESS)).append(":")
                        .append(properties.getProperty(ConstantsProperty.CALL_CENTER_SERVER_PORT)).append("/")
                        .append(properties.getProperty(ConstantsProperty.CALL_CENTER_POINT)).append("/");

                /*
                 * Generate remaining WSDL
                 */
                for (String webService : callCenterWebServices) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Working with WebService:");
                        logger.debug(WSDLPath.toString() + webService + ";jsessionid=" + callCenterSessionId + "?wsdl");
                    }

                    /*
                     * For details execute as WSDL2Code.main(new String[] { "" } ); and see usage details
                     */
                    WSDL2Code.main(new String[] { "-o", properties.getProperty(ConstantsProperty.WSDL_SOURCE_FOLDER),
                            WSDLPath.toString() + webService + ";jsessionid=" + callCenterSessionId + "?wsdl" });
                }
            } else {
                throw new RuntimeException("User is not authenticated");
            }

            // TODO Divide Call Center and Web
            // Web WSDL path
            StringBuilder webWSDLPath = new StringBuilder().append(properties.getProperty(ConstantsProperty.WEB_PROTOCOL)).append("://")
                    .append(properties.getProperty(ConstantsProperty.WEB_SERVER_ADDRESS)).append(":")
                    .append(properties.getProperty(ConstantsProperty.WEB_SERVER_PORT)).append("/")
                    .append(properties.getProperty(ConstantsProperty.WEB_POINT)).append("/")
                    .append(properties.getProperty(ConstantsProperty.WEB_AUTHENTICATION_WSDL)).append("?wsdl");

            /*
             * Path to generated from WSDL java sources
             */
            StringBuilder webJavaSourcePath = new StringBuilder().append(System.getProperty("user.dir"))
                    .append(System.getProperty("file.separator")).append(properties.getProperty(ConstantsProperty.WSDL_SOURCE_FOLDER));

            /*
             * Path to compiled WSDL java classes
             */
            StringBuilder webJavaCompilePath = new StringBuilder().append(System.getProperty("user.dir"))
                    .append(System.getProperty("file.separator")).append(properties.getProperty(ConstantsProperty.WSDL_CLASSES_FOLDER));

            /*
             * For details execute as WSDL2Code.main(new String[] { "" } ); and see usage details
             */
            WSDL2Code.main(new String[] { "-o", properties.getProperty(ConstantsProperty.WSDL_SOURCE_FOLDER), webWSDLPath.toString() });

            /*
             * Get folder for WSDL generated java sources
             */
            File webSourceDirectory = new File(webJavaSourcePath.append(System.getProperty("file.separator"))
                    .append(properties.getProperty(ConstantsProperty.WSDL_PACKAGE).replace(".", System.getProperty("file.separator")))
                    .toString());

            /*
             * Create directory for compiled classes
             */
            File webCompileDirectory = new File(webJavaCompilePath.toString());
            if (!webCompileDirectory.exists()) {
                webCompileDirectory.mkdirs();
            }

            /*
             * Get list of java sources generated from WSDL
             */
            String[] webJavaSources = webSourceDirectory.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".java");
                }
            });

            /*
             * Convert sources list to its full pathes
             */
            for (int i = 0; i < webJavaSources.length; i++) {
                webJavaSources[i] = webJavaSourcePath.toString() + System.getProperty("file.separator") + webJavaSources[i];
            }

            if (logger.isDebugEnabled()) {
                logger.debug("List of files generated from " + properties.getProperty(ConstantsProperty.WEB_AUTHENTICATION_WSDL)
                        + " to be compiled:");
                for (String javaSource : webJavaSources) {
                    logger.debug(javaSource);
                }
            }

            /*
             * Compose arguments for java compiler
             */
            List<String> webArguments = new ArrayList<String>();
            // Output directory
            webArguments.add("-d");
            webArguments.add(webCompileDirectory.toString());
            // Current class path
            webArguments.add("-cp");
            webArguments.add(System.getProperty("java.class.path"));
            // Enable all warnings
            webArguments.add("-Xlint:all");
            // Add sources which need to be compiled
            for (String javaSource : webJavaSources) {
                webArguments.add(javaSource);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Call java compiler with next arguments:");
                logger.debug(webArguments);
            }

            /*
             * Compile java sources
             */
            JavaCompiler webJavaCompiler = ToolProvider.getSystemJavaCompiler();
            webJavaCompiler.run(null, null, null, webArguments.toArray(new String[0]));

            /*
             * Load AuthenticationWSBindingStub class generated from AuthenticationWS
             */
            Class<?> webAuthenticationWSBindingStubClass = ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty(ConstantsProperty.WSDL_PACKAGE) + "."
                            + properties.getProperty(ConstantsProperty.AUTHENTICATION_WS_BINDING_STUB));

            /*
             * Load AuthenticationWSServiceLocator class generated from AuthenticationWS
             */
            Class<?> webAuthenticationWSServiceLocatorClass = ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty(ConstantsProperty.WSDL_PACKAGE) + "."
                            + properties.getProperty(ConstantsProperty.AUTHENTICATION_WS_BINDING_SERVICE_LOCATOR));

            /*
             * Load AuthenticationResult class generated from AuthenticationWS
             */
            Class<?> webAuthenticationResultClass = ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty(ConstantsProperty.WSDL_PACKAGE) + "."
                            + properties.getProperty(ConstantsProperty.AUTHENTICATION_WS_AUTHENTICATION_RESULT));

            /*
             * Load CredentialsDTO class generated from AuthenticationWS
             */
            Class<?> webCredentialsDTOClass = ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty(ConstantsProperty.WSDL_PACKAGE) + "."
                            + properties.getProperty(ConstantsProperty.AUTHENTICATION_WS_CREDENTIALS_DTO));

            /*
             * Get constructor AuthenticationWSBindingStub(URL, Service)
             */
            Constructor<?> webAuthenticationWSBindingStubConstructor = webAuthenticationWSBindingStubClass.getDeclaredConstructor(
                    URL.class, Service.class);
            webAuthenticationWSBindingStubConstructor.setAccessible(true);

            /*
             * Get constructor AuthenticationWSServiceLocator()
             */
            Constructor<?> webAuthenticationWSServiceLocatorConstructor = webAuthenticationWSServiceLocatorClass.getDeclaredConstructor();
            webAuthenticationWSServiceLocatorConstructor.setAccessible(true);

            /*
             * Get constructor AuthenticationResult()
             */
            Constructor<?> webAuthenticationResultConstructor = webAuthenticationResultClass.getDeclaredConstructor();
            webAuthenticationResultConstructor.setAccessible(true);

            /*
             * Get constructor CredentialsDTO(String, String)
             */
            Constructor<?> webCredentialsDTOConstructor = webCredentialsDTOClass.getDeclaredConstructor(String.class, String.class);
            webCredentialsDTOConstructor.setAccessible(true);

            /*
             * Instantiate AuthenticationWSBindingStub object
             */
            Object webAuthenticationWSBindingStubObject = webAuthenticationWSBindingStubConstructor.newInstance(
                    new URL(webWSDLPath.toString()), webAuthenticationWSServiceLocatorConstructor.newInstance());

            /*
             * Get login(CredentialsDTO) method of AuthenticationWSBindingStub object
             */
            Method webAuthenticationWSBindingStubObjectLoginMethod = webAuthenticationWSBindingStubObject.getClass().getMethod("login",
                    webCredentialsDTOClass);

            /*
             * Invoke login(CredentialsDTO) method of AuthenticationWSBindingStub
             */
            Object webAuthenticationResultObject = webAuthenticationWSBindingStubObjectLoginMethod.invoke(
                    webAuthenticationWSBindingStubObject,
                    webCredentialsDTOConstructor.newInstance(properties.getProperty(ConstantsProperty.WEB_LOGIN),
                            properties.getProperty(ConstantsProperty.WEB_PASSWORD)));

            /*
             * Get isSuccessful() method of AuthenticationResultObject object
             */
            Method webAuthenticationResultObjectIsSuccessfulMethod = webAuthenticationResultObject.getClass().getMethod("isSuccessful");

            /*
             * Verify authentication results
             */
            if (Boolean.valueOf(String.valueOf(webAuthenticationResultObjectIsSuccessfulMethod.invoke(webAuthenticationResultObject)))) {
                /*
                 * Get getSessionID() method of AuthenticationResultObject object
                 */
                Method authenticationResultObjectGetSessionIdMethod = webAuthenticationResultObject.getClass().getMethod("getSessionID");

                /*
                 * Invoke getSessionID() method of AuthenticationResultObject
                 */
                String sessionId = String.valueOf(authenticationResultObjectGetSessionIdMethod.invoke(webAuthenticationResultObject));

                /*
                 * Get getWebServices() method of AuthenticationResultObject object
                 */
                Method authenticationResultObjectGetWebServicesMethod = webAuthenticationResultObject.getClass()
                        .getMethod("getWebServices");

                /*
                 * Invoke getWebServices() method of AuthenticationResultObject
                 */
                String[] webServices = (String[]) authenticationResultObjectGetWebServicesMethod.invoke(webAuthenticationResultObject);

                if (logger.isDebugEnabled()) {
                    for (String webService : webServices) {
                        logger.debug("Returned WebServices:");
                        logger.debug(webService);
                    }
                }

                /*
                 * WSDL point path
                 */
                StringBuilder WSDLPath = new StringBuilder().append(properties.getProperty(ConstantsProperty.WEB_PROTOCOL)).append("://")
                        .append(properties.getProperty(ConstantsProperty.WEB_SERVER_ADDRESS)).append(":")
                        .append(properties.getProperty(ConstantsProperty.WEB_SERVER_PORT)).append("/")
                        .append(properties.getProperty(ConstantsProperty.WEB_POINT)).append("/");

                /*
                 * Generate remaining WSDL
                 */
                for (String webService : webServices) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Working with WebService:");
                        logger.debug(WSDLPath.toString() + webService + ";jsessionid=" + sessionId + "?wsdl");
                    }

                    /*
                     * For details execute as WSDL2Code.main(new String[] { "" } ); and see usage details
                     */
                    WSDL2Code.main(new String[] { "-o", properties.getProperty(ConstantsProperty.WSDL_SOURCE_FOLDER),
                            WSDLPath.toString() + webService + ";jsessionid=" + sessionId + "?wsdl" });
                }
            } else {
                throw new RuntimeException("User is not authenticated");
            }
        } catch (Exception e) {
            logger.fatal(e);
        }

    }
}
