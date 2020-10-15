package io.jenkins.plugins.github_branch_pr_change_filter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.console.HyperlinkNote;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.trait.Discovery;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceContext;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSourceRequest;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * A {@link Discovery} trait for GitHub that will only
 * select pull requests that match a given regex
 *
 */
public class PathBasedPullRequestFilterTrait extends SCMSourceTrait {
  private static final String DEFAULT_MATCH_ALL_REGEX = ".*";

  /**
   * The regex for including pull request files changed.
   */
  private String inclusionField = DEFAULT_MATCH_ALL_REGEX;

  /**
   * The regex for excluding pull request files changed.
   */
  private String exclusionField;

  /**
   * The pattern compiled from supplied inclusion regex
   */
  private Pattern inclusionPattern;

  /**
   * The pattern compiled from supplied exclusion regex
   */
  private Pattern exclusionPattern;

  public String getInclusionField() {
    return this.inclusionField;
  }

  public String getExclusionField() {
    return this.exclusionField;
  }

  /**
   * Constructor for stapler.
   *
   * @param inclusionField Path regex for which pull request files to include
   * @param exclusionField Path regex for which pull request files to exclude
   */
  @DataBoundConstructor
  public PathBasedPullRequestFilterTrait(String inclusionField, String exclusionField) {
    // TODO Allow flags to change via checkboxes

    this.inclusionField = inclusionField;
    this.inclusionPattern = Pattern.compile(inclusionField, Pattern.CASE_INSENSITIVE);

    this.exclusionField = exclusionField;
    this.exclusionPattern = Pattern.compile(exclusionField, Pattern.CASE_INSENSITIVE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void decorateContext(SCMSourceContext<?, ?> context) {
    GitHubSCMSourceContext ctx = (GitHubSCMSourceContext) context;
    ctx.withFilter(getScmHeadFilter());
  }

  private Boolean pathIsIncluded(String path) {
    if(path == null || path == ""){
      return false;
    }
    else if (DEFAULT_MATCH_ALL_REGEX.equals(inclusionField) || inclusionField == null || inclusionPattern == null) {
      return true;
    }

    return inclusionPattern.matcher(path).matches();
  }

  private Boolean pathIsNotExcluded(String path) {
    if(path == null || path == "" || exclusionField == null || exclusionPattern == null){
      return true;
    }
    else if(DEFAULT_MATCH_ALL_REGEX.equals(exclusionField)) {
      return false;
    }

    return !exclusionPattern.matcher(path).matches();
  }

  private SCMHeadFilter getScmHeadFilter() {
    SCMHeadFilter scmHeadFilter = new SCMHeadFilter() {

      @Override
      public boolean isExcluded(@NonNull SCMSourceRequest request, @NonNull SCMHead head)
          throws IOException, InterruptedException {
        if (request instanceof GitHubSCMSourceRequest && head instanceof PullRequestSCMHead) {
          for (GHPullRequest ghPullRequest : ((GitHubSCMSourceRequest)request).getPullRequests()) {
            if (ghPullRequest.getNumber() == ((PullRequestSCMHead) head).getNumber()) {
              for (GHPullRequestFileDetail fileDetail : ghPullRequest.listFiles()) {
                String filename = fileDetail.getFilename();
                if (pathIsIncluded(filename) && pathIsNotExcluded(filename)) {
                  request.listener().getLogger().format("%n    Will Build PR %s. Found matching file : %s%n",
                      HyperlinkNote.encodeTo(ghPullRequest.getHtmlUrl().toString(), "#" + ghPullRequest.getNumber()),
                      filename);
                  return false;
                }
                String previousFilename = fileDetail.getFilename();
                if (pathIsIncluded(previousFilename) && pathIsNotExcluded(previousFilename)) {
                  request.listener().getLogger().format("%n    Will Build PR %s. Found matching (previous) file : %s%n",
                      HyperlinkNote.encodeTo(ghPullRequest.getHtmlUrl().toString(), "#" + ghPullRequest.getNumber()),
                      fileDetail.getPreviousFilename());
                  return false;
                }
              }
            }
          }
          return true;
        }

        return false;
      }
    };

    return scmHeadFilter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean includeCategory(@NonNull SCMHeadCategory category) {
    return category instanceof ChangeRequestSCMHeadCategory;
  }

  @Extension
  @Discovery
  public static class DescriptorImpl extends SCMSourceTraitDescriptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
      return "Include discovered GitHub pull requests by changed files via regex";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends SCMSourceContext<?, ?>> getContextClass() {
      return GitHubSCMSourceContext.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends SCMSource> getSourceClass() {
      return GitHubSCMSource.class;
    }

    @Restricted(NoExternalUse.class)
    public FormValidation doCheckInclusionField(@QueryParameter String value) {
      FormValidation formValidation;
      try {
        if (value.trim().isEmpty()) {
          formValidation = FormValidation.error("Must provide regex for inclusion.");
        } else {
          Pattern.compile(value);
          formValidation = FormValidation.ok();
        }
      } catch (PatternSyntaxException e) {
        formValidation = FormValidation.error("Invalid Regex : " + e.getMessage());
      }

      return formValidation;
    }

    @Restricted(NoExternalUse.class)
    public FormValidation doCheckExclusionField(@QueryParameter String value) {
      FormValidation formValidation;
      try {
        if (!value.trim().isEmpty()) {
          if(DEFAULT_MATCH_ALL_REGEX.equals(value)) {
            return FormValidation.warning("All files will be excluded.");
          } else {
            Pattern.compile(value);
          }
        }
        formValidation = FormValidation.ok();
      } catch (PatternSyntaxException e) {
        formValidation = FormValidation.error("Invalid Regex : " + e.getMessage());
      }

      return formValidation;
    }
  }
}
