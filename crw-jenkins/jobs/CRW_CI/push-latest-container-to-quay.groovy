def JOB_BRANCHES = ["2.6":"7.24.x", "2.7":"7.25.x", "2":"master"] // TODO switch to 7.26.x
def JOB_DISABLED = ["2.6":true, "2.7":false, "2":true]
for (JB in JOB_BRANCHES) {
    SOURCE_BRANCH=JB.value
    JOB_BRANCH=JB.key
    MIDSTM_BRANCH="crw-"+JOB_BRANCH+"-rhel-8"
    jobPath="${FOLDER_PATH}/${ITEM_NAME}_" + JOB_BRANCH
    if (JOB_BRANCH.equals("2")) { jobPath="${FOLDER_PATH}/${ITEM_NAME}_" + JOB_BRANCH + ".x" }
    pipelineJob(jobPath){
        disabled(JOB_DISABLED[JB.key]) // on reload of job, disable to avoid churn
        description('''
Push 1 or more containers from OSBS to quay.io/crw/. 
Triggered by  <a href=../get-sources-rhpkg-container-build/>get-sources-rhpkg-container-build</a>, but can be used manually too.
   
<p>
  
Images to copy to quay:
<table>
<tr><td>

  <li> <a href=https://quay.io/repository/crw/configbump-rhel8?tab=tags>configbump</a> </li>
  <li><a href=https://quay.io/repository/crw/crw-2-rhel8-operator?tab=tags>operator</a> 
  <li><a href=https://quay.io/repository/crw/crw-2-rhel8-operator-metadata?tab=tags>operator-metadata</a></li>
  <li><a href=https://quay.io/repository/crw/devfileregistry-rhel8?tab=tags>devfileregistry</a></li>
  <li> <a href=https://quay.io/repository/crw/imagepuller-rhel8?tab=tags>imagepuller</a></li>

  </td><td>

  <li> <a href=https://quay.io/repository/crw/jwtproxy-rhel8?tab=tags>jwtproxy</a> </li>
  <li> <a href=https://quay.io/repository/crw/machineexec-rhel8?tab=tags>machineexec</a> </li>
  <li> <a href=https://quay.io/repository/crw/pluginbroker-artifacts-rhel8?tab=tags>pluginbroker-artifacts</a> </li>
  <li> <a href=https://quay.io/repository/crw/pluginbroker-metadata-rhel8?tab=tags>pluginbroker-metadata</a>   </li>
  <li> <a href=https://quay.io/repository/crw/plugin-intellij-rhel8?tab=tags>plugin-intellij</a></li>

  </td><td>

  <li> <a href=https://quay.io/repository/crw/plugin-java11-openj9-rhel8?tab=tags>plugin-java11-openj9</a></li>
  <li> <a href=https://quay.io/repository/crw/plugin-java11-rhel8?tab=tags>plugin-java11</a></li>
  <li> <a href=https://quay.io/repository/crw/plugin-java8-openj9-rhel8?tab=tags>plugin-java8-openj9</a> 
  <li> <a href=https://quay.io/repository/crw/plugin-java8-rhel8?tab=tags>plugin-java8</a> 
  <li> <a href=https://quay.io/repository/crw/plugin-kubernetes-rhel8?tab=tags>plugin-kubernetes</a></li>

  </td><td>

  <li> <a href=https://quay.io/repository/crw/plugin-openshift-rhel8?tab=tags>plugin-openshift</a> </li>
  <li><a href=https://quay.io/repository/crw/pluginregistry-rhel8?tab=tags>pluginregistry</a></li>
  <li><a href=https://quay.io/repository/crw/server-rhel8?tab=tags>server</a> </li>
  <li> <a href=https://quay.io/repository/crw/stacks-cpp-rhel8?tab=tags>stacks-cpp</a> </li>
  <li> <a href=https://quay.io/repository/crw/stacks-dotnet-rhel8?tab=tags>stacks-dotnet</a> </li>

  </td></tr><tr><td>

  <li> <a href=https://quay.io/repository/crw/stacks-golang-rhel8?tab=tags>stacks-golang</a> </li>
  <li> <a href=https://quay.io/repository/crw/stacks-php-rhel8?tab=tags>stacks-php</a> </li>
  <li> <a href=https://quay.io/repository/crw/theia-rhel8?tab=tags>theia</a> </li>
  <li> <a href=https://quay.io/repository/crw/theia-dev-rhel8?tab=tags>theia-dev</a> </li>
  <li> <a href=https://quay.io/repository/crw/theia-endpoint-rhel8?tab=tags>theia-endpoint</a> </li>

  </td><td>

  <li> <a href=https://quay.io/repository/crw/traefik-rhel8?tab=tags>traefik</a> </li>

  </td></tr>
  </table>
</ul>
            <p>NOTE:  If no nodes are available, run: <br/>
    <b><a href=https://github.com/redhat-developer/codeready-workspaces/blob/crw-2.y-rhel-8/product/getLatestImageTags.sh>getLatestImageTags.sh</a> 
    -c "codeready-workspaces-plugin-openshift-rhel8" --osbs --pushtoquay="2.y latest"</b>
  
  to get latest from osbs and push to quay.
''')

        properties {
            ownership {
                primaryOwnerId("nboldt")
            }
        }

        throttleConcurrentBuilds {
            maxPerNode(2)
            maxTotal(10)
        }

        logRotator {
            daysToKeep(10)
            numToKeep(10)
            artifactDaysToKeep(2)
            artifactNumToKeep(1)
        }

        /* requires naginator plugin */
        /* publishers {
            retryBuild {
                rerunIfUnstable()
                retryLimit(1)
                progressiveDelay(30,90)
            }
        } */

        parameters{
            textParam("CONTAINERS", '''configbump devfileregistry operator operator-metadata imagepuller \
jwtproxy machineexec pluginbroker-metadata pluginbroker-artifacts plugin-intellij \
plugin-java11-openj9 plugin-java11  plugin-java8-openj9 plugin-java8 plugin-kubernetes \
plugin-openshift pluginregistry server stacks-cpp stacks-dotnet \
stacks-golang stacks-php theia theia-dev theia-endpoint \
traefik''', "list of containers to copy: can 1, all or some, as needed")

            stringParam("TAGS", "latest", "By default, update :latest tag in addition to the latest one (2.y-4) and the base one (2.y).")
            stringParam("MIDSTM_BRANCH", MIDSTM_BRANCH, "")
        }

        // Trigger builds remotely (e.g., from scripts), using Authentication Token = CI_BUILD
        authenticationToken('CI_BUILD')

        // TODO: enable naginator plugin to re-trigger if job fails

        // TODO: add email notification to nboldt@, anyone who submits a bad build, etc.

        // TODO: enable console log parser ?

        definition {
            cps{
                sandbox(true)
                script(readFileFromWorkspace('jobs/CRW_CI/push-latest-container-to-quay_'+JOB_BRANCH+'.jenkinsfile'))
            }
        }
    }
}