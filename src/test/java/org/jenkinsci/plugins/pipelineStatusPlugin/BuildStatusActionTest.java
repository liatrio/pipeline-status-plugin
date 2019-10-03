/*
 * The MIT License
 *
 * Copyright 2017 jxpearce.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.pipelineStatusPlugin;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author jxpearce
 */
public class BuildStatusActionTest {

    static String jobName = "mock-job";
    static String stageName = "Stage 1";
    static String repoName = "mock-repo";
    static String branchName = "mock-branch";
    static String sha = "mock-sha";
    static String targetUrl = "http://mock-target";
    Run<?,?> mockRun;

    public BuildStatusActionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        mockRun = mock(AbstractBuild.class);
        when(mockRun.getExternalizableId()).thenReturn(jobName);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIsDeclarativePipelineFalse() throws IOException {
        BuildStatusAction instance = new BuildStatusAction(mockRun, targetUrl, new ArrayList<>());
        instance.setIsDeclarativePipeline(false);

        assumeFalse(instance.isIsDeclarativePipeline());
    }

    @Test
    public void testIsDeclarativePipelineTrue() throws IOException {
        BuildStatusAction instance = new BuildStatusAction(mockRun, targetUrl, new ArrayList<>());
        instance.setIsDeclarativePipeline(true);

        assumeTrue(instance.isIsDeclarativePipeline());
    }
}
