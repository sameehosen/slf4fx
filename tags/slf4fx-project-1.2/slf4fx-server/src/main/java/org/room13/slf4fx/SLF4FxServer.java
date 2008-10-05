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
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.integration.beans.InetSocketAddressEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Bootstrap class for SLF4Fx server.
 */
public class SLF4FxServer {
    private static final Logger _log = LoggerFactory.getLogger(SLF4FxServer.class);

    private static Options buildOptions() {
        final OptionGroup controlGroup = new OptionGroup();
        OptionBuilder.withLongOpt("bind");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("ADDRESS[:PORT]");
        OptionBuilder.withDescription("bind SLF4Fx server to this address");
        controlGroup.addOption(OptionBuilder.create('b'));

        OptionBuilder.withLongOpt("session-timeout");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("TIMEOUT");
        OptionBuilder.withDescription("session timeout in seconds");
        controlGroup.addOption(OptionBuilder.create('t'));

        OptionBuilder.withLongOpt("policy-file");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("FILE");
        OptionBuilder.withDescription("socket policy file for Adobe Flash Player");
        controlGroup.addOption(OptionBuilder.create('p'));

        OptionBuilder.withLongOpt("reader-buffer-size");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("BYTES");
        OptionBuilder.withDescription("protocol decoder buffer size");
        controlGroup.addOption(OptionBuilder.create('r'));

        OptionBuilder.withLongOpt("known-applications");
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("FILE");
        OptionBuilder.withDescription("known applications descriptor file" +
                "(one pair APPLICATION=SECRET per line)");
        controlGroup.addOption(OptionBuilder.create('k'));

        final OptionGroup helpGroup = new OptionGroup();
        helpGroup.addOption(new Option("h", "help", false, "print this message"));

        return new Options()
                .addOptionGroup(helpGroup)
                .addOptionGroup(controlGroup);
    }

    private static Map<String, String> loadKnownApplications(final File file) {
        final Properties props = new Properties();
        InputStream istream = null;
        try {
            istream = new FileInputStream(file);
            props.load(istream);
        } catch (Exception e) {
            _log.warn("failed to load known application descriptor", e);
        } finally {
            if (istream != null)
                try {
                    istream.close();
                } catch (IOException e) {
                    // ignore
                }
        }

        final Map<String, String> map = new HashMap<String, String>();
        for (final Object key : props.keySet()) {
            map.put(String.valueOf(key), props.getProperty(String.valueOf(key)));
        }
        return map;
    }

    private static String loadPolicyFile(final File file) {
        Reader reader = null;
        try {
            reader = new FileReader(file);
            final StringBuilder sb = new StringBuilder();
            final char[] buffer = new char[4096];
            for (int size; (size = reader.read(buffer)) != -1;) {
                sb.append(buffer, 0, size);
            }
            return sb.toString();
        } catch (Exception e) {
            _log.warn("failed to load policy file", e);
            return null;
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
        }
    }

    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();
        final Options options = buildOptions();
        final CommandLineParser parser = new GnuParser();

        try {
            final ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/slf4fx-context.xml");

            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.setWidth(80);
                formatter.printHelp(
                        "java -jar slf4fx-server.jar [OPTIONS]",
                        String.format("Version %s",context.getMessage("slf4fx.version", null, null)),
                        options,
                        "",
                        false
                );
                System.exit(1);
            }

            _log.info("slf4fx (version {})", context.getMessage("slf4fx.version", null, null));

            final IoAcceptor acceptor = (IoAcceptor) context.getBean("ioAcceptor");
            final SLF4FxStateMachine stateMachine = (SLF4FxStateMachine) context.getBean("stateMachine");

            if (commandLine.hasOption("session-timeout")) {
                stateMachine.setSessionTimeout(Integer.parseInt(commandLine.getOptionValue("session-timeout")));
            }
            _log.info("session timeout is {} seconds", stateMachine.getSessionTimeout());

            if (commandLine.hasOption("reader-buffer-size")) {
                stateMachine.setReaderBufferSize(Integer.parseInt(commandLine.getOptionValue("reader-buffer-size")));
            }
            _log.info("reader buffer size is {}", stateMachine.getReaderBufferSize());

            if (commandLine.hasOption("known-applications")) {
                final File file = new File(commandLine.getOptionValue("known-applications"));
                _log.info("loading known applications from {}", file.getAbsolutePath());
                stateMachine.setKnownApplicaions(loadKnownApplications(file));
            }
            _log.info("known applications {}", stateMachine.getKnownApplicaions().keySet());

            if (commandLine.hasOption("policy-file")) {
                final File file = new File(commandLine.getOptionValue("policy-file"));
                _log.info("loading policy file from {}", file.getAbsolutePath());
                stateMachine.setPolicyContent(loadPolicyFile(file));
            }
            _log.info("support for <policy-file-request/> is {}",
                    stateMachine.getPolicyContent() == null ? "disabled" : "enabled");

            if (commandLine.hasOption("bind")) {
                final InetSocketAddressEditor editor = (InetSocketAddressEditor) context.getBean("socketAddressEditor");
                editor.setAsText(commandLine.getOptionValue("bind"));
                acceptor.setDefaultLocalAddress((SocketAddress) editor.getValue());
            }
            _log.info("listen {}", acceptor.getDefaultLocalAddress());

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    acceptor.unbind();
                    _log.info("server stopped");
                }
            }));

            acceptor.bind();
            _log.info("server started in {}ms", System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            _log.error("failed to start slf4fx server", e);
        } catch (ParseException e) {
            _log.error("failed to parse command line", e);
        }
    }

}