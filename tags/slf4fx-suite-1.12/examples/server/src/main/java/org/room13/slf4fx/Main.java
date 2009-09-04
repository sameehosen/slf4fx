/*
 * Copyright 2008 Dmitry Motylev
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
package org.room13.slf4fx;

import org.apache.commons.cli.*;
import org.apache.mina.integration.beans.InetSocketAddressEditor;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;

/**
 * Bootstrap class for simple SLF4Fx server.
 */
public class Main {
    private static final String DEFAULT_POLICY = "<?xml version='1.0'?>\n" +
            "<!DOCTYPE cross-domain-policy SYSTEM 'http://www.adobe.com/xml/dtds/cross-domain-policy.dtd'>\n" +
            "<cross-domain-policy>\n" +
            "    <allow-access-from domain='*' to-ports='18888'/>\n" +
            "</cross-domain-policy>";

    public static void main(String[] args) {
        configureLogging();
        try {
            final SLF4FxServer server = new SLF4FxServer();

            configureServerFromCommandLine(server, args);

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    server.stop();
                }
            }));

            server.start();
        } catch (IOException e) {
            System.err.println("failed to start slf4fx server");
            e.printStackTrace(System.err);
        } catch (ParseException e) {
            System.err.println("failed to parse command line");
            e.printStackTrace(System.err);
        }
    }

    private static void configureLogging() {
        final File file = new File("log4j.properties");
        if (file.exists()) {
            System.setProperty("log4j.configuration", "file:log4j.properties");
            return;
        }
        System.setProperty("log4j.configuration", "log4j.default.properties");
    }

    private static Options buildOptions() {
        final Options options = new Options();
        OptionBuilder.withLongOpt("bind");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("ADDRESS[:PORT]");
        OptionBuilder.withDescription("bind SLF4Fx server to this address");
        options.addOption(OptionBuilder.create('b'));

        OptionBuilder.withLongOpt("session-timeout");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("TIMEOUT");
        OptionBuilder.withDescription("session timeout in seconds");
        options.addOption(OptionBuilder.create('t'));

        OptionBuilder.withLongOpt("policy-file");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("FILE");
        OptionBuilder.withDescription("socket policy file for Adobe Flash Player");
        options.addOption(OptionBuilder.create('p'));

        OptionBuilder.withLongOpt("disable-policy");
        OptionBuilder.withDescription("disable any socket policy for Adobe Flash Player");
        options.addOption(OptionBuilder.create('d'));

        OptionBuilder.withLongOpt("reader-buffer-size");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("BYTES");
        OptionBuilder.withDescription("protocol decoder buffer size");
        options.addOption(OptionBuilder.create('r'));

        OptionBuilder.withLongOpt("known-applications");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("FILE");
        OptionBuilder.withDescription("known applications descriptor file" +
                "(one pair APPLICATION=SECRET per line)");
        options.addOption(OptionBuilder.create('k'));

        options.addOption(new Option("h", "help", false, "print this message"));

        return options;
    }

    private static void configureServerFromCommandLine(final SLF4FxServer aServer, final String[] args)
            throws ParseException, IOException {
        final Options options = buildOptions();
        final CommandLineParser parser = new GnuParser();

        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(80);
            formatter.printHelp(
                    "java -jar slf4fx-server.jar [OPTIONS]",
                    "SLF4Fx simple server",
                    options,
                    "",
                    false
            );
            System.exit(1);
        }

        if (commandLine.hasOption("session-timeout")) {
            aServer.setSessionTimeout(Integer.parseInt(commandLine.getOptionValue("session-timeout")));
        }

        if (commandLine.hasOption("reader-buffer-size")) {
            aServer.setReaderBufferSize(Integer.parseInt(commandLine.getOptionValue("reader-buffer-size")));
        }

        if (commandLine.hasOption("known-applications")) {
            final File file = new File(commandLine.getOptionValue("known-applications"));
            aServer.setCredentials(file);
        }

        if (commandLine.hasOption("policy-file")) {
            final File file = new File(commandLine.getOptionValue("policy-file"));
            aServer.setFlexPolicyResponse(file);
        } else {
            aServer.setFlexPolicyResponse(DEFAULT_POLICY);
        }

        if (commandLine.hasOption("disable-policy")) {
            aServer.setFlexPolicyResponse((String) null);
        }

        if (commandLine.hasOption("bind")) {
            final InetSocketAddressEditor editor = new InetSocketAddressEditor();
            editor.setAsText(commandLine.getOptionValue("bind"));
            aServer.setDefaultLocalAddress((SocketAddress) editor.getValue());
        }
    }
}
