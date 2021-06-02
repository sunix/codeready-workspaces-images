def JOB_BRANCHES = ["2.7":"7.26.x", "2.8":"7.28.x", "2.x":"7.30.x"] // TODO switch 2.x to main, when 2.9 branches/jobs created
def JOB_DISABLED = ["2.7":true, "2.8":true, "2.x":false]
for (JB in JOB_BRANCHES) {
    SOURCE_BRANCH=JB.value
    JOB_BRANCH=""+JB.key
    MIDSTM_BRANCH="crw-" + JOB_BRANCH.replaceAll(".x","") + "-rhel-8"
    jobPath="${FOLDER_PATH}/${ITEM_NAME}_" + JOB_BRANCH
    pipelineJob(jobPath){
        disabled(JOB_DISABLED[JB.key]) // on reload of job, disable to avoid churn
        UPSTM_NAME="che-machine-exec"
        MIDSTM_NAME="machineexec"
        SOURCE_REPO="eclipse-che/" + UPSTM_NAME
        MIDSTM_REPO="redhat-developer/codeready-workspaces-images"

        description('''
Artifact builder + sync job; triggers brew after syncing

<ul>
<li>Upstream: <a href=https://github.com/''' + SOURCE_REPO + '''>''' + UPSTM_NAME + '''</a></li>
<li>Midstream: <a href=https://github.com/''' + MIDSTM_REPO + '''/tree/''' + MIDSTM_BRANCH + '''/codeready-workspaces-''' + MIDSTM_NAME + '''/>crw-''' + MIDSTM_NAME + '''</a></li>
<li>Downstream: <a href=http://pkgs.devel.redhat.com/cgit/containers/codeready-workspaces-''' + MIDSTM_NAME + '''?h=''' + MIDSTM_BRANCH + '''>''' + MIDSTM_NAME + '''</a></li>
</ul>

<p>If <b style="color:green">downstream job fires</b>, see 
<a href=../sync-to-downstream_''' + JOB_BRANCH + '''/>sync-to-downstream</a>, then
<a href=../get-sources-rhpkg-container-build_''' + JOB_BRANCH + '''/>get-sources-rhpkg-container-build</a>. <br/>
   If <b style="color:orange">job is yellow</b>, no changes found to push, so no container-build triggered. </p>
        ''')

        properties {
            ownership {
                primaryOwnerId("nboldt")
            }

            githubProjectUrl("https://github.com/" + SOURCE_REPO)

            pipelineTriggers {
                triggers{
                    pollSCM{
                        scmpoll_spec("H H/24 * * *") // every 24hrs
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
            booleanParam("FORCE_BUILD", false, "If true, trigger a rebuild even if no changes were pushed to pkgs.devel")
        }

        // Trigger builds remotely (e.g., from scripts), using Authentication Token = CI_BUILD
        authenticationToken('CI_BUILD')

        definition {
            cps{
                sandbox(true)
                script(readFileFromWorkspace('jobs/CRW_CI/crw-'+MIDSTM_NAME+'_'+JOB_BRANCH+'.jenkinsfile'))
            }
        }
    }
}