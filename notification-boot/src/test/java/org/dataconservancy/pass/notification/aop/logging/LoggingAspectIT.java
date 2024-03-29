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
package org.dataconservancy.pass.notification.aop.logging;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.dataconservancy.pass.notification.NotificationApp;
import org.dataconservancy.pass.notification.dispatch.DispatchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Simply checks to see that the Dispatch implementation is an Advised instance.
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NotificationApp.class)
public class LoggingAspectIT {

    @Autowired
    private DispatchService dispatchService;

    @Test
    public void dispatchIsAdvised() {
        assertNotNull("DispatchService was not autowired.", dispatchService);
        assertTrue("DispatchService is not an instance of an Advised class.",
                dispatchService instanceof Advised);
    }
}
