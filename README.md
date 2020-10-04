# GitHub Branch Source Pull Request Change Filter

[![Build Status](https://ci.jenkins.io/job/Plugins/job/github-branch-pr-change-filter-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/github-branch-pr-change-filter-plugin/job/master/)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/github-branch-pr-change-filter-plugin.svg)](https://github.com/jenkinsci/github-branch-pr-change-filter-plugin/graphs/contributors)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/github-branch-pr-change-filter.svg)](https://plugins.jenkins.io/github-branch-pr-change-filter)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/github-branch-pr-change-filter-plugin.svg?label=changelog)](https://github.com/jenkinsci/github-branch-pr-change-filter-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/github-branch-pr-change-filter.svg?color=blue)](https://plugins.jenkins.io/github-branch-pr-change-filter)

## Introduction

When using a GitHub branch source, this trait allows you to filter discovered pull requests by including only those that match a provided regex.

![Screenshot of Plugin In Action](media/screenshot_01.png)

## Getting started

Add a discovery trait a provide a valid regex to match against full file names. If any files in a PR matched the provided regex, the discovered pull request will be included.

## Issues

Report issues and enhancements in the [Jenkins issue tracker](https://issues.jenkins-ci.org/).

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)
