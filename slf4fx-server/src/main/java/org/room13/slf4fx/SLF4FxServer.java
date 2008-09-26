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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        return new Options()
                .addOption(new Option("h", "help", false, "print this message"))
                .addOption(new Option("b", "bind", true, "bind SLF4Fx server to this ip address and port (ADDRESS:PORT) "))
                .addOption(new Option("t", "session-timeout", true, "session timeout in seconds"))
                .addOption(new Option("k", "known-applications", true, "known applications descriptor file" +
                        "(one pair application=secret per line)"));
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

    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();
        final Options options = buildOptions();
        final CommandLineParser parser = new GnuParser();

        try {
            CommandLine commandLine = parser.parse(options, args);

            if (commandLine.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("SLF4Fx", options);
                return;
            }

            final ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/slf4fx-context.xml");
            _log.info("slf4fx (version {})", context.getMessage("slf4fx.version", null, null));

            final IoAcceptor acceptor = (IoAcceptor) context.getBean("ioAcceptor");
            final SLF4FxStateMachine stateMachine = (SLF4FxStateMachine) context.getBean("stateMachine");

            if (commandLine.hasOption("session-timeout")) {
                stateMachine.setSessionTimeout(Integer.parseInt(commandLine.getOptionValue("session-timeout")));
            }
            _log.info("session timeout is {} seconds", stateMachine.getSessionTimeout());

            if (commandLine.hasOption("known-applications")) {
                final File file = new File(commandLine.getOptionValue("known-applications"));
                _log.info("loading known applications from {}", file.getAbsolutePath());
                stateMachine.setKnownApplicaions(loadKnownApplications(file));
            }
            _log.info("known applications {}", stateMachine.getKnownApplicaions().keySet());

            if (commandLine.hasOption("bind")) {
                final InetSocketAddressEditor editor = (InetSocketAddressEditor) context.getBean("socketAddressEditor");
                editor.setAsText(commandLine.getOptionValue("bind"));
                acceptor.setDefaultLocalAddress((SocketAddress) editor.getValue());
            }
            _log.info("listen {}", acceptor.getDefaultLocalAddress());

            acceptor.bind();
            _log.info("server started in {}ms", System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            _log.error("failed to start slf4fx server", e);
        } catch (ParseException e) {
            _log.error("failed to parse command line", e);
        }
    }
}
