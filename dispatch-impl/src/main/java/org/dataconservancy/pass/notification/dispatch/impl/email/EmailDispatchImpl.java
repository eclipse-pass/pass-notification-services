/*
 * Copyright 2018 Johns Hopkins University
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
package org.dataconservancy.pass.notification.dispatch.impl.email;

import com.github.jknack.handlebars.Handlebars;
import org.dataconservancy.pass.client.PassClient;
import org.dataconservancy.pass.model.User;
import org.dataconservancy.pass.notification.dispatch.DispatchService;
import org.dataconservancy.pass.notification.model.Notification;
import org.dataconservancy.pass.notification.model.config.NotificationConfig;
import org.dataconservancy.pass.notification.model.config.template.TemplatePrototype;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class EmailDispatchImpl implements DispatchService {

    private NotificationConfig notificationConfig;

    private PassClient passClient;

    private TemplateResolver templateResolver;

    private Handlebars handlebars;

    @Override
    public void dispatch(Notification notification) {

        String emailToAddress = String.join(",", parseRecipientUris(notification.getRecipients()
                .stream().map(URI::create).collect(Collectors.toSet()), passClient));

        String fromAddress = notification.getSender();

        Notification.Type notificationType = notification.getType();

        // resolve templates for subject, body, footer for type

        TemplatePrototype template = notificationConfig.getTemplates().stream()
                .filter(candidate -> candidate.getNotificationType() == notificationType)
                .findAny()
                .orElseThrow(() ->
                        new RuntimeException("Missing notification template for mode '" + notificationType + "'"));

        Map<TemplatePrototype.Name, InputStream> templates =
            template.getBodies()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> templateResolver.resolve(entry.getValue())));

        // perform pararmeterization on all templates

//        Map<TemplatePrototype.Name, String> parameterized =
//                templates.entrySet()
//                .stream()
//


        // compose email
        // send email

    }

    static Collection<String> parseRecipientUris(Collection<URI> recipientUris, PassClient passClient) {
        return recipientUris.stream().map(recipientUri -> {
            if (recipientUri.getScheme().equalsIgnoreCase("mailto")) {
                String to = recipientUri.getSchemeSpecificPart();
                int i;
                if ((i = to.indexOf('?')) > -1) {
                    to = to.substring(0, i);
                }
                return to;
            }

            User u = passClient.readResource(recipientUri, User.class);
            return u.getEmail();
        }).collect(Collectors.toSet());
    }
}
