def JOB_BRANCHES = ["2.6"] // , "2.7"]
for (String JOB_BRANCH : JOB_BRANCHES) {
    pipelineJob("${FOLDER_PATH}/${ITEM_NAME}_${JOB_BRANCH}"){
        MIDSTM_BRANCH="crw-"+JOB_BRANCH+"-rhel-8"

        UPSTM_NAME="chectl"
        UPSTM_REPO="https://github.com/che-incubator/" + UPSTM_NAME

        description('''
Artifact builder + sync job; triggers cli build after syncing from upstream

<ul>
<li>Upstream: <a href=''' + UPSTM_REPO + '''>''' + UPSTM_NAME + '''</a></li>
<li>Downstream: <a href=https://github.com/redhat-developer/codeready-workspaces-chectl/tree/''' + MIDSTM_BRANCH + '''>crwctl</a></li>
</ul>

Results:  <a href=https://github.com/redhat-developer/codeready-workspaces-chectl/releases>chectl/releases</a>
  
  <p>
  TODO: get image tags using one of these and set them automatically:
  <li>$➔ getLatestImageTags.sh --quay  -c "crw/server-rhel8 crw/crw-2-rhel8-operator"
    <li>$➔ getLatestImageTags.sh --stage  -c "codeready-workspaces/server-rhel8 codeready-workspaces/crw-2-rhel8-operator"

        ''')

        properties {
            ownership {
                primaryOwnerId("nboldt")
            }

            // poll SCM every 2 hrs for changes in upstream
            pipelineTriggers {
                [$class: "SCMTrigger", scmpoll_spec: "H H/12 * * *"] // every 12 hrs
            }
        }

        throttleConcurrentBuilds {
            maxPerNode(1)
            maxTotal(1)
        }

        logRotator {
            daysToKeep(15)
            numToKeep(15)
            artifactDaysToKeep(7)
            artifactNumToKeep(5)
        }

        parameters{
            stringParam("MIDSTM_BRANCH", MIDSTM_BRANCH)
            stringParam("CSV_VERSION", JOB_BRANCH + ".0", "Full version (x.y.z), used in CSV and crwctl version")
            stringParam("CRW_SERVER_TAG", JOB_BRANCH, "set 2.y-zz for GA release")
            stringParam("CRW_OPERATOR_TAG", JOB_BRANCH, "set 2.y-zz for GA release")
            MMdd = ""+(new java.text.SimpleDateFormat("MM-dd")).format(new Date())
            stringParam("versionSuffix", "", 
                "if set, use as version suffix before commitSHA: RC-" + MMdd + " --> " + JOB_BRANCH + ".0-RC-" + MMdd + "-commitSHA; \n\
<br/>\n\
if unset, version is CRW_VERSION-YYYYmmdd-commitSHA \n\
<br/>\n\
:: NOTE: yarn will fail for version = x.y.z.a but works with x.y.z-a")
            booleanParam("PUBLISH_ARTIFACTS_TO_GITHUB", false, "default false; check box to publish to GH releases")
            booleanParam("PUBLISH_ARTIFACTS_TO_RCM", false, "default false; check box to upload sources + binaries to RCM for a GA release ONLY")
        }

        // Trigger builds remotely (e.g., from scripts), using Authentication Token = CI_BUILD
        authenticationToken('CI_BUILD')

        definition {
            cps{
                sandbox(true)
                script(readFileFromWorkspace('jobs/CRW_CI/crwctl_'+JOB_BRANCH+'.jenkinsfile'))
            }
        }
    }
}