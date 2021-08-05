// TODO compute these from https://github.com/redhat-developer/codeready-workspaces/blob/crw-2-rhel-8/dependencies/VERSION.json
def DEV_WORKSPACE_CONTROLLER_VERSIONS = [
    "2.11":"0.8.x", 
    "2.x" :"main"
    ]
def DEV_WORKSPACE_CHE_OPERATOR_VERSIONS = [
    "2.11":"7.34.x",
    "2.x" :"main"
    ]

def JOB_BRANCHES = ["2.11":"7.34.x", "2.x":"main"] 
def JOB_DISABLED = ["2.11":false, "2.x":false]
for (JB in JOB_BRANCHES) {
    SOURCE_BRANCH=JB.value
    JOB_BRANCH=""+JB.key
    MIDSTM_BRANCH="crw-" + JOB_BRANCH.replaceAll(".x","") + "-rhel-8"
    jobPath="${FOLDER_PATH}/${ITEM_NAME}_" + JOB_BRANCH
    pipelineJob(jobPath){
        disabled(JOB_DISABLED[JB.key]) // on reload of job, disable to avoid churn
        UPSTM_NAME="che-operator"
        MIDSTM_NAME="operator"
        SOURCE_REPO="eclipse-che/" + UPSTM_NAME
        MIDSTM_REPO="redhat-developer/codeready-workspaces-images"

        description('''
Artifact builder + sync job; triggers brew after syncing

<p>There are two operator-related sync jobs:<br/>
1. <a href=../crw-operator_''' + JOB_BRANCH + '''>crw-operator_''' + JOB_BRANCH + '''</a>: go code<br/>
2. <a href=../crw-operator-metadata_''' + JOB_BRANCH + '''>crw-operator-metadata_''' + JOB_BRANCH + '''</a>: CRD, CSV</p>

<ul>
<li>Upstream: <a href=https://github.com/''' + SOURCE_REPO + '''>''' + UPSTM_NAME + '''</a></li>
<li>Pre-Mistream (transformation code, deprecated): 
<a href=https://github.com/redhat-developer/codeready-workspaces-operator/tree/''' + MIDSTM_BRANCH + '''/>crw-operator</a></li> 
<li>Midstream: <a href=https://github.com/''' + MIDSTM_REPO + '''/tree/''' + MIDSTM_BRANCH + '''/codeready-workspaces-''' + MIDSTM_NAME + '''/>crw-''' + MIDSTM_NAME + '''</a></li>
<li>Downstream: <a href=http://pkgs.devel.redhat.com/cgit/containers/codeready-workspaces-''' + MIDSTM_NAME + '''?h=''' + MIDSTM_BRANCH + '''>''' + MIDSTM_NAME + '''</a></li>
</ul>

<p>If <b style="color:green">downstream job fires</b>, see 
<a href=../sync-to-downstream_''' + JOB_BRANCH + '''/>sync-to-downstream</a>, then
<a href=../get-sources-rhpkg-container-build_''' + JOB_BRANCH + '''/>get-sources-rhpkg-container-build</a>. <br/>
   If <b style="color:orange">job is yellow</b>, no changes found to push, so no container-build triggered. </p>

<p> If this job is ever disabled and you want to update the LATEST_IMAGES files yourself, see 
<a href=https://github.com/redhat-developer/codeready-workspaces/blob/''' + MIDSTM_BRANCH + '''/dependencies/LATEST_IMAGES.sh>https://github.com/redhat-developer/codeready-workspaces/blob/''' + MIDSTM_BRANCH + '''/dependencies/LATEST_IMAGES.sh</a>
        ''')

        properties {
            ownership {
                primaryOwnerId("nboldt")
            }

            githubProjectUrl("https://github.com/" + SOURCE_REPO)

            pipelineTriggers {
                triggers{
                    pollSCM{
                        scmpoll_spec("H H/4 * * *") // every 4hrs
                    }
                }
            }

            disableResumeJobProperty()
        }

        logRotator {
            daysToKeep(5)
            numToKeep(5)
            artifactDaysToKeep(2)
            artifactNumToKeep(1)
        }

        parameters{
            stringParam("SOURCE_REPO", SOURCE_REPO)
            stringParam("SOURCE_BRANCH", SOURCE_BRANCH)
            stringParam("MIDSTM_REPO", MIDSTM_REPO)
            stringParam("MIDSTM_BRANCH", MIDSTM_BRANCH)
            stringParam("MIDSTM_NAME", MIDSTM_NAME)
            // TODO compute these from https://github.com/redhat-developer/codeready-workspaces/blob/crw-2-rhel-8/dependencies/VERSION.json
            stringParam("DEV_WORKSPACE_CONTROLLER_VERSION", DEV_WORKSPACE_CONTROLLER_VERSIONS[JB.key], "Branch (0.y.x or main) used to get deployment templates")
            stringParam("DEV_WORKSPACE_CHE_OPERATOR_VERSION", DEV_WORKSPACE_CHE_OPERATOR_VERSIONS[JB.key], "Branch (7.yy.x or main) used to get deployment templates")
            booleanParam("FORCE_BUILD", false, "If true, trigger a rebuild even if no changes were pushed to pkgs.devel")
        }

        // Trigger builds remotely (e.g., from scripts), using Authentication Token = CI_BUILD
        authenticationToken('CI_BUILD')

        definition {
            cps{
                sandbox(true)
                if (JOB_BRANCH.equals("2.10")) {
                    script(readFileFromWorkspace('jobs/CRW_CI/crw-operator_'+JOB_BRANCH+'.jenkinsfile'))
                } else {
                    script(readFileFromWorkspace('jobs/CRW_CI/template_'+JOB_BRANCH+'.jenkinsfile'))
                }
            }
        }
    }
}