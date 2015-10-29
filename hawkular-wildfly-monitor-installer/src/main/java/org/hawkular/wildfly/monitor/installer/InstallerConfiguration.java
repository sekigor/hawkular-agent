/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.wildfly.monitor.installer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Installer values to be used for installation.
 *
 * @author lzoubek@redhat.com
 */
public class InstallerConfiguration {
    static final String OPTION_INSTALLER_CONFIG = "installer-config";
    static final String OPTION_WILDFLY_HOME = "wildfly-home";
    static final String OPTION_MODULE_DISTRIBUTION = "module-dist";
    static final String OPTION_SERVER_CONFIG = "server-config";
    static final String OPTION_HAWKULAR_SERVER_URL = "hawkular-server-url";
    static final String OPTION_KEYSTORE_PATH = "keystore-path";
    static final String OPTION_KEYSTORE_PASSWORD = "keystore-password";
    static final String OPTION_KEY_PASSWORD = "key-password";
    static final String OPTION_KEY_ALIAS = "key-alias";
    static final String OPTION_HAWKULAR_USERNAME = "hawkular-username";
    static final String OPTION_HAWKULAR_PASSWORD = "hawkular-password";
    static final String OPTION_HAWKULAR_TOKEN = "hawkular-token";

    static Options buildCommandLineOptions() {
        Options options = new Options();

        options.addOption(Option.builder("D")
                .hasArgs()
                .valueSeparator('=')
                .build());
        options.addOption(Option.builder()
                .argName(InstallerConfiguration.OPTION_INSTALLER_CONFIG)
                .longOpt(InstallerConfiguration.OPTION_INSTALLER_CONFIG)
                .desc("Installer .properties configuration file")
                .numberOfArgs(1)
                .build());
        options.addOption(Option.builder()
                .argName(InstallerConfiguration.OPTION_WILDFLY_HOME)
                .longOpt(InstallerConfiguration.OPTION_WILDFLY_HOME)
                .desc("Target WildFly home directory")
                .numberOfArgs(1)
                .build());
        options.addOption(Option.builder()
                .argName(InstallerConfiguration.OPTION_MODULE_DISTRIBUTION)
                .longOpt(InstallerConfiguration.OPTION_MODULE_DISTRIBUTION)
                .desc("Hawkular WildFly Monitor Agent Module distribution zip file")
                .numberOfArgs(1)
                .build());
        options.addOption(Option.builder()
                .argName(InstallerConfiguration.OPTION_HAWKULAR_SERVER_URL)
                .longOpt(InstallerConfiguration.OPTION_HAWKULAR_SERVER_URL)
                .desc("Hawkular Server URL")
                .numberOfArgs(1)
                .build());
        options.addOption(Option.builder()
                .argName(InstallerConfiguration.OPTION_SERVER_CONFIG)
                .longOpt(InstallerConfiguration.OPTION_SERVER_CONFIG)
                .desc("Server config to write to. Can be either absolute path or relative to "
                        + InstallerConfiguration.OPTION_WILDFLY_HOME)
                .numberOfArgs(1)
                .build());

        // SSL related config options
        options.addOption(Option.builder()
                .argName(InstallerConfiguration.OPTION_KEYSTORE_PATH)
                .longOpt(InstallerConfiguration.OPTION_KEYSTORE_PATH)
                .desc("Keystore file. Required when " + InstallerConfiguration.OPTION_HAWKULAR_SERVER_URL
                        + " protocol is https")
                .numberOfArgs(1)
                .build());
        options.addOption(Option.builder()
                .argName(InstallerConfiguration.OPTION_KEYSTORE_PASSWORD)
                .longOpt(InstallerConfiguration.OPTION_KEYSTORE_PASSWORD)
                .desc("Keystore password. When " + InstallerConfiguration.OPTION_HAWKULAR_SERVER_URL
                        + " protocol is https and this option is not passed, installer will ask for password")
                .numberOfArgs(1)
                .build());
        options.addOption(Option.builder()
                .argName(InstallerConfiguration.OPTION_KEY_PASSWORD)
                .longOpt(InstallerConfiguration.OPTION_KEY_PASSWORD)
                .desc("Key password. When " + InstallerConfiguration.OPTION_HAWKULAR_SERVER_URL
                        + " protocol is https and this option is not passed, installer will ask for password")
                .numberOfArgs(1)
                .build());
        options.addOption(Option.builder()
                .argName(InstallerConfiguration.OPTION_KEY_ALIAS)
                .longOpt(InstallerConfiguration.OPTION_KEY_ALIAS)
                .desc("Key alias. Required when " + InstallerConfiguration.OPTION_HAWKULAR_SERVER_URL
                        + " protocol is https")
                .numberOfArgs(1)
                .build());
        options.addOption(Option.builder()
                .argName(InstallerConfiguration.OPTION_HAWKULAR_USERNAME)
                .longOpt(InstallerConfiguration.OPTION_HAWKULAR_USERNAME)
                .desc("User the agent will use when connecting to Hawkular Server. Ignored if a token is provided.")
                .numberOfArgs(1)
                .build());
        options.addOption(Option
                .builder()
                .argName(InstallerConfiguration.OPTION_HAWKULAR_PASSWORD)
                .longOpt(InstallerConfiguration.OPTION_HAWKULAR_PASSWORD)
                .desc("Credentials agent will use when connecting to Hawkular Server. Ignored if a token is provided.")
                .numberOfArgs(1)
                .build());
        options.addOption(Option.builder()
                .argName(InstallerConfiguration.OPTION_HAWKULAR_TOKEN)
                .longOpt(InstallerConfiguration.OPTION_HAWKULAR_TOKEN)
                .desc("Security token agent will use when connecting to Hawkular Server.")
                .numberOfArgs(1)
                .build());

        return options;
    }

    private final Properties properties;

    public InstallerConfiguration(CommandLine commandLine) throws IOException {
        this.properties = new Properties();

        // we allow the user to set system properties through the -D argument
        Properties sysprops = commandLine.getOptionProperties("D");
        for (Map.Entry<Object, Object> sysprop : sysprops.entrySet()) {
            System.setProperty(sysprop.getKey().toString(), sysprop.getValue().toString());
        }

        // seed our properties with the installer config properties file
        String installerConfig = commandLine.getOptionValue(OPTION_INSTALLER_CONFIG,
                "classpath:/hawkular-wildfly-monitor-installer.properties");

        if (installerConfig.startsWith("classpath:")) {
            installerConfig = installerConfig.substring(10);
            if (!installerConfig.startsWith("/")) {
                installerConfig = "/" + installerConfig;
            }
            properties.load(InstallerConfiguration.class.getResourceAsStream(installerConfig));
        } else if (installerConfig.matches("(http|https|file):.*")) {
            URL installerConfigUrl = new URL(installerConfig);
            try (InputStream is = installerConfigUrl.openStream()) {
                properties.load(is);
            }
        } else {
            File installerConfigFile = new File(installerConfig);
            try (FileInputStream fis = new FileInputStream(installerConfigFile)) {
                properties.load(fis);
            }
        }

        // now we override the defaults with options the user provided us
        setProperty(properties, commandLine, OPTION_WILDFLY_HOME);
        setProperty(properties, commandLine, OPTION_MODULE_DISTRIBUTION);
        setProperty(properties, commandLine, OPTION_SERVER_CONFIG);
        setProperty(properties, commandLine, OPTION_HAWKULAR_SERVER_URL);
        setProperty(properties, commandLine, OPTION_KEYSTORE_PATH);
        setProperty(properties, commandLine, OPTION_KEYSTORE_PASSWORD);
        setProperty(properties, commandLine, OPTION_KEY_PASSWORD);
        setProperty(properties, commandLine, OPTION_KEY_ALIAS);
        setProperty(properties, commandLine, OPTION_HAWKULAR_USERNAME);
        setProperty(properties, commandLine, OPTION_HAWKULAR_PASSWORD);
        setProperty(properties, commandLine, OPTION_HAWKULAR_TOKEN);
    }

    private void setProperty(Properties props, CommandLine commandLine, String option) {
        String value = commandLine.getOptionValue(option);
        if (value != null) {
            properties.setProperty(option, value);
        }
    }

    public String getInstallerConfig() {
        return properties.getProperty(OPTION_INSTALLER_CONFIG);
    }

    public String getWildFlyHome() {
        return properties.getProperty(OPTION_WILDFLY_HOME);
    }

    public String getModuleDistribution() {
        return properties.getProperty(OPTION_MODULE_DISTRIBUTION);
    }

    public String getServerConfig() {
        return properties.getProperty(OPTION_SERVER_CONFIG);
    }

    public String getHawkularServerUrl() {
        return properties.getProperty(OPTION_HAWKULAR_SERVER_URL);
    }

    public String getKeystorePath() {
        return properties.getProperty(OPTION_KEYSTORE_PATH);
    }

    public String getKeystorePassword() {
        return properties.getProperty(OPTION_KEYSTORE_PASSWORD);
    }

    public String getKeyPassword() {
        return properties.getProperty(OPTION_KEY_PASSWORD);
    }

    public String getKeyAlias() {
        return properties.getProperty(OPTION_KEY_ALIAS);
    }

    public String getHawkularUsername() {
        return properties.getProperty(OPTION_HAWKULAR_USERNAME);
    }

    public String getHawkularPassword() {
        return properties.getProperty(OPTION_HAWKULAR_PASSWORD);
    }

    public String getHawkularToken() {
        return properties.getProperty(OPTION_HAWKULAR_TOKEN);
    }
}