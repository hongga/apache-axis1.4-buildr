package org;

import java.util.List;

import org.apache.axis.constants.Scope;
import org.apache.axis.utils.CLArgsParser;
import org.apache.axis.utils.CLOption;
import org.apache.axis.utils.CLOptionDescriptor;
import org.apache.axis.utils.ClassUtils;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.Messages;
import org.apache.axis.wsdl.WSDL2Java;
import org.apache.axis.wsdl.gen.Parser;
import org.apache.axis.wsdl.gen.WSDL2;
import org.apache.axis.wsdl.toJava.Emitter;
import org.apache.axis.wsdl.toJava.NamespaceSelector;
import org.apache.log4j.Logger;

/**
 * WSDL2Code class implementation which is almost copy of {@link WSDL2Java}
 * Method main() from {@link WSDL2} is overridden to avoid exit after success
 * execution
 * 
 * @author oleksandr_kyetov
 * 
 */
public class WSDL2Code extends WSDL2 {

    /**
     * 
     */
    private static final Logger logger = Logger.getLogger(CodeGenerator.class);

    private boolean bPackageOpt = false;
    private static final int SERVER_OPT = 's';
    private static final int SKELETON_DEPLOY_OPT = 'S';
    private static final int NAMESPACE_OPT = 'N';
    private static final int NAMESPACE_FILE_OPT = 'f';
    private static final int PACKAGE_OPT = 'p';
    private static final int OUTPUT_OPT = 'o';
    private static final int SCOPE_OPT = 'd';
    private static final int TEST_OPT = 't';
    private static final int ALL_OPT = 'a';
    private static final int TYPEMAPPING_OPT = 'T';
    private static final int FACTORY_CLASS_OPT = 'F';
    private static final int HELPER_CLASS_OPT = 'H';
    private static final int BUILDFILE_OPT = 'B';
    private static final int USERNAME_OPT = 'U';
    private static final int PASSWORD_OPT = 'P';
    private static final int CLASSPATH_OPT = 'X';
    private static final int NS_INCLUDE_OPT = 'i';
    private static final int NS_EXCLUDE_OPT = 'x';
    private static final int IMPL_CLASS_OPT = 'c';
    private static final int ALLOW_INVALID_URL_OPT = 'u';
    private static final int WRAP_ARRAYS_OPT = 'w';
    private static final CLOptionDescriptor[] options = new CLOptionDescriptor[] {
            new CLOptionDescriptor("server-side", CLOptionDescriptor.ARGUMENT_DISALLOWED, SERVER_OPT, Messages.getMessage("optionSkel00")),
            new CLOptionDescriptor("skeletonDeploy", CLOptionDescriptor.ARGUMENT_REQUIRED, SKELETON_DEPLOY_OPT,
                    Messages.getMessage("optionSkeletonDeploy00")),
            new CLOptionDescriptor("NStoPkg", CLOptionDescriptor.DUPLICATES_ALLOWED + CLOptionDescriptor.ARGUMENTS_REQUIRED_2,
                    NAMESPACE_OPT, Messages.getMessage("optionNStoPkg00")),
            new CLOptionDescriptor("fileNStoPkg", CLOptionDescriptor.ARGUMENT_REQUIRED, NAMESPACE_FILE_OPT,
                    Messages.getMessage("optionFileNStoPkg00")),
            new CLOptionDescriptor("package", CLOptionDescriptor.ARGUMENT_REQUIRED, PACKAGE_OPT, Messages.getMessage("optionPackage00")),
            new CLOptionDescriptor("output", CLOptionDescriptor.ARGUMENT_REQUIRED, OUTPUT_OPT, Messages.getMessage("optionOutput00")),
            new CLOptionDescriptor("deployScope", CLOptionDescriptor.ARGUMENT_REQUIRED, SCOPE_OPT, Messages.getMessage("optionScope00")),
            new CLOptionDescriptor("testCase", CLOptionDescriptor.ARGUMENT_DISALLOWED, TEST_OPT, Messages.getMessage("optionTest00")),
            new CLOptionDescriptor("all", CLOptionDescriptor.ARGUMENT_DISALLOWED, ALL_OPT, Messages.getMessage("optionAll00")),
            new CLOptionDescriptor("typeMappingVersion", CLOptionDescriptor.ARGUMENT_REQUIRED, TYPEMAPPING_OPT,
                    Messages.getMessage("optionTypeMapping00")),
            new CLOptionDescriptor("factory", CLOptionDescriptor.ARGUMENT_REQUIRED, FACTORY_CLASS_OPT,
                    Messages.getMessage("optionFactory00")),
            new CLOptionDescriptor("helperGen", CLOptionDescriptor.ARGUMENT_DISALLOWED, HELPER_CLASS_OPT,
                    Messages.getMessage("optionHelper00")),
            new CLOptionDescriptor("buildFile", CLOptionDescriptor.ARGUMENT_DISALLOWED, BUILDFILE_OPT,
                    Messages.getMessage("optionBuildFile00")),
            new CLOptionDescriptor("user", CLOptionDescriptor.ARGUMENT_REQUIRED, USERNAME_OPT, Messages.getMessage("optionUsername")),
            new CLOptionDescriptor("password", CLOptionDescriptor.ARGUMENT_REQUIRED, PASSWORD_OPT, Messages.getMessage("optionPassword")),
            new CLOptionDescriptor("classpath", CLOptionDescriptor.ARGUMENT_OPTIONAL, CLASSPATH_OPT, Messages.getMessage("optionClasspath")),
            new CLOptionDescriptor("nsInclude", CLOptionDescriptor.DUPLICATES_ALLOWED + CLOptionDescriptor.ARGUMENT_REQUIRED,
                    NS_INCLUDE_OPT, Messages.getMessage("optionNSInclude")),
            new CLOptionDescriptor("nsExclude", CLOptionDescriptor.DUPLICATES_ALLOWED + CLOptionDescriptor.ARGUMENT_REQUIRED,
                    NS_EXCLUDE_OPT, Messages.getMessage("optionNSExclude")),
            new CLOptionDescriptor("implementationClassName", CLOptionDescriptor.ARGUMENT_REQUIRED, IMPL_CLASS_OPT,
                    Messages.getMessage("implementationClassName")),
            new CLOptionDescriptor("allowInvalidURL", CLOptionDescriptor.ARGUMENT_DISALLOWED, ALLOW_INVALID_URL_OPT,
                    Messages.getMessage("optionAllowInvalidURL")),
            new CLOptionDescriptor("wrapArrays", CLOptionDescriptor.ARGUMENT_OPTIONAL, WRAP_ARRAYS_OPT,
                    Messages.getMessage("optionWrapArrays")), };

    /**
     * 
     */
    private Emitter emitter;

    /**
     * 
     */
    public WSDL2Code() {
        emitter = (Emitter) parser;
        this.addOptions(options);
    }

    /**
     * Method main() to imitate axis2
     * 
     * @param args
     */
    public static void main(String[] args) {
        WSDL2Code wsdl2Code = new WSDL2Code();

        wsdl2Code.run(args);
    }

    /**
     * 
     */
    @Override
    protected Parser createParser() {
        return new Emitter();
    }

    /**
     * Parse an option
     * 
     * @param option
     *            is the option
     */
    @SuppressWarnings("unchecked")
    protected void parseOption(CLOption option) {
        switch (option.getId()) {
        case FACTORY_CLASS_OPT:
            emitter.setFactory(option.getArgument());
            break;
        case HELPER_CLASS_OPT:
            emitter.setHelperWanted(true);
            break;
        case SKELETON_DEPLOY_OPT:
            emitter.setSkeletonWanted(JavaUtils.isTrueExplicitly(option.getArgument(0)));
            break;
        case SERVER_OPT:
            emitter.setServerSide(true);
            break;
        case NAMESPACE_OPT:
            String namespace = option.getArgument(0);
            String packageName = option.getArgument(1);
            emitter.getNamespaceMap().put(namespace, packageName);
            break;
        case NAMESPACE_FILE_OPT:
            emitter.setNStoPkg(option.getArgument());
            break;
        case PACKAGE_OPT:
            bPackageOpt = true;
            emitter.setPackageName(option.getArgument());
            break;
        case OUTPUT_OPT:
            emitter.setOutputDir(option.getArgument());
            break;
        case SCOPE_OPT:
            String arg = option.getArgument();
            Scope scope = Scope.getScope(arg, null);
            if (scope != null) {
                emitter.setScope(scope);
            } else {
                System.err.println(Messages.getMessage("badScope00", arg));
            }
            break;
        case TEST_OPT:
            emitter.setTestCaseWanted(true);
            break;
        case BUILDFILE_OPT:
            emitter.setBuildFileWanted(true);
            break;
        case ALL_OPT:
            emitter.setAllWanted(true);
            break;
        case TYPEMAPPING_OPT:
            String tmValue = option.getArgument();
            if (tmValue.equals("1.0")) {
                emitter.setTypeMappingVersion("1.0");
            } else if (tmValue.equals("1.1")) {
                emitter.setTypeMappingVersion("1.1");
            } else if (tmValue.equals("1.2")) {
                emitter.setTypeMappingVersion("1.2");
            } else if (tmValue.equals("1.3")) {
                emitter.setTypeMappingVersion("1.3");
            } else {
                System.out.println(Messages.getMessage("badTypeMappingOption00"));
            }
            break;
        case USERNAME_OPT:
            emitter.setUsername(option.getArgument());
            break;
        case PASSWORD_OPT:
            emitter.setPassword(option.getArgument());
            break;
        case CLASSPATH_OPT:
            ClassUtils.setDefaultClassLoader(ClassUtils.createClassLoader(option.getArgument(), this.getClass().getClassLoader()));
            break;
        case NS_INCLUDE_OPT:
            NamespaceSelector include = new NamespaceSelector();
            include.setNamespace(option.getArgument());
            emitter.getNamespaceIncludes().add(include);
            break;
        case NS_EXCLUDE_OPT:
            NamespaceSelector exclude = new NamespaceSelector();
            exclude.setNamespace(option.getArgument());
            emitter.getNamespaceExcludes().add(exclude);
            break;
        case IMPL_CLASS_OPT:
            emitter.setImplementationClassName(option.getArgument());
            break;
        case ALLOW_INVALID_URL_OPT:
            emitter.setAllowInvalidURL(true);
            break;
        case WRAP_ARRAYS_OPT:
            emitter.setWrapArrays(true);
            break;
        default:
            super.parseOption(option);
        }
    }

    /**
     * validateOptions This method is invoked after the options are set to
     * validate the option settings.
     */
    protected void validateOptions() {
        super.validateOptions();

        if (emitter.isSkeletonWanted() && !emitter.isServerSide()) {
            System.out.println(Messages.getMessage("badSkeleton00"));
            printUsage();
        }

        if (!emitter.getNamespaceMap().isEmpty() && bPackageOpt) {
            System.out.println(Messages.getMessage("badpackage00"));
            printUsage();
        }
    }

    /**
     * Override WSDL2 run() to prevent exit after successful execution
     * See {@link WSDL2}.run() for more details
     */
    @Override
    public void run(String[] args) {
        try {
            CLArgsParser argsParser = new CLArgsParser(args, options);
            if (null != argsParser.getErrorString()) {
                logger.error(argsParser.getErrorString());
                this.printUsage();
            }

            @SuppressWarnings("rawtypes")
            List clOptions = argsParser.getArguments();
            int size = clOptions.size();
            for (int i = 0; i < size; i++) {
                parseOption((CLOption) clOptions.get(i));
            }

            this.validateOptions();
            parser.run(wsdlURI);
        } catch (Exception e) {
            logger.fatal(e);
        }
    }
}
