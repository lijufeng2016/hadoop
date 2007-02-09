/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.mapred;

import java.io.IOException;
import java.io.File;
import junit.framework.TestCase;

/**
 * A JUnit test to test min map-reduce cluster with local file system.
 *
 * @author Milind Bhandarkar
 */
public class TestMiniMRLocalFS extends TestCase {
  
  static final int NUM_MAPS = 10;
  static final int NUM_SAMPLES = 100000;
  private static String TEST_ROOT_DIR =
    new File(System.getProperty("test.build.data","/tmp"))
    .toString().replace(' ', '+');
    
  public void testWithLocal() throws IOException {
      MiniMRCluster mr = null;
      try {
          mr = new MiniMRCluster(60030, 60040, 2, "local", false, 3);
          double estimate = PiEstimator.launch(NUM_MAPS, NUM_SAMPLES, 
                                               mr.createJobConf());
          double error = Math.abs(Math.PI - estimate);
          assertTrue("Error in PI estimation "+error+" exceeds 0.01", (error < 0.01));
          // run the wordcount example with caching
          JobConf job = mr.createJobConf();
          boolean ret = MRCaching.launchMRCache(TEST_ROOT_DIR + "/wc/input",
                                                TEST_ROOT_DIR + "/wc/output", 
                                                job,
                                                "The quick brown fox\nhas many silly\n"
                                                    + "red fox sox\n");
          // assert the number of lines read during caching
          assertTrue("Failed test archives not matching", ret);
          // test the task report fetchers
          JobClient client = new JobClient(job);
          TaskReport[] reports = client.getMapTaskReports("job_0001");
          assertEquals("number of maps", 10, reports.length);
          reports = client.getReduceTaskReports("job_0001");
          assertEquals("number of reduces", 1, reports.length);
      } finally {
          if (mr != null) { mr.shutdown(); }
      }
  }
}
