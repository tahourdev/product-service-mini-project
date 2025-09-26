pipeline {
  agent { label "agent-2" }

  environment {
    DOCKER_HUB_REPO           = 'keanghor31/spring-app01'
    DOCKER_HUB_CREDENTIALS_ID = 'docker-hub-credentials'

    GITOPS_URL            = 'https://github.com/tahourdev/Jenkins-ArgoCD-GitOps.git'
    GITOPS_BRANCH         = 'main'
    GITOPS_CREDENTIALS_ID = 'github-jenkins-tahourdev'
    DEV_VALUES_FILE       = 'manifests/spring-jpa-helm/values-dev.yaml'

    BASE_VERSION          = '1.0'
    GIT_USER_NAME         = 'tahourdev'
    GIT_USER_EMAIL        = 'enghourheng26@gmail.com'
  }

  stages {
    stage('Checkout') { steps { checkout scm } }

    stage('Build Spring Boot') {
      steps {
        sh '''
          chmod +x ./gradlew || true
          ./gradlew clean build -x test
        '''
      }
    }

    stage('Build & Push Image') {
      steps {
        script {
          def shortSha = sh(returnStdout:true, script:"git rev-parse --short HEAD").trim()
          def imageTag = "${BASE_VERSION}.${env.BUILD_NUMBER}-${shortSha}"
          env.IMAGE_TAG = imageTag

          def img = docker.build("${DOCKER_HUB_REPO}:${imageTag}")
          docker.withRegistry('', "${DOCKER_HUB_CREDENTIALS_ID}") {
            img.push("${imageTag}")
            img.push("latest")
          }
        }
      }
    }

    stage('Trivy Scan') {
      steps {
        sh '''
          docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image \
            --severity HIGH,CRITICAL --no-progress --format table \
            -o trivy-scan-report.txt "$DOCKER_HUB_REPO:$IMAGE_TAG" || echo "Trivy scan failed but continuing..."
        '''
      }
    }

    stage('Bump Helm values-dev.yaml') {
      steps {
        dir('gitops-tmp') {
          withCredentials([usernamePassword(
            credentialsId: "${GITOPS_CREDENTIALS_ID}",
            usernameVariable: 'GIT_USER',
            passwordVariable: 'GIT_TOKEN'
          )]) {
            sh '''
              set -e
              git init
              git config user.name  "$GIT_USER_NAME"
              git config user.email "$GIT_USER_EMAIL"
              git -c credential.helper='!f() { echo username=$GIT_USER; echo password=$GIT_TOKEN; }; f' \
                  remote add origin "$GITOPS_URL"
              git fetch origin "$GITOPS_BRANCH"
              git checkout -b work "origin/$GITOPS_BRANCH"

              echo "📝 Updating tag in $DEV_VALUES_FILE -> $IMAGE_TAG"
              sed -i -E "s#(^\\s*tag:\\s*).*$#\\1 \\\"$IMAGE_TAG\\\"#" "$DEV_VALUES_FILE"

              git add "$DEV_VALUES_FILE"
              git commit -m "ci(dev): bump image to $DOCKER_HUB_REPO:$IMAGE_TAG" || echo "Nothing to commit"

              git -c credential.helper='!f() { echo username=$GIT_USER; echo password=$GIT_TOKEN; }; f' \
                  push origin HEAD:"$GITOPS_BRANCH"
            '''
          }
        }
      }
    }

    stage('(Info) Argo CD auto-sync') {
      steps { echo '🧠 Argo CD will detect the commit and roll out dev automatically.' }
    }
  }

  post {
    success {
      echo '✅ CI done: image pushed & Helm values-dev.yaml updated.'
      archiveArtifacts artifacts: 'trivy-scan-report.txt', allowEmptyArchive: true
    }
    failure {
      echo '❌ Pipeline failed.'
      archiveArtifacts artifacts: 'trivy-scan-report.txt', allowEmptyArchive: true
    }
  }
}
