// map branch to tag to use in operator.yaml and csv.yaml
def CSV_QUAY_TAGS = [
    "2.6":"2.6",
    "2.7":"latest",
    "2"  :"nightly"
    ]
def JOB_BRANCHES = ["2.6":"7.24.x", "2.7":"7.26.x", "2":"master"]
def JOB_DISABLED = ["2.6":true, "2.7":false, "2":true]
for (JB in JOB_BRANCHES) {
    SOURCE_BRANCH=JB.value
    JOB_BRANCH=JB.key
    MIDSTM_BRANCH="crw-"+JOB_BRANCH+"-rhel-8"
    jobPath="${FOLDER_PATH}/${ITEM_NAME}_" + JOB_BRANCH
    if (JOB_BRANCH.equals("2")) { jobPath="${FOLDER_PATH}/${ITEM_NAME}_" + JOB_BRANCH + ".x" }
    pipelineJob(jobPath){
        disabled(JOB_DISABLED[JB.key]) // on reload of job, disable to avoid churn
        UPSTM_NAME="chectl"
        UPSTM_REPO="https://github.com/che-incubator/" + UPSTM_NAME

        description('''
Artifact builder + sync job; triggers cli build after syncing from upstream

<ul>
<li>Upstream: <a href=''' + UPSTM_REPO + '''>''' + UPSTM_NAME + '''</a></li>
<li>Downstream: <a href=https://github.com/redhat-developer/codeready-workspaces-chectl/tree/''' + MIDSTM_BRANCH + '''>crwctl</a></li>
</ul>

Results:  <a href=https://github.com/redhat-developer/codeready-workspaces-chectl/releases>chectl/releases</a>
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
            stringParam("SOURCE_BRANCH", SOURCE_BRANCH)
            stringParam("MIDSTM_BRANCH", MIDSTM_BRANCH)
            stringParam("CSV_VERSION", JOB_BRANCH + ".0", "Full version (x.y.z), used in CSV and crwctl version")
            stringParam("CSV_QUAY_TAG", CSV_QUAY_TAGS[JB.key], "Floating tag to use operator.yaml and csv.yaml")
            MMdd = ""+(new java.text.SimpleDateFormat("MM-dd")).format(new Date())
            stringParam("versionSuffix", "", '''
if set, use as version suffix before commitSHA: RC-''' + MMdd + ''' --> ''' + JOB_BRANCH + '''.0-RC-''' + MMdd + '''-commitSHA;<br/>
if unset, version is CRW_VERSION-YYYYmmdd-commitSHA<br/>
:: if suffix = GA, use server and operator tags from RHEC stage<br/>
:: if suffix contains RC, use server and operator tags from Quay<br/>
:: for all other suffixes, use server and operator tags = ''' + JOB_BRANCH + '''<br/>
:: NOTE: yarn will fail for version = x.y.z.a but works with x.y.z-a''')
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