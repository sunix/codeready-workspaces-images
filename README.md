## Midstream code
This repo is used to house identical copies of the code used to build the **CodeReady Workspaces images** in Brew/OSBS, but made public to enable pull requests and easier contribution.

* Downstream code can be found in http://pkgs.devel.redhat.com/cgit/?q=codeready-workspaces
    - select the `crw-2-rhel-8` branch for the latest `2.x` synced from upstream main branches, or 
    - select a branch like `crw-2.7-rhel-8` for a specific release, synced to a stable branch like `7.26.x`.

## Jenkins jobs

This repo also contains an identical copy of the [Jenkinsfiles and groovy](https://gitlab.cee.redhat.com/codeready-workspaces/crw-jenkins/-/tree/master/jobs/CRW_CI) sources used to configure the [jenkins-csb](https://gitlab.cee.redhat.com/ccit/jenkins-csb) Configuration-as-Code (casc) Jenkins instance used to build the artifacts needed for Brew/OSBS builds. Since the server and config sources are internally-facing, this copy is provided to make it easier to see how CodeReady Workspaces is built. Hooray for open source!

* To run a local Jenkins, see [README](https://gitlab.cee.redhat.com/codeready-workspaces/crw-jenkins/-/blob/master/README.md#first-time-user-setup)
* [Job](https://main-jenkins-csb-crwqe.apps.ocp4.prod.psi.redhat.com/job/CRW_CI/job/Releng/job/sync-jenkins-gitlab-to-github/) that performs the sync from [gitlab](https://gitlab.cee.redhat.com/codeready-workspaces/crw-jenkins/-/blob/master/jobs/CRW_CI/Releng/sync-jenkins-gitlab-to-github.jenkinsfile) to [github](https://github.com/redhat-developer/codeready-workspaces-images/blob/crw-2-rhel-8/crw-jenkins/jobs/CRW_CI/Releng/sync-jenkins-gitlab-to-github.jenkinsfile) at intervals
* Other jobs are used to:
    * [build artifacts](https://github.com/redhat-developer/codeready-workspaces-images/tree/crw-2-rhel-8/crw-jenkins/jobs/CRW_CI/), 
    * [orchestrate Brew builds](https://github.com/redhat-developer/codeready-workspaces-images/blob/crw-2-rhel-8/crw-jenkins/jobs/CRW_CI/get-sources-rhpkg-container-build.groovy), 
    * [copy containers to quay](https://github.com/redhat-developer/codeready-workspaces-images/blob/crw-2-rhel-8/crw-jenkins/jobs/CRW_CI/push-latest-container-to-quay.groovy)
    * [check digests in registries/metadata images](https://github.com/redhat-developer/codeready-workspaces-images/blob/crw-2-rhel-8/crw-jenkins/jobs/CRW_CI/update-digests-in-registries-and-metadata.groovy)
* Or, to:
    * [send email notifications](https://github.com/redhat-developer/codeready-workspaces-images/blob/crw-2-rhel-8/crw-jenkins/jobs/CRW_CI/Releng/send-email-qe-build-list.groovy) of ER and RC builds
    * [tag sources & collect manifests](https://github.com/redhat-developer/codeready-workspaces-images/blob/crw-2-rhel-8/crw-jenkins/jobs/CRW_CI/Releng/get-3rd-party-deps-manifests.groovy), [collect sources](https://github.com/redhat-developer/codeready-workspaces-images/blob/crw-2-rhel-8/crw-jenkins/jobs/CRW_CI/Releng/get-3rd-party-sources.groovy) to create a release
    * set up subsequent releases ([branching](https://github.com/redhat-developer/codeready-workspaces-images/blob/crw-2-rhel-8/crw-jenkins/jobs/CRW_CI/Releng/create-branches.groovy), [bumping versions](https://github.com/redhat-developer/codeready-workspaces-images/blob/crw-2-rhel-8/crw-jenkins/jobs/CRW_CI/Releng/update-version-and-registry-tags.groovy))
