#!groovy
@Library("Reform")
import uk.gov.hmcts.Ansible
import uk.gov.hmcts.Packager
import uk.gov.hmcts.RPMTagger

packager = new Packager(this, 'ccdata');
ansible = new Ansible(this, 'ccdata');
server = Artifactory.server 'artifactory.reform'
buildInfo = Artifactory.newBuildInfo()

properties(
    [[$class: 'GithubProjectProperty', displayName: 'User Profile API', projectUrlStr: 'https://git.reform.hmcts.net/case-management/user-profile-app/'],
     pipelineTriggers([[$class: 'GitHubPushTrigger']])]
)

milestone()
lock(resource: "user-profile-app-${env.BRANCH_NAME}", inversePrecedence: true) {
    node {
        try {
            stage('Checkout') {
                deleteDir()
                checkout scm
            }

            stage('Build') {
                sh "./gradlew clean build sonarqube -Dsonar.host.url=https://sonar.reform.hmcts.net/ " +
                    " -Dsonar.projectName=\"CCD :: User Profile API\" "
            }

            onDevelop {
                publishAndDeploy('develop', 'dev')
            }

            onMaster {
                publishAndDeploy('master', 'test')
            }

            milestone()
        } catch (err) {
            notifyBuildFailure channel: '#ccd-notifications'
            throw err
        } finally {
            junit '**/build/test-results/test/**/*.xml'
        }
    }
}

def publishAndDeploy(branch, env) {
    def rpmVersion
    def version
    // Temporary port offset avoiding collision till Dev and Test environments are fully separated by DevOps
    def backendPort = (env == 'test') ? '4481' : '4451'

    stage('Publish JAR') {
        server.publishBuildInfo buildInfo
    }

    stage('Publish RPM') {
        rpmVersion = packager.javaRPM(branch, 'user-profile-api',
            'build/libs/user-profile-$(./gradlew -q projectVersion)-all.jar',
            'springboot', 'src/main/resources/application.properties')
        packager.publishJavaRPM('user-profile-api')
    }

    stage('Package (Docker)') {
        userProfileVersion = dockerImage imageName: 'ccd/ccd-user-profile-api', tags: [branch]
        userProfileDatabaseVersion = dockerImage imageName: 'ccd/ccd-user-profile-database', context: 'docker/database', tags: [branch]
    }

    def rpmTagger = new RPMTagger(
        this,
        'user-profile-api',
        packager.rpmName('user-profile-api', rpmVersion),
        'ccdata-local'
    )

    stage('Deploy: ' + env) {
        version = "{ccd_user_profile_api_version: ${rpmVersion}}"
        ansible.runDeployPlaybook(version, env, branch)
        rpmTagger.tagDeploymentSuccessfulOn(env)
    }

    stage('Smoke Tests: ' + env) {
        sh "curl -vf https://case-user-profile-app." + env + ".ccd.reform.hmcts.net:" + backendPort + "/status/health"
        rpmTagger.tagTestingPassedOn(env)
    }
}
