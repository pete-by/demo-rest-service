package com.axamit.springboot.demo.demorestservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This controller outputs build information.
 * See https://www.baeldung.com/spring-git-information
 * See also GitInfoContributor in Spring Actuator
 */
@RestController
public class CommitInfoController {

    private static final Logger logger = LoggerFactory.getLogger(CommitInfoController.class);

    @Value("${git.commit.message.short}")
    private String commitMessage;

    @Value("${git.branch}")
    private String branch;

    @Value("${git.commit.id}")
    private String commitId;

    @GetMapping("/version/commit")
    public Map<String, String> getCommitId() {
        Map<String, String> result = new HashMap<>();
        result.put("Commit message",commitMessage);
        result.put("Commit branch", branch);
        result.put("Commit id", commitId);
        return result;
    }

    @GetMapping(value = "/version")
    public String versionInformation() {
        return readGitInfo();
    }

    private String readGitInfo() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream("git.properties");
        if (is != null) {
            try (InputStream bis = new BufferedInputStream(is)) {
                Properties props = new Properties();
                props.load(bis);
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(props);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return "Version information could not be retrieved";
    }

}