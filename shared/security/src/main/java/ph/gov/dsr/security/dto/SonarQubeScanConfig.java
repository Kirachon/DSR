package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration DTO for SonarQube scans
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SonarQubeScanConfig {

    private String projectKey;
    private String projectName;
    private String projectVersion;
    private String sourceDirectory;
    private String qualityGate;
    private String qualityProfile;
    private String language;
    private String encoding;
    private String exclusions;
    private String inclusions;
    private String testInclusions;
    private String testExclusions;
    private String coverageExclusions;
    private String duplicateExclusions;
    private String issueExclusions;
    private String branch;
    private String pullRequest;
    private String baseDir;
    private String workDir;
    private String binaries;
    private String libraries;
    private String javaVersion;
    private String javaSource;
    private String javaTarget;
    private String javaClasspath;
    private String javaLibraries;
    private String javaBinaries;
    private String javaTest;
    private String javaTestClasspath;
    private String javaTestLibraries;
    private String javaTestBinaries;
    private List<String> additionalProperties;
    private String token;
    private String host;
    private Integer port;
    private String login;
    private String password;
    private Boolean skipTests;
    private Boolean verbose;
    private Boolean debug;
    private String logLevel;
    
    /**
     * Check if quality gate is configured
     */
    public boolean hasQualityGate() {
        return qualityGate != null && !qualityGate.trim().isEmpty();
    }
    
    /**
     * Check if custom quality profile is configured
     */
    public boolean hasCustomQualityProfile() {
        return qualityProfile != null && !qualityProfile.trim().isEmpty();
    }
    
    /**
     * Check if branch analysis is configured
     */
    public boolean isBranchAnalysis() {
        return branch != null && !branch.trim().isEmpty();
    }
    
    /**
     * Check if pull request analysis is configured
     */
    public boolean isPullRequestAnalysis() {
        return pullRequest != null && !pullRequest.trim().isEmpty();
    }
    
    /**
     * Check if authentication is configured
     */
    public boolean hasAuthentication() {
        return (token != null && !token.trim().isEmpty()) ||
               (login != null && !login.trim().isEmpty() && 
                password != null && !password.trim().isEmpty());
    }
    
    /**
     * Get effective encoding (default to UTF-8 if not specified)
     */
    public String getEffectiveEncoding() {
        return encoding != null ? encoding : "UTF-8";
    }
    
    /**
     * Get effective language (default to java if not specified)
     */
    public String getEffectiveLanguage() {
        return language != null ? language : "java";
    }
    
    /**
     * Get effective java version (default to 11 if not specified)
     */
    public String getEffectiveJavaVersion() {
        return javaVersion != null ? javaVersion : "11";
    }
    
    /**
     * Get SonarQube server URL
     */
    public String getSonarQubeUrl() {
        String serverHost = host != null ? host : "localhost";
        int serverPort = port != null ? port : 9000;
        return String.format("http://%s:%d", serverHost, serverPort);
    }
    
    /**
     * Get effective base directory (default to current directory if not specified)
     */
    public String getEffectiveBaseDir() {
        return baseDir != null ? baseDir : ".";
    }
    
    /**
     * Get effective source directory (default to src if not specified)
     */
    public String getEffectiveSourceDirectory() {
        return sourceDirectory != null ? sourceDirectory : "src";
    }
    
    /**
     * Validate configuration
     */
    public boolean isValid() {
        return projectKey != null && !projectKey.trim().isEmpty() &&
               sourceDirectory != null && !sourceDirectory.trim().isEmpty();
    }
}
