/*
 *  Copyright 2012-2013 E.Hooijmeijer
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package nl.tweeenveertig.cloudie.ops;

import java.io.File;

import nl.tweeenveertig.cloudie.ops.CloudieOperations.CloudieCallback;
import nl.tweeenveertig.cloudie.util.AsyncWrapper;

import org.javaswift.joss.client.mock.ClientMock;
import org.javaswift.joss.exception.CommandException;
import org.javaswift.joss.model.Account;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CloudieOperationsAsyncTest {

    private ClientMock client;
    private CloudieOperations ops;
    private CloudieCallback callback;
    private Account account;

    @Before
    public void init() {
        client = new ClientMock();
        client.setAllowEveryone(true);
        account = client.authenticate("", "", "", "");
        ops = AsyncWrapper.async(new CloudieOperationsImpl(client, account));
        callback = Mockito.mock(CloudieCallback.class);
    }

    @Test
    public void shouldSignalStartAndDone() throws InterruptedException {
        ops.createContainer(new ContainerSpecification("x", true), callback);
        Thread.sleep(500L);
        Mockito.verify(callback, Mockito.atLeastOnce()).onStart();
        Mockito.verify(callback, Mockito.atLeastOnce()).onDone();
    }

    @Test
    public void shouldSignalCommandException() throws InterruptedException {
        ops.createStoredObjects(account.getContainer("x"), new File[] { new File("pom.xml") }, callback); // container
                                                                                                          // does
                                                                                                          // not
                                                                                                          // exist.
        Thread.sleep(500L);
        Mockito.verify(callback, Mockito.atLeastOnce()).onStart();
        Mockito.verify(callback, Mockito.atLeastOnce()).onError(Mockito.any(CommandException.class));
        Mockito.verify(callback, Mockito.atLeastOnce()).onDone();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAcceptNullCallback() {
        ops.createContainer(new ContainerSpecification("x", true), null);
    }
}
