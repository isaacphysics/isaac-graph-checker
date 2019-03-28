/**
 * Copyright 2019 University of Cambridge
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
package org.isaacphysics.graphchecker.bluefin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.cam.cl.dtg.isaac.graphmarker.dos.GraphAnswer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Examples {
    private static final Logger log = LoggerFactory.getLogger(Examples.class);

    private static final ObjectMapper om = new ObjectMapper();

    public static List<ExampleSet> load() {
        List<ExampleSet> examples = new ArrayList<>();

        File[] directories = new File("../samples").listFiles(File::isDirectory);
        Arrays.stream(directories).forEach(path -> {
            String id = path.getName();
            try {
                ExampleSet exampleSet = om.readValue(new File(path, "specification.json"), ExampleSet.class);

                exampleSet.setId(id);

                Map<String, AnswerStatus> directoryMap = ImmutableMap.of(
                    "unknown", AnswerStatus.UNKNOWN,
                    "correct", AnswerStatus.CORRECT,
                    "incorrect", AnswerStatus.INCORRECT);

                directoryMap.forEach((dirName, status) -> {
                    File[] files = new File(path, dirName).listFiles();
                    if (files != null) {
                        Arrays.stream(files).forEach(file -> {
                            String name = file.getName();
                            try {
                                GraphAnswer answer = om.readValue(file, GraphAnswer.class);
                                exampleSet.getAnswers().put(name, answer);
                                exampleSet.getResults().put(name, status);
                            } catch (IOException e) {
                                log.error("Couldn't load example " + file + ": " + e);
                            }

                        });
                    } else {
                        log.error("Couldn't load example directory named " + dirName);
                    }
                });

                examples.add(exampleSet);
            } catch (IOException e) {
                log.error("Couldn't load example specification for " + id + ": " + e);
            }
        });

        return examples;
    }

    public static void move(String from, String to) throws IOException {
        String[] path = from.split("/");
        File existing = new File("../samples", from);
        path[1] = to;
        File destination = new File("../samples", Joiner.on(File.separator).join(path));

        File destinationDir = new File("../samples", Joiner.on(File.separator).join(path[0], path[1]));

        Files.createDirectories(destinationDir.toPath());

        Files.move(existing.toPath(), destination.toPath());
    }
}
